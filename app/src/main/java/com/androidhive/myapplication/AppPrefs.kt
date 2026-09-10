package com.androidhive.myapplication

import android.content.Context
import android.content.SharedPreferences

object AppPrefs {

    private const val PREFS = "app_prefs"

    private const val KEY_LAST_LAT = "last_lat"
    private const val KEY_LAST_LON = "last_lon"

    private const val KEY_NEXT_PRAYER_TIME = "next_prayer_time"
    private const val KEY_NEXT_PRAYER_NAME = "next_prayer_name"

    private const val KEY_NOTIF_HIDDEN = "notif_hidden"
    private const val KEY_AUTO_ADHAN = "auto_adhan"

    private const val KEY_LOCATION_MODE = "location_mode" // none/gps/manual
    private const val KEY_LOCATION_CITY = "location_city" // فقط برای manual

    private const val KEY_PRAYER_NOTIF_ENABLED = "prayer_notif_enabled" // نوتیفیکیشن وقت اذان

    private fun p(c: Context): SharedPreferences =
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ---- LOCATION ----
    fun saveLocation(c: Context, lat: Double, lon: Double) {
        p(c).edit()
            .putLong(KEY_LAST_LAT, java.lang.Double.doubleToRawLongBits(lat))
            .putLong(KEY_LAST_LON, java.lang.Double.doubleToRawLongBits(lon))
            .apply()
    }

    fun getLat(c: Context): Double {
        val prefs = p(c)
        return try {
            val v = prefs.getLong(KEY_LAST_LAT, 0L)
            if (v == 0L) 0.0 else java.lang.Double.longBitsToDouble(v)
        } catch (e: ClassCastException) {
            val old = prefs.getFloat(KEY_LAST_LAT, 0f)
            val value = old.toDouble()
            prefs.edit().putLong(KEY_LAST_LAT, java.lang.Double.doubleToRawLongBits(value)).apply()
            value
        }
    }

    fun getLon(c: Context): Double {
        val prefs = p(c)
        return try {
            val v = prefs.getLong(KEY_LAST_LON, 0L)
            if (v == 0L) 0.0 else java.lang.Double.longBitsToDouble(v)
        } catch (e: ClassCastException) {
            val old = prefs.getFloat(KEY_LAST_LON, 0f)
            val value = old.toDouble()
            prefs.edit().putLong(KEY_LAST_LON, java.lang.Double.doubleToRawLongBits(value)).apply()
            value
        }
    }

    fun clearAll(c: Context) {
        p(c).edit().clear().apply()
    }

    // ---- NEXT PRAYER ----
    fun saveNextPrayer(c: Context, nameFa: String, timeMillis: Long) {
        p(c).edit()
            .putString(KEY_NEXT_PRAYER_NAME, nameFa)
            .putLong(KEY_NEXT_PRAYER_TIME, timeMillis)
            .apply()
    }

    fun getNextPrayerName(c: Context): String =
        p(c).getString(KEY_NEXT_PRAYER_NAME, "") ?: ""

    fun getNextPrayerTime(c: Context): Long =
        p(c).getLong(KEY_NEXT_PRAYER_TIME, 0L)

    fun getNextPrayerTimeMillis(c: Context): Long = getNextPrayerTime(c)

    // ---- NOTIFICATION STATE ----
    fun setNotifHidden(c: Context, hidden: Boolean) {
        p(c).edit().putBoolean(KEY_NOTIF_HIDDEN, hidden).apply()
    }

    fun isNotifHidden(c: Context): Boolean =
        p(c).getBoolean(KEY_NOTIF_HIDDEN, false)

    fun resetNotifHiddenOnNewPrayer(c: Context) {
        setNotifHidden(c, false)
    }

    // ---- AUTO ADHAN ----
    fun setAutoAdhanEnabled(c: Context, enabled: Boolean) {
        p(c).edit().putBoolean(KEY_AUTO_ADHAN, enabled).apply()
    }

    fun isAutoAdhanEnabled(c: Context): Boolean =
        p(c).getBoolean(KEY_AUTO_ADHAN, false)

    // ---- LOCATION MODE / LABEL ----
    fun setLocationMode(c: Context, mode: String) {
        p(c).edit().putString(KEY_LOCATION_MODE, mode).apply()
    }

    fun getLocationMode(c: Context): String =
        p(c).getString(KEY_LOCATION_MODE, "none") ?: "none"

    fun setManualCityName(c: Context, city: String?) {
        p(c).edit().putString(KEY_LOCATION_CITY, city ?: "").apply()
    }

    fun getManualCityName(c: Context): String =
        p(c).getString(KEY_LOCATION_CITY, "") ?: ""

    // ---- PRAYER NOTIFICATIONS ----
    fun setPrayerNotificationsEnabled(c: Context, enabled: Boolean) {
        p(c).edit().putBoolean(KEY_PRAYER_NOTIF_ENABLED, enabled).apply()
    }

    fun isPrayerNotificationsEnabled(c: Context): Boolean =
        p(c).getBoolean(KEY_PRAYER_NOTIF_ENABLED, true)
}