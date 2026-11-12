package com.androidhive.myapplication.data.content

import com.androidhive.myapplication.core.model.*

data class ReadingTarget(val lessonId: String, val sectionId: String?, val offset: Int = 0)
fun readingTarget(id: String, section: String?, school: School, offset: Int = 0): ReadingTarget = when {
    id == "wudu-remembrance" -> ReadingTarget("purification-${school.name.lowercase(java.util.Locale.ROOT)}", section ?: "after-wudu", offset)
    id == "wudu" && school == School.HANAFI -> ReadingTarget("purification-hanafi", section ?: "vuzu_text1", offset)
    else -> ReadingTarget(id, section, offset)
}
fun readingStep(steps: List<LessonStep>, id: String?) = steps.firstOrNull { it.id == id || it.related.any { note -> note.id == id } }

/** Presentation-only joins. Original bodies, recitations, media and IDs are never rewritten. */
fun readingCatalogue(catalog: List<Lesson>, settings: AppSettings): List<Lesson> {
    val school = settings.school
    val language = settings.language
    val canonical = "purification-${school.name.lowercase(java.util.Locale.ROOT)}"
    val guide = catalog.firstOrNull { it.id == canonical }?.variant(school, language)
    val legacy = if (school == School.HANAFI) catalog.firstOrNull { it.id == "wudu" }?.variant(school, language) else null
    val remembrance = catalog.firstOrNull { it.id == "wudu-remembrance" }?.variant(school, language)
    val groups = mapOf(
        "intention-and-hands" to listOf("vuzu_text1", "vuzu1", "vuzu_text2", "vuzu2"),
        "mouth-nose-and-face" to listOf("vuzu3", "vuzu4", "vuzu5"),
        "arms" to listOf("vuzu6", "vuzu7"),
        "head-ears-and-feet" to listOf("vuzu8", "vuzu9", "vuzu10", "vuzu11", "vuzu_text3"),
    )
    val withExistingDhikr = setOf("vuzu1", "vuzu3", "vuzu4", "vuzu5", "vuzu6", "vuzu7", "vuzu8", "vuzu9", "vuzu10", "vuzu11")
    fun draft(step: LessonStep, origin: String) = step.copy(originLessonId = origin, arrangement = ArrangementStatus.DRAFT_PENDING_REVIEW,
        needsDhikrSource = origin == "wudu" && step.id !in withExistingDhikr)
    return catalog.filterNot { guide != null && (it.id == "wudu-remembrance" || school == School.HANAFI && legacy != null && it.id == "wudu") }.map { lesson ->
        lesson.copy(variants = lesson.variants.map { variant ->
            if (variant.school != school || variant.language != language.tag) variant
            else if (lesson.id == canonical && guide != null) {
                val used = mutableSetOf<String>()
                val steps = buildList {
                    for (main in variant.steps) {
                        add(if (school == School.HANAFI) main.copy(needsDhikrSource = true) else main)
                        for (id in groups[main.id].orEmpty()) legacy?.steps?.firstOrNull { it.id == id }?.let { step ->
                            add(draft(step, "wudu")); used += id
                        }
                    }
                    legacy?.steps?.filter { it.id !in used }?.forEach { add(draft(it, "wudu")) }
                    remembrance?.steps?.firstOrNull { it.id == "after-wudu" }?.let { after ->
                        add(draft(after, "wudu-remembrance").copy(related = remembrance.steps.filter { it.id != after.id }.map { draft(it, "wudu-remembrance") }))
                    }
                }
                variant.copy(steps = steps, sources = (variant.sources + legacy?.sources.orEmpty() + remembrance?.sources.orEmpty()).distinctBy { it.url })
            } else if (lesson.id == "ghusl") variant.copy(steps = variant.steps.map { it.copy(arrangement = ArrangementStatus.DRAFT_PENDING_REVIEW, needsDhikrSource = true) })
            else variant
        })
    }
}
