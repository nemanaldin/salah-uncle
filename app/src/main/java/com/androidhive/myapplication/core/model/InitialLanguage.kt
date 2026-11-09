package com.androidhive.myapplication.core.model

/** A stored choice always wins; device/app locales seed only a new installation. */
fun initialLanguage(saved: String?, systemTags: List<String>): AppLanguage {
    AppLanguage.entries.firstOrNull { it.name == saved }?.let { return it }
    return systemTags.firstNotNullOfOrNull { tag ->
        val code = tag.substringBefore('-').substringBefore('_').lowercase(java.util.Locale.ROOT)
        AppLanguage.entries.firstOrNull { it.tag == code || (code == "in" && it == AppLanguage.INDONESIAN) }
    } ?: AppLanguage.PERSIAN
}
