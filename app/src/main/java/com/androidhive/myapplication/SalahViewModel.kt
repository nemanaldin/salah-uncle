package com.androidhive.myapplication

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.Lesson
import com.androidhive.myapplication.data.content.TrainingVideo
import com.androidhive.myapplication.data.location.City
import com.androidhive.myapplication.domain.prayer.DailyPrayers
import com.androidhive.myapplication.domain.prayer.PrayerMoment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.Instant

data class DeviceCapabilities(val exactAlarms: Boolean = true, val notifications: Boolean = true, val fullScreen: Boolean = true)
data class ContentState(val lessons: List<Lesson> = emptyList(), val cities: List<City> = emptyList(), val videos: List<TrainingVideo> = emptyList(), val error: Int? = null)
data class SalahUiState(
    val settings: AppSettings? = null,
    val content: ContentState = ContentState(),
    val now: Instant = Instant.EPOCH,
    val today: DailyPrayers? = null,
    val next: PrayerMoment? = null,
    val locationBusy: Boolean = false,
    val capabilities: DeviceCapabilities = DeviceCapabilities(),
    val error: Int? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SalahViewModel(private val container: AppContainer) : ViewModel() {
    private val eventsMutable = MutableSharedFlow<Int>(extraBufferCapacity = 8)
    val events = eventsMutable.asSharedFlow()
    private val settingsError = MutableStateFlow<Int?>(null)
    private val settingsState = container.settings.settings.catch { settingsError.value = R.string.ui_error_generic }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val content = MutableStateFlow(ContentState())
    private val locating = MutableStateFlow(false)
    private val capabilities = MutableStateFlow(DeviceCapabilities())
    private var contentJob: Job? = null
    private val countdownVisible = MutableStateFlow(true)
    private val ticks = countdownVisible.flatMapLatest { visible ->
        if (!visible) flowOf(Instant.now()) else flow {
            while (currentCoroutineContext().isActive) { emit(Instant.now()); delay(1_000) }
        }
    }
    val state = combine(settingsState, content, ticks, locating) { settings, loaded, now, busy ->
        val result = if (settings?.location != null) runCatching {
            container.prayerEngine.day(settings.location, settings.calculationPreset, settings.asrConvention) to container.prayerEngine.next(settings, now)
        } else null
        SalahUiState(settings, loaded, now, result?.getOrNull()?.first, result?.getOrNull()?.second,
            busy, error = loaded.error ?: if (result?.isFailure == true) R.string.ui_error_calculation else null)
    }.combine(capabilities) { state, caps -> state.copy(capabilities = caps) }
        .combine(settingsError) { state, error -> state.copy(error = error ?: state.error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalahUiState())

    private data class AlarmConfig(val location: SavedLocation?, val preset: CalculationPreset, val asr: AsrConvention, val mode: AlertMode, val overrides: Map<PrayerId, AlertMode>)

    init {
        loadContent()
        refreshCapabilities()
        viewModelScope.launch {
            settingsState.filterNotNull().map { AlarmConfig(it.location, it.calculationPreset, it.asrConvention, it.alertMode, it.prayerAlerts) }
                .distinctUntilChanged().collectLatest {
                    try { container.alarms.refresh() }
                    catch (error: CancellationException) { throw error }
                    catch (_: Exception) { message(R.string.ui_error_generic) }
                }
        }
    }

    fun loadContent() {
        contentJob?.cancel()
        contentJob = viewModelScope.launch {
            try {
                coroutineScope {
                    val lessons = async { container.lessons.load() }
                    val cities = async { container.cities.load() }
                    val videos = async { container.lessons.videos() }
                    content.value = ContentState(lessons.await(), cities.await(), videos.await())
                }
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { content.value = content.value.copy(error = R.string.ui_error_content) }
        }
    }

    fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            try { container.settings.update(transform) }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { message(R.string.ui_error_generic) }
        }
    }
    fun completeOnboarding(language: AppLanguage, school: School) {
        if (!school.contentAvailable) { message(R.string.ui_school_unavailable); return }
        update { it.copy(language = language, school = school, onboardingComplete = true) }
    }
    fun selectCity(id: String) {
        val city = content.value.cities.firstOrNull { it.id == id } ?: return
        update { it.copy(location = city.location()) }
    }
    fun currentLocation() {
        if (locating.value) {
            return
        }
        locating.value = true
        viewModelScope.launch {
            try {
                val location = container.deviceLocation.current()
                container.settings.update { it.copy(location = location) }
            } catch (error: CancellationException) {
                if (error is TimeoutCancellationException) message(R.string.ui_error_location) else throw error
            } catch (_: SecurityException) { message(R.string.ui_error_location_permission) }
            catch (_: Exception) { message(R.string.ui_error_location) }
            finally { locating.value = false }
        }
    }
    fun bookmark(id: String) = update { current ->
        current.copy(bookmarks = if (id in current.bookmarks) current.bookmarks - id else current.bookmarks + id)
    }
    fun recordReading(lesson: String, step: String) = update { it.copy(lastLessonId = lesson, lastStepId = step) }
    fun completeStep(lesson: String, step: String) = update { it.copy(completedSteps = it.completedSteps + "$lesson:$step") }
    fun message(resource: Int) { eventsMutable.tryEmit(resource) }
    fun showCountdown(visible: Boolean) { countdownVisible.value = visible }

    fun refreshCapabilities() {
        capabilities.value = container.capabilities()
    }
    fun onResume() {
        refreshCapabilities()
        viewModelScope.launch {
            try { container.alarms.refresh() }
            catch (error: CancellationException) { throw error }
            catch (_: Exception) { message(R.string.ui_error_generic) }
        }
    }
}
