package com.androidhive.myapplication.data

interface NotificationSettingsStore {
    fun getAutoAdhanEnabled(): Boolean
    fun setAutoAdhanEnabled(enabled: Boolean)
    fun getPrayerNotificationsEnabled(): Boolean
    fun setPrayerNotificationsEnabled(enabled: Boolean)
}