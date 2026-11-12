package com.androidhive.myapplication.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val SalahLightScheme = lightColorScheme(
    primary = Color(0xFF16685D), onPrimary = Color.White,
    primaryContainer = Color(0xFFD6ECE4), onPrimaryContainer = Color(0xFF123E36),
    secondary = Color(0xFF876335), secondaryContainer = Color(0xFFF5E8D6), onSecondaryContainer = Color(0xFF503918),
    background = Color(0xFFF5F7F3), onBackground = Color(0xFF20332E),
    surface = Color(0xFFFCFDF9), onSurface = Color(0xFF20332E),
    surfaceVariant = Color(0xFFEAF0E9), onSurfaceVariant = Color(0xFF52645C), outlineVariant = Color(0xFFD8E2D9),
)
val SalahDarkScheme = darkColorScheme(
    primary = Color(0xFF8BD3BF), onPrimary = Color(0xFF07382E),
    primaryContainer = Color(0xFF234F43), onPrimaryContainer = Color(0xFFB9EFDC),
    secondary = Color(0xFFE0BE86), secondaryContainer = Color(0xFF59452B), onSecondaryContainer = Color(0xFFFFE6BD),
    background = Color(0xFF101C18), onBackground = Color(0xFFE1EAE1),
    surface = Color(0xFF15231E), onSurface = Color(0xFFE1EAE1),
    surfaceVariant = Color(0xFF263B30), onSurfaceVariant = Color(0xFFB9CBBE), outlineVariant = Color(0xFF3A5145),
)

/** Existing home-photo palette moved intact out of composables. Both modes use a dark photo overlay. */
object SalahPhotoTokens {
    val support = Color(0xFFCCE9DB)
    val date = Color(0xFFE9D3A7)
    val countdown = Color(0xFFF5E3B8)
    val location = Color(0xFFD2E8DF)
    val onPhoto = Color.White
    val overlayTop = Color(0x660B2725)
    val overlayBottom = Color(0xDD0B2725)
}
