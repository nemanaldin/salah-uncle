package com.androidhive.myapplication.core.model

/** Source IDs and the school/language scope survive presentation changes. */
data class SectionBookmark(val lessonId: String, val sectionId: String, val school: School, val language: AppLanguage, val note: String = "") {
    fun sameSection(other: SectionBookmark) = lessonId == other.lessonId && sectionId == other.sectionId && school == other.school && language == other.language
}
fun boundedNote(text: String): String = text.substring(0, text.offsetByCodePoints(0, text.codePointCount(0, text.length).coerceAtMost(200)))
fun AppSettings.withSectionBookmark(value: SectionBookmark): AppSettings = copy(
    sectionBookmarks = sectionBookmarks.filterNot { it.sameSection(value) } + value.copy(note = boundedNote(value.note)))
fun AppSettings.withoutSectionBookmark(value: SectionBookmark) = copy(sectionBookmarks = sectionBookmarks.filterNot { it.sameSection(value) })
