package com.androidhive.myapplication

import androidx.test.platform.app.InstrumentationRegistry
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ReadingCompositionCatalogTest {
    @Test fun runtimeArrangementUsesExactExistingTextForEveryActiveLanguage() = runBlocking {
        val catalog=LessonRepository(InstrumentationRegistry.getInstrumentation().targetContext).load()
        for (language in ActiveContent.policy.languages) {
            val settings=AppSettings(language=language)
            val composed=readingCatalogue(catalog,settings)
            val guide=composed.single { it.id=="purification-hanafi" }.variant(School.HANAFI,language)!!
            assertEquals("after-wudu",guide.steps.last().id)
            for (step in guide.steps.filter { it.originLessonId!=null }) {
                val source=catalog.single { it.id==step.originLessonId }.variant(School.HANAFI,language)!!.steps.single { it.id==step.id }
                assertEquals(source.body,step.body);assertEquals(source.audio,step.audio);assertEquals(source.illustrations,step.illustrations)
                step.related.forEach { note ->
                    val original=catalog.single { it.id==note.originLessonId }.variant(School.HANAFI,language)!!.steps.single { it.id==note.id }
                    assertEquals(original.body,note.body)
                }
            }
        }
    }
}
