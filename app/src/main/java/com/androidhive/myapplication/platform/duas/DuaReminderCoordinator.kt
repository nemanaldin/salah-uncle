package com.androidhive.myapplication.platform.duas

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import androidx.core.net.toUri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.androidhive.myapplication.MainActivity
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahApplication
import com.androidhive.myapplication.core.localizedContext
import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.data.duas.DuaItem
import com.androidhive.myapplication.data.duas.DuaRepository
import com.androidhive.myapplication.data.settings.SettingsRepository
import com.androidhive.myapplication.domain.duas.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.ZoneId

/** A single inexact alarm and independent channel. No prayer/adhan alarm is read, cancelled or changed. */
class DuaReminderCoordinator(context: Context, private val settings: SettingsRepository, private val repository: DuaRepository) {
    private val app = context.applicationContext
    private val alarms = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val manager = NotificationManagerCompat.from(app)
    private val mutex = Mutex()
    fun notificationsAllowed(): Boolean = manager.areNotificationsEnabled() && (Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
        (Build.VERSION.SDK_INT < 26 || manager.getNotificationChannel(CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE)

    private fun pending(slot: Long = 0) = PendingIntent.getBroadcast(app, REQUEST_CODE,
        Intent(app, DuaDueReceiver::class.java).setAction(ACTION).putExtra(EXTRA_SLOT, slot),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    suspend fun refresh() = mutex.withLock { refreshLocked() }
    private suspend fun refreshLocked() {
        val saved = settings.snapshot()
        alarms.cancel(pending())
        val options = saved.duaReminders
        val enabled = options.canSchedule && notificationsAllowed()
        val pool = if (enabled) repository.load().reminderCandidates(options.categoryIds, saved.language.tag, saved.duaBookmarks) else emptyList()
        val now = Instant.now(); val zone = ZoneId.systemDefault()
        val old = saved.duaReminderHistory.scheduledSlot
        val pendingDue = old.takeIf { it > saved.duaReminderHistory.lastSlot && it > 0L }?.let(Instant::ofEpochMilli)
            ?.takeIf { it in duaSlots(now.atZone(zone).toLocalDate(), zone, options) && insideDuaWindow(now, zone, options) }
        val slot = if (pool.isEmpty()) null else pendingDue ?: nextDuaSlot(now, zone, options, saved.duaReminderHistory.lastSlot)
        val millis = slot?.toEpochMilli() ?: 0L
        if (slot != null) {
            // setAndAllowWhileIdle is inexact; late valid reminders are not converted to exact alarms.
            val trigger = maxOf(millis, now.toEpochMilli() + 3000)
            if (Build.VERSION.SDK_INT >= 23) alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending(millis))
            else alarms.set(AlarmManager.RTC_WAKEUP, trigger, pending(millis))
        }
        if (old != millis) try {
            settings.update { it.copy(duaReminderHistory = it.duaReminderHistory.copy(scheduledSlot = millis)) }
        } catch (error: Exception) {
            alarms.cancel(pending()); throw error
        }
    }

    suspend fun deliver(slot: Long) = mutex.withLock {
        val saved = settings.snapshot(); val history = saved.duaReminderHistory
        if (slot <= 0 || slot != history.scheduledSlot || slot <= history.lastSlot) return@withLock
        val now = Instant.now(); val zone = ZoneId.systemDefault(); val due = Instant.ofEpochMilli(slot)
        if (now < due) return@withLock
        try {
            if (!saved.duaReminders.canSchedule || !notificationsAllowed() || !insideDuaWindow(now, zone, saved.duaReminders) ||
                now.atZone(zone).toLocalDate() != due.atZone(zone).toLocalDate()) return@withLock
            val catalog = repository.load()
            val pool = catalog.reminderCandidates(saved.duaReminders.categoryIds, saved.language.tag, saved.duaBookmarks)
            val byKey = pool.associateBy { it.reminderKey }
            val favorites = pool.filter { it.id in saved.duaBookmarks }.map { it.reminderKey }.toSet()
            val draw = drawDua(byKey.keys.toList(), favorites, history.seen, history.lastKey) ?: return@withLock
            val dua = byKey.getValue(draw.id)
            val section = catalog.sectionFor(dua.id) ?: return@withLock
            val latest = settings.snapshot()
            if (latest.duaReminders != saved.duaReminders || latest.language != saved.language) return@withLock
            if (post(saved, dua, "duas/read/${section.id}/${dua.id}", slot)) settings.update { current ->
                current.copy(duaReminderHistory = current.duaReminderHistory.copy(seen = draw.seen, lastKey = draw.id, lastSlot = slot, scheduledSlot = 0L))
            }
        } finally {
            // A skipped/blocked delivery never consumes the deck; missed earlier slots are not replayed as a backlog.
            settings.update { current ->
                if (current.duaReminderHistory.scheduledSlot == slot) current.copy(duaReminderHistory = current.duaReminderHistory.copy(scheduledSlot = 0L)) else current
            }
            refreshLocked()
        }
    }

    private fun post(saved: AppSettings, dua: DuaItem, route: String, slot: Long): Boolean {
        if (!notificationsAllowed()) return false
        val localized = localizedContext(app, saved.language)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
            NotificationChannel(CHANNEL, localized.getString(R.string.ui_dua_reminders), NotificationManager.IMPORTANCE_DEFAULT).apply {
                setSound(null, null); enableVibration(false)
            })
        val open = PendingIntent.getActivity(app, REQUEST_CODE,
            Intent(app, MainActivity::class.java).apply {
                data = "salah://dua/${dua.id}/$slot".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(MainActivity.EXTRA_ROUTE, route)
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val text = duaNotificationText(dua, saved.language.tag, localized.getString(R.string.ui_dua_translation_draft))
        return try {
            manager.notify("dua:$slot", NOTIFICATION_ID, NotificationCompat.Builder(app, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_prayer).setContentTitle(dua.title(saved.language.tag))
                .setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text)).setContentIntent(open)
                .setAutoCancel(true).setOnlyAlertOnce(true).setSilent(true).build())
            true
        } catch (_: SecurityException) { false }
    }

    companion object {
        const val CHANNEL = "dua_reminders_v1"
        const val ACTION = "com.androidhive.myapplication.DUA_REMINDER"
        const val EXTRA_SLOT = "dua_slot"
        private const val REQUEST_CODE = 76_000_001
        private const val NOTIFICATION_ID = 76_000_002
    }
}

private val duaReceiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
private fun BroadcastReceiver.duaAsync(block: suspend () -> Unit) {
    val result = goAsync()
    duaReceiverScope.launch {
        try { withTimeout(8_000) { block() } }
        catch (error: Exception) { Log.e("DuaReminder", "Unable to update dua reminders", error) }
        finally { result.finish() }
    }
}
class DuaDueReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DuaReminderCoordinator.ACTION) return
        val coordinator = (context.applicationContext as SalahApplication).container.duaReminders
        val slot = intent.getLongExtra(DuaReminderCoordinator.EXTRA_SLOT, 0L)
        duaAsync { coordinator.deliver(slot) }
    }
}
class RestoreDuaRemindersReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) return
        val coordinator = (context.applicationContext as SalahApplication).container.duaReminders
        duaAsync { coordinator.refresh() }
    }
}
