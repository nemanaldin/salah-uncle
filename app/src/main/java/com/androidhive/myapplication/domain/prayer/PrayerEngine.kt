package com.androidhive.myapplication.domain.prayer

import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.core.model.AsrConvention
import com.androidhive.myapplication.core.model.CalculationPreset
import com.androidhive.myapplication.core.model.PrayerId
import com.androidhive.myapplication.core.model.SavedLocation
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PrayerMoment(val id: PrayerId, val time: Instant)
data class DailyPrayers(val date: LocalDate, val moments: List<PrayerMoment>)

/** Android-free calculation shared by the visible UI, notifications and alarm scheduling. */
class PrayerEngine(private val clock: Clock = Clock.systemUTC()) {
    private data class Key(
        val latitude: Double, val longitude: Double, val date: LocalDate,
        val preset: CalculationPreset, val asr: AsrConvention,
    )
    private val cache = object : LinkedHashMap<Key, DailyPrayers>(32, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, DailyPrayers>?) = size > 32
    }

    fun day(
        location: SavedLocation,
        preset: CalculationPreset,
        asr: AsrConvention,
        date: LocalDate = clock.instant().atZone(location.zone).toLocalDate(),
    ): DailyPrayers {
        val key = Key(location.latitude, location.longitude, date, preset, asr)
        synchronized(cache) { cache[key]?.let { return it } }
        val times = PrayerTimes(
            Coordinates(location.latitude, location.longitude),
            DateComponents(date.year, date.monthValue, date.dayOfMonth),
            preset.parameters(asr),
        )
        val moments = listOf(
            PrayerId.FAJR to times.fajr, PrayerId.SUNRISE to times.sunrise,
            PrayerId.DHUHR to times.dhuhr, PrayerId.ASR to times.asr,
            PrayerId.MAGHRIB to times.maghrib, PrayerId.ISHA to times.isha,
        // Adhan 1.x leaves Calendar milliseconds from the time of calculation in its Date values.
        // Strip that noise so recomputation after process death can validate the same alarm.
        ).mapNotNull { (id, dateTime) -> dateTime?.let { PrayerMoment(id, it.toInstant().truncatedTo(ChronoUnit.SECONDS)) } }
        check(moments.count { it.id.canAlert } == 5) { "Prayer times unavailable for this date and location" }
        return DailyPrayers(date, moments).also { synchronized(cache) { cache[key] = it } }
    }

    fun upcoming(settings: AppSettings, after: Instant = clock.instant()): List<PrayerMoment> {
        val location = settings.location ?: return emptyList()
        val date = after.atZone(location.zone).toLocalDate()
        return (0L..2L).flatMap { offset ->
            day(location, settings.calculationPreset, settings.asrConvention, date.plusDays(offset)).moments
        }.filter { it.id.canAlert && it.time.isAfter(after) }.sortedBy { it.time }
    }

    fun next(settings: AppSettings, after: Instant = clock.instant()): PrayerMoment? = upcoming(settings, after).firstOrNull()

    fun nextAlert(settings: AppSettings, after: Instant = clock.instant()): PrayerMoment? =
        upcoming(settings, after).firstOrNull { settings.alertFor(it.id) != com.androidhive.myapplication.core.model.AlertMode.OFF }
}
