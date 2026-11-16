package com.androidhive.myapplication

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.ui.settings.ReadingFontDialog
import com.androidhive.myapplication.ui.theme.SalahTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ReadingFontUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun changingTheRadioChoiceUpdatesPreviewBeforeSaving() {
        var chosen: ReadingFont?=null
        compose.setContent { SalahTheme { ReadingFontDialog(AppSettings(),{chosen=it},{}) } }
        compose.onNodeWithTag("font_AMIRI").performScrollTo().performClick()
        compose.onNodeWithTag("font_live_preview").performScrollTo()
            .assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription,"Amiri"))
        compose.runOnIdle { assertNull(chosen) }
        val save=InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.ui_save)
        compose.onNodeWithText(save).performClick()
        compose.runOnIdle { assertEquals(ReadingFont.AMIRI,chosen) }
    }
    @Test fun fontAndArabicSizePersistWithoutChangingBookmarks() = runBlocking {
        val app=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as SalahApplication
        val repo=app.container.settings;val before=repo.snapshot()
        try {
            repo.update { it.copy(readingFont=ReadingFont.NOTO_NASKH,arabicTextScale=1.7f) }
            val after=repo.snapshot()
            assertEquals(ReadingFont.NOTO_NASKH,after.readingFont);assertEquals(1.7f,after.arabicTextScale)
            assertEquals(before.bookmarks,after.bookmarks);assertEquals(before.sectionBookmarks,after.sectionBookmarks)
        } finally { repo.update { before } }
    }
}
