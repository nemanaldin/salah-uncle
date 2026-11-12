package com.androidhive.myapplication

import androidx.test.platform.app.InstrumentationRegistry
import com.androidhive.myapplication.core.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SectionBookmarkStorageTest {
    @Test fun newBookmarkFieldsRoundTripWithoutReplacingExistingTopicStars() = runBlocking {
        val application=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as SalahApplication
        val repository=application.container.settings
        val before=repository.snapshot()
        val sample=SectionBookmark("test-source","test-section",School.HANAFI,AppLanguage.ENGLISH,"test note",42)
        try {
            repository.update { it.withSectionBookmark(sample) }
            val saved=repository.snapshot()
            assertEquals(before.bookmarks,saved.bookmarks)
            assertEquals(before.completedSteps,saved.completedSteps)
            assertEquals(before.lastLessonId,saved.lastLessonId)
            assertEquals(sample,saved.sectionBookmarks.single { it.sameSection(sample) })
        } finally { repository.update { before } }
    }
}
