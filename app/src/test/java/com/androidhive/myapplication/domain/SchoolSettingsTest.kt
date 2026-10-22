package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.domain.prayer.PrayerEngine
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class SchoolSettingsTest {
    @Test fun switchingSchoolSetsAsrWithoutChangingRegionalMethodOrLearningData() {
        val original = AppSettings(calculationPreset = CalculationPreset.DUBAI,
            alertMode = AlertMode.NOTIFICATION, prayerAlerts = mapOf(PrayerId.FAJR to AlertMode.OFF),
            bookmarks = setOf("wudu"), completedSteps = setOf("wudu:vuzu1"),
            lastLessonId = "wudu", lastStepId = "vuzu1", hiddenNotificationUntil = 123L)
        School.entries.forEach { school ->
            val changed = original.withSchool(school)
            assertEquals(school, changed.school)
            assertEquals(if (school == School.HANAFI) AsrConvention.HANAFI else AsrConvention.STANDARD, changed.asrConvention)
            assertEquals(original.calculationPreset, changed.calculationPreset)
            assertEquals(original.alertMode, changed.alertMode)
            assertEquals(original.prayerAlerts, changed.prayerAlerts)
            assertEquals(original.bookmarks, changed.bookmarks)
            assertEquals(original.completedSteps, changed.completedSteps)
            assertEquals(original.lastLessonId, changed.lastLessonId)
            assertEquals(0L, changed.hiddenNotificationUntil)
        }
    }

    @Test fun fourSchoolSchedulesUseTheirAsrConventionAndShareTheOtherDailyTimes() {
        val location = SavedLocation(24.8607, 67.0011, "Karachi", "Asia/Karachi")
        val engine = PrayerEngine()
        val schedules = School.entries.associateWith { school ->
            val settings = AppSettings(location = location).withSchool(school)
            engine.day(location, settings.calculationPreset, settings.asrConvention, LocalDate.of(2026, 10, 16)).moments
        }
        val standard = schedules.getValue(School.SHAFII)
        assertEquals(standard, schedules.getValue(School.MALIKI))
        assertEquals(standard, schedules.getValue(School.HANBALI))
        val hanafi = schedules.getValue(School.HANAFI)
        assertTrue(hanafi.single { it.id == PrayerId.ASR }.time > standard.single { it.id == PrayerId.ASR }.time)
        assertEquals(standard.filter { it.id != PrayerId.ASR }, hanafi.filter { it.id != PrayerId.ASR })
    }

    @Test fun southeastAsianPresetUsesTwentyAndEighteenDegreeTwilight() {
        val parameters = CalculationPreset.SINGAPORE.parameters(AsrConvention.STANDARD)
        assertEquals(20.0, parameters.fajrAngle, 0.0)
        assertEquals(18.0, parameters.ishaAngle, 0.0)
    }

    @Test fun restoredEnumIdsAndSupportedSchoolScopeAreStable() {
        assertEquals(setOf("HANAFI", "SHAFII", "MALIKI", "HANBALI"), School.entries.map { it.name }.toSet())
        assertEquals(AppLanguage.INDONESIAN, enumOrDefault("INDONESIAN", AppLanguage.PERSIAN))
        assertEquals(School.HANAFI, enumOrDefault("unsupported", School.HANAFI))
        assertEquals(AsrConvention.STANDARD, AppSettings(school = School.MALIKI).asrConvention)
    }
}
