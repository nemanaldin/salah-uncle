package com.androidhive.myapplication

data class MainUiState(
    // Next prayer countdown
    val nextPrayerName: String = "",
    val nextPrayerTime: String = "--:--",
    val remainingTime: String = "--:--:--",

    // Location
    val currentLocationLabel: String = "در انتظار انتخاب مکان",
    val hasValidLocation: Boolean = false
)