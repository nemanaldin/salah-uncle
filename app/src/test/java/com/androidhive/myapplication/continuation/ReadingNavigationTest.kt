package com.androidhive.myapplication.continuation

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import org.junit.Assert.*
import org.junit.Test

class ReadingNavigationTest {
    private fun variant(school: School, lang: String, text: String) = LessonVariant(school, lang, listOf(LessonStep("stable-section", text, null, emptyList())))
    private val lesson = Lesson("article", "عنوان", "Title", "basics", "", listOf(variant(School.HANAFI,"en","needle in an English body"), variant(School.HANAFI,"fa","فقط فارسی"), variant(School.MALIKI,"en","Maliki needle")))
    @Test fun bodySearchFindsTheExactSectionAndExcerpt() {
        val hit = searchLessons(listOf(lesson), AppSettings(language = AppLanguage.ENGLISH), "needle").single()
        assertEquals("stable-section", hit.sectionId); assertTrue(hit.excerpt.contains("needle"))
    }
    @Test fun searchNeverBorrowsAnotherLanguage() = assertTrue(searchLessons(listOf(lesson), AppSettings(), "needle").none())
    @Test fun searchNeverBorrowsAnotherSchool() {
        val onlyOther = lesson.copy(variants = listOf(variant(School.MALIKI,"en","needle")))
        assertTrue(searchLessons(listOf(onlyOther), AppSettings(language = AppLanguage.ENGLISH), "needle").none())
    }
    @Test fun hiddenSelectionHasNoResults() = assertTrue(searchLessons(listOf(lesson), AppSettings(school = School.MALIKI, language = AppLanguage.ENGLISH), "needle").none())
    @Test fun addingSectionBookmarksPreservesEveryLegacyTopicBookmark() {
        val old = AppSettings(bookmarks = setOf("wudu", "article"), completedSteps = setOf("old"), lastStepId = "old-step")
        val value = SectionBookmark("article", "stable-section", School.HANAFI, AppLanguage.PERSIAN, "note")
        val updated = old.withSectionBookmark(value)
        assertEquals(old.copy(sectionBookmarks = listOf(value)), updated)
        assertEquals(old, updated.withoutSectionBookmark(value))
    }
    @Test fun editingOneSectionDoesNotRemoveOtherLanguagesOrSchools() {
        val en = SectionBookmark("a","b",School.HANAFI,AppLanguage.ENGLISH,"one")
        val fa = en.copy(language = AppLanguage.PERSIAN)
        val changed = AppSettings().withSectionBookmark(en).withSectionBookmark(fa).withSectionBookmark(en.copy(note="new"))
        assertEquals(2, changed.sectionBookmarks.size); assertTrue(changed.sectionBookmarks.contains(fa))
    }
    @Test fun noteLimitCountsUnicodeCharactersWithoutBreakingSurrogates() {
        val text = "🙂".repeat(201)
        assertEquals("🙂".repeat(200), boundedNote(text))
    }
}
