package com.androidhive.myapplication.ui.duas

import android.app.TimePickerDialog
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.duas.DuaCatalog
import com.androidhive.myapplication.domain.duas.reminderCandidates
import com.androidhive.myapplication.ui.components.*
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DuaReminderSettings(catalog: DuaCatalog, settings: AppSettings, notificationsAllowed: Boolean,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit, onPermission: () -> Unit, onError: (Int) -> Unit) {
    val context = LocalContext.current
    val options = settings.duaReminders
    var consentDialog by rememberSaveable { mutableStateOf(false) }
    var count by remember(options.count) { mutableFloatStateOf(options.count.coerceIn(1, 8).toFloat()) }
    val eligible = remember(catalog, options.categoryIds, settings.language, settings.duaBookmarks) {
        catalog.reminderCandidates(options.categoryIds, settings.language.tag, settings.duaBookmarks).size
    }
    fun clock(minute: Int): String {
        val numbers = NumberFormat.getIntegerInstance(Locale.forLanguageTag(settings.language.tag)).apply { minimumIntegerDigits = 2 }
        return "${numbers.format(minute / 60)}:${numbers.format(minute % 60)}"
    }
    val enableLabel = stringResource(R.string.ui_dua_enable)
    PageColumn(Modifier.testTag("dua_reminder_settings")) {
        AppCard {
            SupportingText(stringResource(R.string.ui_dua_reminder_consent))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(enableLabel, Modifier.weight(1f))
                Switch(options.enabled, { enabled ->
                    if (enabled) consentDialog = true else onUpdate { it.copy(duaReminders = it.duaReminders.copy(enabled = false)) }
                }, Modifier.testTag("dua_reminders_enabled").semantics { contentDescription = enableLabel },
                    enabled = options.enabled || eligible > 0 && options.valid)
            }
            if (options.enabled && !notificationsAllowed) {
                InfoCard(stringResource(R.string.ui_notifications_denied))
                OutlinedButton(onPermission, Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_grant_notifications)) }
            }
            InfoCard(stringResource(R.string.ui_dua_schedule_note))
        }
        AppCard {
            val frequency = stringResource(R.string.ui_dua_frequency)
            Text("$frequency · ${NumberFormat.getIntegerInstance(Locale.forLanguageTag(settings.language.tag)).format(count.roundToInt())}")
            Slider(count, { count = it }, valueRange = 1f..8f, steps = 6,
                onValueChangeFinished = { val n = count.roundToInt(); onUpdate { it.copy(duaReminders = it.duaReminders.copy(count = n)) } },
                modifier = Modifier.testTag("dua_reminder_count").semantics { contentDescription = frequency })
            ChoiceRow(stringResource(R.string.ui_dua_window_start), clock(options.startMinute), {
                TimePickerDialog(context, { _, hour, minute ->
                    val value = hour * 60 + minute
                    if (value >= options.endMinute) onError(R.string.ui_dua_invalid_window)
                    else onUpdate { it.copy(duaReminders = it.duaReminders.copy(startMinute = value)) }
                }, options.startMinute.coerceIn(0,1439) / 60, options.startMinute.coerceIn(0,1439) % 60, true).show()
            }, Modifier.testTag("dua_reminder_start"))
            ChoiceRow(stringResource(R.string.ui_dua_window_end), clock(options.endMinute), {
                TimePickerDialog(context, { _, hour, minute ->
                    val value = hour * 60 + minute
                    if (value <= options.startMinute) onError(R.string.ui_dua_invalid_window)
                    else onUpdate { it.copy(duaReminders = it.duaReminders.copy(endMinute = value)) }
                }, options.endMinute.coerceIn(0,1439) / 60, options.endMinute.coerceIn(0,1439) % 60, true).show()
            }, Modifier.testTag("dua_reminder_end"))
        }
        AppCard {
            SectionTitle(stringResource(R.string.ui_dua_categories_select))
            catalog.categories.forEach { category ->
                val checked = category.id in options.categoryIds
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(checked, role = Role.Checkbox) { _ ->
                    onUpdate { saved -> saved.copy(duaReminders = saved.duaReminders.copy(categoryIds =
                        if (category.id in saved.duaReminders.categoryIds) saved.duaReminders.categoryIds - category.id else saved.duaReminders.categoryIds + category.id)) }
                }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked, null)
                    Text(category.title(settings.language.tag), Modifier.weight(1f))
                }
            }
            SupportingText(stringResource(R.string.ui_dua_eligible, eligible))
            if (!options.valid) InfoCard(stringResource(R.string.ui_dua_invalid_window))
            if (eligible == 0) InfoCard(stringResource(R.string.ui_dua_none_eligible))
        }
    }
    if (consentDialog) AlertDialog(onDismissRequest = { consentDialog = false },
        title = { Text(stringResource(R.string.ui_dua_reminders)) }, text = { Text(stringResource(R.string.ui_dua_reminder_consent)) },
        confirmButton = { TextButton({
            consentDialog = false
            onUpdate { it.copy(duaReminders = it.duaReminders.copy(enabled = true, consent = true)) }
            onPermission()
        }, Modifier.testTag("dua_reminder_consent_confirm"), enabled = eligible > 0 && options.valid) { Text(enableLabel) } },
        dismissButton = { TextButton({ consentDialog = false }) { Text(stringResource(R.string.ui_close)) } })
}
