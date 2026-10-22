package com.androidhive.myapplication.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.labelRes
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(settings: AppSettings, onLanguage: (AppLanguage) -> Unit, onSchool: (School) -> Unit, onFinish: () -> Unit) {
    PageColumn {
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.ui_welcome), style = MaterialTheme.typography.headlineLarge)
        SupportingText(stringResource(R.string.ui_welcome_body))
        AppCard {
            SectionTitle(stringResource(R.string.ui_choose_language))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(selected = language == settings.language, onClick = { onLanguage(language) },
                        label = { Text(language.nativeName) },
                        modifier = Modifier.testTag("language_${language.tag}"))
                }
            }
        }
        AppCard {
            SectionTitle(stringResource(R.string.ui_choose_school))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                School.entries.forEach { school ->
                    FilterChip(school == settings.school, onClick = { onSchool(school) }, enabled = school.contentAvailable,
                        label = { Text(stringResource(school.labelRes())) }, modifier = Modifier.testTag("school_${school.name}"))
                }
            }
            SupportingText(stringResource(R.string.ui_school_note))
        }
        SupportingText(stringResource(R.string.ui_location_optional))
        Button(onFinish, Modifier.fillMaxWidth().heightIn(min = 54.dp).testTag("onboarding_start")) { Text(stringResource(R.string.ui_start)) }
    }
}
