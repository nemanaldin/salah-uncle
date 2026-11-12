package com.androidhive.myapplication.continuation

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import org.junit.Assert.*
import org.junit.Test

class TayammumEntryTest {
    private fun catalog(school: School, language: String = "en") = listOf(Lesson("tayammum","تیمم","Tayammum","purity","",
        listOf(LessonVariant(school,language,listOf(LessonStep("step","Sample",null,emptyList()))))))
    private val english = AppSettings(language=AppLanguage.ENGLISH)
    @Test fun neverUsesAnotherSchoolsTayammumForHanafi() = assertNull(catalog(School.MALIKI).visibleTopic("tayammum",english))
    @Test fun neverFallsBackToAnotherLanguage() = assertNull(catalog(School.HANAFI,"fa").visibleTopic("tayammum",english))
    @Test fun hiddenSchoolCannotExposeAnEntryEvenWhenContentExists() = assertNull(catalog(School.MALIKI).visibleTopic("tayammum",english.copy(school=School.MALIKI)))
    @Test fun oneCentralPolicyEntryCanRestoreOnlyMatchingContent() {
        val policy = ActiveContent.policy.copy(schools=ActiveContent.policy.schools+School.MALIKI)
        assertNotNull(catalog(School.MALIKI).visibleTopic("tayammum",english.copy(school=School.MALIKI),policy))
        assertNull(catalog(School.MALIKI).visibleTopic("tayammum",english,policy))
        assertNotNull(catalog(School.HANAFI).visibleTopic("tayammum",english,policy))
    }
}
