package com.androidhive.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object PrayerScheduler {

    private const val ALARM_REQUEST_CODE = 100

    fun computeAndScheduleNext(context: Context) {
        // وقتی یکی از این‌ها روشن باشد آلارم را ست کن: «پخش خودکار اذان» یا «فقط نوتیفیکیشن»
        val auto = AppPrefs.isAutoAdhanEnabled(context)
        val notif = AppPrefs.isPrayerNotificationsEnabled(context)
        if (!auto && !notif) {
            // هیچ حالتی فعال نیست؛ آلارم قبلی را هم کنسل کنیم
            cancelAlarm(context)
            return
        }

        val lat = AppPrefs.getLat(context)
        val lon = AppPrefs.getLon(context)
        if (lat == 0.0 && lon == 0.0) return

        val np = PrayerTimesCalculator.computeNext(lat, lon) ?: return
        if (np.timeMillis <= 0) return

        AppPrefs.saveNextPrayer(context, np.nameFa, np.timeMillis)

        // هر بار نماز جدید شد، نوتیفیکیشن دوباره اجازه نمایش داشته باشد
        AppPrefs.resetNotifHiddenOnNewPrayer(context)

        scheduleAlarm(context, np.timeMillis)

        // نوتیفیکیشن نماز بعدی
        PrayerNotification.showOrUpdate(context, true)
    }

    private fun scheduleAlarm(context: Context, triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, ALARM_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.cancel(pi)

        // Android 12+ : Exact alarm ممکنه نیاز به اجازه داشته باشه
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                // بدون کرش: fallback
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    fun cancelAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, ALARM_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.cancel(pi)
    }
}