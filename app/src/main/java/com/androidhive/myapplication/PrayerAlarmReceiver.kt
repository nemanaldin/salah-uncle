package com.androidhive.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // اگر نه پخش خودکار و نه نوتیفیکیشن فعال است، هیچ کاری نکن
        val auto = AppPrefs.isAutoAdhanEnabled(context)
        val notif = AppPrefs.isPrayerNotificationsEnabled(context)
        if (!auto && !notif) return

        // اگر فقط نوتیفیکیشن فعال است، همینجا نوتیف را نشان بده (بدون صدا)
        if (notif) {
            PrayerNotification.showOrUpdate(context, true)
        }

        // پخش اذان (فقط وقتی حالت «پخش خودکار اذان» روشن است)
        if (auto) {
            val s = Intent(context, PrayerSoundService::class.java)
            s.action = PrayerSoundService.ACTION_PLAY
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(s)
            } else {
                context.startService(s)
            }
        }

        // نماز بعدی را محاسبه کن و آلارم بعدی را ست کن
        PrayerScheduler.computeAndScheduleNext(context)
    }
}