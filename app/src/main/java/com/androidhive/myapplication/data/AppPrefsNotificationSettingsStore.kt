package com.androidhive.myapplication.data

import android.content.Context
import com.androidhive.myapplication.AppPrefs

class AppPrefsNotificationSettingsStore(
    private val context: Context
) : NotificationSettingsStore {

    private val prefs = AppPrefs

    override fun getAutoAdhanEnabled(): Boolean =
        prefs.isAutoAdhanEnabled(context)

    override fun setAutoAdhanEnabled(enabled: Boolean) =
        prefs.setAutoAdhanEnabled(context, enabled)

    override fun getPrayerNotificationsEnabled(): Boolean =
        prefs.isPrayerNotificationsEnabled(context)

    override fun setPrayerNotificationsEnabled(enabled: Boolean) =
        prefs.setPrayerNotificationsEnabled(context, enabled)
}