package com.androidhive.myapplication.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoadState<T>(val data: T, val loading: Boolean = true, val failed: Boolean = false)

/** Independent retry boundary; a failed catalog never cancels another catalog. */
class LoadSlot<T>(initial: T, private val scope: CoroutineScope, private val load: suspend () -> T) {
    private val mutable = MutableStateFlow(LoadState(initial))
    val state = mutable.asStateFlow()
    private var job: Job? = null
    fun retry() {
        job?.cancel()
        mutable.value = mutable.value.copy(loading = true, failed = false)
        job = scope.launch {
            try {
                val data = load()
                ensureActive()
                mutable.value = LoadState(data, loading = false)
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) {
                ensureActive()
                mutable.value = mutable.value.copy(loading = false, failed = true)
            }
        }
    }
}
