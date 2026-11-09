package com.androidhive.myapplication.domain.qibla

enum class HeadingStatus { WAITING, INVALID, NEEDS_CALIBRATION, VALID }
enum class HeadingConfidence { UNRELIABLE, LOW, MEDIUM, HIGH }
data class CompassHeading(val status: HeadingStatus = HeadingStatus.WAITING, val magneticDegrees: Float? = null) {
    val usable get() = status == HeadingStatus.VALID && magneticDegrees?.isFinite() == true
}
fun compassHeading(degrees: Float?, confidence: HeadingConfidence, registered: Boolean = true): CompassHeading = when {
    !registered -> CompassHeading(HeadingStatus.INVALID)
    degrees == null -> CompassHeading()
    !degrees.isFinite() -> CompassHeading(HeadingStatus.INVALID)
    confidence == HeadingConfidence.UNRELIABLE -> CompassHeading(HeadingStatus.INVALID)
    confidence == HeadingConfidence.LOW -> CompassHeading(HeadingStatus.NEEDS_CALIBRATION)
    else -> CompassHeading(HeadingStatus.VALID, QiblaMath.normalize(degrees))
}
