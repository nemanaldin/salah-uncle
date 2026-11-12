package com.androidhive.myapplication.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.ui.components.*
import com.androidhive.myapplication.ui.theme.SalahTheme

@Composable
private fun QuickAccessPreview(language: AppLanguage, dark: Boolean = false) {
    SalahTheme(AppSettings(language = language, theme = if (dark) ThemeMode.DARK else ThemeMode.LIGHT)) {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(16.dp)) {
            val items = listOf("ramadan" to R.string.ui_ramadan, "prayer" to R.string.ui_teach_prayer,
                "wudu" to R.string.ui_teach_wudu, "ghusl" to R.string.ui_teach_ghusl,
                "tayammum" to R.string.ui_teach_tayammum, "sajdah-sahw" to R.string.ui_teach_sujud,
                "types" to R.string.ui_prayer_types, "wudu-duas" to R.string.ui_wudu_duas, "qibla" to R.string.ui_qibla)
                .map { (id, title) -> QuickAccessItem(id, stringResource(title)) {} }
            QuickAccessGrid(items) { id, modifier ->
                val art = when (id) {
                    "prayer", "types" -> R.drawable.heritage_prayer
                    "wudu-duas" -> R.drawable.heritage_dua
                    "qibla" -> R.drawable.heritage_qibla
                    else -> lessonArtwork(id)
                }
                if (id == "ramadan") PrayerArtwork(PrayerSymbol.TARAWIH, modifier)
                else Image(painterResource(art), null, modifier)
            }
        }
    }
}
@Preview(name = "Persian light", locale = "fa", widthDp = 360, heightDp = 640, showBackground = true)
@Composable private fun PersianQuickAccess() = QuickAccessPreview(AppLanguage.PERSIAN)
@Preview(name = "English dark", locale = "en", widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable private fun EnglishDarkQuickAccess() = QuickAccessPreview(AppLanguage.ENGLISH, true)
@Preview(name = "Urdu narrow", locale = "ur", widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable private fun UrduNarrowQuickAccess() = QuickAccessPreview(AppLanguage.URDU)
@Preview(name = "Bengali 200 percent", locale = "bn", widthDp = 360, heightDp = 800, fontScale = 2f)
@Composable private fun BengaliLargeQuickAccess() = QuickAccessPreview(AppLanguage.BENGALI)
@Preview(name = "Pashto dark 150 percent", locale = "ps", widthDp = 360, heightDp = 800, fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable private fun PashtoLargeQuickAccess() = QuickAccessPreview(AppLanguage.PASHTO, true)
