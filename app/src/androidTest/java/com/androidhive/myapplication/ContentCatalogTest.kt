package com.androidhive.myapplication

import androidx.test.platform.app.InstrumentationRegistry
import com.androidhive.myapplication.core.model.School
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
        assertEquals(21, lessons.size)
        assertEquals(73, lessons.sumOf { it.variant(School.HANAFI)!!.steps.size })
        lessons.forEach { lesson ->
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
    @Test fun everyManualCityHasAnExplicitValidTimezone() = runBlocking {
        val cities = CityRepository(context).load()
        assertEquals(279, cities.size)
        cities.forEach { assertNotNull(it.location().zone) }
    }
}
