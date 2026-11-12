package com.androidhive.myapplication.continuation

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import org.junit.Assert.*
import org.junit.Test

class VisibilityPolicyTest {
    @Test fun pickerPolicyHasExactlyTheRequestedOrder() {
        assertEquals(listOf("fa", "en", "tr", "ur", "bn", "ps", "ar"), ActiveContent.policy.languages.map { it.tag })
        assertEquals(listOf(School.HANAFI), ActiveContent.policy.schools)
    }
    @Test fun hiddenSettingsRecoverWithoutChangingAnyOtherData() {
        val old = AppSettings(school = School.MALIKI, language = AppLanguage.FRENCH, bookmarks = setOf("wudu", "prayer-maliki"),
            lastLessonId = "prayer-maliki", completedSteps = setOf("saved"), textScale = 1.3f)
        assertEquals(old.copy(school = School.HANAFI, language = AppLanguage.TURKISH), old.recoverVisibility(listOf("fr-FR", "tr-TR")))
    }
    @Test fun hiddenSystemLanguageFallsBackToEnglish() = assertEquals(AppLanguage.ENGLISH,
        AppSettings(language = AppLanguage.INDONESIAN).recoverVisibility(listOf("id-ID")).language)
    @Test fun savedActiveChoiceStillWins() = assertEquals(AppLanguage.PERSIAN,
        AppSettings(language = AppLanguage.PERSIAN).recoverVisibility(listOf("en")).language)
    @Test fun addingOneEntryReenablesItsContentWithoutChangingTheCatalog() {
        val variant = LessonVariant(School.MALIKI, "en", listOf(LessonStep("x", "original", null, emptyList())))
        val lesson = Lesson("test", "test", "test", "basics", "", listOf(variant))
        val settings = AppSettings(school = School.MALIKI, language = AppLanguage.ENGLISH)
        assertFalse(lesson.visibleFor(settings))
        assertTrue(lesson.visibleFor(settings, ActiveContent.policy.copy(schools = ActiveContent.policy.schools + School.MALIKI)))
    }
    @Test fun hiddenLanguageNeverAppearsInPresentation() {
        assertFalse(ActiveContent.policy.allows(AppSettings(language = AppLanguage.FRENCH)))
        assertEquals(10, AppLanguage.entries.size); assertEquals(4, School.entries.size)
    }
}
