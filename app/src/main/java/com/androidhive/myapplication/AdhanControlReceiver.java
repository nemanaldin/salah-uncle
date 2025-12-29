package com.androidhive.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AdhanControlReceiver extends BroadcastReceiver {

    public static final String ACTION_STOP = "com.androidhive.myapplication.STOP_ADHAN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            Intent s = new Intent(context, PrayerSoundService.class);
            s.setAction(PrayerSoundService.ACTION_STOP);
            context.startService(s);
        }
    }
}
