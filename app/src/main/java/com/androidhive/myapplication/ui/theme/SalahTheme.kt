package com.androidhive.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.core.model.ThemeMode

private val Light = lightColorScheme(
    primary = Color(0xFF16685D), onPrimary = Color.White,
    primaryContainer = Color(0xFFD6ECE4), onPrimaryContainer = Color(0xFF123E36),
    secondary = Color(0xFF876335), secondaryContainer = Color(0xFFF5E8D6), onSecondaryContainer = Color(0xFF503918),
    background = Color(0xFFF5F7F3), onBackground = Color(0xFF20332E),
    surface = Color(0xFFFCFDF9), onSurface = Color(0xFF20332E),
    surfaceVariant = Color(0xFFEAF0E9), onSurfaceVariant = Color(0xFF52645C), outlineVariant = Color(0xFFD8E2D9),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF8BD3BF), onPrimary = Color(0xFF07382E),
    primaryContainer = Color(0xFF234F43), onPrimaryContainer = Color(0xFFB9EFDC),
    secondary = Color(0xFFE0BE86), secondaryContainer = Color(0xFF59452B), onSecondaryContainer = Color(0xFFFFE6BD),
    background = Color(0xFF101C18), onBackground = Color(0xFFE1EAE1),
    surface = Color(0xFF15231E), onSurface = Color(0xFFE1EAE1),
    surfaceVariant = Color(0xFF263B30), onSurfaceVariant = Color(0xFFB9CBBE), outlineVariant = Color(0xFF3A5145),
)
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

@Composable
fun SalahTheme(settings: AppSettings = AppSettings(), content: @Composable () -> Unit) {
    val dark = when (settings.theme) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true }
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * settings.textScale),
        LocalLayoutDirection provides if (settings.language == AppLanguage.PERSIAN) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) { MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Typography, content = content) }
}
