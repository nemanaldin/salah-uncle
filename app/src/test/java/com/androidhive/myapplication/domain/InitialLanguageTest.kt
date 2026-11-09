package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class InitialLanguageTest {
    @Test fun savedEnglishWinsOverSystemArabic() = assertEquals(AppLanguage.ENGLISH, initialLanguage("ENGLISH", listOf("ar")))
    @Test fun savedPersianWinsOverSystemTurkish() = assertEquals(AppLanguage.PERSIAN, initialLanguage("PERSIAN", listOf("tr")))
    @Test fun regionLocaleSeedsNewInstallation() = assertEquals(AppLanguage.TURKISH, initialLanguage(null, listOf("tr-TR")))
    @Test fun firstSupportedLocaleIsUsed() = assertEquals(AppLanguage.URDU, initialLanguage(null, listOf("de-DE", "ur-PK", "en")))
    @Test fun indonesianLegacyLocaleIsRecognized() = assertEquals(AppLanguage.INDONESIAN, initialLanguage(null, listOf("in_ID")))
    @Test fun unsupportedSystemRetainsPersianDefault() = assertEquals(AppLanguage.PERSIAN, initialLanguage(null, listOf("de", "es")))
    @Test fun previouslyInitializedChoiceSurvivesSystemChange() {
        val saved = initialLanguage(null, listOf("en-US")).name
        assertEquals(AppLanguage.ENGLISH, initialLanguage(saved, listOf("ar-SA")))
    }
}
