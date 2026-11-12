package com.androidhive.myapplication.core.model

/** Presentation policy only: the complete offline catalog is deliberately retained. */
data class VisibilityPolicy(
    val schools: List<School> = listOf(School.HANAFI),
    val languages: List<AppLanguage> = listOf(AppLanguage.PERSIAN, AppLanguage.ENGLISH, AppLanguage.TURKISH,
        AppLanguage.URDU, AppLanguage.BENGALI, AppLanguage.PASHTO, AppLanguage.ARABIC),
) {
    init { require(schools.isNotEmpty() && languages.isNotEmpty()); require(schools.distinct() == schools && languages.distinct() == languages) }
    val fingerprint get() = schools.joinToString { it.name } + "/" + languages.joinToString { it.name }
    fun allows(settings: AppSettings) = settings.school in schools && settings.language in languages
}
object ActiveContent { val policy = VisibilityPolicy() }

fun AppSettings.recoverVisibility(systemTags: List<String>, policy: VisibilityPolicy = ActiveContent.policy): AppSettings {
    val fallback = systemTags.firstNotNullOfOrNull { tag ->
        policy.languages.firstOrNull { it.tag == java.util.Locale.forLanguageTag(tag.replace('_', '-')).language }
    } ?: AppLanguage.ENGLISH.takeIf { it in policy.languages } ?: policy.languages.first()
    return copy(school = school.takeIf { it in policy.schools } ?: policy.schools.first(),
        language = language.takeIf { it in policy.languages } ?: fallback)
}
