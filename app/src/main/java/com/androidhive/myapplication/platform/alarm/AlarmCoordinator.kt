package com.androidhive.myapplication.platform.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.androidhive.myapplication.core.model.AlertMode
import com.androidhive.myapplication.core.model.PrayerId
import com.androidhive.myapplication.core.model.enumOrDefault
import com.androidhive.myapplication.data.settings.SettingsRepository
import com.androidhive.myapplication.domain.prayer.PrayerEngine
import com.androidhive.myapplication.domain.prayer.PrayerMoment
import com.androidhive.myapplication.platform.audio.AdhanService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Duration

class AlarmCoordinator(
    context: Context, private val settings: SettingsRepository, private val engine: PrayerEngine,
    private val notifications: PrayerNotifications, private val clock: Clock = Clock.systemUTC(),
) {
    private val app = context.applicationContext
    private val manager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val mutex = Mutex()
    fun exactAllowed() = Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        mutex.withLock {
            val snapshot = settings.snapshot()
            val (next, nextAlert) = try {
                engine.next(snapshot, clock.instant()) to engine.nextAlert(snapshot, clock.instant())
            } catch (error: Exception) {
                cancel(); notifications.cancelAll(); throw error
            }
            if (snapshot.alertMode == AlertMode.OFF || nextAlert == null) {
                cancel(); notifications.cancelAll(); return@withLock
            }
            schedule(nextAlert)
            if (next != null) notifications.showNext(snapshot, next)
        }
    }

    private fun pending(moment: PrayerMoment? = null): PendingIntent = PendingIntent.getBroadcast(
        app, 100, Intent(app, PrayerDueReceiver::class.java).apply {
            action = ACTION_DUE
            moment?.let { putExtra(EXTRA_ID, it.id.name); putExtra(PrayerNotifications.EXTRA_TIME, it.time.toEpochMilli()) }
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    private fun cancel() {
        manager.cancel(pending())
        // Remove alarms scheduled by the pre-Compose application during an upgrade.
        val legacy = PendingIntent.getBroadcast(app, 100, Intent().setClassName(app, "com.androidhive.myapplication.PrayerAlarmReceiver"),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (legacy != null) { manager.cancel(legacy); legacy.cancel() }
    }
    private fun schedule(moment: PrayerMoment) {
        cancel()
        val intent = pending(moment)
        val millis = moment.time.toEpochMilli()
        try {
            when {
                Build.VERSION.SDK_INT >= 23 && exactAllowed() -> manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent)
                Build.VERSION.SDK_INT >= 23 -> manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent)
                else -> manager.setExact(AlarmManager.RTC_WAKEUP, millis, intent)
            }
        } catch (_: SecurityException) {
            if (Build.VERSION.SDK_INT >= 23) manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent)
            else manager.set(AlarmManager.RTC_WAKEUP, millis, intent)
        }
    }

    suspend fun deliver(intent: Intent) {
        try {
            val snapshot = settings.snapshot()
            val time = intent.getLongExtra(PrayerNotifications.EXTRA_TIME, 0L)
            if (time <= 0L || snapshot.location == null) return
            val id = enumOrDefault(intent.getStringExtra(EXTRA_ID), PrayerId.SUNRISE)
            val moment = PrayerMoment(id, java.time.Instant.ofEpochMilli(time))
            val lateness = Duration.between(moment.time, clock.instant()).seconds
            if (lateness !in 0L..600L) return
            val mode = snapshot.alertFor(id)
            if (mode == AlertMode.OFF) return
            // Reject an obsolete delivery after location or calculation settings changed.
            val valid = engine.upcoming(snapshot, moment.time.minusSeconds(1)).any { it.id == id && it.time == moment.time }
            if (!valid) return
            notifications.showDue(snapshot, moment)
            if (mode == AlertMode.ADHAN) {
                val service = Intent(app, AdhanService::class.java).putExtra(EXTRA_ID, id.name)
                    .putExtra(AdhanService.EXTRA_LANGUAGE, snapshot.language.name)
                try {
                    if (Build.VERSION.SDK_INT >= 26) app.startForegroundService(service) else app.startService(service)
                } catch (error: RuntimeException) {
                    // Inexact alarms may not be allowed to start an FGS. The due notification remains usable.
                    Log.w("PrayerAlarm", "Adhan foreground playback unavailable", error)
                }
            }
        } finally {
            refresh()
        }
    }

    companion object {
        const val ACTION_DUE = "com.androidhive.myapplication.PRAYER_DUE"
        const val EXTRA_ID = "prayer_id"
    }
}
