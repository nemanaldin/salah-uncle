package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.settings.LegacySettings
import org.junit.Assert.*
import org.junit.Test

class LegacySettingsTest {
    @Test fun newInstallDoesNotEnableAlertsWithoutAnExplicitChoice() { assertNull(LegacySettings.restore(emptyMap<String, Any>(), null, "UTC")) }
    @Test fun oldFloatCoordinatesAndNotificationOnlyModeSurvive() {
        val restored = LegacySettings.restore(mapOf("last_lat" to 35.5f, "last_lon" to 51.25f,
            "location_mode" to "manual", "location_city" to "تهران", "auto_adhan" to false, "prayer_notif_enabled" to true), "ایران", "UTC")!!
        assertEquals(35.5, restored.location!!.latitude, .000001)
        assertEquals("Asia/Tehran", restored.location.zoneId)
        assertEquals(AlertMode.NOTIFICATION, restored.alertMode)
        assertEquals(AsrConvention.HANAFI, restored.asrConvention)
    }
    @Test fun bitEncodedCoordinatesAndMethodSurvive() {
        val restored = LegacySettings.restore(mapOf("last_lat" to 34.5553.toBits(), "last_lon" to 69.2075.toBits(),
            "location_mode" to "manual", "calculation_method" to "DUBAI", "auto_adhan" to true), "افغانستان", "UTC")!!
        assertEquals(69.2075, restored.location!!.longitude, .000001)
        assertEquals("Asia/Kabul", restored.location.zoneId)
        assertEquals(CalculationPreset.DUBAI, restored.calculationPreset)
        assertEquals(AlertMode.ADHAN, restored.alertMode)
    }
    @Test fun gpsUsesDeviceZoneInsteadOfAnOldManualCountry() {
        val restored = LegacySettings.restore(mapOf("last_lat" to 51.5f, "last_lon" to -.1f, "location_mode" to "gps"), "ایران", "Europe/London")!!
        assertEquals("Europe/London", restored.location!!.zoneId)
    }
    @Test fun invalidLegacyLocationDoesNotDiscardOtherSettings() {
        val restored = LegacySettings.restore(mapOf("last_lat" to Float.NaN, "last_lon" to 51f,
            "auto_adhan" to false, "prayer_notif_enabled" to false, "calculation_method" to "INVALID"), null, "UTC")!!
        assertNull(restored.location)
        assertEquals(AlertMode.OFF, restored.alertMode)
        assertEquals(CalculationPreset.KARACHI, restored.calculationPreset)
    }
    @Test fun hidingTheCurrentPrayerNotificationSurvivesMigration() {
        val restored = LegacySettings.restore(mapOf("notif_hidden" to true, "next_prayer_time" to 1_800_000_000_123L), null, "UTC")!!
        assertEquals(1_800_000_000_000L, restored.hiddenNotificationUntil)
    }
}
