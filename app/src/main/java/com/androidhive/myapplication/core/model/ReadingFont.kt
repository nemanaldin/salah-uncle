package com.androidhive.myapplication.core.model

/** Stable persisted IDs; the default remains the original app family. */
enum class ReadingFont(val displayName: String) {
    VAZIRMATN("Vazirmatn"), SAHEL("Sahel"), AMIRI("Amiri"), NOTO_NASKH("Noto Naskh Arabic"), NOTO_SANS("Noto Sans");
    companion object { fun fromStored(value: String?) = entries.firstOrNull { it.name == value } ?: VAZIRMATN }
    /** Coverage was checked against the bundled font cmaps, not guessed from the font name. */
    fun resolve(language: AppLanguage): ReadingFont? = when {
        language == AppLanguage.BENGALI -> null // Preserve system Bengali shaping/fallback.
        this == NOTO_SANS && language.isRtl -> VAZIRMATN
        this == NOTO_NASKH && !language.isRtl -> NOTO_SANS
        this == SAHEL && language in listOf(AppLanguage.URDU, AppLanguage.PASHTO) -> VAZIRMATN
        this == SAHEL && language == AppLanguage.TURKISH -> NOTO_SANS
        else -> this
    }
}
fun readingArabicScale(value: Float): Float = if (value.isFinite()) value.coerceIn(.85f, 2f) else 1f
fun AppSettings.resetReadingAppearance() = copy(readingFont = ReadingFont.VAZIRMATN, textScale = 1f, arabicTextScale = 1f)

/** Ranges change presentation only; the original Unicode text is never rewritten. */
fun arabicTextRanges(text: String): List<IntRange> = Regex("[\\u0600-\\u06ff\\u0750-\\u077f\\u08a0-\\u08ff\\ufb50-\\ufdff\\ufe70-\\ufeff]+").findAll(text).map { it.range }.toList()
