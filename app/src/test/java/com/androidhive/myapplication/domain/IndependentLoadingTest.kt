package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.LoadSlot
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IndependentLoadingTest {
    @Test fun failureDoesNotCancelOrWaitForOtherCatalogs() = runTest {
        val lessons = LoadSlot(emptyList<String>(), backgroundScope) { error("lessons broken") }
        val cities = LoadSlot(emptyList<String>(), backgroundScope) { listOf("city") }
        val videos = LoadSlot(emptyList<String>(), backgroundScope) { delay(1000); listOf("video") }
        lessons.retry(); cities.retry(); videos.retry(); runCurrent()
        assertTrue(lessons.state.value.failed)
        assertEquals(listOf("city"), cities.state.value.data)
        assertTrue(videos.state.value.loading)
        advanceTimeBy(1000); runCurrent()
        assertEquals(listOf("video"), videos.state.value.data)
    }
    @Test fun retryOnlyReloadsItsOwnCatalogAndClearsError() = runTest {
        var lessonCalls = 0; var cityCalls = 0
        val lessons = LoadSlot(emptyList<String>(), backgroundScope) { if (++lessonCalls == 1) error("broken"); listOf("lesson") }
        val cities = LoadSlot(emptyList<String>(), backgroundScope) { cityCalls++; listOf("city") }
        lessons.retry(); cities.retry(); runCurrent()
        lessons.retry(); runCurrent()
        assertFalse(lessons.state.value.failed); assertEquals(listOf("lesson"), lessons.state.value.data)
        assertEquals(1, cityCalls)
    }
    @Test fun failedRefreshRetainsLastGoodData() = runTest {
        var fail = false
        val slot = LoadSlot(emptyList<String>(), backgroundScope) { if (fail) error("broken"); listOf("saved") }
        slot.retry(); runCurrent(); fail = true; slot.retry(); runCurrent()
        assertTrue(slot.state.value.failed); assertEquals(listOf("saved"), slot.state.value.data)
    }
    @Test fun supersededReadCannotPublishStaleResults() = runTest {
        var calls = 0
        val slot = LoadSlot("", backgroundScope) {
            if (++calls == 1) withContext(NonCancellable) { delay(1000); "old" } else "new"
        }
        slot.retry(); runCurrent(); slot.retry(); runCurrent(); advanceTimeBy(1000); runCurrent()
        assertEquals("new", slot.state.value.data)
    }
}
