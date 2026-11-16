package com.androidhive.myapplication.data.duas

import java.text.Normalizer
import java.time.DayOfWeek
import java.util.Locale

enum class DuaGrade { SAHIH, HASAN, QURAN, UNVERIFIED }
data class DuaSource(val book: String, val reference: String, val url: String, val grade: DuaGrade,
    val grader: String, val checked: Boolean)
data class DuaCategory(val id: String, val titles: Map<String, String>) {
    fun title(tag: String) = titles[tag] ?: titles["en"].orEmpty()
}
data class DuaSection(val id: String, val category: String, val titles: Map<String, String>, val duaIds: List<String>) {
    fun title(tag: String) = titles[tag] ?: titles["en"].orEmpty()
}
data class DuaRepeat(val arabic: String, val count: Int)
data class DuaItem(
    val id: String, val category: String, val section: String, val titles: Map<String, String>,
    val arabic: String = "", val pronunciations: Map<String, List<String>> = emptyMap(),
    val translations: Map<String, String> = emptyMap(), val notes: Map<String, String> = emptyMap(),
    val sources: List<DuaSource> = emptyList(), val repeats: List<DuaRepeat> = emptyList(),
    val audio: String = "", val reviewFields: Set<String> = emptySet(), val ownerText: Boolean = false,
    val reminder: Boolean = false,
    val guidance: Boolean = false,
) {
    fun title(tag: String) = titles[tag] ?: titles["en"].orEmpty()
    fun translation(tag: String) = translations[tag].orEmpty()
    fun note(tag: String) = notes[tag].orEmpty()
    val hasVerifiedText get() = arabic.isNotBlank() && sources.any {
        it.checked && it.grade != DuaGrade.UNVERIFIED && it.reference.isNotBlank() && it.url.startsWith("https://")
    } && reviewFields.none { it in setOf("arabic", "source", "grade") }
    fun matches(query: String): Boolean {
        val terms = normalizeDuaSearch(query).split(' ').filter { it.isNotBlank() }
        val body = normalizeDuaSearch((titles.values + arabic + translations.values).joinToString(" "))
        return terms.all { it in body }
    }
}
data class DuaCatalog(val categories: List<DuaCategory>, val sections: List<DuaSection>, val items: List<DuaItem>) {
    init {
        require(categories.size == 7 && categories.map { it.id }.distinct().size == 7)
        require(items.map { it.id }.distinct().size == items.size)
        require(sections.map { it.id }.distinct().size == sections.size)
        require(items.all { item -> categories.any { it.id == item.category } && sections.any { it.id == item.section && item.id in it.duaIds } })
        require(sections.all { section -> categories.any { it.id == section.category } && section.duaIds.all { id -> items.any { it.id == id } } })
    }
    fun item(id: String) = items.firstOrNull { it.id == id }
    fun section(id: String) = sections.firstOrNull { it.id == id }
    fun itemsIn(sectionId: String) = section(sectionId)?.duaIds.orEmpty().mapNotNull(::item)
    fun sectionFor(duaId: String, preferred: String? = null) =
        sections.firstOrNull { it.id == preferred && duaId in it.duaIds } ?: sections.firstOrNull { duaId in it.duaIds }
    fun validReaderRoute(duaId: String, sectionId: String) = section(sectionId)?.duaIds?.contains(duaId) == true
}

/** Normalize an index only. Display, copy and owner-provided Arabic remain byte-for-byte unchanged. */
fun normalizeDuaSearch(text: String): String = buildString {
    Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFKC).forEach { char ->
        when (char) {
            in '\u0610'..'\u061a', in '\u064b'..'\u065f', '\u0670', in '\u06d6'..'\u06ed', '\u0640', '\u200c', '\u200d' -> Unit
            'أ', 'إ', 'آ', 'ٱ' -> append('ا')
            'ی', 'ى', 'ئ' -> append('ي')
            'ک' -> append('ك')
            else -> append(if (char.isWhitespace()) ' ' else char)
        }
    }
}.trim().replace(Regex(" +"), " ")
fun todayDuaSection(day: DayOfWeek) = when (day) {
    DayOfWeek.FRIDAY -> "friday"
    DayOfWeek.MONDAY, DayOfWeek.THURSDAY -> "fasting"
    else -> "weekday-common"
}
fun advanceDuaCounter(current: Int, target: Int?): Int = if (target == null || target <= 0) 0 else
    (current.coerceIn(0, target).toLong() + 1).coerceAtMost(target.toLong()).toInt()
fun rememberDua(recent: List<String>, id: String) = (listOf(id) + recent.filterNot { it == id }).distinct().take(30)
