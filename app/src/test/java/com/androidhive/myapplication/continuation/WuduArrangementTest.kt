package com.androidhive.myapplication.continuation

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import org.junit.Assert.*
import org.junit.Test
class WuduArrangementTest {
    private fun step(id:String,body:String)=LessonStep(id,body,null,emptyList())
    private fun lesson(id:String,school:School,steps:List<LessonStep>)=Lesson(id,id,id,"basics","",listOf(LessonVariant(school,"en",steps)))
    private val settings=AppSettings(language=AppLanguage.ENGLISH)
    private val guide=lesson("purification-hanafi",School.HANAFI,listOf(step("intention-and-hands","guide unchanged")))
    private val legacy=lesson("wudu",School.HANAFI,listOf(step("vuzu1","source unchanged")))
    private val post=lesson("wudu-remembrance",School.HANAFI,listOf(step("after-wudu","existing text"),step("shared-source","existing source review")))
    @Test fun originalTextAndIdsArePreservedAndPostWuduIsLast() {
        val catalog=listOf(guide,legacy,post)
        val composed=readingCatalogue(catalog,settings)
        val steps=composed.single { it.id==guide.id }.variant(School.HANAFI,AppLanguage.ENGLISH)!!.steps
        assertEquals(listOf("intention-and-hands","vuzu1","after-wudu"),steps.map { it.id })
        assertEquals(legacy.variants.single().steps.single().body,steps[1].body)
        assertEquals(post.variants.single().steps[0].body,steps.last().body)
        assertEquals(post.variants.single().steps[1].body,steps.last().related.single().body)
        assertEquals(ArrangementStatus.DRAFT_PENDING_REVIEW,steps.last().arrangement)
        assertEquals(3,catalog.size);assertFalse(composed.any { it.id=="wudu-remembrance" })
    }
    @Test fun anotherSchoolNeverReceivesHanafiLimbPrayers() {
        val other=lesson("purification-maliki",School.MALIKI,listOf(step("intention-and-hands","Maliki")))
        val composed=readingCatalogue(listOf(other,legacy,post),settings.copy(school=School.MALIKI))
        assertEquals(listOf("Maliki"),composed.single { it.id==other.id }.variants.single().steps.map { it.body })
    }
    @Test fun legacyPostWuduBookmarksMoveWithoutLosingIdsNotesOrTopicStars() {
        val old=settings.copy(bookmarks=setOf("wudu-remembrance","ghusl"),lastLessonId="wudu-remembrance")
        val migrated=old.migratePostWuduBookmark()
        assertEquals(old.bookmarks,migrated.bookmarks);assertEquals(old.lastLessonId,migrated.lastLessonId)
        assertEquals("wudu-remembrance",migrated.sectionBookmarks.single().lessonId)
        assertEquals(migrated,migrated.migratePostWuduBookmark())
        val note=migrated.withSectionBookmark(migrated.sectionBookmarks.single().copy(note="keep",offset=10))
        assertEquals(note,note.migratePostWuduBookmark())
    }
    @Test fun bookmarkScopesAreNotCombinedAcrossSchools() {
        val old=settings.copy(school=School.MALIKI,bookmarks=setOf("wudu-remembrance"))
        assertEquals(School.MALIKI,old.migratePostWuduBookmark().sectionBookmarks.single().school)
    }
    @Test fun oldLinksResolveToTheSameSourceSectionInsideTheGuide() {
        assertEquals(ReadingTarget("purification-hanafi","after-wudu"),readingTarget("wudu-remembrance",null,School.HANAFI))
        assertEquals(ReadingTarget("purification-hanafi","vuzu1",15),readingTarget("wudu","vuzu1",School.HANAFI,15))
    }
    @Test fun searchIncludesTheUnchangedReviewNoteInsideTheWuduArticle() {
        val composed=readingCatalogue(listOf(guide,legacy,post),settings)
        val hit=searchLessons(composed,settings,"existing source review").single()
        assertEquals("purification-hanafi",hit.lessonId);assertEquals("shared-source",hit.sectionId)
        assertEquals("after-wudu",readingStep(composed.single().variants.single().steps,hit.sectionId)!!.id)
    }
    @Test fun legacyWuduTopicStarsStillMatchTheCanonicalGuide() {
        val old=setOf("wudu", "ghusl")
        assertTrue(topicBookmarkIds("purification-hanafi",School.HANAFI).any { it in old })
        assertFalse(topicBookmarkIds("purification-maliki",School.MALIKI).any { it in old })
    }
}
