package com.androidhive.myapplication.data

import android.content.Context

class ContextBoundPreferencesDataSource(
    private val context: Context,
    private val delegate: PreferencesDataSource
) : PreferencesDataSource {

    override fun saveLocation(context: Context, lat: Double, lon: Double) =
        delegate.saveLocation(this.context, lat, lon)

    override fun getLat(context: Context): Double =
        delegate.getLat(this.context)

    override fun getLon(context: Context): Double =
        delegate.getLon(this.context)

    override fun clearLocation(context: Context) =
        delegate.clearLocation(this.context)

    override fun clearAll(context: Context) =
        delegate.clearAll(this.context)

    override fun saveNextPrayer(context: Context, nameFa: String, timeMillis: Long) =
        delegate.saveNextPrayer(this.context, nameFa, timeMillis)

    override fun getNextPrayerName(context: Context): String =
        delegate.getNextPrayerName(this.context)

    override fun getNextPrayerTime(context: Context): Long =
        delegate.getNextPrayerTime(this.context)

    override fun setNotifHidden(context: Context, hidden: Boolean) =
        delegate.setNotifHidden(this.context, hidden)

    override fun isNotifHidden(context: Context): Boolean =
        delegate.isNotifHidden(this.context)

    override fun resetNotifHiddenOnNewPrayer(context: Context) =
        delegate.resetNotifHiddenOnNewPrayer(this.context)

    override fun setAutoAdhanEnabled(context: Context, enabled: Boolean) =
        delegate.setAutoAdhanEnabled(this.context, enabled)

    override fun isAutoAdhanEnabled(context: Context): Boolean =
        delegate.isAutoAdhanEnabled(this.context)

    override fun setLocationMode(context: Context, mode: String) =
        delegate.setLocationMode(this.context, mode)

    override fun getLocationMode(context: Context): String =
        delegate.getLocationMode(this.context)

    override fun setManualCityName(context: Context, city: String?) =
        delegate.setManualCityName(this.context, city)

    override fun getManualCityName(context: Context): String =
        delegate.getManualCityName(this.context)

    override fun setPrayerNotificationsEnabled(context: Context, enabled: Boolean) =
        delegate.setPrayerNotificationsEnabled(this.context, enabled)

    override fun isPrayerNotificationsEnabled(context: Context): Boolean =
        delegate.isPrayerNotificationsEnabled(this.context)

    override fun setCalculationMethod(context: Context, method: String) =
        delegate.setCalculationMethod(this.context, method)

    override fun getCalculationMethod(context: Context): String =
        delegate.getCalculationMethod(this.context)
}