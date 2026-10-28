package com.androidhive.myapplication.platform.ramadan

import android.app.*
import android.content.*
import android.os.Build
import android.util.Log
import com.androidhive.myapplication.SalahApplication
import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.data.settings.SettingsRepository
import com.androidhive.myapplication.domain.ramadan.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.*

class RamadanAlarmCoordinator(context: Context, private val settings: SettingsRepository,
    private val engine: RamadanEngine, val notifications: RamadanNotifications,
    private val clock: Clock = Clock.systemUTC()) {
    private val app = context.applicationContext
    private val manager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val mutex = Mutex()
    fun exactAllowed() = Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms()
    private fun pending(kind: RamadanReminderKind, reminder: RamadanReminder? = null) = PendingIntent.getBroadcast(
        app, 420 + kind.ordinal, Intent(app, RamadanDueReceiver::class.java).apply {
            action = ACTION_DUE
            putExtra(KIND, kind.name)
            reminder?.let { putExtra(TIME, it.time.toEpochMilli()); putExtra(DATE, it.fastingDate.toString()); putExtra(FAJR, it.fajr.toEpochMilli()) }
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private fun cancelScheduledReminders() { RamadanReminderKind.entries.forEach { manager.cancel(pending(it)) } }
    suspend fun refresh() = withContext(Dispatchers.IO) { mutex.withLock {
        cancelScheduledReminders()
        val s = settings.snapshot()
        if (!s.ramadan.suhurEnabled && !(app as SalahApplication).container.suhurSession.state.value.preview) app.stopService(Intent(app, SuhurAlarmService::class.java))
        if (!s.ramadan.iftarEnabled) notifications.cancelIftar()
        // A wake-up alarm is never advertised as scheduled while exact delivery or
        // visible notification controls are unavailable. Preferences retain intent.
        if (!s.ramadan.suhurEnabled && !s.ramadan.iftarEnabled) return@withLock
        if (s.location == null || !exactAllowed()) return@withLock
        notifications.channels(s.language)
        try {
            for (kind in RamadanReminderKind.entries) if (notifications.allowed(kind)) engine.next(s, kind, clock.instant())?.let { reminder ->
                val operation = pending(kind, reminder)
                if (kind == RamadanReminderKind.SUHUR) manager.setAlarmClock(AlarmManager.AlarmClockInfo(reminder.time.toEpochMilli(), notifications.open()), operation)
                else if (Build.VERSION.SDK_INT >= 23) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.time.toEpochMilli(), operation)
                else manager.setExact(AlarmManager.RTC_WAKEUP, reminder.time.toEpochMilli(), operation)
            }
        } catch (error: Exception) { cancelScheduledReminders(); throw error }
    } }
    suspend fun deliver(intent: Intent) {
        try {
            val kind = runCatching { RamadanReminderKind.valueOf(intent.getStringExtra(KIND).orEmpty()) }.getOrNull() ?: return
            val time = intent.getLongExtra(TIME, 0L)
            val fajr = intent.getLongExtra(FAJR, 0L)
            val date = runCatching { LocalDate.parse(intent.getStringExtra(DATE)) }.getOrNull() ?: return
            if (time <= 0 || fajr <= 0) return
            val now = clock.instant()
            val reminder = RamadanReminder(kind, Instant.ofEpochMilli(time), date, Instant.ofEpochMilli(fajr))
            if (Duration.between(reminder.time, now).seconds !in 0L..600L || kind == RamadanReminderKind.SUHUR && !now.isBefore(reminder.fajr)) return
            val s = settings.snapshot()
            if (!engine.validates(s, reminder) || !exactAllowed() || !notifications.allowed(kind)) return
            if (kind == RamadanReminderKind.IFTAR) notifications.iftar(s.language)
            else start(s)
        } finally { refresh() }
    }
    private fun start(s: AppSettings) {
        val service = Intent(app, SuhurAlarmService::class.java).putExtra(SuhurAlarmService.LANGUAGE, s.language.name)
        try { if (Build.VERSION.SDK_INT >= 26) app.startForegroundService(service) else app.startService(service) }
        catch (error: RuntimeException) { notifications.soundError(s.language); Log.w("SuhurAlarm", "Foreground alarm unavailable", error) }
    }
    fun preview(s: AppSettings) {
        val service = Intent(app, SuhurAlarmService::class.java).putExtra(SuhurAlarmService.LANGUAGE, s.language.name).putExtra(SuhurAlarmService.PREVIEW, true)
        if (Build.VERSION.SDK_INT >= 26) app.startForegroundService(service) else app.startService(service)
    }
    fun stop() { app.stopService(Intent(app, SuhurAlarmService::class.java)) }
    companion object {
        const val ACTION_DUE = "com.androidhive.myapplication.RAMADAN_DUE"
        const val KIND = "ramadan_kind"
        const val TIME = "ramadan_time"
        const val DATE = "fasting_date"
        const val FAJR = "fasting_fajr"
    }
}
class RamadanDueReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != RamadanAlarmCoordinator.ACTION_DUE) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try { (context.applicationContext as SalahApplication).container.ramadanAlarms.deliver(intent) }
            catch (error: Exception) { Log.e("SuhurAlarm", "Unable to deliver reminder", error) }
            finally { pending.finish() }
        }
    }
}
class StopSuhurReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) { context.stopService(Intent(context, SuhurAlarmService::class.java)) }
}
