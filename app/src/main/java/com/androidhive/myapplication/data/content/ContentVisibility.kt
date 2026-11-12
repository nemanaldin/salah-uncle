package com.androidhive.myapplication.data.content

import com.androidhive.myapplication.core.model.*

fun Lesson.visibleFor(settings: AppSettings, policy: VisibilityPolicy = ActiveContent.policy): Boolean =
    policy.allows(settings) && variant(settings.school, settings.language) != null

/** No cross-school or cross-language fallback for home shortcuts, including tayammum. */
fun Iterable<Lesson>.visibleTopic(id: String, settings: AppSettings,
    policy: VisibilityPolicy = ActiveContent.policy): Lesson? = firstOrNull { it.id == id && it.visibleFor(settings, policy) }
