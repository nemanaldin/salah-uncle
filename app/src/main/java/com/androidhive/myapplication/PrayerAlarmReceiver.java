package com.androidhive.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // اگر اذان خودکار خاموش است، هیچ کاری نکن
        if (!AppPrefs.isAutoAdhanEnabled(context)) return;

        // پخش اذان + فول‌اسکرین (از طریق نوتیفیکیشن Foreground)
        Intent s = new Intent(context, PrayerSoundService.class);
        s.setAction(PrayerSoundService.ACTION_PLAY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(s);
        } else {
            context.startService(s);
        }

        // نماز بعدی را محاسبه کن و آلارم بعدی را ست کن
        PrayerScheduler.computeAndScheduleNext(context);
    }
}
