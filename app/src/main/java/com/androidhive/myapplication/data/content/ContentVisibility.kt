package com.androidhive.myapplication.data.content

import com.androidhive.myapplication.core.model.*

fun Lesson.visibleFor(settings: AppSettings, policy: VisibilityPolicy = ActiveContent.policy): Boolean =
    policy.allows(settings) && variant(settings.school, settings.language) != null
