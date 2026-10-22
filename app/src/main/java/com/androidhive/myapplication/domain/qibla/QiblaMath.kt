package com.androidhive.myapplication.domain.qibla

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaMath {
    fun bearing(latitude: Double, longitude: Double): Float {
        val from = Math.toRadians(latitude)
        val to = Math.toRadians(21.422487)
        val difference = Math.toRadians(39.826206 - longitude)
        val y = sin(difference) * cos(to)
        val x = cos(from) * sin(to) - sin(from) * cos(to) * cos(difference)
        return normalize(Math.toDegrees(atan2(y, x)).toFloat())
    }
    fun normalize(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f
    fun relativeBearing(bearing: Float, trueHeading: Float): Float = normalize(bearing - trueHeading)
    fun shortestDelta(from: Float, to: Float): Float = ((to - from + 540f) % 360f) - 180f
}
