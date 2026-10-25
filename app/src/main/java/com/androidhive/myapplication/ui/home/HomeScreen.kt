package com.androidhive.myapplication.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahUiState
import com.androidhive.myapplication.core.labelRes
import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.ui.components.*
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun formatPrayerTime(time: Instant, settings: AppSettings): String = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag(settings.language.tag))
    .withZone(settings.location?.zone ?: java.time.ZoneId.systemDefault()).format(time)

fun countdown(now: Instant, next: Instant): String {
    val seconds = Duration.between(now, next).seconds.coerceAtLeast(0)
    return String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
}

private data class Shortcut(val id: String, val title: String, val action: () -> Unit)

@Composable
fun HomeScreen(state: SalahUiState, onTimes: () -> Unit, onLesson: (String) -> Unit, onLessons: () -> Unit,
    onTraining: () -> Unit, onQibla: () -> Unit, onPrayerTypes: () -> Unit) {
    val settings = state.settings ?: return
    val available = state.content.lessons.filter { it.variant(settings.school) != null }
    val last = available.firstOrNull { it.id == settings.lastLessonId }
    PageColumn(Modifier.testTag("home_screen")) {
        PhotoPanel(Modifier.testTag("home_hero")) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.ui_next_prayer), Modifier.weight(1f), color = Color(0xFFCCE9DB), style = MaterialTheme.typography.labelLarge)
                Text(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.forLanguageTag(settings.language.tag))
                    .withZone(settings.location?.zone ?: java.time.ZoneId.systemDefault()).format(state.now),
                    color = Color(0xFFE9D3A7), style = MaterialTheme.typography.labelMedium)
            }
            if (state.next != null) {
                Text("${stringResource(state.next.id.labelRes())} · ${formatPrayerTime(state.next.time, settings)}", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(countdown(state.now, state.next.time), color = Color(0xFFF5E3B8),
                        style = MaterialTheme.typography.headlineMedium.copy(textDirection = TextDirection.Ltr), modifier = Modifier.testTag("prayer_countdown"))
                    TextButton(onTimes, Modifier.weight(1f).testTag("home_all_times")) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(settings.location?.label?.ifBlank { stringResource(R.string.ui_device_location) }.orEmpty(),
                                color = Color(0xFFD2E8DF), style = MaterialTheme.typography.bodySmall)
                            Text(stringResource(R.string.ui_prayer_times), color = Color.White)
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.ui_location_needed), color = Color.White, style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onTimes, Modifier.testTag("home_set_location")) { Text(stringResource(R.string.ui_set_location)) }
            }
        }
        state.today?.let { today ->
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                items(today.moments.filter { it.id.canAlert }, key = { it.id }) { moment ->
                    val active = moment == state.next
                    Surface(shape = RoundedCornerShape(16.dp), color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Column(Modifier.widthIn(min = 68.dp).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(moment.id.labelRes()), style = MaterialTheme.typography.labelMedium)
                            Text(formatPrayerTime(moment.time, settings), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
        SectionTitle(stringResource(R.string.ui_quick_access))
        val shortcuts = buildList {
            add(Shortcut("prayer", stringResource(R.string.ui_teach_prayer)) { onLesson(settings.school.guideId) })
            add(Shortcut("wudu", stringResource(R.string.ui_teach_wudu)) { onLesson("purification-${settings.school.name.lowercase(Locale.ROOT)}") })
            listOf("ghusl", "tayammum", "sajdah-sahw").forEach { id ->
                available.firstOrNull { it.id == id }?.let { lesson ->
                    add(Shortcut(id, lesson.title(settings.language)) { onLesson(id) })
                }
            }
            add(Shortcut("types", stringResource(R.string.ui_prayer_types), onPrayerTypes))
            add(Shortcut("wudu-duas", stringResource(R.string.ui_wudu_duas)) { onLesson("wudu-remembrance") })
            add(Shortcut("qibla", stringResource(R.string.ui_qibla), onQibla))
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 600.dp) 3 else if (maxWidth < 240.dp) 1 else 2
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                shortcuts.chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { shortcut ->
                            Card(onClick = shortcut.action, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.weight(1f).heightIn(min = 128.dp).testTag("home_${shortcut.id}"),
                                shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val art = when (shortcut.id) {
                                        "prayer", "types" -> R.drawable.heritage_prayer
                                        "wudu" -> R.drawable.heritage_wudu
                                        "wudu-duas" -> R.drawable.heritage_dua
                                        "qibla" -> R.drawable.heritage_qibla
                                        else -> lessonArtwork(shortcut.id)
                                    }
                                    Image(painterResource(art), null, Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Fit)
                                    Text(shortcut.title, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        OutlinedButton(onLessons, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(stringResource(R.string.ui_all_lessons)) }
        if (last != null) AppCard(modifier = Modifier.testTag("home_continue"), onClick = { onLesson(last.id) }) {
            Text(stringResource(R.string.ui_resume_lesson), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(last.title(settings.language), style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(onTraining, Modifier.fillMaxWidth().testTag("home_training")) {
            Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.ui_training))
        }
        state.error?.let { InfoCard(stringResource(it)) }
    }
}
