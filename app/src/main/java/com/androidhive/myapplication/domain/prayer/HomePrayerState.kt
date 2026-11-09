package com.androidhive.myapplication.domain.prayer

enum class HomePrayerState { LOADING, LOCATION_NOT_SET, CALCULATION_UNAVAILABLE, LOADING_FAILED, READY }
fun homePrayerState(hasLocation: Boolean, hasNext: Boolean, calculationFailed: Boolean = false,
    readFailed: Boolean = false, loading: Boolean = false): HomePrayerState = when {
    readFailed -> HomePrayerState.LOADING_FAILED
    loading -> HomePrayerState.LOADING
    !hasLocation -> HomePrayerState.LOCATION_NOT_SET
    calculationFailed || !hasNext -> HomePrayerState.CALCULATION_UNAVAILABLE
    else -> HomePrayerState.READY
}
