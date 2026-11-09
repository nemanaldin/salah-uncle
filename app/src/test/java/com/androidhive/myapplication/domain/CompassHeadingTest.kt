package com.androidhive.myapplication.domain

import com.androidhive.myapplication.domain.qibla.*
import org.junit.Assert.*
import org.junit.Test
class CompassHeadingTest {
    @Test fun noSampleMeansWaitingAndNoNeedle() { val h=compassHeading(null, HeadingConfidence.HIGH);assertEquals(HeadingStatus.WAITING,h.status);assertFalse(h.usable) }
    @Test fun failedRegistrationIsInvalid() { assertEquals(HeadingStatus.INVALID,compassHeading(null,HeadingConfidence.HIGH,false).status) }
    @Test fun genuineNorthSampleIsValid() { val h=compassHeading(0f,HeadingConfidence.HIGH);assertTrue(h.usable);assertEquals(0f,h.magneticDegrees!!,0f) }
    @Test fun unreliableSampleDoesNotDrawANeedle() { val h=compassHeading(120f,HeadingConfidence.UNRELIABLE);assertEquals(HeadingStatus.INVALID,h.status);assertFalse(h.usable) }
    @Test fun lowAccuracyRequiresCalibrationWithoutANeedle() { val h=compassHeading(120f,HeadingConfidence.LOW);assertEquals(HeadingStatus.NEEDS_CALIBRATION,h.status);assertFalse(h.usable) }
    @Test fun mediumAccuracyCanShowTheNeedle() { assertTrue(compassHeading(120f,HeadingConfidence.MEDIUM).usable) }
    @Test fun nonFiniteSamplesAreInvalid() { for (v in listOf(Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY)) assertFalse(compassHeading(v,HeadingConfidence.HIGH).usable) }
    @Test fun accuracyCanBecomeInvalidAfterAValidSample() { assertTrue(compassHeading(350f,HeadingConfidence.HIGH).usable);assertFalse(compassHeading(350f,HeadingConfidence.UNRELIABLE).usable) }
    @Test fun validSamplesAreNormalized() { assertEquals(350f,compassHeading(-10f,HeadingConfidence.HIGH).magneticDegrees!!,0f) }
}

class HeadingSamplesTest {
    @Test fun accuracyBeforeFirstSampleStillWaits() {
        assertEquals(HeadingStatus.WAITING, HeadingSamples().accuracy(HeadingConfidence.HIGH).status)
    }
    @Test fun failedMatrixCannotBeResurrectedByAnAccuracyCallback() {
        val samples = HeadingSamples()
        assertTrue(samples.accept(120f, HeadingConfidence.HIGH).usable)
        samples.invalidate()
        val state = samples.accuracy(HeadingConfidence.HIGH)
        assertEquals(HeadingStatus.INVALID, state.status); assertFalse(state.usable)
    }
    @Test fun aNewSampleRecoversAfterAnInvalidMatrix() {
        val samples = HeadingSamples()
        samples.accept(120f, HeadingConfidence.HIGH); samples.invalidate()
        assertEquals(30f, samples.accept(30f, HeadingConfidence.MEDIUM).magneticDegrees!!, 0f)
    }
}
