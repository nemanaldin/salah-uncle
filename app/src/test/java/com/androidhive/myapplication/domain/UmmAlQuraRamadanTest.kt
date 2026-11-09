package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.domain.prayer.PrayerEngine
import com.batoulapps.adhan.CalculationMethod
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import java.time.chrono.HijrahDate

class UmmAlQuraRamadanTest {
    private val site = SavedLocation(21.4225, 39.8262, "Makkah", "Asia/Riyadh")
    private val start = LocalDate.from(HijrahDate.of(1447, 9, 1))
    private val end = LocalDate.from(HijrahDate.of(1447, 10, 1))
    private fun interval(date: LocalDate): Long {
        val moments = PrayerEngine().day(site, CalculationPreset.UMM_AL_QURA, AsrConvention.STANDARD, date).moments
        return Duration.between(moments.single { it.id == PrayerId.MAGHRIB }.time, moments.single { it.id == PrayerId.ISHA }.time).toMinutes()
    }
    @Test fun pinnedLibraryDefaultRequiresAnExplicitRamadanAdjustment() {
        // Adhan 1.2.1 CalculationMethod.java documents the caller's +30 minute duty.
        assertEquals(90, CalculationMethod.UMM_AL_QURA.parameters.ishaInterval)
    }
    @Test fun firstRamadanDayUses120Minutes() = assertEquals(120L, interval(start))
    @Test fun lastRamadanDayUses120Minutes() = assertEquals(120L, interval(end.minusDays(1)))
    @Test fun dayBeforeRamadanUses90Minutes() = assertEquals(90L, interval(start.minusDays(1)))
    @Test fun eidUses90Minutes() = assertEquals(90L, interval(end))
    @Test fun ramadanDoesNotChangeFajrOrOtherMethods() {
        val engine = PrayerEngine()
        val normal = CalculationPreset.UMM_AL_QURA.parameters(AsrConvention.STANDARD)
        val fajr = com.batoulapps.adhan.PrayerTimes(com.batoulapps.adhan.Coordinates(site.latitude, site.longitude),
            com.batoulapps.adhan.data.DateComponents(start.year, start.monthValue, start.dayOfMonth), normal).fajr.toInstant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
        assertEquals(fajr, engine.day(site, CalculationPreset.UMM_AL_QURA, AsrConvention.STANDARD, start).moments.single { it.id == PrayerId.FAJR }.time)
        assertEquals(0, CalculationPreset.KARACHI.parameters(AsrConvention.HANAFI).ishaInterval)
    }
    @Test fun cachingDoesNotLeakRamadanAdjustmentIntoOtherDates() {
        val engine = PrayerEngine()
        val ramadan = engine.day(site, CalculationPreset.UMM_AL_QURA, AsrConvention.STANDARD, start)
        val outside = engine.day(site, CalculationPreset.UMM_AL_QURA, AsrConvention.STANDARD, end)
        assertEquals(ramadan, engine.day(site, CalculationPreset.UMM_AL_QURA, AsrConvention.STANDARD, start))
        assertEquals(90L, Duration.between(outside.moments.single { it.id == PrayerId.MAGHRIB }.time, outside.moments.single { it.id == PrayerId.ISHA }.time).toMinutes())
    }
}
