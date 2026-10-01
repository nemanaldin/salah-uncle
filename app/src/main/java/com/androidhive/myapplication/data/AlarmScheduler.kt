package com.androidhive.myapplication.data

import android.content.Context

interface AlarmScheduler {
    fun computeAndScheduleNext(context: Context)
    fun cancelAlarm(context: Context)
}