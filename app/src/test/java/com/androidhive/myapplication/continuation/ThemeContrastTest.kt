package com.androidhive.myapplication.continuation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.androidhive.myapplication.ui.theme.SalahLightScheme
import com.androidhive.myapplication.ui.theme.SalahDarkScheme
import org.junit.Assert.assertTrue
import org.junit.Test
class ThemeContrastTest {
    private fun ratio(a:Color,b:Color):Float = (maxOf(a.luminance(),b.luminance())+.05f)/(minOf(a.luminance(),b.luminance())+.05f)
    @Test fun bodyAndSecondaryTextMeetNormalTextContrastInBothThemes() {
        for (scheme in listOf(SalahLightScheme,SalahDarkScheme)) {
            assertTrue(ratio(scheme.onSurface,scheme.surface)>=4.5f)
            assertTrue(ratio(scheme.onSurfaceVariant,scheme.surface)>=4.5f)
        }
    }
    @Test fun primaryAndStatusTextRemainReadableInBothThemes() {
        for (scheme in listOf(SalahLightScheme,SalahDarkScheme)) {
            assertTrue(ratio(scheme.primary,scheme.background)>=4.5f)
            assertTrue(ratio(scheme.onSecondaryContainer,scheme.secondaryContainer)>=4.5f)
            assertTrue(ratio(scheme.onPrimaryContainer,scheme.primaryContainer)>=4.5f)
        }
    }
}
