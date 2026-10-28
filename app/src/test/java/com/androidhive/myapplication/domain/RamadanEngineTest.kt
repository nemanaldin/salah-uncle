package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.domain.prayer.PrayerEngine
import com.androidhive.myapplication.domain.ramadan.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class RamadanEngineTest {
    private val prayers = PrayerEngine()
    private val engine = RamadanEngine(prayers)
    private val location = SavedLocation(35.6892, 51.389, "Tehran", "Asia/Tehran")
    private val settings = AppSettings(location = location, ramadan = RamadanOptions(suhurEnabled = true, iftarEnabled = true))
    private val date = LocalDate.of(2026, 10, 28)

    @Test fun newInstallLeavesRemindersOffAndUsesNinetyMinutes() {
        val value = AppSettings().ramadan
        assertFalse(value.suhurEnabled); assertFalse(value.iftarEnabled)
        assertTrue(value.onlyRamadan); assertEquals(90, value.minutesBeforeFajr)
    }
    @Test fun entireMonthUsesTheExistingPrayerCalculation() {
        val month = engine.calendar(settings, date)
        assertTrue(month.days.size in 29..30)
        assertTrue(engine.isRamadan(month.start, 0)); assertTrue(engine.isRamadan(month.end, 0))
        assertFalse(engine.isRamadan(month.end.plusDays(1), 0))
        month.days.forEachIndexed { index, day ->
            assertEquals(index + 1, day.dayOfRamadan)
            assertEquals(month.start.plusDays(index.toLong()), day.date)
            val original = prayers.day(location, settings.calculationPreset, settings.asrConvention, day.date)
            assertEquals(original.moments.single { it.id == PrayerId.FAJR }.time, day.fajr)
            assertEquals(original.moments.single { it.id == PrayerId.MAGHRIB }.time, day.iftar)
        }
    }
    @Test fun suhurIsIndependentOfDisabledAdhanAndPrayerOverrides() {
        val value = settings.copy(alertMode = AlertMode.OFF, prayerAlerts = PrayerId.obligatory.associateWith { AlertMode.OFF }, ramadan = settings.ramadan.copy(onlyRamadan = false))
        val before = date.atStartOfDay(location.zone).toInstant().minusSeconds(1)
        val alarm = requireNotNull(engine.next(value, RamadanReminderKind.SUHUR, before))
        assertEquals(engine.fastingDay(value, date).fajr.minusSeconds(90 * 60L), alarm.time)
        assertEquals(date, alarm.fastingDate)
        assertNull(prayers.nextAlert(value, before))
    }
    @Test fun aWakeUpBeforeMidnightKeepsTheFollowingFastingDate() {
        val place = SavedLocation(41.0082, 28.9784, "Istanbul", "Europe/Istanbul")
        val value = settings.copy(location = place, ramadan = RamadanOptions(true, 240, onlyRamadan = false))
        val fasting = LocalDate.of(2026, 6, 15)
        val before = fasting.minusDays(1).atTime(18, 0).atZone(place.zone).toInstant()
        val reminder = requireNotNull(engine.next(value, RamadanReminderKind.SUHUR, before))
        assertEquals(fasting, reminder.fastingDate)
        assertEquals(fasting.minusDays(1), reminder.time.atZone(place.zone).toLocalDate())
        assertEquals(engine.fastingDay(value, fasting).fajr.minusSeconds(240 * 60L), reminder.time)
    }
    @Test fun nextDayAndDstRecalculateInsteadOfAddingTwentyFourHours() {
        val place = SavedLocation(51.5074, -.1278, "London", "Europe/London")
        val value = settings.copy(location = place, ramadan = settings.ramadan.copy(onlyRamadan = false))
        val firstDay = LocalDate.of(2026, 3, 28)
        val first = engine.fastingDay(value, firstDay).fajr.minusSeconds(5400)
        val next = requireNotNull(engine.next(value, RamadanReminderKind.SUHUR, first.plusSeconds(1)))
        assertEquals(firstDay.plusDays(1), next.fastingDate)
        assertEquals(engine.fastingDay(value, next.fastingDate).fajr.minusSeconds(5400), next.time)
        assertNotEquals(first.plusSeconds(86_400), next.time)
        assertEquals(ZoneOffset.ofHours(1), next.fajr.atZone(place.zone).offset)
    }
    @Test fun afterFinalIftarBothReminderChainsMoveToNextRamadan() {
        val month = engine.calendar(settings, date)
        val after = month.days.last().iftar.plusSeconds(1)
        for (kind in RamadanReminderKind.entries) {
            val next = requireNotNull(engine.next(settings, kind, after))
            assertEquals(engine.calendar(settings, month.end, month.hijriYear + 1).start, next.fastingDate)
            assertTrue(next.time.isAfter(after)); assertTrue(engine.validates(settings, next))
        }
    }
    @Test fun localMoonAdjustmentDoesNotShiftPrayerClockTimes() {
        val original = engine.calendar(settings, date)
        val shifted = settings.copy(ramadan = settings.ramadan.copy(calendarAdjustment = 1))
        val month = engine.calendar(shifted, date)
        assertEquals(original.start.minusDays(1), month.start)
        assertEquals(engine.fastingDay(settings, original.start).fajr, engine.fastingDay(shifted, original.start).fajr)
        assertEquals(engine.fastingDay(settings, original.start).iftar, engine.fastingDay(shifted, original.start).iftar)
    }
    @Test fun staleLocationDelayAndDisabledRemindersAreRejected() {
        val original = requireNotNull(engine.next(settings, RamadanReminderKind.SUHUR, date.atStartOfDay(location.zone).toInstant()))
        assertTrue(engine.validates(settings, original))
        assertFalse(engine.validates(settings.copy(ramadan = settings.ramadan.copy(minutesBeforeFajr = 120)), original))
        assertFalse(engine.validates(settings.copy(ramadan = settings.ramadan.copy(suhurEnabled = false)), original))
        assertFalse(engine.validates(settings.copy(location = SavedLocation(-6.2, 106.8, "Jakarta", "Asia/Jakarta")), original))
    }
    @Test fun noLocationOrDisabledSettingsNeverScheduleAnything() {
        for (kind in RamadanReminderKind.entries) {
            assertNull(engine.next(AppSettings(), kind, Instant.now()))
            assertNull(engine.next(settings.copy(location = null), kind, Instant.now()))
        }
    }
}
