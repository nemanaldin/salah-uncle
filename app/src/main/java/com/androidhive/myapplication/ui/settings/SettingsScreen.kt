package com.androidhive.myapplication.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahUiState
import com.androidhive.myapplication.core.labelRes
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.platform.audio.LessonAudioController
import com.androidhive.myapplication.ui.components.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SettingsScreen(state: SalahUiState, onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onAlertMode: (AlertMode) -> Unit, onNotifications: () -> Unit, onExact: () -> Unit,
    onFullScreen: () -> Unit, onAbout: () -> Unit, onError: (Int) -> Unit) {
    val settings = state.settings ?: return
    var dialog by rememberSaveable { mutableStateOf<String?>(null) }
    var scale by remember(settings.textScale) { mutableFloatStateOf(settings.textScale) }
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val error by rememberUpdatedState(onError)
    val preview = remember { LessonAudioController(context) { error(R.string.ui_error_audio) } }
    val previewState by preview.state.collectAsStateWithLifecycle()
    DisposableEffect(owner, preview) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) preview.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); preview.release() }
    }
    PageColumn(Modifier.testTag("settings_screen")) {
        AppCard {
            SectionTitle(stringResource(R.string.ui_choose_language))
            ChoiceRow(stringResource(R.string.ui_choose_language), settings.language.nativeName, { dialog = "language" }, Modifier.testTag("setting_language"))
            ChoiceRow(stringResource(R.string.ui_choose_school), stringResource(settings.school.labelRes()), { dialog = "school" }, Modifier.testTag("setting_school"))
            SupportingText(stringResource(R.string.ui_school_note))
        }
        AppCard {
            SectionTitle(stringResource(R.string.ui_appearance))
            ChoiceRow(stringResource(R.string.ui_theme), stringResource(when (settings.theme) {
                ThemeMode.SYSTEM -> R.string.ui_theme_system; ThemeMode.LIGHT -> R.string.ui_theme_light; ThemeMode.DARK -> R.string.ui_theme_dark
            }), { dialog = "theme" }, Modifier.testTag("setting_theme"))
            val textSizeLabel = stringResource(R.string.ui_text_size)
            Text("$textSizeLabel · ${NumberFormat.getPercentInstance(Locale.forLanguageTag(settings.language.tag)).format(scale)}")
            Slider(scale, { scale = it }, valueRange = .85f..1.5f, steps = 12,
                onValueChangeFinished = { onUpdate { it.copy(textScale = scale) } }, modifier = Modifier.semantics { contentDescription = textSizeLabel })
            val reduceLabel = stringResource(R.string.ui_reduce_motion)
            ListItem(headlineContent = { Text(reduceLabel) }, trailingContent = {
                Switch(settings.reduceMotion, { checked -> onUpdate { it.copy(reduceMotion = checked) } }, Modifier.semantics { contentDescription = reduceLabel })
            })
        }
        AppCard {
            SectionTitle(stringResource(R.string.ui_notifications))
            ChoiceRow(stringResource(R.string.ui_notification_mode), stringResource(settings.alertMode.labelRes()), { dialog = "alerts" }, Modifier.testTag("setting_alert_mode"))
            if (settings.alertMode != AlertMode.OFF) {
                if (!state.capabilities.notifications) {
                    InfoCard(stringResource(R.string.ui_notifications_denied))
                    OutlinedButton(onNotifications, Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_grant_notifications)) }
                }
                InfoCard(stringResource(if (state.capabilities.exactAlarms) R.string.ui_exact_enabled else R.string.ui_exact_missing))
                if (!state.capabilities.exactAlarms) OutlinedButton(onExact, Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_grant_exact)) }
                SectionTitle(stringResource(R.string.ui_per_prayer))
                PrayerId.obligatory.forEach { prayer ->
                    ChoiceRow(stringResource(prayer.labelRes()), stringResource(settings.alertFor(prayer).labelRes()), { dialog = "alert_${prayer.name}" })
                }
                if (settings.alertMode == AlertMode.ADHAN && !state.capabilities.fullScreen) {
                    SupportingText(stringResource(R.string.ui_full_screen_body))
                    TextButton(onFullScreen) { Text(stringResource(R.string.ui_full_screen)) }
                }
            }
            OutlinedButton({ if (previewState.playing) preview.stop() else preview.toggle(R.raw.dhan_sound) }, Modifier.fillMaxWidth()) {
                Text(stringResource(if (previewState.playing) R.string.ui_stop_audio else R.string.ui_test_adhan))
            }
            SupportingText(stringResource(R.string.ui_notification_preview))
        }
        AppCard {
            SectionTitle(stringResource(R.string.ui_calculation))
            ChoiceRow(stringResource(R.string.ui_calculation_method), stringResource(settings.calculationPreset.labelRes()), { dialog = "calculation" }, Modifier.testTag("setting_calculation"))
            ChoiceRow(stringResource(R.string.ui_asr_convention), stringResource(if (settings.asrConvention == AsrConvention.HANAFI) R.string.ui_asr_hanafi else R.string.ui_asr_standard), { dialog = "asr" })
            SupportingText(stringResource(R.string.ui_calculation_note))
        }
        OutlinedButton(onAbout, Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_about)) }
    }
    val dismiss = { dialog = null }
    when (val active = dialog) {
        "language" -> ChoiceDialog(stringResource(R.string.ui_choose_language), AppLanguage.entries, settings.language,
            { it.nativeName }, { language -> onUpdate { it.copy(language = language) } }, dismiss)
        "school" -> ChoiceDialog(stringResource(R.string.ui_choose_school), School.entries.filter { it.contentAvailable }, settings.school,
            { stringResource(it.labelRes()) }, { school -> onUpdate { it.withSchool(school) } }, dismiss)
        "theme" -> ChoiceDialog(stringResource(R.string.ui_theme), ThemeMode.entries, settings.theme,
            { stringResource(when (it) { ThemeMode.SYSTEM -> R.string.ui_theme_system; ThemeMode.LIGHT -> R.string.ui_theme_light; ThemeMode.DARK -> R.string.ui_theme_dark }) },
            { theme -> onUpdate { it.copy(theme = theme) } }, dismiss)
        "alerts" -> ChoiceDialog(stringResource(R.string.ui_notification_mode), AlertMode.entries, settings.alertMode,
            { stringResource(it.labelRes()) }, onAlertMode, dismiss)
        "calculation" -> ChoiceDialog(stringResource(R.string.ui_calculation_method), CalculationPreset.entries, settings.calculationPreset,
            { stringResource(it.labelRes()) }, { method -> onUpdate { it.copy(calculationPreset = method) } }, dismiss)
        "asr" -> ChoiceDialog(stringResource(R.string.ui_asr_convention), AsrConvention.entries, settings.asrConvention,
            { stringResource(if (it == AsrConvention.HANAFI) R.string.ui_asr_hanafi else R.string.ui_asr_standard) }, { asr -> onUpdate { it.copy(asrConvention = asr) } }, dismiss)
        else -> if (active?.startsWith("alert_") == true) {
            val prayer = PrayerId.valueOf(active.removePrefix("alert_"))
            ChoiceDialog(stringResource(prayer.labelRes()), AlertMode.entries, settings.alertFor(prayer), { stringResource(it.labelRes()) },
                { mode -> onUpdate { it.copy(prayerAlerts = it.prayerAlerts + (prayer to mode)) } }, dismiss)
        }
    }
}
