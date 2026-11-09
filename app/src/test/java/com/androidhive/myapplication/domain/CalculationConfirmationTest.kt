package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import org.junit.Assert.*
import org.junit.Test

class CalculationConfirmationTest {
    private val location = SavedLocation(35.0, 51.0, "Tehran", "Asia/Tehran")
    private val settings = AppSettings(location = location)
    @Test fun newLocationNeedsConfirmationAndKeepsKarachiDefault() {
        assertTrue(settings.needsCalculationConfirmation()); assertEquals(CalculationPreset.KARACHI, settings.calculationPreset)
    }
    @Test fun confirmationRetainsConfigurationAndBookmarks() {
        val original = settings.copy(bookmarks = setOf("wudu"))
        val confirmed = original.confirmCalculation()
        assertFalse(confirmed.needsCalculationConfirmation()); assertEquals(original.copy(confirmedCalculation = original.calculationKey()), confirmed)
    }
    @Test fun methodAndAsrChangesNeedConfirmationAgain() {
        val confirmed = settings.confirmCalculation()
        assertTrue(confirmed.copy(calculationPreset = CalculationPreset.UMM_AL_QURA).needsCalculationConfirmation())
        assertTrue(confirmed.copy(asrConvention = AsrConvention.STANDARD).needsCalculationConfirmation())
    }
    @Test fun coordinatesAndTimezoneChangesNeedConfirmationAgain() {
        val confirmed = settings.confirmCalculation()
        assertTrue(confirmed.copy(location = location.copy(latitude = 36.0)).needsCalculationConfirmation())
        assertTrue(confirmed.copy(location = location.copy(zoneId = "UTC")).needsCalculationConfirmation())
    }
    @Test fun translatingLocationOrChangingLanguageDoesNotInvalidateConfirmation() {
        val confirmed = settings.confirmCalculation().copy(location = location.copy(label = "تهران", names = mapOf("en" to "Tehran")), language = AppLanguage.ENGLISH)
        assertFalse(confirmed.needsCalculationConfirmation())
    }
    @Test fun noLocationDoesNotAskForConfirmation() = assertFalse(AppSettings().needsCalculationConfirmation())
}
