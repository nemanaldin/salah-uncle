package com.androidhive.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.intl.LocaleList
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.*

val VazirmatnReadingFamily = FontFamily(Font(R.font.vazirmatn_regular), Font(R.font.vazirmatn_medium, FontWeight.Medium), Font(R.font.vazirmatn_bold, FontWeight.Bold))
private val SahelFamily = FontFamily(Font(R.font.sahel_regular))
private val AmiriFamily = FontFamily(Font(R.font.amiri_regular))
private val NaskhFamily = FontFamily(Font(R.font.noto_naskh_arabic_regular))
private val NotoSansFamily = FontFamily(Font(R.font.noto_sans_regular))
fun bundledFontFamily(font: ReadingFont?) = when (font) {
    ReadingFont.VAZIRMATN -> VazirmatnReadingFamily
    ReadingFont.SAHEL -> SahelFamily
    ReadingFont.AMIRI -> AmiriFamily
    ReadingFont.NOTO_NASKH -> NaskhFamily
    ReadingFont.NOTO_SANS -> NotoSansFamily
    null -> FontFamily.Default
}
@Composable
fun readingTextStyle(settings: AppSettings, language: AppLanguage = settings.language): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(fontFamily = bundledFontFamily(settings.readingFont.resolve(language)), localeList = LocaleList(language.tag))

@Composable
fun ReadingText(body: String, settings: AppSettings, language: AppLanguage, modifier: Modifier = Modifier) {
    val style = readingTextStyle(settings, language)
    val arabicFamily = bundledFontFamily(settings.readingFont.resolve(AppLanguage.ARABIC))
    val text = remember(body, language, arabicFamily) {
        buildAnnotatedString {
            append(body)
            if (!language.isRtl) arabicTextRanges(body).forEach { range ->
                addStyle(SpanStyle(fontFamily = arabicFamily), range.first, range.last + 1)
            }
        }
    }
    Text(text, modifier, style = style)
}
