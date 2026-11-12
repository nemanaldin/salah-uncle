package com.androidhive.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.intl.LocaleList
import com.androidhive.myapplication.core.model.AppLanguage
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.core.model.ThemeMode

private val SalahShapes = Shapes(medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(23.dp), extraLarge = RoundedCornerShape(26.dp))

private val Vazir = FontFamily(Font(R.font.vazirmatn_regular), Font(R.font.vazirmatn_medium, FontWeight.Medium), Font(R.font.vazirmatn_bold, FontWeight.Bold))
private val Typography = Typography(
    headlineLarge = TextStyle(fontFamily = Vazir, fontSize = 31.sp, lineHeight = 45.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = Vazir, fontSize = 26.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontFamily = Vazir, fontSize = 22.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = Vazir, fontSize = 20.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontFamily = Vazir, fontSize = 16.sp, lineHeight = 27.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontFamily = Vazir, fontSize = 16.sp, lineHeight = 29.sp),
    bodyMedium = TextStyle(fontFamily = Vazir, fontSize = 14.sp, lineHeight = 25.sp),
    bodySmall = TextStyle(fontFamily = Vazir, fontSize = 12.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontFamily = Vazir, fontSize = 14.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontFamily = Vazir, fontSize = 12.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = Vazir, fontSize = 11.sp, lineHeight = 19.sp),
)

private fun TextStyle.forLanguage(language: AppLanguage) = copy(
    fontFamily = if (language == AppLanguage.BENGALI) FontFamily.Default else fontFamily,
    localeList = LocaleList(language.tag),
    platformStyle = PlatformTextStyle(includeFontPadding = true),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

@Composable
fun SalahTheme(settings: AppSettings = AppSettings(), content: @Composable () -> Unit) {
    val dark = when (settings.theme) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true }
    val density = LocalDensity.current
    val typography = Typography.copy(
        displayLarge = Typography.displayLarge.forLanguage(settings.language),
        displayMedium = Typography.displayMedium.forLanguage(settings.language),
        displaySmall = Typography.displaySmall.forLanguage(settings.language),
        headlineLarge = Typography.headlineLarge.forLanguage(settings.language),
        headlineMedium = Typography.headlineMedium.forLanguage(settings.language),
        headlineSmall = Typography.headlineSmall.forLanguage(settings.language),
        titleLarge = Typography.titleLarge.forLanguage(settings.language),
        titleMedium = Typography.titleMedium.forLanguage(settings.language),
        titleSmall = Typography.titleSmall.forLanguage(settings.language),
        bodyLarge = Typography.bodyLarge.forLanguage(settings.language),
        bodyMedium = Typography.bodyMedium.forLanguage(settings.language),
        bodySmall = Typography.bodySmall.forLanguage(settings.language),
        labelLarge = Typography.labelLarge.forLanguage(settings.language),
        labelMedium = Typography.labelMedium.forLanguage(settings.language),
        labelSmall = Typography.labelSmall.forLanguage(settings.language),
    )
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * settings.textScale),
        LocalLayoutDirection provides if (settings.language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) { com.androidhive.myapplication.ui.components.PrayerArtworkProvider {
        MaterialTheme(colorScheme = if (dark) SalahDarkScheme else SalahLightScheme, typography = typography, shapes = SalahShapes, content = content)
    } }
}
