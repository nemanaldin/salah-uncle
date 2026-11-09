package com.androidhive.myapplication.domain.location

import kotlinx.coroutines.CancellationException

/** Monotonic timestamps prevent a wall-clock adjustment making an old fix look fresh. */
data class LocationFix(val latitude: Double, val longitude: Double, val accuracyMeters: Float, val elapsedNanos: Long) {
    fun usable(nowNanos: Long, preferred: Boolean = false): Boolean {
        val maxAge = if (preferred) 120_000_000_000L else 600_000_000_000L
        val maxAccuracy = if (preferred) 200f else 5000f
        return latitude.isFinite() && latitude in -90.0..90.0 && longitude.isFinite() && longitude in -180.0..180.0 &&
            accuracyMeters.isFinite() && accuracyMeters in 0f..maxAccuracy && elapsedNanos >= 0 &&
            nowNanos >= elapsedNanos && nowNanos - elapsedNanos <= maxAge
    }
}

/** GPS, network and cached fixes share the same rules. Provider failures are isolated. */
suspend fun selectLocation(
    providers: List<String>, cached: List<LocationFix>, nowNanos: () -> Long,
    request: suspend (String) -> LocationFix?,
): LocationFix? {
    val candidates = cached.toMutableList()
    fun best(preferred: Boolean) = candidates.filter { it.usable(nowNanos(), preferred) }
        .minWithOrNull(compareBy<LocationFix> { it.accuracyMeters }.thenByDescending { it.elapsedNanos })
    best(true)?.let { return it }
    for (provider in providers.distinct()) {
        try { request(provider)?.let { candidates += it } }
        catch (error: CancellationException) { throw error }
        catch (_: Exception) { /* A disabled/unavailable provider must not block the next one. */ }
        best(true)?.let { return it }
    }
    return best(false)
}
