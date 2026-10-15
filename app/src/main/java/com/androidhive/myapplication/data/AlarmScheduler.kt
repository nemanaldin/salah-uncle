package com.androidhive.myapplication.data

import android.content.Context
import com.batoulapps.adhan.CalculationParameters

interface AlarmScheduler {
    fun computeAndScheduleNext(context: Context, calcParams: CalculationParameters)
    fun computeAndScheduleNext(context: Context)
    fun cancelAlarm(context: Context)
}