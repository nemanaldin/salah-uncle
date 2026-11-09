package com.androidhive.myapplication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.core.model.ReviewStatus

@Composable
fun languageChoiceLabel(language: AppLanguage): String = language.nativeName +
    if (language.reviewStatus == ReviewStatus.EXPERIMENTAL) " · " + stringResource(R.string.ui_experimental_status) else ""

@Composable
fun LanguageReadinessNotice(language: AppLanguage) {
    if (language.reviewStatus == ReviewStatus.EXPERIMENTAL)
        InfoCard(stringResource(R.string.ui_experimental_language_notice, language.nativeName))
}
