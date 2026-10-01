package com.androidhive.myapplication.data

import android.content.Context

interface PrayerNotificationManager {
    fun showOrUpdate(context: Context, forceShow: Boolean)
    fun hide(context: Context)
    fun cancel(context: Context)
}