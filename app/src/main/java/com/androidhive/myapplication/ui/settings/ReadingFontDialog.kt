package com.androidhive.myapplication.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.ui.components.SupportingText
import com.androidhive.myapplication.ui.theme.*

@Composable
fun ReadingFontDialog(settings: AppSettings, onChoose: (ReadingFont) -> Unit, onDismiss: () -> Unit) {
    var selected by rememberSaveable(settings.readingFont) { mutableStateOf(settings.readingFont) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.ui_reading_font)) }, text = {
        Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SupportingText(stringResource(R.string.ui_fonts_offline))
            ReadingFont.entries.forEach { font ->
                val sampleLanguage = when (font) {
                    ReadingFont.NOTO_SANS -> AppLanguage.ENGLISH
                    ReadingFont.AMIRI, ReadingFont.NOTO_NASKH -> AppLanguage.ARABIC
                    else -> AppLanguage.PERSIAN
                }
                val sample = stringResource(when (sampleLanguage) {
                    AppLanguage.ARABIC -> R.string.ui_font_arabic_sample
                    AppLanguage.ENGLISH -> R.string.ui_font_latin_sample
                    else -> R.string.ui_font_persian_sample
                })
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("font_${font.name}")
                    .selectable(selected == font, role = Role.RadioButton) { selected = font }.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadioButton(selected == font, onClick = null)
                    Column(Modifier.weight(1f)) {
                        Text(font.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(sample, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = bundledFontFamily(font)))
                    }
                }
            }
            HorizontalDivider()
            Text(stringResource(R.string.ui_font_preview), style = MaterialTheme.typography.titleMedium)
            ReadingText(stringResource(R.string.ui_font_preview_sample), settings.copy(readingFont = selected), settings.language,
                Modifier.testTag("font_live_preview").semantics { stateDescription = selected.displayName })
            val resolved = selected.resolve(settings.language)
            if (resolved != selected) SupportingText(stringResource(R.string.ui_font_script_fallback,
                resolved?.displayName ?: stringResource(R.string.ui_system_font)))
        }
    }, confirmButton = { TextButton({ onChoose(selected); onDismiss() }) { Text(stringResource(R.string.ui_save)) } },
        dismissButton = { Row {
            TextButton({ selected = ReadingFont.VAZIRMATN }) { Text(stringResource(R.string.ui_reset_reading)) }
            TextButton(onDismiss) { Text(stringResource(R.string.ui_cancel)) }
        } })
}
