package com.androidhive.myapplication.core.model

/** Source IDs and the school/language scope survive presentation changes. */
data class SectionBookmark(val lessonId: String, val sectionId: String, val school: School, val language: AppLanguage, val note: String = "", val offset: Int = 0) {
    fun sameSection(other: SectionBookmark) = lessonId == other.lessonId && sectionId == other.sectionId && school == other.school && language == other.language
}
fun boundedNote(text: String): String = text.substring(0, text.offsetByCodePoints(0, text.codePointCount(0, text.length).coerceAtMost(200)))
fun AppSettings.withSectionBookmark(value: SectionBookmark): AppSettings = copy(
    sectionBookmarks = sectionBookmarks.filterNot { it.sameSection(value) } + value.copy(note = boundedNote(value.note)))
fun AppSettings.withoutSectionBookmark(value: SectionBookmark) = copy(sectionBookmarks = sectionBookmarks.filterNot { it.sameSection(value) })

/** The old topic key stays intact; only a scoped section reference is added. */
fun AppSettings.migratePostWuduBookmark(): AppSettings {
    if ("wudu-remembrance" !in bookmarks) return this
    val value = SectionBookmark("wudu-remembrance", "after-wudu", school, language)
    return if (sectionBookmarks.any { it.sameSection(value) }) this else withSectionBookmark(value)
}

fun topicBookmarkIds(id: String, school: School): Set<String> =
    if (school == School.HANAFI && id in setOf("wudu", "purification-hanafi")) setOf("wudu", "purification-hanafi") else setOf(id)
