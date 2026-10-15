package com.androidhive.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.androidhive.myapplication.data.AlarmScheduler
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters

object PrayerScheduler : AlarmScheduler {

    private const val ALARM_REQUEST_CODE = 100

    override fun computeAndScheduleNext(context: Context, calcParams: CalculationParameters) {
        computeAndScheduleNextInternal(context, calcParams)
    }

    override fun computeAndScheduleNext(context: Context) {
        val calcParams = getCalculationParameters(context)
        computeAndScheduleNextInternal(context, calcParams)
    }

    private fun computeAndScheduleNextInternal(context: Context, calcParams: CalculationParameters) {
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

        val np = PrayerTimesCalculator.computeNext(lat, lon, calcParams) ?: return
        if (np.timeMillis <= 0) return

        AppPrefs.saveNextPrayer(context, np.nameFa, np.timeMillis)

        // هر بار نماز جدید شد، نوتیفیکیشن دوباره اجازه نمایش داشته باشد
        AppPrefs.resetNotifHiddenOnNewPrayer(context)

        scheduleAlarm(context, np.timeMillis)

        // نوتیفیکیشن نماز بعدی — فقط اگر اعلان نماز فعال باشد
        if (notif) {
            PrayerNotification.showOrUpdate(context, true)
        }
    }

    private fun getCalculationParameters(context: Context): CalculationParameters {
        val methodString = AppPrefs.getCalculationMethod(context)
        val method: CalculationMethod = when (methodString) {
            "MUSLIM_WORLD_LEAGUE" -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            "EGYPTIAN" -> CalculationMethod.EGYPTIAN
            "KARACHI" -> CalculationMethod.KARACHI
            "UMM_AL_QURA" -> CalculationMethod.UMM_AL_QURA
            "DUBAI" -> CalculationMethod.DUBAI
            "MOON_SIGHTING_COMMITTEE" -> CalculationMethod.MOON_SIGHTING_COMMITTEE
            else -> CalculationMethod.KARACHI
        }
        return method.parameters
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

    override fun cancelAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, ALARM_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        am.cancel(pi)
    }
}