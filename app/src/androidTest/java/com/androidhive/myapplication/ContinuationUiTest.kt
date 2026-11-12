package com.androidhive.myapplication

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import com.androidhive.myapplication.ui.lessons.LessonScreen
import com.androidhive.myapplication.ui.onboarding.OnboardingScreen
import com.androidhive.myapplication.ui.theme.SalahTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ContinuationUiTest {
    @get:Rule val compose = createComposeRule()
    private val lesson = Lesson("prayer-hanafi","نمونه","Sample","basics","https://example.org", AppLanguage.entries.map { language ->
        LessonVariant(School.HANAFI,language.tag,(0..2).map { LessonStep("sample-$it","Sample text $it.",null,emptyList()) })
    })
    @Test fun rtlAndLtrSwipesUseTheStandardPagerDirection() {
        var language by mutableStateOf(AppLanguage.PERSIAN)
        compose.setContent { val settings=AppSettings(language=language); SalahTheme(settings) {
            LessonScreen(lesson,settings,{_,_->},{},{},initialSection="sample-0")
        } }
        for (value in ActiveContent.policy.languages) {
            compose.runOnIdle { language=value }; compose.waitForIdle()
            compose.onNodeWithTag("guide_pager").performTouchInput { if(value.isRtl) swipeRight() else swipeLeft() }
            compose.waitForIdle()
            val range=compose.onNodeWithTag("guide_pager").fetchSemanticsNode().config[SemanticsProperties.HorizontalScrollAxisRange]
            assertTrue(value.tag,range.value()>.5f)
        }
    }
    @Test fun activePickerHidesOtherSchoolsAndLanguages() {
        compose.setContent { SalahTheme { OnboardingScreen(AppSettings(),{},{},{}) } }
        compose.onNodeWithTag("language_fr").assertDoesNotExist()
        compose.onNodeWithTag("language_id").assertDoesNotExist()
        compose.onNodeWithTag("language_ms").assertDoesNotExist()
        compose.onNodeWithTag("school_HANAFI").assertDoesNotExist()
        compose.onNodeWithTag("school_MALIKI").assertDoesNotExist()
        ActiveContent.policy.languages.forEach { compose.onNodeWithTag("language_${it.tag}").performScrollTo().assertExists() }
    }
    @Test fun largeFontAndDarkModeKeepSlideControlsReachableInEveryActiveScript() {
        var language by mutableStateOf(AppLanguage.PERSIAN)
        compose.setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,2f)) {
                val settings=AppSettings(language=language,theme=ThemeMode.DARK)
                SalahTheme(settings) { LessonScreen(lesson,settings,{_,_->},{},{},initialSection="sample-0") }
            }
        }
        for (value in ActiveContent.policy.languages) {
            compose.runOnIdle { language=value }; compose.waitForIdle()
            compose.onNodeWithTag("reader_mode").assertIsDisplayed().assertHasClickAction()
            compose.onNodeWithTag("slide_next").assertIsDisplayed().assertHasClickAction()
            compose.onNodeWithTag("slide_position").assertIsDisplayed()
        }
    }
}
