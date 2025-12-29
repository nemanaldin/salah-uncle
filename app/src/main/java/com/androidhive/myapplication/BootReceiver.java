package com.androidhive.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (!AppPrefs.isAutoAdhanEnabled(context)) return;

        PrayerScheduler.computeAndScheduleNext(context);
        PrayerNotification.showOrUpdate(context, false);
    }
}
