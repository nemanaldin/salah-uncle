package com.androidhive.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrayerNotification {

    private static final String CHANNEL_ID = "next_prayer";
    private static final int NOTIF_ID = 2001;

    public static void showOrUpdate(Context context, boolean forceShow) {
        if (AppPrefs.isNotifHidden(context) && !forceShow) return;

        long t = AppPrefs.getNextPrayerTime(context);
        String name = AppPrefs.getNextPrayerName(context);
        if (t <= 0 || name == null || name.isEmpty()) return;

        ensureChannelSilent(context);

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent openPi = PendingIntent.getActivity(
                context, 10, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent closeIntent = new Intent(context, NotificationActionReceiver.class);
        closeIntent.setAction(NotificationActionReceiver.ACTION_CLOSE);

        PendingIntent closePi = PendingIntent.getBroadcast(
                context, 11, closeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String hhmm = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(t));
        long diff = t - System.currentTimeMillis();
        if (diff < 0) diff = 0;

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.untitled) // اگر نداری، بگو تا با icon موجودت ست کنم
                .setContentTitle("نماز بعدی: " + name + " - " + hhmm)
                .setContentText("زمان باقی‌مانده: " + formatHMS(diff))
                .setContentIntent(openPi)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)       // ✅ آپدیت‌ها دیگر صدا نمی‌دهند
                .setSilent(true)              // ✅ سایلنت
                .addAction(0, "بستن", closePi)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIF_ID, b.build());
    }

    public static void hide(Context context) {
        AppPrefs.setNotifHidden(context, true);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIF_ID);
    }

    public static void cancel(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIF_ID);
    }

    private static void ensureChannelSilent(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Next Prayer", NotificationManager.IMPORTANCE_LOW
            );
            ch.setSound(null, null);
            ch.enableVibration(false);
            ch.enableLights(false);

            NotificationManager nm = c.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }

    private static String formatHMS(long millis) {
        long total = millis / 1000;
        long h = total / 3600;
        long m = (total % 3600) / 60;
        long s = total % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }
}
