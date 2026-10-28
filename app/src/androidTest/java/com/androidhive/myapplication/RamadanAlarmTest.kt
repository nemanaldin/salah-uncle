package com.androidhive.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.domain.ramadan.RamadanReminderKind
import com.androidhive.myapplication.platform.ramadan.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.time.*

@RunWith(AndroidJUnit4::class)
class RamadanAlarmTest {
    @SdkSuppress(minSdkVersion = 31)
    @Test fun independentExactSuhurAlarmRestoresAfterTimezoneChange() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        fun shell(command: String) {
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
        }
        shell("appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow")
        if (Build.VERSION.SDK_INT >= 33) shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        val container = (context.applicationContext as SalahApplication).container
        val original = container.settings.snapshot()
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            container.settings.update { it.copy(location = SavedLocation(35.7, 51.4, "Tehran", "Asia/Tehran"),
                ramadan = RamadanOptions(suhurEnabled = true, onlyRamadan = false), alertMode = AlertMode.OFF) }
            val settings = container.settings.snapshot()
            val expected = requireNotNull(container.ramadanEngine.next(settings, RamadanReminderKind.SUHUR, Instant.now()))
            assertEquals(90 * 60L, Duration.between(expected.time, expected.fajr).seconds)
            container.ramadanAlarms.refresh()
            withTimeout(15_000) { while (manager.nextAlarmClock?.triggerTime != expected.time.toEpochMilli()) delay(20) }
            val operation = PendingIntent.getBroadcast(context, 420,
                Intent(context, RamadanDueReceiver::class.java).setAction(RamadanAlarmCoordinator.ACTION_DUE),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            manager.cancel(requireNotNull(operation))
            withTimeout(10_000) { while (manager.nextAlarmClock != null) delay(20) }
            // TIME_SET/TIMEZONE_CHANGED are protected broadcasts. Ask the system to
            // change and restore the emulator zone, so its real receiver is exercised.
            val originalZone = java.util.TimeZone.getDefault().id
            instrumentation.uiAutomation.adoptShellPermissionIdentity(android.Manifest.permission.SET_TIME_ZONE)
            try {
                manager.setTimeZone(if (originalZone == "Pacific/Honolulu") "Europe/London" else "Pacific/Honolulu")
                withTimeout(15_000) { while (manager.nextAlarmClock?.triggerTime != expected.time.toEpochMilli()) delay(20) }
                assertEquals("Asia/Tehran", container.settings.snapshot().location!!.zoneId)
            } finally {
                manager.setTimeZone(originalZone)
                instrumentation.uiAutomation.dropShellPermissionIdentity()
            }
            assertEquals(AlertMode.OFF, container.settings.snapshot().alertMode)
        } finally {
            container.settings.update { original }
            container.ramadanAlarms.refresh()
        }
    }
}
