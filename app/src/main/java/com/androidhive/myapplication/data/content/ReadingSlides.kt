package com.androidhive.myapplication.data.content

import com.androidhive.myapplication.core.model.School

data class TextSlice(val offset: Int, val text: String)
data class ReadingSlide(val step: LessonStep, val offset: Int, val text: String) {
    val key get() = "${step.id}:$offset"
}
fun isSlideGuide(id: String, school: School) = id == school.guideId || id == "purification-${school.name.lowercase(java.util.Locale.ROOT)}" || id == "ghusl"

/** Slices retain every original character. An overlong sentence stays intact. */
fun sentenceSlices(text: String, preferredLength: Int = 360): List<TextSlice> {
    require(preferredLength > 0)
    if (text.isEmpty()) return listOf(TextSlice(0, text))
    val boundaries = Regex("(?<=[.!?؟۔])\\s+").findAll(text).map { it.range.last + 1 }.toList() + text.length
    val result = mutableListOf<TextSlice>()
    var start = 0
    var previous = 0
    for (end in boundaries) {
        if (end - start > preferredLength && previous > start) {
            result += TextSlice(start, text.substring(start, previous)); start = previous
        }
        previous = end
    }
    if (start < text.length) result += TextSlice(start, text.substring(start))
    return result
}
fun readingSlides(steps: List<LessonStep>) = steps.flatMap { step -> sentenceSlices(step.body).map { ReadingSlide(step, it.offset, it.text) } }
fun targetSlide(slides: List<ReadingSlide>, sectionId: String?, offset: Int = 0): Int =
    slides.indexOfLast { it.step.id == sectionId && it.offset <= offset.coerceAtLeast(0) }.coerceAtLeast(0)
