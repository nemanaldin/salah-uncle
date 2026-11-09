package com.androidhive.myapplication.core

/** Main-thread confined owner. Inspecting or disposing it never creates the resource. */
class LazyResource<T : Any>(private val create: () -> T, private val dispose: (T) -> Unit) {
    private var value: T? = null
    private var released = false
    fun peek(): T? = value
    fun get(): T {
        check(!released) { "Resource has been released" }
        return value ?: create().also { value = it }
    }
    fun release() {
        if (released) return
        released = true
        value?.let(dispose)
        value = null
    }
}
