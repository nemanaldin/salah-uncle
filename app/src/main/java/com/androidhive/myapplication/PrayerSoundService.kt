package com.androidhive.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat

class PrayerSoundService : Service() {

    companion object {
        const val ACTION_PLAY = "com.androidhive.myapplication.ACTION_PLAY_ADHAN"
        const val ACTION_STOP = "com.androidhive.myapplication.ACTION_STOP_ADHAN"

        private const val CHANNEL_ID = "adhan_playback"
        private const val NOTIF_ID = 3001
    }

    private var player: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.action
        if (ACTION_PLAY == action) {
            startAsForeground()
            playAdhan()
        } else if (ACTION_STOP == action) {
            stopAdhan()
        }

        return START_NOT_STICKY
    }
    private fun startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Adhan Playback",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Plays adhan and shows full-screen alert"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        // Stop action from notification
        val stopIntent = Intent(this, AdhanControlReceiver::class.java).apply {
            action = AdhanControlReceiver.ACTION_STOP
        }
        val piStop = android.app.PendingIntent.getBroadcast(
            this, 11, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // Full-screen PendingIntent
        val fsIntent = Intent(this, AdhanAlertActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
        val piFullScreen = android.app.PendingIntent.getActivity(
            this, 12, fsIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("اذان")
            .setContentText("در حال پخش اذان…")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setFullScreenIntent(piFullScreen, true)
            .addAction(0, "قطع اذان", piStop)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    private fun playAdhan() {
        stopPlayerIfAny()
        player = MediaPlayer.create(this, R.raw.dhan_sound)
        if (player == null) {
            stopSelf()
            return
        }

        player?.setOnCompletionListener { stopAdhan() }
        player?.start()
    }

    private fun stopAdhan() {
        stopPlayerIfAny()
        stopForeground(true)
        stopSelf()
    }

    private fun stopPlayerIfAny() {
        try {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (ignored: Exception) {}
        player = null
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopPlayerIfAny()
        super.onDestroy()
    }
}
