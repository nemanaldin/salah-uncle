package com.androidhive.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.androidhive.myapplication.data.PrayerNotificationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrayerNotification : PrayerNotificationManager {

    private const val CHANNEL_ID = "next_prayer"
    private const val NOTIF_ID = 2001

    override fun showOrUpdate(context: Context, forceShow: Boolean) {
        if (AppPrefs.isNotifHidden(context) && !forceShow) return

        val t = AppPrefs.getNextPrayerTime(context)
        val name = AppPrefs.getNextPrayerName(context)
        if (t <= 0 || name.isEmpty()) return

        ensureChannelSilent(context)

        val openIntent = Intent(context, MainActivity::class.java)
        openIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val openPi = PendingIntent.getActivity(
            context, 10, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val closeIntent = Intent(context, NotificationActionReceiver::class.java)
        closeIntent.action = NotificationActionReceiver.ACTION_CLOSE

        val closePi = PendingIntent.getBroadcast(
            context, 11, closeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(t))
        var diff = t - System.currentTimeMillis()
        if (diff < 0) diff = 0

        val b = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.untitled)
            .setContentTitle("نماز بعدی: $name - $hhmm")
            .setContentText("زمان باقی‌مانده: ${formatHMS(diff)}")
            .setContentIntent(openPi)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true) // ✅ آپدیت‌ها دیگر صدا نمی‌دهند
            .setSilent(true)        // ✅ سایلنت
            .addAction(0, "بستن", closePi)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, b.build())
    }

    override fun hide(context: Context) {
        AppPrefs.setNotifHidden(context, true)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIF_ID)
    }

    override fun cancel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIF_ID)
    }

    private fun ensureChannelSilent(c: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Next Prayer", NotificationManager.IMPORTANCE_LOW
            )
            ch.setSound(null, null)
            ch.enableVibration(false)
            ch.enableLights(false)

            val nm = c.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(ch)
        }
    }

    private fun formatHMS(millis: Long): String {
        val total = millis / 1000
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    }
}