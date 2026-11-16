package com.androidhive.myapplication.ui.home

import java.time.Duration
import java.time.Instant

data class RemainingPrayerTime(val hours: Long, val minutes: Int) {
    val lessThanMinute get() = hours == 0L && minutes == 0
}

/** Presentation only: never substitutes a time for a failed prayer calculation. */
fun remainingPrayerTime(now: Instant, next: Instant): RemainingPrayerTime {
    val seconds = Duration.between(now, next).seconds.coerceAtLeast(0)
    return RemainingPrayerTime(seconds / 3600, (seconds % 3600 / 60).toInt())
}

/** The full prayer-times page retains its existing seconds display. */
fun countdown(now: Instant, next: Instant): String {
    val seconds = Duration.between(now, next).seconds.coerceAtLeast(0)
    return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
}
