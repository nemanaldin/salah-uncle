package com.androidhive.myapplication

import com.androidhive.myapplication.data.PrayerTimeRepository
import com.androidhive.myapplication.data.PrayerTimeRepository.NextPrayer
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object PrayerTimesCalculator : PrayerTimeRepository {

    override fun computeNext(lat: Double, lon: Double, calcParams: CalculationParameters): NextPrayer? {
        val coords = Coordinates(lat, lon)
        val params = calcParams

        val now = Calendar.getInstance()

        val today = DateComponents(
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH) + 1,
            now.get(Calendar.DAY_OF_MONTH)
        )

        val todayTimes = PrayerTimes(coords, today, params)
        val nowMillis = System.currentTimeMillis()

        val fajr = todayTimes.fajr.time
        val dhuhr = todayTimes.dhuhr.time
        val asr = todayTimes.asr.time
        val maghrib = todayTimes.maghrib.time
        val isha = todayTimes.isha.time

        if (nowMillis < fajr) return NextPrayer("صبح", fajr)
        if (nowMillis < dhuhr) return NextPrayer("ظهر", dhuhr)
        if (nowMillis < asr) return NextPrayer("عصر", asr)
        if (nowMillis < maghrib) return NextPrayer("مغرب", maghrib)
        if (nowMillis < isha) return NextPrayer("عشاء", isha)

        // بعد از عشاء → فجر فردا
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)

        val tomorrowDate = DateComponents(
            tomorrow.get(Calendar.YEAR),
            tomorrow.get(Calendar.MONTH) + 1,
            tomorrow.get(Calendar.DAY_OF_MONTH)
        )

        val tomorrowTimes = PrayerTimes(coords, tomorrowDate, params)
        return NextPrayer("صبح", tomorrowTimes.fajr.time)
    }

    override suspend fun getPrayerTimes(lat: Double, lon: Double, calcParams: CalculationParameters): PrayerTimes {
        return withContext(Dispatchers.Default) {
            val coords = Coordinates(lat, lon)
            val now = Calendar.getInstance()
            val today = DateComponents(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH)
            )
            PrayerTimes(coords, today, calcParams)
        }
    }
}