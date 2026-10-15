package com.androidhive.myapplication.data

import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.PrayerTimes

interface PrayerTimeRepository {
    data class NextPrayer(val nameFa: String, val timeMillis: Long)

    fun computeNext(lat: Double, lon: Double, calcParams: CalculationParameters): NextPrayer?
    suspend fun getPrayerTimes(lat: Double, lon: Double, calcParams: CalculationParameters): PrayerTimes
}