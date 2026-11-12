package com.androidhive.myapplication.data.content

import com.androidhive.myapplication.core.model.*

data class LessonSearchHit(val lessonId: String, val sectionId: String, val lessonTitle: String, val sectionTitle: String, val excerpt: String, val offset: Int = 0)
fun sectionHeading(step: LessonStep): String = step.body.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty().take(100)

/** No duplicate whole-catalog index: bodies are searched on demand, only in the selected variant. */
fun searchLessons(lessons: List<Lesson>, settings: AppSettings, query: String, policy: VisibilityPolicy = ActiveContent.policy): Sequence<LessonSearchHit> = sequence {
    val needle = query.trim()
    if (needle.isEmpty() || !policy.allows(settings)) return@sequence
    for (lesson in lessons) {
        if (!lesson.visibleFor(settings, policy)) continue
        val variant = lesson.variant(settings.school, settings.language) ?: continue
        val title = lesson.title(settings.language)
        var found = false
        for (parent in variant.steps) for (step in listOf(parent) + parent.related) {
            val index = step.body.indexOf(needle, ignoreCase = true)
            if (index >= 0) {
                val start = (index - 45).coerceAtLeast(0)
                yield(LessonSearchHit(lesson.id, step.id, title, sectionHeading(step), step.body.substring(start, (index + needle.length + 110).coerceAtMost(step.body.length)), index))
                found = true
            }
        }
        if (!found && title.contains(needle, ignoreCase = true)) variant.steps.firstOrNull()?.let { step ->
            yield(LessonSearchHit(lesson.id, step.id, title, sectionHeading(step), step.body.take(160)))
        }
    }
}
