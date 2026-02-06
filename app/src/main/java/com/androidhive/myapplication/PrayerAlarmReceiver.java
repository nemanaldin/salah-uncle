package com.androidhive.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // اگر نه پخش خودکار و نه نوتیفیکیشن فعال است، هیچ کاری نکن
        boolean auto = AppPrefs.isAutoAdhanEnabled(context);
        boolean notif = AppPrefs.isPrayerNotificationsEnabled(context);
        if (!auto && !notif) return;

        // اگر فقط نوتیفیکیشن فعال است، همینجا نوتیف را نشان بده (بدون صدا)
        if (notif) {
            PrayerNotification.showOrUpdate(context, true);
        }

        // پخش اذان (فقط وقتی حالت «پخش خودکار اذان» روشن است)
        if (auto) {
            Intent s = new Intent(context, PrayerSoundService.class);
            s.setAction(PrayerSoundService.ACTION_PLAY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(s);
            } else {
                context.startService(s);
            }
        }

        // نماز بعدی را محاسبه کن و آلارم بعدی را ست کن
        PrayerScheduler.computeAndScheduleNext(context);
    }
}
