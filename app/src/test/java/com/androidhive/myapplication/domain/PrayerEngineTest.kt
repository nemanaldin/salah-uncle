package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.domain.prayer.PrayerEngine
import com.batoulapps.adhan.Madhab
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.util.TimeZone
import java.time.format.DateTimeFormatter

class PrayerEngineTest {
    private val location = SavedLocation(35.6892, 51.389, "Tehran", "Asia/Tehran")
    private val settings = AppSettings(location = location)

    @Test fun hanafiConventionActuallyChangesAsr() {
        val engine = PrayerEngine()
        val date = LocalDate.of(2026, 10, 16)
        val hanafi = engine.day(location, CalculationPreset.KARACHI, AsrConvention.HANAFI, date)
        val standard = engine.day(location, CalculationPreset.KARACHI, AsrConvention.STANDARD, date)
        assertTrue(hanafi.moments.single { it.id == PrayerId.ASR }.time.isAfter(standard.moments.single { it.id == PrayerId.ASR }.time))
        assertEquals(standard.moments.single { it.id == PrayerId.FAJR }, hanafi.moments.single { it.id == PrayerId.FAJR })
        assertEquals(Madhab.HANAFI, CalculationPreset.KARACHI.parameters(AsrConvention.HANAFI).madhab)
    }

    @Test fun hanafiTimesMatchUpstreamReferenceTimetable() {
        // Adhan 1.2.1 PrayerTimesTest: Raleigh, 2015-07-12, North America + Hanafi.
        // https://github.com/batoulapps/adhan-kotlin/blob/v1.2.1/adhan/src/test/java/com/batoulapps/adhan/PrayerTimesTest.java
        val site = SavedLocation(35.7750, -78.6336, "Raleigh", "America/New_York")
        val actual = PrayerEngine().day(site, CalculationPreset.NORTH_AMERICA, AsrConvention.HANAFI, LocalDate.of(2015, 7, 12))
        val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(site.zone)
        assertEquals(listOf("04:42", "06:08", "13:21", "18:22", "20:32", "21:57"), actual.moments.map { formatter.format(it.time) })
    }

    @Test fun dubaiPresetMatchesDubaiReferenceRatherThanKarachi() {
        // Upstream Shared/Times/Dubai-Gulf.json gives a one-minute tolerance.
        val site = SavedLocation(25.263056, 55.297222, "Dubai", "Asia/Dubai")
        val date = LocalDate.of(2016, 1, 1)
        val actual = PrayerEngine().day(site, CalculationPreset.DUBAI, AsrConvention.STANDARD, date)
        val expected = listOf("05:42", "07:01", "12:25", "15:24", "17:44", "19:03")
        actual.moments.zip(expected).forEach { (moment, text) ->
            val reference = date.atTime(LocalTime.parse(text)).atZone(site.zone).toInstant()
            assertTrue("${moment.id}: ${moment.time} vs $reference", Duration.between(reference, moment.time).abs().seconds <= 60)
        }
    }

    @Test fun afterIshaRecalculatesTomorrowInsteadOfAdding24Hours() {
        val engine = PrayerEngine()
        val date = LocalDate.of(2026, 3, 15)
        val today = engine.day(location, settings.calculationPreset, settings.asrConvention, date)
        val afterIsha = today.moments.single { it.id == PrayerId.ISHA }.time.plusSeconds(60)
        val actual = engine.next(settings, afterIsha)!!
        val tomorrow = engine.day(location, settings.calculationPreset, settings.asrConvention, date.plusDays(1))
            .moments.single { it.id == PrayerId.FAJR }
        assertEquals(tomorrow, actual)
        assertNotEquals(today.moments.single { it.id == PrayerId.FAJR }.time.plusSeconds(86_400), actual.time)
    }

    @Test fun displayKeepsWorkingWhenAllAlertsAreOff() {
        val now = Instant.parse("2026-10-16T08:00:00Z")
        val engine = PrayerEngine(Clock.fixed(now, ZoneOffset.UTC))
        assertNotNull(engine.next(settings.copy(alertMode = AlertMode.OFF)))
        assertNull(engine.nextAlert(settings.copy(alertMode = AlertMode.OFF)))
    }

    @Test fun notificationOnlyModeStillProducesAnAlarm() {
        val engine = PrayerEngine(Clock.fixed(Instant.parse("2026-10-16T08:00:00Z"), ZoneOffset.UTC))
        assertEquals(engine.next(settings), engine.nextAlert(settings.copy(alertMode = AlertMode.NOTIFICATION)))
    }

    @Test fun disabledPrayerIsSkippedByAlarmButStillShownByUi() {
        val engine = PrayerEngine(Clock.fixed(Instant.parse("2026-10-16T08:00:00Z"), ZoneOffset.UTC))
        val first = engine.next(settings)!!
        val changed = settings.copy(alertMode = AlertMode.ADHAN, prayerAlerts = mapOf(first.id to AlertMode.OFF))
        assertEquals(first, engine.next(changed))
        assertTrue(engine.nextAlert(changed)!!.time.isAfter(first.time))
    }

    @Test fun sunriseIsNeverAnAdhanCandidate() {
        val engine = PrayerEngine()
        val day = engine.day(location, settings.calculationPreset, settings.asrConvention, LocalDate.of(2026, 10, 16))
        val now = day.moments.single { it.id == PrayerId.FAJR }.time.plusSeconds(1)
        assertEquals(PrayerId.DHUHR, engine.next(settings, now)!!.id)
    }

    @Test fun cityDateIsIndependentOfDeviceTimezone() {
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"))
            val engine = PrayerEngine(Clock.fixed(Instant.parse("2026-10-16T22:00:00Z"), ZoneOffset.UTC))
            assertEquals(LocalDate.of(2026, 10, 17), engine.day(location, settings.calculationPreset, settings.asrConvention).date)
        } finally { TimeZone.setDefault(previous) }
    }

    @Test fun methodIdsResolveToTheirOwnLibraryMethod() {
        CalculationPreset.entries.forEach { method -> assertEquals(method.name, method.parameters(AsrConvention.HANAFI).method.name) }
    }

    @Test fun recomputationHasNoWallClockMillisecondNoise() {
        val day = PrayerEngine().day(location, CalculationPreset.KARACHI, AsrConvention.HANAFI, LocalDate.of(2026, 10, 16))
        day.moments.forEach { assertEquals(0, it.time.nano) }
        val recomputed = PrayerEngine().day(location, CalculationPreset.KARACHI, AsrConvention.HANAFI, day.date)
        assertEquals(day, recomputed)
    }

    @Test fun equatorAndPrimeMeridianAreValidCoordinates() {
        val engine = PrayerEngine(Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC))
        assertNotNull(engine.next(settings.copy(location = SavedLocation(0.0, 0.0, "", "UTC"))))
    }

    @Test fun allPerPrayerAlertsOffCancelsScheduling() {
        val engine = PrayerEngine(Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC))
        assertNull(engine.nextAlert(settings.copy(alertMode = AlertMode.NOTIFICATION, prayerAlerts = PrayerId.obligatory.associateWith { AlertMode.OFF })))
    }

    @Test(expected = IllegalArgumentException::class) fun invalidCoordinatesAreRejected() { SavedLocation(Double.NaN, 51.0, "", "UTC") }
}
