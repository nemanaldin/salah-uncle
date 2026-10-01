package com.androidhive.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AdhanControlReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_STOP = "com.androidhive.myapplication.STOP_ADHAN"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && ACTION_STOP == intent.action) {
            val s = Intent(context, PrayerSoundService::class.java).apply {
                action = PrayerSoundService.ACTION_STOP
            }
            context.startService(s)
        }
    }
}