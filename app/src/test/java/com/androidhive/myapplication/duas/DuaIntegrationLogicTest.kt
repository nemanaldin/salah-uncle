package com.androidhive.myapplication.duas

import com.androidhive.myapplication.data.duas.*
import com.androidhive.myapplication.domain.duas.*
import org.junit.Assert.*
import org.junit.Test

class DuaIntegrationLogicTest {
    private fun item(id: String, section: String = "s", category: String = "c", arabic: String = "نص") =
        DuaItem(id, category, section, mapOf("en" to id), arabic = arabic, translations = mapOf("en" to "Meaning"),
            sources = listOf(DuaSource("Book", "1", "https://example.org/1", DuaGrade.SAHIH, "Author", true)), reminder = true)
    private fun catalog(items: List<DuaItem>, extra: List<DuaSection> = emptyList()) = DuaCatalog(
        (listOf("c") + (1..6).map { "c$it" }).map { DuaCategory(it, mapOf("en" to it)) },
        listOf(DuaSection("s", "c", mapOf("en" to "Section"), items.map { it.id })) + extra, items)
    @Test fun qunutIsNeverAttachedToTheOrdinaryPrayerGuideOrAnotherSchool() {
        listOf("preparation","opening","recitation","bowing","standing","prostrations","second-unit","sitting","ending","daily-units-and-times").forEach {
            assertFalse("witr-qunut" in guideDuaIds("prayer-hanafi", it))
            assertTrue(guideDuaIds("prayer-shafii", it).isEmpty())
        }
    }
    @Test fun sourcedWuduDhikrIsOnlyAttachedAfterWudu() {
        assertEquals(listOf("after-wudu"),guideDuaIds("purification-hanafi","after-wudu"))
        assertFalse("after-wudu" in guideDuaIds("purification-hanafi","head-ears-and-feet"))
        assertFalse("after-wudu" in guideDuaIds("purification-hanafi","intention-and-hands"))
    }
    @Test fun deepLinksCannotEscapeTheRealSubcategory() {
        val data=catalog(listOf(item("a")))
        assertEquals("duas/read/s/a",validatedDuaRoute("duas/read/s/a",data))
        listOf("duas/read/s/unknown","duas/read/other/a","duas/read/s/a?route=home","duas/read/../a","https://example.org").forEach {
            assertNull(validatedDuaRoute(it,data))
        }
    }
    @Test fun unavailableMeaningAndPendingEvidenceAreExcluded() {
        val data=catalog(listOf(item("ready"),item("untranslated").copy(translations=emptyMap()),
            item("pending").copy(reviewFields=setOf("grade")),item("disabled").copy(reminder=false)))
        assertEquals(listOf("ready"),data.reminderCandidates(setOf("c"),"en",emptySet()).map{it.id})
        assertTrue(data.reminderCandidates(setOf("c"),"ur",emptySet()).isEmpty())
    }
    @Test fun aliasBookmarksDoNotMakeTheSameArabicTextRepeat() {
        val data=catalog(listOf(item("a",arabic="سُبْحَانَ اللَّهِ"),item("b",arabic="سبحان الله")))
        val a=data.reminderCandidates(setOf("c"),"en",setOf("a")).single()
        val b=data.reminderCandidates(setOf("c"),"en",setOf("b")).single()
        assertNotEquals(a.id,b.id)
        assertEquals(a.reminderKey,b.reminderKey)
    }
    @Test fun aSharedReferenceIsAvailableFromEachReferencedCategory() {
        val data=catalog(listOf(item("a")), listOf(DuaSection("shared","c1",mapOf("en" to "Shared"),listOf("a"))))
        assertEquals(listOf("a"),data.reminderCandidates(setOf("c1"),"en",emptySet()).map{it.id})
        assertTrue(data.validReaderRoute("a","shared"))
        assertEquals("shared",data.sectionFor("a","shared")!!.id)
    }
}
