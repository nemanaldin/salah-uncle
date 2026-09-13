package com.androidhive.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent != null && ACTION_CLOSE == intent.getAction()) {
            PrayerNotification.hide(context)
        }
    }

    companion object {
        const val ACTION_CLOSE = "com.androidhive.myapplication.CLOSE_NEXT_PRAYER_NOTIF"
    }
}