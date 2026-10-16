package com.androidhive.myapplication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import java.util.Locale

fun formatPrayerTime(time: Instant, settings: AppSettings): String = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag(settings.language.tag))
    .withZone(settings.location?.zone ?: java.time.ZoneId.systemDefault()).format(time)

fun countdown(now: Instant, next: Instant): String {
    val seconds = Duration.between(now, next).seconds.coerceAtLeast(0)
    return String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60)
}

@Composable
fun HomeScreen(state: SalahUiState, onTimes: () -> Unit, onLesson: (String) -> Unit, onLessons: () -> Unit, onTraining: () -> Unit) {
    val settings = state.settings ?: return
    val available = state.content.lessons.filter { it.variant(settings.school) != null }
    val last = available.firstOrNull { it.id == settings.lastLessonId }
    PageColumn {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF144D43), Color(0xFF276B59))), RoundedCornerShape(28.dp))
            .padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.ui_next_prayer), color = Color(0xFFCCE9DB), style = MaterialTheme.typography.titleMedium)
            if (state.next != null) {
                Text("${stringResource(state.next.id.labelRes())} · ${formatPrayerTime(state.next.time, settings)}", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text(countdown(state.now, state.next.time), color = Color(0xFFF5E3B8),
                    style = MaterialTheme.typography.headlineLarge.copy(textDirection = TextDirection.Ltr), modifier = Modifier.testTag("prayer_countdown"))
                Text(settings.location?.label?.ifBlank { stringResource(R.string.ui_device_location) }.orEmpty(), color = Color(0xFFD2E8DF))
                TextButton(onTimes) { Text(stringResource(R.string.ui_prayer_times), color = Color.White) }
            } else {
                Text(stringResource(R.string.ui_location_needed), color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.ui_location_optional), color = Color(0xFFD2E8DF), style = MaterialTheme.typography.bodyMedium)
                FilledTonalButton(onTimes, Modifier.testTag("home_set_location")) { Text(stringResource(R.string.ui_set_location)) }
            }
        }
        SectionTitle(stringResource(R.string.ui_continue_learning))
        AppCard(modifier = Modifier.testTag("home_continue"), onClick = { onLesson(last?.id ?: settings.school.guideId) }) {
            Text(last?.title(settings.language) ?: stringResource(R.string.ui_start_learning), style = MaterialTheme.typography.titleLarge)
            if (last != null) {
                val steps = last.variant(settings.school, settings.language)?.steps.orEmpty()
                val count = steps.count { "${last.id}:${it.id}" in settings.completedSteps }
                LinearProgressIndicator(progress = { if (steps.isEmpty()) 0f else count.toFloat() / steps.size }, modifier = Modifier.fillMaxWidth())
                SupportingText(stringResource(R.string.ui_progress, count, steps.size))
            } else SupportingText(stringResource(R.string.ui_start_learning_body))
            Text(stringResource(if (last == null) R.string.ui_read_lesson else R.string.ui_resume_lesson), color = MaterialTheme.colorScheme.primary)
        }
        AppCard(onClick = onTraining) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.ui_training), style = MaterialTheme.typography.titleMedium)
            }
            SupportingText(stringResource(R.string.ui_training_placeholder))
        }
        OutlinedButton(onLessons, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(stringResource(R.string.ui_lessons_count, available.size)) }
        state.error?.let { InfoCard(stringResource(it)) }
    }
}
