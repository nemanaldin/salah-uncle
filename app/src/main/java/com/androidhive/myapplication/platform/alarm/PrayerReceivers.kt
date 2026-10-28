package com.androidhive.myapplication.platform.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.androidhive.myapplication.SalahApplication
import com.androidhive.myapplication.core.model.LocationMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.ZoneId

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
private fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    receiverScope.launch {
        try { block() } catch (error: Exception) { Log.e("PrayerReceiver", "Unable to update prayer alarms", error) }
        finally { pending.finish() }
    }
}

class PrayerDueReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmCoordinator.ACTION_DUE) return
        val container = (context.applicationContext as SalahApplication).container
        runAsync { container.alarms.deliver(intent) }
    }
}

class RestoreAlarmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in actions) return
        val container = (context.applicationContext as SalahApplication).container
        runAsync {
            if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) container.settings.update { settings ->
                val location = settings.location
                if (location?.mode == LocationMode.GPS) settings.copy(location = location.copy(zoneId = ZoneId.systemDefault().id)) else settings
            }
            try { container.alarms.refresh() } finally { container.ramadanAlarms.refresh() }
        }
    }
    companion object {
        private val actions = buildSet {
            addAll(listOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED))
            if (Build.VERSION.SDK_INT >= 31) add(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
        }
    }
}

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val time = intent.getLongExtra(PrayerNotifications.EXTRA_TIME, 0L)
        if (time <= 0L) return
        val container = (context.applicationContext as SalahApplication).container
        runAsync { container.settings.hideNotification(time); container.notifications.cancelNext() }
    }
}
