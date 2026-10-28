package com.androidhive.myapplication.platform.ramadan

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.androidhive.myapplication.MainActivity
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.localizedContext
import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.domain.ramadan.RamadanReminderKind

class RamadanNotifications(context: Context) {
    private val app = context.applicationContext
    private val manager = NotificationManagerCompat.from(app)
    fun allowed(kind: RamadanReminderKind = RamadanReminderKind.SUHUR): Boolean = manager.areNotificationsEnabled() && (Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        (Build.VERSION.SDK_INT < 26 || manager.getNotificationChannel(if (kind == RamadanReminderKind.SUHUR) SUHUR_CHANNEL else IFTAR_CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE)
    fun channels(language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= 26) {
            val c = localizedContext(app, language)
            manager.createNotificationChannel(NotificationChannel(SUHUR_CHANNEL, c.getString(R.string.ui_suhur_alarm), NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null); enableVibration(false)
            })
            manager.createNotificationChannel(NotificationChannel(IFTAR_CHANNEL, c.getString(R.string.ui_iftar_reminder), NotificationManager.IMPORTANCE_HIGH))
        }
    }
    fun open() = PendingIntent.getActivity(app, 410, Intent(app, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra(MainActivity.EXTRA_ROUTE, "ramadan")
    }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun iftar(language: AppLanguage) {
        channels(language)
        val c = localizedContext(app, language)
        try { manager.notify(IFTAR_ID, NotificationCompat.Builder(app, IFTAR_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_prayer).setContentTitle(c.getString(R.string.ui_iftar))
            .setContentText(c.getString(R.string.ui_iftar_now)).setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(open()).setAutoCancel(true).setTimeoutAfter(30 * 60_000L).build())
        } catch (_: SecurityException) { }
    }
    fun soundError(language: AppLanguage) {
        channels(language)
        val c = localizedContext(app, language)
        try { manager.notify(ERROR_ID, NotificationCompat.Builder(app, SUHUR_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_prayer).setContentTitle(c.getString(R.string.ui_suhur_alarm))
            .setContentText(c.getString(R.string.ui_alarm_sound_error)).setContentIntent(open()).setAutoCancel(true).build())
        } catch (_: SecurityException) { }
    }
    fun cancelIftar() = manager.cancel(IFTAR_ID)
    companion object {
        const val SUHUR_CHANNEL = "suhur_alarm"
        const val IFTAR_CHANNEL = "iftar_reminders"
        const val ALARM_ID = 4001
        const val IFTAR_ID = 4002
        const val ERROR_ID = 4003
    }
}
