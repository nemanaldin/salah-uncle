package com.androidhive.myapplication.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class ReadState<T>(val value: T? = null, val loading: Boolean = true, val error: Throwable? = null)
/** Retry reads only: never resets, overwrites or deletes the backing store. */
class RetryingReader<T>(private val scope: CoroutineScope, private val source: () -> Flow<T>) {
    private val mutable=MutableStateFlow(ReadState<T>())
    val state=mutable.asStateFlow()
    private var job: Job?=null
    init { retry() }
    fun retry() {
        job?.cancel()
        mutable.update {it.copy(loading=true,error=null)}
        job=scope.launch {
            try {
                source().collect { value ->
                    currentCoroutineContext().ensureActive()
                    mutable.value=ReadState(value,loading=false)
                }
            } catch(error: CancellationException) {throw error}
            catch(error: Exception) {mutable.update {it.copy(loading=false,error=error)}}
        }
    }
}
