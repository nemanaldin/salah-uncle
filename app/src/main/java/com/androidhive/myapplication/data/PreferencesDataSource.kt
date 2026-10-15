package com.androidhive.myapplication.data

import android.content.Context

interface PreferencesDataSource {
    // Location
    fun saveLocation(context: Context, lat: Double, lon: Double)
    fun getLat(context: Context): Double
    fun getLon(context: Context): Double
    fun clearAll(context: Context)
    fun clearLocation(context: Context)

    // Next Prayer
    fun saveNextPrayer(context: Context, nameFa: String, timeMillis: Long)
    fun getNextPrayerName(context: Context): String
    fun getNextPrayerTime(context: Context): Long

    // Notification State
    fun setNotifHidden(context: Context, hidden: Boolean)
    fun isNotifHidden(context: Context): Boolean
    fun resetNotifHiddenOnNewPrayer(context: Context)

    // Auto Adhan
    fun setAutoAdhanEnabled(context: Context, enabled: Boolean)
    fun isAutoAdhanEnabled(context: Context): Boolean

    // Location Mode
    fun setLocationMode(context: Context, mode: String)
    fun getLocationMode(context: Context): String
    fun setManualCityName(context: Context, city: String?)
    fun getManualCityName(context: Context): String

    // Prayer Notifications
    fun setPrayerNotificationsEnabled(context: Context, enabled: Boolean)
    fun isPrayerNotificationsEnabled(context: Context): Boolean

    // Calculation Method
    fun setCalculationMethod(context: Context, method: String)
    fun getCalculationMethod(context: Context): String
}