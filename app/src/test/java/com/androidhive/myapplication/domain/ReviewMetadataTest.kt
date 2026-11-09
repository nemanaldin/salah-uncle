package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.ReviewMetadata
import java.io.File
import java.util.Properties
import org.junit.Assert.*
import org.junit.Test

class ReviewMetadataTest {
    @Test fun exactlyTheThreeRequestedLanguagesHaveStableReadiness() {
        assertEquals(setOf("fa", "tr", "en"), AppLanguage.entries.filter { it.reviewStatus == ReviewStatus.REVIEWED }.map { it.tag }.toSet())
        assertEquals(10, AppLanguage.entries.size)
    }
    @Test fun variantStatusIsReadRatherThanInferredFromLanguageAlone() {
        val metadata = ReviewMetadata.parse("schemaVersion=1\nvariant.wudu.HANAFI.en=experimental\nvariant.wudu.HANAFI.fa=reviewed")
        assertEquals(ReviewStatus.EXPERIMENTAL, metadata.status("wudu", School.HANAFI, "en"))
        assertEquals(ReviewStatus.REVIEWED, metadata.status("wudu", School.HANAFI, "fa"))
    }
    @Test fun missingUnknownAndOtherSchoolStatusesFailClosed() {
        val metadata = ReviewMetadata.parse("schemaVersion=1\nvariant.wudu.HANAFI.en=approved")
        assertEquals(ReviewStatus.EXPERIMENTAL, metadata.status("wudu", School.HANAFI, "en"))
        assertEquals(ReviewStatus.EXPERIMENTAL, metadata.status("wudu", School.MALIKI, "en"))
        assertEquals(ReviewStatus.EXPERIMENTAL, metadata.status("wudu", School.HANAFI, "xx"))
    }
    @Test fun experimentalLanguageCannotBePromotedByVariantMetadata() {
        val metadata = ReviewMetadata.parse("schemaVersion=1\nvariant.wudu.HANAFI.ar=reviewed")
        assertEquals(ReviewStatus.EXPERIMENTAL, metadata.status("wudu", School.HANAFI, "ar"))
    }
    @Test fun actualPackagedMetadataCovers330VariantsWithTheExpectedStatuses() {
        val text = File("src/main/assets/metadata/review-status.properties").readText()
        val entries = Properties().apply { text.reader().use { load(it) } }
        val metadata = ReviewMetadata.parse(text)
        val variants = entries.stringPropertyNames().filter { it.startsWith("variant.") }
        assertEquals(330, variants.size)
        variants.forEach { key ->
            val parts = key.split('.')
            val language = AppLanguage.entries.single { it.tag == parts[3] }
            assertEquals(key, language.reviewStatus, metadata.status(parts[1], School.valueOf(parts[2]), parts[3]))
        }
    }
}
