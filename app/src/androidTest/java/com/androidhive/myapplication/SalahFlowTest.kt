package com.androidhive.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidhive.myapplication.core.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SalahFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val repository get() = (compose.activity.application as SalahApplication).container.settings

    private fun ready() {
        runBlocking { repository.update { AppSettings(onboardingComplete = true) } }
        compose.waitUntil(60_000) { compose.onAllNodesWithTag("nav_home").fetchSemanticsNodes().isNotEmpty() && compose.onAllNodesWithTag("home_ghusl").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun homeOpensTeachingDirectlyWithoutACompletedCourse() {
        ready()
        compose.onNodeWithTag("home_prayer").performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("lesson_reader").fetchSemanticsNodes().isNotEmpty() }
        assertTrue(runBlocking { repository.snapshot() }.completedSteps.isEmpty())
        compose.waitUntil(10_000) { runBlocking { repository.snapshot() }.lastLessonId == "prayer-hanafi" }
    }

    @Test fun untranslatedLessonRequiresExplicitChoiceBeforeShowingPersian() {
        ready()
        runBlocking { repository.update { it.copy(language = AppLanguage.ENGLISH) } }
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Learn and explore").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("home_ghusl").performScrollTo().performClick()
        compose.onNodeWithTag("translation_unavailable").assertExists()
        compose.onNodeWithTag("lesson_reader").assertDoesNotExist()
        compose.onNodeWithTag("read_original").performClick()
        compose.onNodeWithTag("lesson_reader").assertExists()
        assertEquals(AppLanguage.ENGLISH, runBlocking { repository.snapshot() }.language)
    }

    @Test fun countryThenCitySelectsTheActualIndonesianTimezone() {
        ready()
        runBlocking { repository.update { it.copy(language = AppLanguage.ENGLISH) } }
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Learn and explore").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("home_set_location").performScrollTo().performClick()
        compose.onNodeWithTag("choose_city").performScrollTo().performClick()
        compose.onNodeWithTag("choose_country").performClick()
        compose.onNodeWithTag("country_search").performTextInput("Indonesia")
        compose.onNodeWithTag("country_search").performImeAction()
        compose.onNodeWithTag("country_ID").performClick()
        compose.onNodeWithTag("city_search").performTextInput("Jakarta")
        compose.onNodeWithTag("city_search").performImeAction()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("city_geonames:1642911").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("city_geonames:1642911").performClick()
        compose.waitUntil(15_000) { runBlocking { repository.snapshot() }.location?.zoneId == "Asia/Jakarta" }
        assertEquals(AlertMode.OFF, runBlocking { repository.snapshot() }.alertMode)
    }

    @Test fun importedVoicesCanBeSelectedPreviewedAndRemovedWithoutChangingAlarms() {
        ready()
        val container = (compose.activity.application as SalahApplication).container
        val first = runBlocking { container.recordings.import(android.net.Uri.parse("content://com.androidhive.myapplication.test.audio/first")) }
        val second = runBlocking { container.recordings.import(android.net.Uri.parse("content://com.androidhive.myapplication.test.audio/second")) }
        runBlocking { repository.update { it.copy(adhanVoices = listOf(first, second), selectedVoiceId = second.id) } }
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("setting_adhan_voice").performScrollTo().performClick()
        compose.onNodeWithTag("voice_" + first.id).performScrollTo().performClick()
        compose.waitUntil(10_000) { runBlocking { repository.snapshot() }.selectedVoiceId == first.id }
        compose.onNodeWithTag("preview_" + first.id).performScrollTo().performClick()
        compose.waitUntil(15_000) { compose.onAllNodesWithText(compose.activity.getString(R.string.ui_stop_audio)).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("remove_" + first.id).performScrollTo().performClick()
        compose.waitUntil(10_000) { runBlocking { repository.snapshot() }.adhanVoices.size == 1 }
        assertNull(runBlocking { repository.snapshot() }.selectedVoiceId)
        assertEquals(AlertMode.OFF, runBlocking { repository.snapshot() }.alertMode)
        compose.waitUntil(10_000) { container.recordings.file(first.fileName) == null }
        assertFalse(container.recordings.file(first.fileName)?.exists() == true)
        container.recordings.delete(second)
    }

    @Test fun onboardingDoesNotRequireLocationPermission() {
        runBlocking { repository.update { AppSettings() } }
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("onboarding_start").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("onboarding_start").performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("nav_home").fetchSemanticsNodes().isNotEmpty() }
        assertTrue(runBlocking { repository.snapshot() }.onboardingComplete)
        assertNull(runBlocking { repository.snapshot() }.location)
        assertEquals(AlertMode.OFF, runBlocking { repository.snapshot() }.alertMode)
    }

    @Test fun onboardingOffersExactlyTheFourRequestedSchoolsAndSetsAsr() {
        runBlocking { repository.update { AppSettings() } }
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("onboarding_start").fetchSemanticsNodes().isNotEmpty() }
        School.entries.forEach { school ->
            compose.onNodeWithTag("school_${school.name}").performScrollTo().performClick()
            compose.waitUntil(10_000) { runBlocking { repository.snapshot() }.school == school }
            assertEquals(school.defaultAsr, runBlocking { repository.snapshot() }.asrConvention)
        }
        compose.onNodeWithTag("school_JAFARI").assertDoesNotExist()
        compose.onNodeWithTag("onboarding_start").performScrollTo().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("nav_home").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(School.HANBALI, runBlocking { repository.snapshot() }.school)
    }

    @Test fun choosingMalikiOpensMalikiGuideAndPreservesHanafiProgress() {
        ready()
        runBlocking { repository.update { it.copy(completedSteps = setOf("wudu:vuzu1"), bookmarks = setOf("wudu")) } }
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("setting_school").performScrollTo().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.ui_school_maliki)).performClick()
        compose.waitUntil(10_000) { runBlocking { repository.snapshot() }.school == School.MALIKI }
        compose.onNodeWithTag("nav_home").performClick()
        compose.onNodeWithTag("home_prayer").performScrollTo().performClick()
        compose.onNodeWithTag("lesson_reader").assertExists()
        compose.waitUntil(10_000) { runBlocking { repository.snapshot() }.lastLessonId == "prayer-maliki" }
        val settings = runBlocking { repository.snapshot() }
        assertEquals(AsrConvention.STANDARD, settings.asrConvention)
        assertTrue("wudu:vuzu1" in settings.completedSteps)
        assertTrue("wudu" in settings.bookmarks)
    }

    @SdkSuppress(minSdkVersion = 26)
    @Test fun allTenLanguagesRenderHomeAndPreserveTheirLayoutDirection() {
        ready()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // AGP copies this directory before uninstalling the app after the test run.
        val outputDirectory = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
            ?.let(::File) ?: (context.getExternalFilesDir(null) ?: context.cacheDir)
        val screenshots = File(outputDirectory, "qa-screenshots").apply { check(mkdirs() || isDirectory) }
        AppLanguage.entries.forEach { language ->
            runBlocking { repository.update { it.copy(language = language) } }
            val localized = com.androidhive.myapplication.core.localizedContext(context, language)
            val label = localized.getString(R.string.ui_quick_access)
            compose.waitUntil(30_000) { compose.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty() }
            compose.runOnIdle {
                assertEquals(if (language.isRtl) android.view.View.LAYOUT_DIRECTION_RTL else android.view.View.LAYOUT_DIRECTION_LTR,
                    compose.activity.resources.configuration.layoutDirection)
            }
            File(screenshots, "home-${language.tag}.png").outputStream().use { stream ->
                assertTrue(compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream))
            }
        }
    }

    @Test fun dubaiSelectionPersistsDubaiRatherThanTheSpinnerPosition() {
        ready()
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("setting_calculation").performScrollTo().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.ui_calc_dubai)).performClick()
        compose.waitUntil(10_000) { runBlocking { repository.snapshot() }.calculationPreset == CalculationPreset.DUBAI }
        assertEquals(CalculationPreset.DUBAI, runBlocking { repository.snapshot() }.calculationPreset)
    }

    @Test fun lessonSearchCompletionAndBookmarkSurviveNavigation() {
        ready()
        compose.onNodeWithTag("nav_lessons").performClick()
        compose.onNodeWithTag("lesson_search").performTextInput("وضو")
        compose.runOnIdle {
            val window = compose.activity.window
            androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                .hide(androidx.core.view.WindowInsetsCompat.Type.ime())
        }
        compose.waitUntil(5_000) {
            androidx.core.view.ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                ?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) == false
        }
        compose.onNodeWithTag("lesson_list").performScrollToNode(hasTestTag("lesson_wudu"))
        compose.onNodeWithTag("bookmark_wudu").performClick()
        compose.waitUntil(10_000) { "wudu" in runBlocking { repository.snapshot() }.bookmarks }
        compose.onNodeWithTag("lesson_list").assertExists().performScrollToNode(hasTestTag("lesson_wudu"))
        compose.onNodeWithTag("lesson_wudu").performClick()
        compose.onNodeWithTag("lesson_next").performScrollTo().performClick()
        compose.waitUntil(10_000) {
            val s = runBlocking { repository.snapshot() }
            "wudu:vuzu_text1" in s.completedSteps && s.lastStepId == "vuzu1"
        }
        val settings = runBlocking { repository.snapshot() }
        assertTrue("wudu" in settings.bookmarks)
        assertEquals("wudu", settings.lastLessonId)
        compose.onNodeWithTag("lesson_audio").performScrollTo().assertIsDisplayed()
    }

    @Test fun trainingPageClearlyShowsThatVideosAreNotYetPresent() {
        ready()
        compose.onNodeWithTag("nav_tools").performClick()
        compose.onNodeWithTag("tool_training").performClick()
        compose.onNodeWithTag("training_screen").assertExists()
        compose.onNodeWithText(compose.activity.getString(R.string.ui_training_placeholder)).assertExists()
        compose.onNodeWithText(compose.activity.getString(R.string.ui_open_prayer_guide)).performScrollTo().performClick()
        compose.onNodeWithTag("lesson_reader").assertExists()
    }

    @Test fun selectingACityShowsTimesEvenWithAlertsOff() {
        ready()
        compose.onNodeWithTag("home_set_location").performScrollTo().performClick()
        compose.onNodeWithTag("choose_city").performScrollTo().performClick()
        compose.onNodeWithTag("city_search").performTextInput("تهران")
        compose.onNodeWithTag("city_search").performImeAction()
        compose.waitUntil(15_000) { compose.onAllNodesWithTag("city_ایران/تهران/تهران").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("city_ایران/تهران/تهران").performScrollTo().performClick()
        compose.waitUntil(15_000) { runBlocking { repository.snapshot() }.location?.zoneId == "Asia/Tehran" }
        assertEquals(AlertMode.OFF, runBlocking { repository.snapshot() }.alertMode)
        compose.onNodeWithText(compose.activity.getString(R.string.ui_fajr)).assertExists()
    }

    @Test fun englishInterfaceUsesLeftToRightLayout() {
        ready()
        runBlocking { repository.update { it.copy(language = AppLanguage.ENGLISH) } }
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Learn and explore").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Learn and explore").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(AppLanguage.ENGLISH, runBlocking { repository.snapshot() }.language)
        compose.runOnIdle { assertEquals(android.view.View.LAYOUT_DIRECTION_LTR, compose.activity.resources.configuration.layoutDirection) }
    }
}
