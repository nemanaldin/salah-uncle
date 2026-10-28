package com.androidhive.myapplication.platform.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.androidhive.myapplication.MainActivity
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.labelRes
import com.androidhive.myapplication.core.localizedContext
import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.domain.prayer.PrayerMoment
import java.time.format.DateTimeFormatter
import java.util.Locale

class PrayerNotifications(context: Context) {
    private val app = context.applicationContext
    private val manager = NotificationManagerCompat.from(app)

    fun allowed(): Boolean = manager.areNotificationsEnabled() && (Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        (Build.VERSION.SDK_INT < 26 || manager.getNotificationChannel(CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE)

    private fun channel(settings: AppSettings) {
        if (Build.VERSION.SDK_INT >= 26) {
            val c = localizedContext(app, settings.language)
            manager.createNotificationChannel(NotificationChannel(CHANNEL, c.getString(R.string.ui_prayer_channel), NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null); enableVibration(false)
            })
        }
    }

    private fun open() = PendingIntent.getActivity(app, 10, Intent(app, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra(MainActivity.EXTRA_ROUTE, "times")
    }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun showNext(settings: AppSettings, next: PrayerMoment) {
        if (!allowed() || settings.hiddenNotificationUntil == next.time.toEpochMilli()) return
        channel(settings)
        val c = localizedContext(app, settings.language)
        val location = settings.location ?: return
        val time = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag(settings.language.tag)).withZone(location.zone).format(next.time)
        val hide = PendingIntent.getBroadcast(app, 11, Intent(app, NotificationDismissReceiver::class.java)
            .putExtra(EXTRA_TIME, next.time.toEpochMilli()), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(app, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_prayer)
            .setContentTitle(c.getString(R.string.ui_notification_next, c.getString(next.id.labelRes())))
            .setContentText(c.getString(R.string.ui_notification_time, time, location.zoneId))
            .setWhen(next.time.toEpochMilli()).setShowWhen(true)
            .setContentIntent(open()).setDeleteIntent(hide)
            .setOnlyAlertOnce(true).setSilent(true).setOngoing(false)
            .addAction(0, c.getString(R.string.ui_hide_notification), hide)
        if (Build.VERSION.SDK_INT >= 24) notification.setUsesChronometer(true).setChronometerCountDown(true)
        try { manager.notify(NEXT_ID, notification.build()) } catch (_: SecurityException) { /* Permission can change between checks. */ }
    }

    fun showDue(settings: AppSettings, moment: PrayerMoment) {
        if (!allowed()) return
        channel(settings)
        val c = localizedContext(app, settings.language)
        try {
            manager.notify(DUE_ID, NotificationCompat.Builder(app, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_prayer)
                .setContentTitle(c.getString(R.string.ui_adhan_now))
                .setContentText(c.getString(R.string.ui_notification_due, c.getString(moment.id.labelRes())))
                .setContentIntent(open()).setAutoCancel(true).setSilent(true).setTimeoutAfter(30 * 60_000L).build())
        } catch (_: SecurityException) { /* A denied notification must not break the alarm chain. */ }
    }
    fun cancelNext() = manager.cancel(NEXT_ID)
    fun cancelAll() { manager.cancel(NEXT_ID); manager.cancel(DUE_ID) }

    companion object {
        const val CHANNEL = "next_prayer"
        const val NEXT_ID = 2001
        const val DUE_ID = 2002
        const val EXTRA_TIME = "prayer_time"
    }
}
