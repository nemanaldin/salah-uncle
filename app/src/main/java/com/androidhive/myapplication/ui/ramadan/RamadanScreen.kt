package com.androidhive.myapplication.ui.ramadan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.androidhive.myapplication.R
import com.androidhive.myapplication.RamadanContent
import com.androidhive.myapplication.SalahUiState
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.platform.ramadan.SuhurPlayback
import com.androidhive.myapplication.ui.components.*
import com.androidhive.myapplication.ui.home.formatPrayerTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun RamadanScreen(state: SalahUiState, content: RamadanContent, playback: SuhurPlayback,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit, onPreview: () -> Unit, onStop: () -> Unit, onLocation: () -> Unit,
    onNotifications: () -> Unit, onExact: () -> Unit, onFullScreen: () -> Unit) {
    val leave by rememberUpdatedState(onStop)
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) leave()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); leave() }
    }
    val settings = state.settings ?: return
    val location = settings.location
    if (location == null) {
        PageColumn(Modifier.testTag("ramadan_screen")) {
            SectionTitle(stringResource(R.string.ui_ramadan))
            SupportingText(stringResource(R.string.ui_location_needed))
            Button(onLocation, Modifier.testTag("ramadan_location")) { Text(stringResource(R.string.ui_set_location)) }
        }
        return
    }
    val scroll = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val options = settings.ramadan
    val locale = Locale.forLanguageTag(settings.language.tag)
    val dateFormat = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    val alarmFormat = remember(locale, location.zoneId) { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale).withZone(location.zone) }
    var selected by rememberSaveable(content.calendar?.start) { mutableStateOf<String?>(null) }
    var offsetDialog by rememberSaveable { mutableStateOf(false) }
    var adjustmentDialog by rememberSaveable { mutableStateOf(false) }
    val calendar = content.calendar
    val day = calendar?.days?.firstOrNull { it.date.toString() == selected }
        ?: calendar?.days?.firstOrNull { it.date == state.now.atZone(location.zone).toLocalDate() } ?: calendar?.days?.firstOrNull()
    fun update(transform: (RamadanOptions) -> RamadanOptions) = onUpdate { it.copy(ramadan = transform(it.ramadan)) }
    val canSuhur = state.capabilities.exactAlarms && state.capabilities.ramadanNotifications
    val canIftar = state.capabilities.exactAlarms && state.capabilities.iftarNotifications
    val missingNotifications = options.suhurEnabled && !state.capabilities.ramadanNotifications || options.iftarEnabled && !state.capabilities.iftarNotifications
    LazyColumn(Modifier.fillMaxSize().testTag("ramadan_screen"), state = scroll, contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            TopicHeading(stringResource(R.string.ui_ramadan), "tarawih")
            SupportingText(location.labelFor(settings.language).ifBlank { stringResource(R.string.ui_device_location) })
            SupportingText(stringResource(R.string.ui_ramadan_intro))
            SupportingText(stringResource(R.string.ui_calendar_estimate_short))
        }
        if (content.loading) item { CircularProgressIndicator() }
        content.error?.let { error -> item { InfoCard(stringResource(error)); TextButton(onLocation) { Text(stringResource(R.string.ui_set_location)) } } }
        day?.let { selectedDay -> item {
            AppCard {
                Text(dateFormat.format(selectedDay.date), style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PrayerArtwork(PrayerSymbol.FAJR, Modifier.size(48.dp))
                        SupportingText(stringResource(R.string.ui_suhur_end))
                        Text(formatPrayerTime(selectedDay.fajr, settings), Modifier.testTag("ramadan_fajr"), style = MaterialTheme.typography.headlineSmall)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PrayerArtwork(PrayerSymbol.MAGHRIB, Modifier.size(48.dp))
                        SupportingText(stringResource(R.string.ui_iftar))
                        Text(formatPrayerTime(selectedDay.iftar, settings), Modifier.testTag("ramadan_iftar"), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        } }
        item {
            AppCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.ui_suhur_alarm), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Switch(options.suhurEnabled, { enabled ->
                        update { it.copy(suhurEnabled = enabled) }
                        if (enabled) { if (!state.capabilities.ramadanNotifications) onNotifications() else if (!state.capabilities.exactAlarms) onExact() }
                    }, Modifier.testTag("suhur_enabled"))
                }
                SupportingText(stringResource(R.string.ui_suhur_alarm_body))
                ChoiceRow(stringResource(R.string.ui_alarm_offset_title), stringResource(R.string.ui_minutes_before_fajr, options.minutesBeforeFajr), { offsetDialog = true }, Modifier.testTag("suhur_offset"))
                if (options.suhurEnabled && canSuhur) content.nextSuhur?.let {
                    SupportingText(stringResource(R.string.ui_next_alarm, alarmFormat.format(it.time)), Modifier.testTag("suhur_next_alarm"))
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.ui_iftar_reminder), Modifier.weight(1f))
                    Switch(options.iftarEnabled, { enabled ->
                        update { it.copy(iftarEnabled = enabled) }
                        if (enabled) { if (!state.capabilities.iftarNotifications) onNotifications() else if (!state.capabilities.exactAlarms) onExact() }
                    }, Modifier.testTag("iftar_enabled"))
                }
                if (options.iftarEnabled && canIftar) content.nextIftar?.let { SupportingText(stringResource(R.string.ui_next_alarm, alarmFormat.format(it.time))) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.ui_only_ramadan), Modifier.weight(1f))
                    Switch(options.onlyRamadan, { enabled -> update { it.copy(onlyRamadan = enabled) } }, Modifier.testTag("ramadan_only"))
                }
                if (options.suhurEnabled && !canSuhur || options.iftarEnabled && !canIftar) {
                    InfoCard(stringResource(R.string.ui_alarm_pending))
                    if (missingNotifications) Button(onNotifications) { Text(stringResource(R.string.ui_enable_alarm_notifications)) }
                    if (!state.capabilities.exactAlarms) OutlinedButton(onExact) { Text(stringResource(R.string.ui_exact_alarm)) }
                }
                Button(onPreview, Modifier.fillMaxWidth().testTag("suhur_preview")) {
                    Text(stringResource(if (playback.active) R.string.ui_stop_alarm else R.string.ui_preview_alarm))
                }
                SupportingText(stringResource(R.string.ui_alarm_preview_note))
                playback.error?.let { InfoCard(stringResource(it)) }
                if (!state.capabilities.fullScreen) TextButton(onFullScreen) { Text(stringResource(R.string.ui_full_screen)) }
                SupportingText(stringResource(R.string.ui_alarm_device_notice))
            }
        }
        item {
            SupportingText(stringResource(R.string.ui_calendar_estimate))
            ChoiceRow(stringResource(R.string.ui_calendar_adjustment), stringResource(R.string.ui_days_adjustment, options.calendarAdjustment), { adjustmentDialog = true }, Modifier.testTag("ramadan_calendar_adjustment"))
        }
        calendar?.let { month ->
            item { SectionTitle(stringResource(R.string.ui_ramadan_period, month.hijriYear, dateFormat.format(month.start), dateFormat.format(month.end))) }
            items(month.days, key = { it.date.toString() }) { value ->
                AppCard(modifier = Modifier.testTag("ramadan_day_${value.dayOfRamadan}"), onClick = { selected = value.date.toString(); scope.launch { if (settings.reduceMotion) scroll.scrollToItem(0) else scroll.animateScrollToItem(0) } }) {
                    Text(stringResource(R.string.ui_ramadan_day, value.dayOfRamadan), color = MaterialTheme.colorScheme.primary)
                    Text(dateFormat.format(value.date), style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.ui_suhur_end)); Text(formatPrayerTime(value.fajr, settings))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.ui_iftar)); Text(formatPrayerTime(value.iftar, settings))
                    }
                }
            }
        }
        item { TextButton(onLocation) { Text(stringResource(R.string.ui_set_location)) } }
    }
    if (offsetDialog) {
        var text by remember { mutableStateOf(options.minutesBeforeFajr.toString()) }
        val minutes = text.toIntOrNull()
        AlertDialog(onDismissRequest = { offsetDialog = false }, title = { Text(stringResource(R.string.ui_alarm_offset_title)) },
            text = { Column {
                val keyboard = LocalSoftwareKeyboardController.current
                OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth().testTag("suhur_minutes"), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }), singleLine = true,
                    isError = minutes == null || minutes !in 5..240)
                SupportingText(stringResource(R.string.ui_alarm_offset_range))
            } }, confirmButton = { TextButton({ update { it.copy(minutesBeforeFajr = minutes!!) }; offsetDialog = false },
                enabled = minutes != null && minutes in 5..240, modifier = Modifier.testTag("suhur_minutes_save")) { Text(stringResource(R.string.ui_save)) } },
            dismissButton = { TextButton({ offsetDialog = false }) { Text(stringResource(R.string.ui_cancel)) } })
    }
    if (adjustmentDialog) ChoiceDialog(stringResource(R.string.ui_calendar_adjustment), (-2..2).toList(), options.calendarAdjustment,
        { stringResource(R.string.ui_days_adjustment, it) }, { value -> update { it.copy(calendarAdjustment = value) } }, { adjustmentDialog = false })
}
