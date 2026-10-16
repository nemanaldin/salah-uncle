package com.androidhive.myapplication

import androidx.test.platform.app.InstrumentationRegistry
import com.androidhive.myapplication.core.model.School
import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.core.localizedContext
import com.androidhive.myapplication.data.content.LessonRepository
import com.androidhive.myapplication.data.content.MediaResources
import com.androidhive.myapplication.data.location.CityRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ContentCatalogTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun everyImportedLessonStepAndMediaReferenceExists() = runBlocking {
        val lessons = LessonRepository(context).load()
        val legacy = lessons.filter { lesson -> lesson.variants.all { it.language == "fa" } }
        assertEquals(21, legacy.size)
        assertEquals(73, legacy.sumOf { it.variant(School.HANAFI)!!.steps.size })
        legacy.forEach { lesson ->
            assertNull(lesson.variant(School.SHAFII))
            lesson.variant(School.HANAFI)!!.steps.forEach { step ->
                assertTrue(step.body.isNotBlank())
                step.audio?.let { name ->
                    context.resources.openRawResourceFd(requireNotNull(MediaResources.audio[name])).close()
                }
                step.illustrations.forEach { name -> assertNotNull(context.getDrawable(requireNotNull(MediaResources.illustrations[name]))) }
            }
        }
    }
    @Test fun everySchoolHasTranslatedPrayerAndWuduWithoutCrossSchoolFallback() = runBlocking {
        val lessons = LessonRepository(context).load()
        School.entries.forEach { school ->
            for (id in listOf(school.guideId, "purification-${school.name.lowercase(java.util.Locale.ROOT)}")) {
                val lesson = lessons.single { it.id == id }
                assertEquals(AppLanguage.entries.size, lesson.variants.size)
                val stepIds = lesson.variant(school, AppLanguage.ENGLISH)!!.steps.map { it.id }
                AppLanguage.entries.forEach { language ->
                    val variant = requireNotNull(lesson.variant(school, language))
                    assertEquals(language.tag, variant.language)
                    assertEquals(stepIds, variant.steps.map { it.id })
                    assertTrue(variant.steps.all { it.body.isNotBlank() })
                    assertTrue(variant.sources.isNotEmpty())
                    assertTrue(lesson.titles.containsKey(language.tag))
                }
                School.entries.filter { it != school }.forEach { assertNull(lesson.variant(it)) }
            }
        }
    }
    @Test fun androidLoadsEachLanguageResourcesAndCorrectLayoutDirection() {
        val labels = mapOf("fa" to "زبان برنامه", "en" to "App language", "ar" to "لغة التطبيق", "tr" to "Uygulama dili",
            "ur" to "ایپ کی زبان", "id" to "Bahasa aplikasi", "bn" to "অ্যাপের ভাষা", "ms" to "Bahasa aplikasi",
            "ps" to "د اپ ژبه", "fr" to "Langue de l’application")
        AppLanguage.entries.forEach { language ->
            val localized = localizedContext(context, language)
            assertEquals(language.tag, labels[language.tag], localized.getString(R.string.ui_choose_language))
            assertEquals(if (language.isRtl) android.view.View.LAYOUT_DIRECTION_RTL else android.view.View.LAYOUT_DIRECTION_LTR,
                localized.resources.configuration.layoutDirection)
        }
    }
    @Test fun everyManualCityHasAnExplicitValidTimezone() = runBlocking {
        val cities = CityRepository(context).load()
        assertEquals(279, cities.size)
        cities.forEach { assertNotNull(it.location().zone) }
    }
}
