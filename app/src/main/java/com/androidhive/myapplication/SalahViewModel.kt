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

data class DeviceCapabilities(val exactAlarms: Boolean = true, val notifications: Boolean = true, val fullScreen: Boolean = true, val ramadanNotifications: Boolean = true, val iftarNotifications: Boolean = true)
data class RamadanContent(
    val calendar: com.androidhive.myapplication.domain.ramadan.RamadanCalendar? = null,
    val nextSuhur: com.androidhive.myapplication.domain.ramadan.RamadanReminder? = null,
    val nextIftar: com.androidhive.myapplication.domain.ramadan.RamadanReminder? = null,
    val error: Int? = null, val loading: Boolean = true,
)
data class ContentState(val lessons: List<Lesson> = emptyList(), val cities: List<City> = emptyList(), val videos: List<TrainingVideo> = emptyList(), val error: Int? = null)
data class SalahUiState(
    val settings: AppSettings? = null,
    val content: ContentState = ContentState(),
    val now: Instant = Instant.EPOCH,
    val today: DailyPrayers? = null,
    val next: PrayerMoment? = null,
    val locationBusy: Boolean = false,
    val audioImportBusy: Boolean = false,
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
    private val importingAudio = MutableStateFlow(false)
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
            locationBusy = busy, error = loaded.error ?: if (result?.isFailure == true) R.string.ui_error_calculation else null)
    }.combine(importingAudio) { state, busy -> state.copy(audioImportBusy = busy) }.combine(capabilities) { state, caps -> state.copy(capabilities = caps) }
        .combine(settingsError) { state, error -> state.copy(error = error ?: state.error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalahUiState())

    val ramadan = combine(settingsState.filterNotNull(), flow {
        while (currentCoroutineContext().isActive) { emit(Instant.now()); delay(30_000) }
    }) { settings, now -> settings to now }.mapLatest { (settings, now) ->
        if (settings.location == null) RamadanContent(loading = false)
        else withContext(Dispatchers.Default) {
            try {
                RamadanContent(container.ramadanEngine.calendar(settings, now.atZone(settings.location.zone).toLocalDate()),
                    container.ramadanEngine.next(settings, com.androidhive.myapplication.domain.ramadan.RamadanReminderKind.SUHUR, now),
                    container.ramadanEngine.next(settings, com.androidhive.myapplication.domain.ramadan.RamadanReminderKind.IFTAR, now), loading = false)
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { RamadanContent(error = R.string.ui_error_calculation, loading = false) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RamadanContent())

    private suspend fun refreshAlarms() {
        try { container.alarms.refresh() }
        catch (error: CancellationException) { throw error }
        catch (_: Exception) { message(R.string.ui_error_generic) }
        try { container.ramadanAlarms.refresh() }
        catch (error: CancellationException) { throw error }
        catch (_: Exception) { message(R.string.ui_error_generic) }
    }
    fun stopSuhurPreview() { if (container.suhurSession.state.value.preview) container.ramadanAlarms.stop() }
    fun previewSuhur() {
        viewModelScope.launch {
            try {
                if (container.suhurSession.state.value.active) container.ramadanAlarms.stop()
                else container.ramadanAlarms.preview(container.settings.snapshot())
            } catch (_: RuntimeException) { message(R.string.ui_alarm_sound_error) }
        }
    }

    private data class AlarmConfig(val location: SavedLocation?, val preset: CalculationPreset, val asr: AsrConvention, val mode: AlertMode, val overrides: Map<PrayerId, AlertMode>, val ramadan: RamadanOptions)

    init {
        loadContent()
        refreshCapabilities()
        viewModelScope.launch {
            combine(settingsState.map { it?.location }.distinctUntilChanged(), content.map { it.cities }.distinctUntilChanged()) { stored, cities -> stored to cities }
                .collect { (stored, cities) ->
                    if (stored != null && stored.mode == LocationMode.MANUAL) {
                        val city = cities.firstOrNull { it.id == stored.cityId } ?: cities.firstOrNull {
                            it.name == stored.label && kotlin.math.abs(it.latitude - stored.latitude) < .001 && kotlin.math.abs(it.longitude - stored.longitude) < .001
                        }
                        if (city != null && (stored.names != city.names || stored.cityId != city.id)) {
                            try {
                                container.settings.update {
                                    if (it.location == stored) it.copy(location = stored.copy(names = city.names, cityId = city.id)) else it
                                }
                            } catch (error: CancellationException) { throw error }
                            catch (_: Exception) { message(R.string.ui_error_generic) }
                        }
                    }
                }
        }
        viewModelScope.launch {
            settingsState.filterNotNull().map { AlarmConfig(it.location, it.calculationPreset, it.asrConvention, it.alertMode, it.prayerAlerts, it.ramadan) }
                .distinctUntilChanged().collectLatest {
                    refreshAlarms()
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
                    val loadedCities = cities.await()
                    content.value = ContentState(lessons.await(), loadedCities, videos.await())
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
        update { it.withSchool(school).copy(language = language, onboardingComplete = true) }
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
    fun importAdhanVoice(uri: android.net.Uri) {
        if (importingAudio.value) return
        importingAudio.value = true
        viewModelScope.launch {
            try {
                val voice = container.recordings.import(uri)
                try { container.settings.update { it.copy(adhanVoices = it.adhanVoices + voice, selectedVoiceId = voice.id) } }
                catch (error: Exception) { container.recordings.delete(voice); throw error }
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { message(R.string.ui_audio_import_error) }
            finally { importingAudio.value = false }
        }
    }
    fun removeAdhanVoice(voice: AdhanVoice) {
        viewModelScope.launch {
            try {
                container.settings.update { it.copy(adhanVoices = it.adhanVoices.filterNot { v -> v.id == voice.id },
                    selectedVoiceId = it.selectedVoiceId.takeUnless { id -> id == voice.id }) }
                withContext(Dispatchers.IO) { container.recordings.delete(voice) }
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { message(R.string.ui_error_generic) }
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
            refreshAlarms()
        }
    }
}
