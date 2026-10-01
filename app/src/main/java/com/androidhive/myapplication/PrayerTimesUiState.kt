package com.androidhive.myapplication

data class PrayerTimesUiState(
    // Prayer times display
    val fajrTime: String = "--:--",
    val sunriseTime: String = "--:--",
    val dhuhrTime: String = "--:--",
    val asrTime: String = "--:--",
    val maghribTime: String = "--:--",
    val ishaTime: String = "--:--",

    // Next prayer countdown
    val nextPrayerName: String = "",
    val nextPrayerTime: String = "--:--",
    val remainingTime: String = "--:--:--",

    // Location
    val currentLocationLabel: String = "در انتظار انتخاب مکان",
    val isManualMode: Boolean = false,

    // Settings
    val isAutoAdhanEnabled: Boolean = false,
    val isPrayerNotificationsEnabled: Boolean = false,
    val calculationMethodPosition: Int = 0,

    // Spinner data (for manual mode)
    val countries: List<String> = emptyList(),
    val provinces: List<String> = emptyList(),
    val cities: List<String> = emptyList(),
    val selectedCountry: String? = null,
    val selectedProvince: String? = null,
    val selectedCity: String? = null,

    // UI state
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)