package com.androidhive.myapplication

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class StageArtworkCatalogTest {
    @Test fun everyActualGuideSourceStageHasDraftMetadataAndAtlasBounds() = runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val catalog=LessonRepository(context).load()
        val options=BitmapFactory.Options().apply { inJustDecodeBounds=true;inScaled=false }
        BitmapFactory.decodeResource(context.resources,R.drawable.stage_art_draft,options)
        assertEquals(1373,options.outWidth);assertEquals(1145,options.outHeight)
        for (language in ActiveContent.policy.languages) {
            val lessons=readingCatalogue(catalog,AppSettings(language=language))
            val stages=lessons.filter { isSlideGuide(it.id,School.HANAFI) }
                .flatMap { lesson -> lesson.variant(School.HANAFI,language)!!.steps.map { lesson to it } }
            assertEquals(30,stages.size)
            stages.forEach { (lesson,step) ->
                val art=stageArtwork(lesson.id,step,School.HANAFI)
                assertNotNull("${language.tag}/${lesson.id}/${step.id}",art)
                assertEquals(IllustrationStatus.DRAFT_PENDING_OWNER_REVIEW,art!!.status)
                assertTrue(art.alt(language).isNotBlank())
            }
        }
    }
}
