package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.core.model.School
import com.androidhive.myapplication.data.content.Lesson
import com.androidhive.myapplication.data.content.LessonVariant
import org.junit.Assert.*
import org.junit.Test

class LessonVariantTest {
    private val persian = LessonVariant(School.HANAFI, "fa", emptyList())
    private val english = LessonVariant(School.HANAFI, "en", emptyList())
    private fun lesson(variants: List<LessonVariant>) = Lesson("lesson", "عنوان", "Title", "basics", "source", variants)
    @Test fun availableTranslationIsSelectedForTheSameSchool() {
        assertEquals(english, lesson(listOf(persian, english)).variant(School.HANAFI, AppLanguage.ENGLISH))
    }
    @Test fun untranslatedOriginalKeepsItsRealLanguageMetadata() {
        assertEquals("fa", lesson(listOf(persian)).variant(School.HANAFI, AppLanguage.ENGLISH)!!.language)
    }
    @Test fun anotherSchoolIsNeverUsedAsAFallback() {
        assertNull(lesson(listOf(persian, english)).variant(School.SHAFII, AppLanguage.ENGLISH))
    }
}
