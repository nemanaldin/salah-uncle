package com.androidhive.myapplication.domain

import com.androidhive.myapplication.domain.qibla.QiblaMath
import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaMathTest {
    @Test fun bearingIsRetainedInTheFinalNeedleAngle() {
        assertEquals(125f, QiblaMath.relativeBearing(215f, 90f), .001f)
        assertEquals(0f, QiblaMath.relativeBearing(90f, 90f), .001f)
    }
    @Test fun wrapAroundUsesTheShortRotation() {
        assertEquals(2f, QiblaMath.shortestDelta(359f, 1f), .001f)
        assertEquals(-2f, QiblaMath.shortestDelta(1f, 359f), .001f)
        assertEquals(30f, QiblaMath.relativeBearing(20f, 350f), .001f)
    }
    @Test fun londonBearingMatchesKnownGreatCircleDirection() { assertEquals(119f, QiblaMath.bearing(51.5074, -.1278), .5f) }
    @Test fun newYorkBearingMatchesKnownGreatCircleDirection() { assertEquals(58.5f, QiblaMath.bearing(40.7128, -74.006), .5f) }
    @Test fun negativeHeadingIsNormalized() { assertEquals(359f, QiblaMath.normalize(-1f), .001f) }
}
