package com.androidhive.myapplication.data.duas

/** Editorial state is retained for the owner's review; it is never a scholarly approval. */
const val PENDING_OWNER_REVIEW = "pending_owner_review"

data class PersianDuaContent(
    val arabic: String,
    val pronunciation: List<String>,
    val meaning: String,
    val practice: String,
    val sources: List<DuaSource>,
    val missingFields: Set<String>,
    val reviewStatus: String = PENDING_OWNER_REVIEW,
    val kind: String = "dua",
    val reminder: Boolean = false,
    val title: String = "",
) {
    val complete: Boolean get() = reviewStatus == PENDING_OWNER_REVIEW && missingFields.isEmpty() &&
        meaning.isNotBlank() && sources.isNotEmpty() && sources.all {
            it.checked && it.grade != DuaGrade.UNVERIFIED && it.book.isNotBlank() &&
                it.reference.isNotBlank() && it.url.startsWith("https://")
        } && if (kind == "practice") practice.isNotBlank() else
            arabic.isNotBlank() && pronunciation.isNotEmpty() && pronunciation.all { it.isNotBlank() }
}

/** A Persian overlay leaves the shared Arabic, other translations and their review gates intact. */
fun DuaItem.forLanguage(tag: String): DuaItem {
    if (tag != "fa") return this
    val content = persian ?: return this
    return copy(titles = if (content.title.isBlank()) titles else titles + ("fa" to content.title),
        arabic = content.arabic, pronunciations = pronunciations + ("fa" to content.pronunciation),
        translations = translations + ("fa" to content.meaning), notes = notes + ("fa" to content.practice),
        sources = content.sources, reviewFields = content.missingFields,
        reminder = content.complete && content.reminder && content.kind == "dua",
        guidance = guidance || content.kind == "practice")
}

fun DuaCatalog.forLanguage(tag: String): DuaCatalog =
    if (tag != "fa") this else copy(items = items.map { it.forLanguage(tag) })

/** The owner requested a clean Persian UI, not deletion of the review metadata. */
fun showEditorialStatus(tag: String) = tag != "fa"
