package com.androidhive.myapplication.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val SalahLightScheme = lightColorScheme(
    primary = Color(0xFF256253), onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E8E1), onPrimaryContainer = Color(0xFF193E34),
    secondary = Color(0xFF876335), secondaryContainer = Color(0xFFF5E8D6), onSecondaryContainer = Color(0xFF503918),
    background = Color(0xFFF3F6F2), onBackground = Color(0xFF1C3B32),
    surface = Color.White, onSurface = Color(0xFF1C3B32),
    surfaceVariant = Color(0xFFF8FAF7), onSurfaceVariant = Color(0xFF486057), outlineVariant = Color(0xFFDFE5DF),
)
val SalahDarkScheme = darkColorScheme(
    primary = Color(0xFF8BD3BF), onPrimary = Color(0xFF07382E),
    primaryContainer = Color(0xFF234F43), onPrimaryContainer = Color(0xFFB9EFDC),
    secondary = Color(0xFFE0BE86), secondaryContainer = Color(0xFF59452B), onSecondaryContainer = Color(0xFFFFE6BD),
    background = Color(0xFF101C18), onBackground = Color(0xFFE1EAE1),
    surface = Color(0xFF15231E), onSurface = Color(0xFFE1EAE1),
    surfaceVariant = Color(0xFF263B30), onSurfaceVariant = Color(0xFFB9CBBE), outlineVariant = Color(0xFF3A5145),
)

/** Reference-image tones. The same dark photo treatment remains legible in both themes. */
object SalahPhotoTokens {
    val support = Color(0xFFE5EAE5)
    val date = Color(0xFFE5EAE5)
    val countdown = Color(0xFFF0DFB0)
    val location = Color(0xFFDEE8E2)
    val onPhoto = Color.White
    val overlayTop = Color(0xD10C2424)
    val overlayBottom = Color(0xE80A251F)
}
