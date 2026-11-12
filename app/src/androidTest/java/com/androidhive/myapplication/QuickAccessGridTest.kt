package com.androidhive.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.ui.home.*
import com.androidhive.myapplication.ui.theme.SalahTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuickAccessGridTest {
    @get:Rule val compose = createComposeRule()
    @Test fun allCardsRemainReachableAndClickableAtLargeScaleInBothDirections() {
        var language by mutableStateOf(AppLanguage.ENGLISH)
        val visited = mutableListOf<String>()
        val labels = listOf("ramadan" to "Ramadan", "prayer" to "نماز", "wudu" to "অজু শিক্ষা",
            "ghusl" to "غسل", "tayammum" to "تیمم", "sajdah-sahw" to "د سهوې سجده",
            "types" to "Prayer types", "wudu-duas" to "وضو کی دعائیں", "qibla" to "Qibla")
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,2f)) {
                SalahTheme(AppSettings(language=language,theme=ThemeMode.DARK)) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        QuickAccessGrid(labels.map { (id,title) -> QuickAccessItem(id,title) { visited += id } }) { _, modifier -> Box(modifier) }
                    }
                }
            }
        }
        for (value in listOf(AppLanguage.ENGLISH,AppLanguage.PASHTO,AppLanguage.BENGALI)) {
            compose.runOnIdle { language = value }; compose.waitForIdle()
            labels.forEach { (id,title) ->
                compose.onNodeWithTag("home_$id").performScrollTo().assertIsDisplayed()
                    .assertHasClickAction().assertContentDescriptionEquals(title).performClick()
            }
        }
        compose.runOnIdle { assertEquals(List(3) { labels.map { it.first } }.flatten(), visited) }
    }
}
