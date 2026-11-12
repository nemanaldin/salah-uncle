package com.androidhive.myapplication.continuation

import com.androidhive.myapplication.ui.home.quickAccessColumns
import org.junit.Assert.assertEquals
import org.junit.Test

class QuickAccessLayoutTest {
    @Test fun normalPhoneContentHasThreeCompactColumns() = assertEquals(3, quickAccessColumns(320f, 1f))
    @Test fun narrowContentUsesTwoColumns() = assertEquals(2, quickAccessColumns(280f, 1f))
    @Test fun accessibilityFontScaleOverridesTabletWidth() = assertEquals(2, quickAccessColumns(800f, 2f))
}
