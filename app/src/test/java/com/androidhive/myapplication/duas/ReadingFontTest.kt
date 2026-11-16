package com.androidhive.myapplication.duas

import com.androidhive.myapplication.core.model.*
import org.junit.Assert.*
import org.junit.Test

class ReadingFontTest {
    @Test fun defaultAndUnknownStoredFontKeepTheExistingFamily() {
        assertEquals(ReadingFont.VAZIRMATN,AppSettings().readingFont)
        assertEquals(ReadingFont.VAZIRMATN,ReadingFont.fromStored("unknown-font"))
    }
    @Test fun bundledLatinFontNeverLeavesRtlTextWithoutAnArabicFallback() {
        assertEquals(ReadingFont.VAZIRMATN,ReadingFont.NOTO_SANS.resolve(AppLanguage.ARABIC))
        assertEquals(ReadingFont.NOTO_SANS,ReadingFont.NOTO_SANS.resolve(AppLanguage.TURKISH))
    }
    @Test fun unsupportedSahelGlyphSetsUseAnExistingSuitableFamily() {
        assertEquals(ReadingFont.VAZIRMATN,ReadingFont.SAHEL.resolve(AppLanguage.PASHTO))
        assertEquals(ReadingFont.VAZIRMATN,ReadingFont.SAHEL.resolve(AppLanguage.URDU))
        assertEquals(ReadingFont.NOTO_SANS,ReadingFont.SAHEL.resolve(AppLanguage.TURKISH))
    }
    @Test fun arabicOnlyAndBengaliFallbacksAreExplicit() {
        assertEquals(ReadingFont.NOTO_SANS,ReadingFont.NOTO_NASKH.resolve(AppLanguage.ENGLISH))
        ReadingFont.entries.forEach { assertNull(it.resolve(AppLanguage.BENGALI)) }
    }
    @Test fun resetPreservesEveryUnrelatedSettingAndBookmark() {
        val old=AppSettings(readingFont=ReadingFont.AMIRI,textScale=1.4f,arabicTextScale=1.8f,bookmarks=setOf("old"),lastStepId="keep")
        assertEquals(old.copy(readingFont=ReadingFont.VAZIRMATN,textScale=1f,arabicTextScale=1f),old.resetReadingAppearance())
    }
    @Test fun invalidArabicScaleCannotBreakTextMeasurement() {
        assertEquals(1f,readingArabicScale(Float.NaN))
        assertEquals(1f,readingArabicScale(Float.POSITIVE_INFINITY))
        assertEquals(2f,readingArabicScale(3f))
    }
    @Test fun mixedArabicRangesPreserveOriginalTextAndDoNotReplaceOtherScripts() {
        val text="English: ذِكْر বাংলা."
        val ranges=arabicTextRanges(text)
        assertEquals(listOf("ذِكْر"),ranges.map { text.substring(it) })
        assertEquals("English: ذِكْر বাংলা.",text)
    }
}
