package com.androidhive.myapplication.continuation

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import org.junit.Assert.*
import org.junit.Test
class ReadingSlidesTest {
    @Test fun sentenceSplittingRetainsEveryCharacter() {
        for (text in listOf("First sentence.  Next one?\nLast!", "نماز.  متن بعدی؟\nپایان۔", "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ", "বাংলা বাক্য। আরও পাঠ্য")) {
            val slices=sentenceSlices(text,12)
            assertEquals(text,slices.joinToString(""){it.text})
            slices.forEach { assertEquals(it.text,text.substring(it.offset,it.offset+it.text.length)) }
        }
    }
    @Test fun aLongSentenceIsNeverRewrittenOrCutMidSentence() {
        val text="unchanged ".repeat(100)
        assertEquals(listOf(TextSlice(0,text)),sentenceSlices(text,30))
    }
    @Test fun targetUsesTheSearchOffsetWithinTheOriginalSection() {
        val steps=listOf(LessonStep("one","First. Second sentence. Third sentence.",null,emptyList()))
        val slides=sentenceSlices(steps[0].body,10).map { ReadingSlide(steps[0],it.offset,it.text) }
        val third=slides.indexOfFirst { it.text.startsWith("Third") }
        assertTrue(third>0);assertEquals(third,targetSlide(slides,"one",steps[0].body.indexOf("Third")))
    }
    @Test fun unknownTargetFallsBackToTheFirstExistingPage() = assertEquals(0,targetSlide(readingSlides(listOf(LessonStep("a","body",null,emptyList()))),"missing"))
    @Test fun unrelatedArticlesDoNotBecomeCourses() {
        assertFalse(isSlideGuide("tayammum",School.HANAFI));assertFalse(isSlideGuide("wudu",School.HANAFI))
        assertTrue(isSlideGuide("ghusl",School.HANAFI));assertTrue(isSlideGuide("prayer-hanafi",School.HANAFI));assertTrue(isSlideGuide("purification-hanafi",School.HANAFI))
    }
    @Test fun bookmarkOffsetAndNoteArePreservedWithoutChangingSourceIds() {
        val value=SectionBookmark("ghusl","ghosl",School.HANAFI,AppLanguage.ENGLISH,"note",400)
        assertEquals(value,AppSettings().withSectionBookmark(value).sectionBookmarks.single())
    }
}
