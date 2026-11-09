package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.RetryingReader
import com.androidhive.myapplication.core.model.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ReadRecoveryTest {
    @Test fun initialFailureCanBeRetriedWithoutCreatingDefaultSettings() = runTest {
        var broken=true
        val saved=AppSettings(bookmarks=setOf("wudu"))
        val reader=RetryingReader(backgroundScope) { flow {if(broken) throw IOException("read unavailable");emit(saved)} }
        runCurrent();assertNull(reader.state.value.value);assertNotNull(reader.state.value.error)
        broken=false;reader.retry();runCurrent()
        assertEquals(saved,reader.state.value.value);assertNull(reader.state.value.error)
    }
    @Test fun failedReadKeepsTheLastGoodBookmarks() = runTest {
        val saved=AppSettings(bookmarks=setOf("wudu","ghusl"))
        val reader=RetryingReader(backgroundScope) {flow {emit(saved);throw IOException("disk")}}
        runCurrent();assertEquals(saved.bookmarks,reader.state.value.value!!.bookmarks);assertNotNull(reader.state.value.error)
    }
    @Test fun retriesReadAgainWithoutAnImplicitResetOrRetryLoop() = runTest {
        var calls=0
        val reader=RetryingReader<AppSettings>(backgroundScope) {flow {calls++;throw IOException("read")}}
        runCurrent();assertEquals(1,calls);reader.retry();runCurrent();assertEquals(2,calls);assertNull(reader.state.value.value)
    }
    @Test fun supersededReadCannotEraseTheRecoveredValue() = runTest {
        var calls=0
        val reader=RetryingReader(backgroundScope) {flow {if(++calls==1) awaitCancellation();emit(AppSettings(bookmarks=setOf("keep")))}}
        runCurrent();reader.retry();runCurrent();assertEquals(setOf("keep"),reader.state.value.value!!.bookmarks);assertNull(reader.state.value.error)
    }
}
