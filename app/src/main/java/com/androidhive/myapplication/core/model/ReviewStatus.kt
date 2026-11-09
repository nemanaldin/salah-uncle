package com.androidhive.myapplication.core.model

/** Release-readiness metadata; this is never a religious-content approval. */
enum class ReviewStatus {
    REVIEWED, EXPERIMENTAL;
    companion object {
        fun fromStored(value: String?): ReviewStatus = if (value?.trim() == "reviewed") REVIEWED else EXPERIMENTAL
    }
}
