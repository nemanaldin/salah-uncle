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

        ensureChannel(context);

        String hhmm = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(t));
        long diff = Math.max(0, t - System.currentTimeMillis());
        String countdown = formatHMS(diff);

        Intent close = new Intent(context, NotificationActionReceiver.class);
        close.setAction(NotificationActionReceiver.ACTION_CLOSE);
        PendingIntent piClose = PendingIntent.getBroadcast(
                context, 10, close,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("نماز بعدی: " + name + " — " + hhmm)
                .setContentText("مانده تا اذان: " + countdown)
                .setAutoCancel(true)
                .setOngoing(false)
                .addAction(0, "بستن", piClose);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIF_ID, b.build());
    }

    public static void hide(Context context) {
        AppPrefs.setNotifHidden(context, true);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIF_ID);
    }

    private static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Next Prayer", NotificationManager.IMPORTANCE_DEFAULT
            );
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
