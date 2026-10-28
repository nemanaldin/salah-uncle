package com.androidhive.myapplication.domain.ramadan

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.domain.prayer.PrayerEngine
import java.time.*
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

data class FastingDay(val date: LocalDate, val dayOfRamadan: Int, val fajr: Instant, val iftar: Instant)
data class RamadanCalendar(val hijriYear: Int, val start: LocalDate, val end: LocalDate, val days: List<FastingDay>)
enum class RamadanReminderKind { SUHUR, IFTAR }
data class RamadanReminder(val kind: RamadanReminderKind, val time: Instant, val fastingDate: LocalDate, val fajr: Instant)

/** Uses the existing prayer engine. The Hijri date is an adjustable Umm al-Qura
 * calendar estimate; neither the offset nor the month changes astronomical times. */
class RamadanEngine(private val prayers: PrayerEngine) {
    private data class Key(val location: SavedLocation, val preset: CalculationPreset, val asr: AsrConvention, val year: Int, val adjustment: Int)
    private val cache = object : LinkedHashMap<Key, RamadanCalendar>(4, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, RamadanCalendar>?) = size > 4
    }
    fun periodYear(date: LocalDate, adjustment: Int): Int {
        val hijri = HijrahDate.from(date.plusDays(adjustment.toLong()))
        val year = hijri.get(ChronoField.YEAR)
        return if (hijri.get(ChronoField.MONTH_OF_YEAR) > 9) year + 1 else year
    }
    fun isRamadan(date: LocalDate, adjustment: Int): Boolean =
        HijrahDate.from(date.plusDays(adjustment.toLong())).get(ChronoField.MONTH_OF_YEAR) == 9

    fun fastingDay(settings: AppSettings, date: LocalDate, number: Int = 0): FastingDay {
        val location = requireNotNull(settings.location)
        val times = prayers.day(location, settings.calculationPreset, settings.asrConvention, date).moments
        return FastingDay(date, number, times.single { it.id == PrayerId.FAJR }.time, times.single { it.id == PrayerId.MAGHRIB }.time)
    }
    fun calendar(settings: AppSettings, date: LocalDate, year: Int = periodYear(date, settings.ramadan.calendarAdjustment)): RamadanCalendar {
        val location = requireNotNull(settings.location)
        val adjustment = settings.ramadan.calendarAdjustment
        val key = Key(location, settings.calculationPreset, settings.asrConvention, year, adjustment)
        synchronized(cache) { cache[key]?.let { return it } }
        val first = HijrahDate.of(year, 9, 1)
        val start = LocalDate.from(first).minusDays(adjustment.toLong())
        val days = (0 until first.lengthOfMonth()).map { fastingDay(settings, start.plusDays(it.toLong()), it + 1) }
        return RamadanCalendar(year, start, days.last().date, days).also { synchronized(cache) { cache[key] = it } }
    }
    fun next(settings: AppSettings, kind: RamadanReminderKind, after: Instant): RamadanReminder? {
        if (kind == RamadanReminderKind.SUHUR && !settings.ramadan.suhurEnabled || kind == RamadanReminderKind.IFTAR && !settings.ramadan.iftarEnabled) return null
        val location = settings.location ?: return null
        val date = after.atZone(location.zone).toLocalDate()
        fun candidate(day: FastingDay): RamadanReminder {
            val time = if (kind == RamadanReminderKind.SUHUR) day.fajr.minusSeconds(settings.ramadan.minutesBeforeFajr * 60L) else day.iftar
            return RamadanReminder(kind, time, day.date, day.fajr)
        }
        if (!settings.ramadan.onlyRamadan) return (0L..2L).asSequence()
            .map { candidate(fastingDay(settings, date.plusDays(it))) }.firstOrNull { it.time.isAfter(after) }
        val year = periodYear(date, settings.ramadan.calendarAdjustment)
        // The next suhur may already belong to the next Ramadan on its last day.
        for (value in year..year + 1) calendar(settings, date, value).days.asSequence()
            .filter { !it.date.isBefore(date) }.map(::candidate).firstOrNull { it.time.isAfter(after) }?.let { return it }
        return null
    }
    fun validates(settings: AppSettings, reminder: RamadanReminder): Boolean {
        if (settings.location == null) return false
        val enabled = if (reminder.kind == RamadanReminderKind.SUHUR) settings.ramadan.suhurEnabled else settings.ramadan.iftarEnabled
        if (!enabled || settings.ramadan.onlyRamadan && !isRamadan(reminder.fastingDate, settings.ramadan.calendarAdjustment)) return false
        val day = fastingDay(settings, reminder.fastingDate)
        val expected = if (reminder.kind == RamadanReminderKind.SUHUR) day.fajr.minusSeconds(settings.ramadan.minutesBeforeFajr * 60L) else day.iftar
        return expected == reminder.time && day.fajr == reminder.fajr
    }
}
