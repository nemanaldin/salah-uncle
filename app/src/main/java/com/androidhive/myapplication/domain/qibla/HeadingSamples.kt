package com.androidhive.myapplication.domain.qibla

/** Accuracy callbacks may update a sample, but must never resurrect a failed matrix. */
class HeadingSamples {
    private var degrees: Float? = null
    fun accept(value: Float, confidence: HeadingConfidence): CompassHeading {
        degrees = value
        return compassHeading(degrees, confidence)
    }
    fun invalidate(): CompassHeading {
        degrees = Float.NaN
        return CompassHeading(HeadingStatus.INVALID)
    }
    fun accuracy(confidence: HeadingConfidence): CompassHeading = compassHeading(degrees, confidence)
}
