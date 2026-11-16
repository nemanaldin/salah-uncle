package com.androidhive.myapplication

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.androidhive.myapplication.core.model.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.io.File
import java.time.Instant

/** Runs the real activity, real offline city catalog and real calculation engine; no reference-image times are inserted. */
class ReferenceHomeFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = compose.activity.application as SalahApplication
    private var before: AppSettings? = null
    @Before fun ready() {
        runBlocking {
            before=app.container.settings.snapshot()
            val city=app.container.cities.load().single { it.id=="ایران/گلستان/مراوه\u200cتپه" }
            app.container.settings.update { AppSettings(onboardingComplete=true,language=AppLanguage.ENGLISH,
                theme=ThemeMode.LIGHT,location=city.location()) }
        }
        compose.waitUntil(90_000) { compose.onAllNodesWithTag("nav_home").fetchSemanticsNodes().isNotEmpty() &&
            compose.onAllNodesWithTag("home_ghusl").fetchSemanticsNodes().isNotEmpty() }
    }
    @After fun restore() { before?.let { saved -> runBlocking { app.container.settings.update { saved } } } }
    private fun backHome() {
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("nav_home").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun capture(name: String) {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val folder=File(context.getExternalFilesDir(null),"reference-ui").apply { check(mkdirs() || isDirectory) }
        File(folder,name).outputStream().use { stream ->
            assertTrue(compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG,100,stream))
        }
    }
    @Test fun bottomNavigationMatchesReferenceAndSettingsIsReachableFromHeader() {
        listOf("home","lessons","tools","duas").forEach { compose.onNodeWithTag("nav_$it").assertExists() }
        compose.onNodeWithTag("nav_settings").assertDoesNotExist()
        compose.onNodeWithTag("header_settings").performClick()
        compose.onNodeWithTag("setting_reading_font").performScrollTo().assertExists()
        backHome()
        compose.onNodeWithTag("nav_duas").performClick()
        compose.onNodeWithTag("nav_duas").assertIsSelected()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("dua_category_inside").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("dua_categories").performScrollToNode(hasTestTag("dua_category_general"))
        compose.onNodeWithTag("dua_category_general").assertExists()
        compose.onNodeWithTag("nav_lessons").performClick()
        compose.onNodeWithTag("lesson_search").assertExists()
        compose.onNodeWithTag("nav_tools").performClick()
        compose.onNodeWithTag("tool_training").assertExists()
    }
    @Test fun homeUsesItsRealSavedEnglishCityAndAllFiveCalculatedTimes() {
        compose.waitUntil(60_000) { compose.onAllNodesWithTag("home_location").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("home_location").assertTextEquals("Maraveh Tappeh")
        val saved=runBlocking { app.container.settings.snapshot() }
        val day=app.container.prayerEngine.day(saved.location!!,saved.calculationPreset,saved.asrConvention,
            Instant.now().atZone(saved.location!!.zone).toLocalDate())
        day.moments.filter { it.id.canAlert }.forEach { moment ->
            compose.onNodeWithTag("home_time_${moment.id.name}").assertTextContains(
                com.androidhive.myapplication.ui.home.formatPrayerTime(moment.time,saved),substring=true)
        }
        listOf("prayer","wudu","ghusl","sajdah-sahw","ramadan","qibla").forEach { compose.onNodeWithTag("home_$it").assertExists() }
        compose.onNodeWithTag("home_tayammum").assertDoesNotExist()
        compose.onNodeWithTag("home_hero").performScrollTo();capture("home-en-actual.png")
        compose.onNodeWithTag("home_qibla").performScrollTo();capture("quick-access-en.png")
    }
    @Test fun prayerStillOpensHorizontalSlidesWithoutACompletionGate() {
        compose.onNodeWithTag("home_prayer").performScrollTo().performClick()
        compose.waitUntil(60_000) { compose.onAllNodesWithTag("guide_pager").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("slide_next").performClick()
        compose.onNodeWithTag("slide_previous").assertIsEnabled()
        assertTrue(runBlocking { app.container.settings.snapshot() }.completedSteps.isEmpty())
        compose.onNodeWithTag("reader_bookmark").performClick()
        compose.waitUntil(15_000) { "prayer-hanafi" in runBlocking { app.container.settings.snapshot() }.bookmarks }
        backHome()
        assertTrue("prayer-hanafi" in runBlocking { app.container.settings.snapshot() }.bookmarks)
    }
    @Test fun persianHomeKeepsAllShortcutsAndLocaleDirection() {
        runBlocking { app.container.settings.update { it.copy(language=AppLanguage.PERSIAN,textScale=1.5f) } }
        compose.waitUntil(60_000) { compose.onAllNodesWithText("دسترسی سریع").fetchSemanticsNodes().isNotEmpty() }
        compose.runOnIdle { assertEquals(android.view.View.LAYOUT_DIRECTION_RTL,compose.activity.resources.configuration.layoutDirection) }
        listOf("prayer","wudu","ghusl","sajdah-sahw","ramadan","qibla").forEach {
            compose.onNodeWithTag("home_$it").performScrollTo().assertIsDisplayed().assertHasClickAction()
        }
        compose.onNodeWithTag("home_hero").performScrollTo();capture("home-fa-large-actual.png")
    }
}
