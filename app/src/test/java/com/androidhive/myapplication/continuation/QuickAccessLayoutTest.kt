package com.androidhive.myapplication.continuation

import com.androidhive.myapplication.ui.home.quickAccessColumns
import org.junit.Assert.assertEquals
import org.junit.Test

class QuickAccessLayoutTest {
    @Test fun normalPhoneContentMatchesTheTwoColumnReference() = assertEquals(2, quickAccessColumns(320f, 1f))
    @Test fun narrowContentUsesOneReadableColumn() = assertEquals(1, quickAccessColumns(280f, 1f))
    @Test fun wideScreensHaveRoomForTwoColumnsAtLargeFont() = assertEquals(2, quickAccessColumns(800f, 2f))
    @Test fun aLargePhoneFontDoesNotTruncateTheInlineTitle() = assertEquals(1, quickAccessColumns(350f, 2f))
    @Test fun twoColumnsNeedRoomForBothPicturesAndTitles() {
        assertEquals(1,quickAccessColumns(307f,1f))
        assertEquals(2,quickAccessColumns(308f,1f))
    }
}
