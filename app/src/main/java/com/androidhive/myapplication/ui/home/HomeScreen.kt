package com.androidhive.myapplication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.util.Locale

fun formatPrayerTime(time: Instant, settings: AppSettings): String = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag(settings.language.tag))
    .withZone(settings.location?.zone ?: java.time.ZoneId.systemDefault()).format(time)

fun countdown(now: Instant, next: Instant): String {
    val seconds = Duration.between(now, next).seconds.coerceAtLeast(0)
    return String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
}

private data class Shortcut(val id: String, val title: String, val icon: ImageVector, val action: () -> Unit)

@Composable
fun HomeScreen(state: SalahUiState, onTimes: () -> Unit, onLesson: (String) -> Unit, onLessons: () -> Unit,
    onTraining: () -> Unit, onQibla: () -> Unit, onPrayerTypes: () -> Unit) {
    val settings = state.settings ?: return
    val available = state.content.lessons.filter { it.variant(settings.school) != null }
    val last = available.firstOrNull { it.id == settings.lastLessonId }
    PageColumn(Modifier.testTag("home_screen")) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF144D43), Color(0xFF276B59))), RoundedCornerShape(22.dp))
            .padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.ui_next_prayer), color = Color(0xFFCCE9DB), style = MaterialTheme.typography.titleMedium)
                Icon(Icons.Default.DateRange, null, tint = Color(0xFFCCE9DB))
            }
            if (state.next != null) {
                Text("${stringResource(state.next.id.labelRes())} · ${formatPrayerTime(state.next.time, settings)}", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text(countdown(state.now, state.next.time), color = Color(0xFFF5E3B8),
                    style = MaterialTheme.typography.headlineMedium.copy(textDirection = TextDirection.Ltr), modifier = Modifier.testTag("prayer_countdown"))
                Text(settings.location?.label?.ifBlank { stringResource(R.string.ui_device_location) }.orEmpty(), color = Color(0xFFD2E8DF), style = MaterialTheme.typography.bodyMedium)
                TextButton(onTimes, Modifier.testTag("home_all_times")) { Text(stringResource(R.string.ui_prayer_times), color = Color.White) }
            } else {
                Text(stringResource(R.string.ui_location_needed), color = Color.White, style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onTimes, Modifier.testTag("home_set_location")) { Text(stringResource(R.string.ui_set_location)) }
            }
        }
        SectionTitle(stringResource(R.string.ui_quick_access))
        val shortcuts = buildList {
            add(Shortcut("prayer", stringResource(R.string.ui_teach_prayer), Icons.AutoMirrored.Filled.List) { onLesson(settings.school.guideId) })
            add(Shortcut("wudu", stringResource(R.string.ui_teach_wudu), Icons.Default.Info) { onLesson("purification-${settings.school.name.lowercase(Locale.ROOT)}") })
            listOf("ghusl", "tayammum", "sajdah-sahw").forEach { id ->
                available.firstOrNull { it.id == id }?.let { lesson ->
                    add(Shortcut(id, lesson.title(settings.language), Icons.Default.CheckCircle) { onLesson(id) })
                }
            }
            add(Shortcut("types", stringResource(R.string.ui_prayer_types), Icons.Default.DateRange, onPrayerTypes))
            add(Shortcut("wudu-duas", stringResource(R.string.ui_wudu_duas), Icons.Default.Favorite) { onLesson("wudu-remembrance") })
            add(Shortcut("qibla", stringResource(R.string.ui_qibla), Icons.Default.Place, onQibla))
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 600.dp) 3 else if (maxWidth < 240.dp) 1 else 2
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                shortcuts.chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { shortcut ->
                            Card(onClick = shortcut.action, modifier = Modifier.weight(1f).heightIn(min = 104.dp).testTag("home_${shortcut.id}"),
                                shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(shortcut.icon, null, tint = MaterialTheme.colorScheme.primary)
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
