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
        val archived = org.json.JSONObject(context.assets.open("content/hanafi-fa-v1.json").bufferedReader().use { it.readText() }).getJSONArray("lessons")
        val legacyIds = (0 until archived.length()).map { archived.getJSONObject(it).getString("id") }.toSet()
        val legacy = lessons.filter { it.id in legacyIds }
        assertEquals(21, legacy.size)
        assertEquals(73, legacy.sumOf { it.variant(School.HANAFI)!!.steps.size })
        legacy.forEach { lesson ->
            assertNull(lesson.variant(School.SHAFII))
            assertEquals(10, lesson.variants.size)
            val original = requireNotNull(lesson.variant(School.HANAFI, AppLanguage.PERSIAN))
            AppLanguage.entries.forEach { language ->
                val translated = requireNotNull(lesson.variant(School.HANAFI, language))
                assertEquals(language.tag, translated.language)
                assertEquals(original.steps.map { it.id }, translated.steps.map { it.id })
                original.steps.zip(translated.steps).forEach { (source, localized) ->
                    if (language != AppLanguage.PERSIAN) assertNotEquals(lesson.id + ":" + language.tag, source.body, localized.body)
                    assertEquals(source.audio, localized.audio)
                    assertEquals(source.illustrations, localized.illustrations)
                    assertTrue(localized.body.isNotBlank())
                }
            }
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
    @Test fun postWuduRemembranceHasTheSamePrimarySourceForAllFourSchools() = runBlocking {
        val lesson = LessonRepository(context).load().single { it.id == "wudu-remembrance" }
        School.entries.forEach { school -> AppLanguage.entries.forEach { language ->
            val variant = requireNotNull(lesson.variant(school, language))
            assertEquals(language.tag, variant.language)
            assertEquals(listOf("after-wudu", "shared-source"), variant.steps.map { it.id })
            assertEquals("https://sunnah.com/muslim:234b", variant.sources.single().url)
        } }
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
        assertEquals(34006 + 279, cities.size)
        assertEquals(244, cities.map { it.countryCode }.distinct().size)
        assertTrue(cities.any { it.countryCode == "ID" && it.name == "Jakarta" && it.zoneId == "Asia/Jakarta" })
        assertTrue(cities.any { it.countryCode == "US" && it.zoneId == "America/New_York" })
        assertTrue(cities.any { it.id == "ایران/تهران/تهران" })
        cities.forEach { assertNotNull(it.location().zone) }
    }
}
