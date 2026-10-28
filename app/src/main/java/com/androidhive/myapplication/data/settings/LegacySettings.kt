package com.androidhive.myapplication.data.settings

import com.androidhive.myapplication.core.model.*

/** Android-free migration of the actual keys used by versions up to 2.2. */
object LegacySettings {
    fun restore(values: Map<String, *>, country: String?, deviceZone: String): AppSettings? {
        if (values.isEmpty()) return null
        fun coordinate(key: String): Double = when (val value = values[key]) {
            is Long -> Double.fromBits(value); is Float -> value.toDouble(); is Double -> value; else -> 0.0
        }
        val latitude = coordinate("last_lat")
        val longitude = coordinate("last_lon")
        val mode = if (values["location_mode"] == "gps") LocationMode.GPS else LocationMode.MANUAL
        val zone = if (mode == LocationMode.GPS) deviceZone else when (country) {
            "ایران" -> "Asia/Tehran"; "افغانستان" -> "Asia/Kabul"; "تاجیکستان" -> "Asia/Dushanbe"; else -> deviceZone
        }
        val location = if (latitude == 0.0 && longitude == 0.0) null else runCatching {
            SavedLocation(latitude, longitude, values["location_city"] as? String ?: "", zone, mode)
        }.getOrNull()
        return AppSettings(location = location,
            calculationPreset = enumOrDefault(values["calculation_method"] as? String, CalculationPreset.KARACHI),
            hiddenNotificationUntil = if (values["notif_hidden"] == true) ((values["next_prayer_time"] as? Long ?: 0L) / 1000L) * 1000L else 0L,
            alertMode = when {
                values["auto_adhan"] == true -> AlertMode.ADHAN
                values["prayer_notif_enabled"] != false -> AlertMode.NOTIFICATION
                else -> AlertMode.OFF
            })
    }
}
