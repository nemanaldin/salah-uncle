package com.androidhive.myapplication.duas

import com.androidhive.myapplication.data.duas.*
import com.androidhive.myapplication.domain.duas.*
import org.junit.Assert.*
import org.junit.Test

class PersianDuaPolicyTest {
    private val source = DuaSource("Source", "1", "https://example.org/1", DuaGrade.SAHIH, "collection", true)
    private val content = PersianDuaContent("عربي", listOf("تلفظ"), "معنی", "", listOf(source), emptySet(), reminder = true)
    private fun item(value: PersianDuaContent = content) = DuaItem("dua", "0", "section", mapOf("fa" to "دعا", "en" to "Dua"),
        arabic = "Original", translations = mapOf("en" to "Original meaning"), reviewFields = setOf("source"), persian = value)
    private fun catalog(value: PersianDuaContent = content) = DuaCatalog(
        (0..6).map { DuaCategory("$it", mapOf("en" to "$it")) },
        listOf(DuaSection("section", "0", mapOf("en" to "Section"), listOf("dua"))), listOf(item(value)))

    @Test fun editorialLabelsAreHiddenOnlyForPersian() {
        assertFalse(showEditorialStatus("fa"))
        listOf("en", "tr", "ar", "ur", "bn", "ps", "id", "ms", "fr").forEach { assertTrue(showEditorialStatus(it)) }
    }
    @Test fun pendingOwnerReviewIsRetainedWhenFieldsAreComplete() {
        assertTrue(content.complete)
        assertEquals(PENDING_OWNER_REVIEW, item().forLanguage("fa").persian!!.reviewStatus)
    }
    @Test fun localizationCannotMutateOtherLanguagesOrTheSharedArabic() {
        val original = item()
        listOf("en", "tr", "ar", "ur", "bn", "ps", "id", "ms", "fr").forEach { assertSame(original, original.forLanguage(it)) }
        assertEquals("عربي", original.forLanguage("fa").arabic)
        assertEquals("Original", original.arabic)
        assertEquals("Original meaning", original.forLanguage("en").translation("en"))
    }
    @Test fun completePersianContentCanRemindDespiteInternalOwnerReview() {
        assertEquals(listOf("dua"), catalog().reminderCandidates(setOf("0"), "fa", emptySet()).map { it.id })
        assertTrue(catalog().reminderCandidates(setOf("0"), "en", emptySet()).isEmpty())
    }
    @Test fun missingArabicMeaningPronunciationAndSourcesNeverQualify() {
        listOf(content.copy(arabic = ""), content.copy(meaning = ""), content.copy(pronunciation = emptyList()),
            content.copy(pronunciation = listOf("")), content.copy(sources = emptyList()), content.copy(missingFields = setOf("source")))
            .forEach { assertFalse(it.complete); assertTrue(catalog(it).reminderCandidates(setOf("0"), "fa", emptySet()).isEmpty()) }
    }
    @Test fun missingReferenceUnknownGradeUncheckedSourceAndUnapprovedUrlNeverQualify() {
        listOf(source.copy(reference = ""), source.copy(grade = DuaGrade.UNVERIFIED), source.copy(checked = false),
            source.copy(book = ""), source.copy(url = "http://example.org/1"))
            .forEach { assertFalse(content.copy(sources = listOf(it)).complete) }
    }
    @Test fun fiqhMethodCannotBeDisguisedAsARecitedReminder() {
        val method = content.copy(arabic = "", pronunciation = emptyList(), practice = "روش", kind = "practice")
        assertTrue(method.complete)
        assertTrue(catalog(method).reminderCandidates(setOf("0"), "fa", emptySet()).isEmpty())
    }
    @Test fun incompletePracticeCannotBeMarkedComplete() {
        assertFalse(content.copy(kind = "practice", practice = "").complete)
    }
    @Test fun persianNotificationDoesNotLeakEditorialLabels() {
        assertEquals("عربي\nمعنی", duaNotificationText(item().forLanguage("fa"), "fa", "draft"))
        assertTrue(duaNotificationText(item(), "en", "draft").endsWith("draft"))
    }
    @Test fun emptyPersianResearchEntriesRemainInDataButNotInReadingLists() {
        val original = catalog(content.copy(arabic = "", practice = "", missingFields = setOf("arabic")))
        assertEquals(1, original.items.size)
        assertTrue(original.forLanguage("fa").items.isEmpty())
        assertTrue(original.forLanguage("fa").section("section")!!.duaIds.isEmpty())
        assertSame(original, original.forLanguage("en"))
    }
    @Test fun aSourcedMethodDoesNotNeedAnInventedArabicFormulaToStayVisible() {
        val original = catalog(content.copy(arabic = "", pronunciation = emptyList(), kind = "practice", practice = "روش"))
        assertEquals("روش", original.forLanguage("fa").items.single().note("fa"))
    }
    @Test fun persianImageDescriptionsDoNotExposeEditorialFlagsButKeepInternalFlags() {
        val art = com.androidhive.myapplication.data.content.StageArtCatalog.entries.first { "بازبینی" in it.altFa }
        assertFalse(art.alt(com.androidhive.myapplication.core.model.AppLanguage.PERSIAN).contains("بازبینی"))
        assertTrue(art.alt(com.androidhive.myapplication.core.model.AppLanguage.ENGLISH).contains("review"))
        assertTrue(art.requiresPoseReview)
    }
}
