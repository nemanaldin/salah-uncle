package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.domain.prayer.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class HighLatitudePrayerTest {
    @Test fun polarDayReturnsAvailableResultsInsteadOfThrowing() {
        val location = SavedLocation(69.6492, 18.9553, "Tromso", "Europe/Oslo")
        val day = PrayerEngine().day(location, CalculationPreset.KARACHI, AsrConvention.HANAFI, LocalDate.of(2026, 6, 21))
        assertFalse(day.moments.any { it.id == PrayerId.FAJR })
        assertFalse(day.moments.any { it.id == PrayerId.MAGHRIB })
    }
}

class PartialDaySearchTest {
    private val date = LocalDate.of(2026, 6, 21)
    private val now = date.atStartOfDay(ZoneOffset.UTC).toInstant()
    private val settings = AppSettings(location = SavedLocation(69.6, 18.9, "", "UTC"), alertMode = AlertMode.NOTIFICATION)
    private fun day(value: LocalDate) = DailyPrayers(value, listOf(PrayerMoment(PrayerId.DHUHR, value.atTime(12, 0).toInstant(ZoneOffset.UTC))))
    private fun engine(calculate: (LocalDate) -> DailyPrayers) = PrayerEngine(Clock.fixed(now, ZoneOffset.UTC), PrayerDayCalculator { _, _, _, day -> calculate(day) })
    @Test fun anyFailingDayLeavesOtherTwoDaysSearchable() {
        for (failedOffset in 0L..2L) {
            val engine = engine { if (it == date.plusDays(failedOffset)) error("unavailable") else day(it) }
            val result = engine.upcoming(settings)
            assertEquals((0L..2L).filter { it != failedOffset }.map { day(date.plusDays(it)).moments.single() }, result)
        }
    }
    @Test fun partialDayKeepsAvailablePrayerAndListsMissingTimes() {
        val partial = day(date)
        assertEquals(PrayerId.entries.filter { it != PrayerId.DHUHR }, partial.unavailable)
        assertEquals(partial.moments.single(), engine { day(it) }.next(settings))
    }
    @Test fun entirelyUnavailableSearchReturnsNoInventedNextPrayer() {
        val empty = engine { DailyPrayers(it, emptyList()) }
        assertTrue(empty.upcoming(settings).isEmpty()); assertNull(empty.next(settings)); assertNull(empty.nextAlert(settings))
    }
    @Test fun exceptionsForEveryDayDoNotCrashTheSearch() {
        assertTrue(engine { error("no calculation") }.upcoming(settings).isEmpty())
    }
    @Test fun unavailableResultsAreStillCachedWithoutFillingMissingValues() {
        var calls = 0
        val engine = engine { calls++; DailyPrayers(it, emptyList()) }
        val location = settings.location!!
        repeat(2) { assertEquals(PrayerId.entries, engine.day(location, settings.calculationPreset, settings.asrConvention, date).unavailable) }
        assertEquals(1, calls)
    }
    @Test fun disabledAvailablePrayerDoesNotCreateAnAlertForMissingPrayers() {
        val engine = engine { day(it) }
        assertNull(engine.nextAlert(settings.copy(prayerAlerts = mapOf(PrayerId.DHUHR to AlertMode.OFF))))
    }
    @Test(expected = kotlinx.coroutines.CancellationException::class) fun cancellationIsNotTreatedAsPolarNight() {
        engine { throw kotlinx.coroutines.CancellationException() }.upcoming(settings)
    }
    @Test fun ramadanCalendarKeepsValidDatesWhenOneDateFails() {
        val first = LocalDate.from(java.time.chrono.HijrahDate.of(1447, 9, 1))
        val broken = first.plusDays(1)
        val prayers = engine { value ->
            if (value == broken) error("missing day")
            DailyPrayers(value, listOf(PrayerMoment(PrayerId.FAJR, value.atTime(4, 0).toInstant(ZoneOffset.UTC)),
                PrayerMoment(PrayerId.MAGHRIB, value.atTime(18, 0).toInstant(ZoneOffset.UTC))))
        }
        val calendar = com.androidhive.myapplication.domain.ramadan.RamadanEngine(prayers).calendar(settings, first, 1447)
        assertEquals(setOf(broken), calendar.unavailableDates)
        assertEquals(first, calendar.start)
        assertEquals(first.plusDays(java.time.chrono.HijrahDate.of(1447, 9, 1).lengthOfMonth() - 1L), calendar.end)
        assertFalse(calendar.days.any { it.date == broken }); assertEquals(first, calendar.days.first().date)
    }
    @Test fun nonRamadanRemindersSkipAnUnavailableToday() {
        val prayers = engine { value ->
            if (value == date) error("missing today")
            DailyPrayers(value, listOf(PrayerMoment(PrayerId.FAJR, value.atTime(4, 0).toInstant(ZoneOffset.UTC)),
                PrayerMoment(PrayerId.MAGHRIB, value.atTime(18, 0).toInstant(ZoneOffset.UTC))))
        }
        val enabled = settings.copy(ramadan = RamadanOptions(suhurEnabled = true, onlyRamadan = false))
        assertEquals(date.plusDays(1), com.androidhive.myapplication.domain.ramadan.RamadanEngine(prayers)
            .next(enabled, com.androidhive.myapplication.domain.ramadan.RamadanReminderKind.SUHUR, now)!!.fastingDate)
    }
}
