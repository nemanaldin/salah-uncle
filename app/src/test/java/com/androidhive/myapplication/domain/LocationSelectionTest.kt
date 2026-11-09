package com.androidhive.myapplication.domain

import com.androidhive.myapplication.domain.location.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LocationSelectionTest {
    private val now = 1_000_000_000_000L
    private fun fix(ageSeconds: Long = 0, accuracy: Float = 20f) = LocationFix(35.0, 51.0, accuracy, now - ageSeconds * 1_000_000_000)
    @Test fun gpsFailureTriesNetwork() = runTest {
        val calls = mutableListOf<String>()
        val result = selectLocation(listOf("gps", "network"), emptyList(), { now }) {
            calls += it; if (it == "gps") error("unavailable") else fix()
        }
        assertEquals(listOf("gps", "network"), calls); assertEquals(fix(), result)
    }
    @Test fun timeoutOrNullFromGpsTriesNetwork() = runTest {
        assertEquals(fix(), selectLocation(listOf("gps", "network"), emptyList(), { now }) { if (it == "gps") null else fix() })
    }
    @Test fun goodRecentCacheAvoidsRadioRequests() = runTest {
        assertEquals(fix(120, 200f), selectLocation(listOf("gps"), listOf(fix(120, 200f)), { now }) { error("must not request") })
    }
    @Test fun fallbackAcceptsTenMinutesAndFiveKilometersInclusively() = runTest {
        assertEquals(fix(600, 5000f), selectLocation(emptyList(), listOf(fix(600, 5000f)), { now }) { null })
    }
    @Test fun staleFutureAndInaccurateFixesAreRejected() = runTest {
        assertNull(selectLocation(emptyList(), listOf(fix(601), fix(-1), fix(0, 5001f), fix(0, Float.NaN)), { now }) { null })
    }
    @Test fun newerPreciseFixReplacesCoarseCachedFallback() = runTest {
        assertEquals(fix(), selectLocation(listOf("network"), listOf(fix(500, 1000f)), { now }) { fix() })
    }
    @Test fun failedLiveSourcesRetainUsableCachedFallback() = runTest {
        assertEquals(fix(500), selectLocation(listOf("gps", "network"), listOf(fix(500)), { now }) { null })
    }
    @Test fun invalidCoordinatesAreNeverReturned() = runTest {
        assertNull(selectLocation(listOf("network"), emptyList(), { now }) { fix().copy(latitude = Double.NaN) })
    }
    @Test(expected = CancellationException::class) fun externalCancellationDoesNotFallBack() = runTest {
        selectLocation(listOf("gps", "network"), emptyList(), { now }) { throw CancellationException() }
    }
}
