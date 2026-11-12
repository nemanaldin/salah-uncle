package com.androidhive.myapplication.continuation

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import org.junit.Assert.*
import org.junit.Test

class StageArtworkTest {
    private val sample = LessonStep("opening","Sample",null,emptyList())
    @Test fun artNeverCrossesSchools() {
        School.entries.filter { it!=School.HANAFI }.forEach { assertNull(stageArtwork("prayer-hanafi",sample,it)) }
    }
    @Test fun everyGeneratedStageHasSpecificAltTextAndStaysADraft() {
        assertEquals(30, StageArtCatalog.entries.size)
        assertEquals(30, StageArtCatalog.entries.map { it.sourceLessonId to it.stepId }.distinct().size)
        StageArtCatalog.entries.forEach { art ->
            assertTrue(art.alt(AppLanguage.PERSIAN).isNotBlank())
            assertTrue(art.alt(AppLanguage.TURKISH).isNotBlank())
            assertTrue(art.alt(AppLanguage.ENGLISH).isNotBlank())
            assertEquals(IllustrationStatus.DRAFT_PENDING_OWNER_REVIEW,art.status)
        }
    }
    @Test fun otherLanguagesUseEnglishImageDescriptions() {
        StageArtCatalog.entries.forEach { art ->
            AppLanguage.entries.filter { it !in listOf(AppLanguage.PERSIAN,AppLanguage.ENGLISH,AppLanguage.TURKISH) }
                .forEach { assertEquals(art.alt(AppLanguage.ENGLISH),art.alt(it)) }
        }
    }
    @Test fun prostrationUsesASeparateDraftInsteadOfTheRejectedAtlasCell() {
        val art = stageArtwork("prayer-hanafi",sample.copy(id="prostrations"),School.HANAFI)!!
        assertTrue(art.displayable)
        assertEquals(StageArtAsset.PROSTRATION,art.asset)
        assertTrue(art.requiresPoseReview)
    }
    @Test fun arrangedWuduStepKeepsItsOriginalArtIdentity() {
        val art = stageArtwork("purification-hanafi",sample.copy(id="vuzu3",originLessonId="wudu"),School.HANAFI)!!
        assertEquals("wudu",art.sourceLessonId); assertEquals("vuzu3",art.stepId)
        assertNull(stageArtwork("unrelated",sample.copy(id="vuzu3",originLessonId="wudu"),School.HANAFI))
    }
    @Test fun fractionalAtlasCellsCoverTheImageWithoutOverlapOrOverflow() {
        val frames = StageArtCatalog.entries.map { stageFrame(it.cell,1373,1145) }
        frames.forEach { assertTrue(it.width>0 && it.height>0 && it.left>=0 && it.top>=0 && it.left+it.width<=1373 && it.top+it.height<=1145) }
        assertEquals(1373*1145,frames.sumOf { it.width*it.height })
    }
    @Test fun ghuslHasOnlySymbolicArt() {
        val art=stageArtwork("ghusl",sample.copy(id="ghosl"),School.HANAFI)!!
        assertEquals(StageArtKind.SYMBOLIC,art.kind)
        assertTrue(art.displayable)
    }
}
