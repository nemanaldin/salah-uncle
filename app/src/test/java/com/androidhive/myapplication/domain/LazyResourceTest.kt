package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.LazyResource
import org.junit.Assert.*
import org.junit.Test

class LazyResourceTest {
    @Test fun openingAndClosingWithoutPlaybackDoesNotCreatePlayer() {
        var creates = 0; var releases = 0
        val owner = LazyResource({ creates++; Any() }, { releases++ })
        assertNull(owner.peek()); owner.release()
        assertEquals(0, creates); assertEquals(0, releases)
    }
    @Test fun playbackAllocatesExactlyOneResource() {
        var creates = 0
        val owner = LazyResource({ creates++; Any() }, {})
        assertSame(owner.get(), owner.get()); assertEquals(1, creates)
    }
    @Test fun releaseIsIdempotentAndClearsTheReference() {
        var releases = 0
        val owner = LazyResource({ Any() }, { releases++ })
        owner.get(); owner.release(); owner.release()
        assertEquals(1, releases); assertNull(owner.peek())
    }
    @Test(expected = IllegalStateException::class) fun disposedOwnerCannotRecreatePlayer() {
        val owner = LazyResource({ Any() }, {})
        owner.release(); owner.get()
    }
}
