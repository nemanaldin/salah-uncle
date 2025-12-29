package com.androidhive.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class PrayerScheduler {

    public static void computeAndScheduleNext(Context context) {
        // فقط وقتی اذان خودکار روشن است آلارم را ست کن
        if (!AppPrefs.isAutoAdhanEnabled(context)) return;

        double lat = AppPrefs.getLat(context);
        double lon = AppPrefs.getLon(context);
        if (lat == 0 && lon == 0) return;

        PrayerTimesCalculator.NextPrayer np = PrayerTimesCalculator.computeNext(lat, lon);
        if (np == null || np.timeMillis <= 0) return;

        AppPrefs.saveNextPrayer(context, np.nameFa, np.timeMillis);

        // هر بار نماز جدید شد، نوتیفیکیشن دوباره اجازه نمایش داشته باشد
        AppPrefs.resetNotifHiddenOnNewPrayer(context);

        scheduleAlarm(context, np.timeMillis);

        // نوتیفیکیشن نماز بعدی
        PrayerNotification.showOrUpdate(context, true);
    }

    private static void scheduleAlarm(Context context, long triggerAtMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, PrayerAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 100, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.cancel(pi);

        // Android 12+ : Exact alarm ممکنه نیاز به اجازه داشته باشه
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            } else {
                // بدون کرش: fallback
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            }
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    }

    public static void cancelAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, PrayerAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 100, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.cancel(pi);
    }
}
