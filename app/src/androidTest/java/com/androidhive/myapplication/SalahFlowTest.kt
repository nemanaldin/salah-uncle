package com.androidhive.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidhive.myapplication.core.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SalahFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val repository get() = (compose.activity.application as SalahApplication).container.settings

    private fun ready() {
        runBlocking { repository.update { AppSettings(onboardingComplete = true) } }
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("nav_home").fetchSemanticsNodes().isNotEmpty() }
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
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.ui_bookmark_add)).performClick()
        compose.onNode(hasText("وضو") and !hasSetTextAction()).performClick()
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
        compose.onNodeWithTag("city_ایران/تهران/تهران").performScrollTo().performClick()
        compose.waitUntil(15_000) { runBlocking { repository.snapshot() }.location?.zoneId == "Asia/Tehran" }
        assertEquals(AlertMode.OFF, runBlocking { repository.snapshot() }.alertMode)
        compose.onNodeWithText(compose.activity.getString(R.string.ui_fajr)).assertExists()
    }

    @Test fun englishInterfaceUsesLeftToRightLayout() {
        ready()
        runBlocking { repository.update { it.copy(language = AppLanguage.ENGLISH) } }
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Take the first step").fetchSemanticsNodes().isNotEmpty() }
        compose.runOnIdle { assertEquals(android.view.View.LAYOUT_DIRECTION_LTR, compose.activity.resources.configuration.layoutDirection) }
    }
}
