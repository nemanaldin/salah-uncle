package com.androidhive.myapplication.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahUiState
import com.androidhive.myapplication.core.labelRes
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.domain.prayer.HomePrayerState
import com.androidhive.myapplication.domain.prayer.homePrayerState
import com.androidhive.myapplication.ui.components.*
import com.androidhive.myapplication.ui.theme.SalahPhotoTokens
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun formatPrayerTime(time: Instant, settings: AppSettings): String = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag(settings.language.tag))
    .withZone(settings.location?.zone ?: java.time.ZoneId.systemDefault()).format(time)

@Composable
fun HomeScreen(state: SalahUiState, onNavigate: (String) -> Unit, onRetrySettings: () -> Unit = {}) {
    val settings = state.settings ?: return
    val onTimes = { onNavigate("times") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp).padding(bottom = 16.dp).testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            PrayerHero(state, settings, onTimes, onRetrySettings)
            TodayPrayerStrip(state, settings, onTimes)
        }
        Card(Modifier.fillMaxWidth().testTag("home_quick_access"), shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.ui_quick_access), style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                val shortcuts = listOf(
                    QuickAccessItem("prayer", stringResource(R.string.ui_teach_prayer)) { onNavigate("lesson/${settings.school.guideId}") },
                    QuickAccessItem("wudu", stringResource(R.string.ui_teach_wudu)) { onNavigate("lesson/purification-${settings.school.name.lowercase(Locale.ROOT)}") },
                    QuickAccessItem("ghusl", stringResource(R.string.ui_home_ghusl)) { onNavigate("lesson/ghusl") },
                    QuickAccessItem("sajdah-sahw", stringResource(R.string.ui_home_sujud)) { onNavigate("lesson/sajdah-sahw") },
                    QuickAccessItem("ramadan", stringResource(R.string.ui_ramadan)) { onNavigate("ramadan") },
                    QuickAccessItem("qibla", stringResource(R.string.ui_qibla)) { onNavigate("qibla") },
                )
                QuickAccessGrid(shortcuts) { id, modifier -> HomeShortcutArtwork(id, modifier) }
            }
        }
        state.error?.let { InfoCard(stringResource(it)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrayerHero(state: SalahUiState, settings: AppSettings, onTimes: () -> Unit, onRetrySettings: () -> Unit) {
    PhotoPanel(Modifier.heightIn(min = 194.dp).testTag("home_hero"), shape = RectangleShape) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.ui_next_prayer), Modifier.weight(1f), color = SalahPhotoTokens.support, style = MaterialTheme.typography.bodyMedium)
            if (state.now != Instant.EPOCH) Text(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(Locale.forLanguageTag(settings.language.tag)).withZone(settings.location?.zone ?: java.time.ZoneId.systemDefault()).format(state.now),
                color = SalahPhotoTokens.date, style = MaterialTheme.typography.bodyMedium)
        }
        if (state.next != null) {
            Spacer(Modifier.height(8.dp))
            Text("${stringResource(state.next.id.labelRes())} · ${formatPrayerTime(state.next.time, settings)}", Modifier.testTag("home_next_prayer"),
                color = SalahPhotoTokens.onPhoto, style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp, lineHeight = 50.sp))
            Spacer(Modifier.height(8.dp))
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(remainingPrayerLabel(state.now, state.next.time), Modifier.padding(end = 12.dp).testTag("prayer_countdown"),
                    color = SalahPhotoTokens.countdown, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                Text(settings.location?.labelFor(settings.language)?.ifBlank { stringResource(R.string.ui_device_location) }.orEmpty(),
                    Modifier.testTag("home_location"), color = SalahPhotoTokens.location, style = MaterialTheme.typography.bodyMedium)
            }
        } else when (homePrayerState(settings.location != null, false, state.calculationFailed, state.settingsReadFailed)) {
            HomePrayerState.LOCATION_NOT_SET -> {
                Text(stringResource(R.string.ui_location_needed), color = SalahPhotoTokens.onPhoto, style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onTimes, Modifier.testTag("home_set_location")) { Text(stringResource(R.string.ui_set_location)) }
            }
            HomePrayerState.LOADING_FAILED -> {
                Text(stringResource(R.string.ui_saved_data_unavailable), color = SalahPhotoTokens.onPhoto)
                FilledTonalButton(onRetrySettings) { Text(stringResource(R.string.ui_retry)) }
            }
            HomePrayerState.LOADING -> CircularProgressIndicator(color = SalahPhotoTokens.onPhoto)
            else -> {
                Text(stringResource(R.string.ui_error_calculation), color = SalahPhotoTokens.onPhoto)
                FilledTonalButton(onTimes) { Text(stringResource(R.string.ui_prayer_times)) }
            }
        }
    }
}

@Composable
private fun remainingPrayerLabel(now: Instant, next: Instant): String {
    val remaining = remainingPrayerTime(now, next)
    if (remaining.lessThanMinute) return stringResource(R.string.ui_prayer_in_less_than_minute)
    val hours = if (remaining.hours > 0) pluralStringResource(R.plurals.ui_remaining_hours,
        remaining.hours.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), remaining.hours) else null
    val minutes = if (remaining.minutes > 0) pluralStringResource(R.plurals.ui_remaining_minutes, remaining.minutes, remaining.minutes) else null
    val duration = when {
        hours != null && minutes != null -> stringResource(R.string.ui_remaining_parts, hours, minutes)
        hours != null -> hours
        else -> minutes.orEmpty()
    }
    return stringResource(R.string.ui_prayer_in_duration, duration)
}

@Composable
private fun TodayPrayerStrip(state: SalahUiState, settings: AppSettings, onTimes: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.ui_today_prayers), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            TextButton(onTimes, Modifier.testTag("home_all_times"), contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(stringResource(R.string.ui_all_times)); Icon(ReferenceIcons.Chevron, null, Modifier.size(18.dp))
            }
        }
        val fontScale = LocalDensity.current.fontScale
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val width = ((maxWidth - 24.dp) / 5).coerceAtLeast((58 * fontScale).dp)
            LazyRow(Modifier.testTag("home_prayer_strip"), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(PrayerId.obligatory, key = { it }) { prayer ->
                    val moment = state.today?.moments?.firstOrNull { it.id == prayer }
                    val active = moment != null && moment == state.next
                    val unavailable = stringResource(R.string.ui_time_unavailable)
                    Card(onTimes, Modifier.width(width).testTag("home_time_${prayer.name}").semantics { selected = active }, shape = MaterialTheme.shapes.large,
                        border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary.copy(alpha = .2f) else MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.fillMaxWidth().heightIn(min = 90.dp).padding(horizontal = 2.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            PrayerArtwork(prayer.artwork(), Modifier.size(30.dp))
                            Text(stringResource(prayer.labelRes()), style = MaterialTheme.typography.labelMedium)
                            Text(moment?.let { formatPrayerTime(it.time, settings) } ?: "—",
                                Modifier.semantics { if (moment == null) contentDescription = unavailable }, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
