package com.androidhive.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidhive.myapplication.data.PrayerTimeRepository
import com.androidhive.myapplication.data.PreferencesDataSource
import com.androidhive.myapplication.data.NotificationSettingsStore
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.PrayerTimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerTimesViewModel(
    private val prayerTimeRepository: PrayerTimeRepository,
    private val preferences: PreferencesDataSource,
    private val notificationSettings: NotificationSettingsStore
) : ViewModel() {

    private val _state = MutableStateFlow(PrayerTimesUiState())
    val state = _state.asStateFlow()

    private var _calculationParams = CalculationMethod.KARACHI.parameters
    private var _latitude: Double = 0.0
    private var _longitude: Double = 0.0
    private var _isManualMode = false
    private var _countdownJob: kotlinx.coroutines.Job? = null
    private var _calculationId = 0

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        _state.value = _state.value.copy(
            isAutoAdhanEnabled = false,
            isPrayerNotificationsEnabled = false,
            calculationMethodPosition = 0
        )
    }

    fun loadPreferences(
        isAutoAdhan: Boolean,
        isPrayerNotif: Boolean,
        locationMode: String,
        manualCityName: String,
        calculationMethod: String,
        latitude: Double,
        longitude: Double
    ) {
        val calcPos = when (calculationMethod) {
            "MUSLIM_WORLD_LEAGUE" -> 1
            "EGYPTIAN" -> 2
            "KARACHI" -> 3
            "UMM_AL_QURA" -> 4
            "DUBAI" -> 5
            "MOON_SIGHTING_COMMITTEE" -> 6
            else -> 0
        }
        // Set calculation method WITHOUT triggering calculation
        val method: CalculationMethod = when (calcPos) {
            1 -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            2 -> CalculationMethod.EGYPTIAN
            3 -> CalculationMethod.KARACHI
            4 -> CalculationMethod.UMM_AL_QURA
            5 -> CalculationMethod.DUBAI
            6 -> CalculationMethod.MOON_SIGHTING_COMMITTEE
            else -> CalculationMethod.KARACHI
        }
        _calculationParams = method.getParameters()

        _isManualMode = locationMode == "manual"
        _latitude = latitude
        _longitude = longitude

        _state.value = _state.value.copy(
            isManualMode = _isManualMode,
            isAutoAdhanEnabled = isAutoAdhan,
            isPrayerNotificationsEnabled = isPrayerNotif,
            calculationMethodPosition = calcPos,
            currentLocationLabel = if (_isManualMode && manualCityName.isNotBlank()) {
                "موقعیت فعلی: $manualCityName"
            } else if (!_isManualMode && (latitude != 0.0 || longitude != 0.0)) {
                String.format(Locale.US, "موقعیت فعلی: %.5f, %.5f", latitude, longitude)
            } else {
                "در انتظار انتخاب مکان"
            }
        )
        // Do NOT calculate prayer times here - wait for explicit location event
    }

    fun onCalculationMethodChanged(position: Int) {
        val method: CalculationMethod = when (position) {
            1 -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            2 -> CalculationMethod.EGYPTIAN
            3 -> CalculationMethod.KARACHI
            4 -> CalculationMethod.UMM_AL_QURA
            5 -> CalculationMethod.DUBAI
            6 -> CalculationMethod.MOON_SIGHTING_COMMITTEE
            else -> CalculationMethod.KARACHI
        }
        _calculationParams = method.getParameters()
        _state.value = _state.value.copy(calculationMethodPosition = position)

        if (_latitude != 0.0 || _longitude != 0.0) {
            calculateAndDisplayPrayerTimes(_latitude, _longitude)
        }
    }

    fun onLocationReceived(latitude: Double, longitude: Double) {
        _latitude = latitude
        _longitude = longitude
        _isManualMode = false
        _state.value = _state.value.copy(isManualMode = false)
        calculateAndDisplayPrayerTimes(latitude, longitude)
    }

    fun onManualLocationSelected(latitude: Double, longitude: Double, cityName: String) {
        _latitude = latitude
        _longitude = longitude
        _isManualMode = true
        _state.value = _state.value.copy(
            isManualMode = true,
            currentLocationLabel = "موقعیت فعلی: $cityName"
        )
        calculateAndDisplayPrayerTimes(latitude, longitude)
    }

    fun setCountries(countries: List<String>) {
        _state.value = _state.value.copy(countries = countries)
    }

    fun setProvinces(provinces: List<String>) {
        _state.value = _state.value.copy(provinces = provinces)
    }

    fun setCities(cities: List<String>) {
        _state.value = _state.value.copy(cities = cities)
    }

    fun onCountrySelected(country: String) {
        _state.value = _state.value.copy(selectedCountry = country, selectedProvince = null, selectedCity = null)
    }

    fun onProvinceSelected(province: String) {
        _state.value = _state.value.copy(selectedProvince = province, selectedCity = null)
    }

    fun onCitySelected(city: String) {
        _state.value = _state.value.copy(selectedCity = city)
    }

    fun setLocationMode(isManual: Boolean) {
        _isManualMode = isManual
        // Clear stale coordinates when switching modes
        if (isManual) {
            // GPS -> Manual: clear GPS coordinates
            _latitude = 0.0
            _longitude = 0.0
        } else {
            // Manual -> GPS: clear manual coordinates
            _latitude = 0.0
            _longitude = 0.0
        }
        _state.value = _state.value.copy(isManualMode = isManual)
        if (!isManual) {
            _state.value = _state.value.copy(currentLocationLabel = "در انتظار انتخاب مکان")
        } else {
            _state.value = _state.value.copy(currentLocationLabel = "در انتظار انتخاب مکان دستی")
        }
        // Do NOT trigger prayer calculation here - wait for coordinates
    }

    fun hasValidLocation(): Boolean = _latitude != 0.0 || _longitude != 0.0

    fun setAutoAdhanEnabled(enabled: Boolean) {
        if (enabled && !_state.value.isPrayerNotificationsEnabled) {
            notificationSettings.setAutoAdhanEnabled(true)
            notificationSettings.setPrayerNotificationsEnabled(true)

            _state.value = _state.value.copy(
                isAutoAdhanEnabled = true,
                isPrayerNotificationsEnabled = true
            )
        } else {
            notificationSettings.setAutoAdhanEnabled(enabled)

            _state.value = _state.value.copy(
                isAutoAdhanEnabled = enabled
            )
        }
    }

    fun setPrayerNotificationsEnabled(enabled: Boolean) {
        if (!enabled && _state.value.isAutoAdhanEnabled) {
            notificationSettings.setPrayerNotificationsEnabled(false)
            notificationSettings.setAutoAdhanEnabled(false)

            _state.value = _state.value.copy(
                isAutoAdhanEnabled = false,
                isPrayerNotificationsEnabled = false
            )
        } else {
            notificationSettings.setPrayerNotificationsEnabled(enabled)

            _state.value = _state.value.copy(
                isPrayerNotificationsEnabled = enabled
            )
        }
    }

    private fun calculateAndDisplayPrayerTimes(latitude: Double, longitude: Double) {
        val requestId = ++_calculationId
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.Default) {
            val prayerTimes = prayerTimeRepository.getPrayerTimes(latitude, longitude, _calculationParams)

            // Discard if superseded by a newer request
            if (requestId != _calculationId) return@launch

            // Switch back to Main thread for UI updates
            withContext(Dispatchers.Main) {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

                _state.value = _state.value.copy(
                    fajrTime = sdf.format(prayerTimes.fajr),
                    sunriseTime = sdf.format(prayerTimes.sunrise),
                    dhuhrTime = sdf.format(prayerTimes.dhuhr),
                    asrTime = sdf.format(prayerTimes.asr),
                    maghribTime = sdf.format(prayerTimes.maghrib),
                    ishaTime = sdf.format(prayerTimes.isha),
                    isLoading = false
                )

                updateNextPrayerAndCountdown(prayerTimes)

                val locationLabel = if (_isManualMode) {
                    val city = _state.value.selectedCity ?: "مکان دستی"
                    "موقعیت فعلی: $city"
                } else {
                    String.format(Locale.US, "موقعیت فعلی: %.5f, %.5f", latitude, longitude)
                }
                _state.value = _state.value.copy(currentLocationLabel = locationLabel)
            }
        }
    }

    private fun updateNextPrayerAndCountdown(prayerTimes: PrayerTimes) {
        val now = System.currentTimeMillis()
        var nextTime: Long = 0
        var nextName = ""

        when {
            prayerTimes.fajr != null && prayerTimes.fajr.time > now -> {
                nextTime = prayerTimes.fajr.time
                nextName = "صبح"
            }
            prayerTimes.dhuhr != null && prayerTimes.dhuhr.time > now -> {
                nextTime = prayerTimes.dhuhr.time
                nextName = "ظهر"
            }
            prayerTimes.asr != null && prayerTimes.asr.time > now -> {
                nextTime = prayerTimes.asr.time
                nextName = "عصر"
            }
            prayerTimes.maghrib != null && prayerTimes.maghrib.time > now -> {
                nextTime = prayerTimes.maghrib.time
                nextName = "مغرب"
            }
            prayerTimes.isha != null && prayerTimes.isha.time > now -> {
                nextTime = prayerTimes.isha.time
                nextName = "عشاء"
            }
            prayerTimes.fajr != null -> {
                nextTime = prayerTimes.fajr.time + 24L * 60L * 60L * 1000L
                nextName = "صبح"
            }
        }

        if (nextTime == 0L) return

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        _state.value = _state.value.copy(
            nextPrayerName = nextName,
            nextPrayerTime = sdf.format(Date(nextTime))
        )

        startCountdown(nextTime)
    }

    private fun startCountdown(targetMillis: Long) {
        _countdownJob?.cancel()
        _countdownJob = viewModelScope.launch {
            while (true) {
                val diff = targetMillis - System.currentTimeMillis()
                if (diff <= 0) {
                    _state.value = _state.value.copy(remainingTime = "00:00:00")
                    break
                }

                val total = diff / 1000
                val h = total / 3600
                val m = (total % 3600) / 60
                val s = total % 60

                _state.value = _state.value.copy(
                    remainingTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
                )

                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _countdownJob?.cancel()
    }
}