package com.androidhive.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION_CLOSE = "com.androidhive.myapplication.CLOSE_NEXT_PRAYER_NOTIF";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_CLOSE.equals(intent.getAction())) {
            PrayerNotification.hide(context);
        }
    }
}
