package com.androidhive.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!AppPrefs.isAutoAdhanEnabled(context)) return

        PrayerScheduler.computeAndScheduleNext(context)
        PrayerNotification.showOrUpdate(context, false)
    }
}