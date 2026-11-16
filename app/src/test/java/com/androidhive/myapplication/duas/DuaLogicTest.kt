package com.androidhive.myapplication.duas

import com.androidhive.myapplication.data.duas.*
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek

class DuaLogicTest {
    @Test fun searchIgnoresArabicMarksAndPersianLetterVariants() {
        assertEquals(normalizeDuaSearch("اللَّهُمَّ إِنِّي"), normalizeDuaSearch("اللهم اني"))
        assertEquals(normalizeDuaSearch("يَكْفِي"), normalizeDuaSearch("یکفی"))
        assertEquals(normalizeDuaSearch("سبـحان"), normalizeDuaSearch("سبحان"))
    }
    @Test fun searchDoesNotDeleteBengaliVowels() {
        assertNotEquals(normalizeDuaSearch("দোয়া"), normalizeDuaSearch("দয়া"))
    }
    @Test fun unknownOrWeakSourcesCannotEnterReminderPool() {
        val item = DuaItem("a", "c", "s", mapOf("en" to "Dua"), arabic = "نص",
            sources = listOf(DuaSource("Book", "1", "https://example.org/1", DuaGrade.SAHIH, "Author", true)))
        assertTrue(item.hasVerifiedText)
        assertFalse(item.copy(sources = emptyList()).hasVerifiedText)
        assertFalse(item.copy(reviewFields = setOf("grade")).hasVerifiedText)
        assertFalse(item.copy(sources = item.sources.map { it.copy(checked = false) }).hasVerifiedText)
        assertFalse(item.copy(sources = item.sources.map { it.copy(grade = DuaGrade.UNVERIFIED) }).hasVerifiedText)
    }
    @Test fun translationsNeverFallBackSilentlyToPersian() {
        val item = DuaItem("a", "c", "s", mapOf("fa" to "دعا", "en" to "Dua"), translations = mapOf("fa" to "متن"))
        assertEquals("", item.translation("tr"))
        assertEquals("Dua", item.title("tr"))
        assertTrue(item.matches("متن")) // Search includes every actual translation.
    }
    @Test fun todayHasNoInventedWeekdaySpecificDhikr() {
        assertEquals("friday", todayDuaSection(DayOfWeek.FRIDAY))
        assertEquals("fasting", todayDuaSection(DayOfWeek.MONDAY))
        assertEquals("fasting", todayDuaSection(DayOfWeek.THURSDAY))
        listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY).forEach {
            assertEquals("weekday-common", todayDuaSection(it))
        }
    }
    @Test fun counterCannotOverflowOrExceedTransmittedTarget() {
        assertEquals(33, advanceDuaCounter(33, 33))
        assertEquals(1, advanceDuaCounter(-7, 33))
        assertEquals(0, advanceDuaCounter(4, null))
        assertEquals(34, advanceDuaCounter(33, 34))
    }
    @Test fun recentAndBookmarksRemainSeparateFromLessonBookmarks() {
        assertEquals(listOf("b", "a"), rememberDua(listOf("a", "b"), "b"))
        assertEquals(30, rememberDua((1..35).map { "$it" }, "new").size)
    }
}
