package com.androidhive.myapplication.continuation

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.TrainingVideo
import org.junit.Assert.*
import org.junit.Test
class VideoScopeTest {
    private val video = TrainingVideo("test", "عنوان", "English", "training/test.mp4", School.HANAFI,
        setOf(AppLanguage.ENGLISH, AppLanguage.TURKISH), ReviewStatus.REVIEWED, mapOf("en" to "English", "tr" to "Türkçe"), "https://example.org/source", "1")
    @Test fun hanafiVideoIsNeverShownForAnotherSchool() {
        School.entries.filter { it != School.HANAFI }.forEach { assertFalse(video.matches(it, AppLanguage.ENGLISH)) }
    }
    @Test fun languageAvailabilityIsExplicit() { assertTrue(video.matches(School.HANAFI,AppLanguage.ENGLISH)); assertFalse(video.matches(School.HANAFI,AppLanguage.PERSIAN)) }
    @Test fun localizedTitleFallsBackToEnglish() { assertEquals("Türkçe",video.title(AppLanguage.TURKISH)); assertEquals("English",video.title(AppLanguage.ARABIC)) }
    @Test fun legacyUnscopedVideoIsNotAssumedToBeHanafi() { assertFalse(TrainingVideo("old","fa","en","training/old.mp4").visibleFor(AppSettings(language=AppLanguage.ENGLISH))) }
    @Test fun experimentalLanguageCannotBeMarkedReviewed() { assertEquals(ReviewStatus.EXPERIMENTAL,video.status(AppLanguage.ARABIC)) }
    @Test fun hiddenSchoolNeedsAnExplicitPolicyEntry() {
        val other=video.copy(school=School.MALIKI)
        val settings=AppSettings(school=School.MALIKI,language=AppLanguage.ENGLISH)
        assertFalse(other.visibleFor(settings))
        assertTrue(other.visibleFor(settings,ActiveContent.policy.copy(schools=ActiveContent.policy.schools+School.MALIKI)))
    }
    @Test fun blankLocalizedTitlesFallBackToAnAvailableEnglishTitle() {
        assertEquals("English", TrainingVideo("id", "فارسی", "Legacy", "training/file.mp4", titles = mapOf("en" to "English", "ar" to " ")).title(AppLanguage.ARABIC))
        assertEquals("Legacy", TrainingVideo("id", "فارسی", "Legacy", "training/file.mp4", titles = mapOf("en" to "")).title(AppLanguage.ENGLISH))
    }
}
