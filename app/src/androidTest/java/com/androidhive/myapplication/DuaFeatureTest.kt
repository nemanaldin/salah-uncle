package com.androidhive.myapplication

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.ui.duas.*
import com.androidhive.myapplication.ui.theme.SalahTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DuaFeatureTest {
    @get:Rule val compose = createComposeRule()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as SalahApplication
    private fun catalog() = runBlocking { app.container.duas.load() }
    @Test fun realAssetParsesAndArabicSearchIgnoresMarks() {
        val catalog=catalog();assertEquals(7,catalog.categories.size)
        compose.setContent { SalahTheme { DuaLibraryScreen(catalog,AppSettings(language=AppLanguage.ENGLISH),{},{_,_->},{},{},{}) } }
        compose.onNodeWithTag("dua_categories").performScrollToNode(hasTestTag("dua_category_general"))
        compose.onNodeWithTag("dua_category_general").assertExists()
        compose.onNodeWithTag("dua_search").performTextInput("اللهم انك عفو")
        compose.onNodeWithTag("dua_title_qadr").assertExists()
    }
    @Test fun nextStaysInSubcategoryAndMissingAudioCreatesNoPlayerButton() {
        val catalog=catalog();var viewed=""
        compose.setContent { SalahTheme { DuaReaderScreen(catalog,"ramadan","iftar",AppSettings(language=AppLanguage.ENGLISH),{viewed=it},{},{},{},{}) } }
        compose.onNodeWithTag("dua_arabic_iftar").assertTextEquals(catalog.item("iftar")!!.arabic)
        compose.onNodeWithTag("dua_audio").assertDoesNotExist()
        compose.onNodeWithTag("dua_next").performClick()
        compose.waitUntil(5000) { viewed=="qadr" }
        compose.onNodeWithTag("dua_next").assertIsNotEnabled()
    }
    @Test fun ownerArabicIsNotNormalizedForDisplay() {
        val catalog=catalog();val owner=catalog.item("owner-assembly")!!
        compose.setContent { SalahTheme { DuaReaderScreen(catalog,"owner",owner.id,AppSettings(),{},{},{},{},{}) } }
        compose.onNodeWithTag("dua_arabic_${owner.id}").assertTextEquals(owner.arabic)
    }
    @Test fun permissionIsNotRequestedUntilExplicitConsent() {
        val catalog=catalog();var updated:AppSettings?=null;var permissionCalls=0
        compose.setContent { SalahTheme { DuaReminderSettings(catalog,AppSettings(),false,
            { transform -> updated=transform(AppSettings()) },{permissionCalls++},{}) } }
        compose.onNodeWithTag("dua_reminders_enabled").assertIsOff().performClick()
        compose.runOnIdle { assertNull(updated);assertEquals(0,permissionCalls) }
        compose.onNodeWithTag("dua_reminder_consent_confirm").performClick()
        compose.runOnIdle { assertTrue(updated!!.duaReminders.enabled);assertTrue(updated!!.duaReminders.consent);assertEquals(1,permissionCalls) }
    }
    @Test fun newPreferencesRoundTripWithoutChangingExistingBookmarksOrAdhan() = runBlocking {
        val repo=app.container.settings;val before=repo.snapshot()
        try {
            repo.update { it.copy(duaBookmarks=setOf("qadr"),duaRecent=listOf("qadr","iftar"),
                duaReminders=DuaReminderOptions(count=8,categoryIds=setOf("occasions"))) }
            val saved=repo.snapshot()
            assertEquals(setOf("qadr"),saved.duaBookmarks);assertEquals(listOf("qadr","iftar"),saved.duaRecent)
            assertEquals(8,saved.duaReminders.count);assertFalse(saved.duaReminders.enabled)
            assertEquals(before.bookmarks,saved.bookmarks);assertEquals(before.sectionBookmarks,saved.sectionBookmarks)
            assertEquals(before.prayerAlerts,saved.prayerAlerts);assertEquals(before.alertMode,saved.alertMode)
            assertEquals(before.selectedVoiceId,saved.selectedVoiceId);assertEquals(before.ramadan,saved.ramadan)
        } finally { repo.update { before } }
    }
}
