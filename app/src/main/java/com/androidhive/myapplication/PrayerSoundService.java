package com.androidhive.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class PrayerSoundService extends Service {

    public static final String ACTION_PLAY = "com.androidhive.myapplication.ACTION_PLAY_ADHAN";
    public static final String ACTION_STOP = "com.androidhive.myapplication.ACTION_STOP_ADHAN";

    private static final String CHANNEL_ID = "adhan_playback";
    private static final int NOTIF_ID = 3001;

    private MediaPlayer player;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (ACTION_PLAY.equals(action)) {
            startAsForeground();
            playAdhan();
        } else if (ACTION_STOP.equals(action)) {
            stopAdhan();
        }

        return START_NOT_STICKY;
    }

    private void startAsForeground() {
        // Full-screen intent باید از داخل نوتیفیکیشن (Foreground) بیاد، نه startActivity مستقیم
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Adhan Playback", NotificationManager.IMPORTANCE_HIGH
            );
            ch.setDescription("Plays adhan and shows full-screen alert");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }

        // اکشن قطع از نوتیفیکیشن
        Intent stop = new Intent(this, AdhanControlReceiver.class);
        stop.setAction(AdhanControlReceiver.ACTION_STOP);
        android.app.PendingIntent piStop = android.app.PendingIntent.getBroadcast(
                this, 11, stop,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        // PendingIntent فول‌اسکرین
        Intent fs = new Intent(this, AdhanAlertActivity.class);
        fs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        android.app.PendingIntent piFullScreen = android.app.PendingIntent.getActivity(
                this, 12, fs,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("اذان")
                .setContentText("در حال پخش اذان…")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setFullScreenIntent(piFullScreen, true)
                .addAction(0, "قطع اذان", piStop)
                .build();

        startForeground(NOTIF_ID, n);
    }

    private void playAdhan() {
        stopPlayerIfAny();
        player = MediaPlayer.create(this, R.raw.dhan_sound);
        if (player == null) {
            stopSelf();
            return;
        }

        player.setOnCompletionListener(mp -> stopAdhan());
        player.start();
    }

    private void stopAdhan() {
        stopPlayerIfAny();
        stopForeground(true);
        stopSelf();
    }

    private void stopPlayerIfAny() {
        try {
            if (player != null) {
                if (player.isPlaying()) player.stop();
                player.release();
            }
        } catch (Exception ignored) {}
        player = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        stopPlayerIfAny();
        super.onDestroy();
    }
}
