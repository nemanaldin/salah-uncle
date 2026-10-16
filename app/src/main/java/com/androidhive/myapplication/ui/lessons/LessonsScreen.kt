package com.androidhive.myapplication.ui.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahUiState
import com.androidhive.myapplication.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonsScreen(state: SalahUiState, onLesson: (String) -> Unit, onBookmark: (String) -> Unit, onTraining: () -> Unit, onRetry: () -> Unit) {
    val settings = state.settings ?: return
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("all") }
    val categories = listOf("all" to R.string.ui_all_lessons, "basics" to R.string.ui_basics,
        "prayers" to R.string.ui_prayers, "remembrance" to R.string.ui_remembrance, "saved" to R.string.ui_bookmarks)
    val filtered = remember(state.content.lessons, settings.school, settings.language, settings.bookmarks, query, category) {
        state.content.lessons.filter { lesson ->
            lesson.variant(settings.school) != null &&
                (category == "all" || category == lesson.category || category == "saved" && lesson.id in settings.bookmarks) &&
                (query.isBlank() || lesson.titleFa.contains(query, true) || lesson.titleEn.contains(query, true))
        }
    }
    LazyColumn(Modifier.fillMaxSize().testTag("lesson_list"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().testTag("lesson_search"),
                label = { Text(stringResource(R.string.ui_search_lessons)) }, singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton({ query = "" }) { Icon(Icons.Default.Close, stringResource(R.string.ui_clear_search)) } })
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { (id, label) -> FilterChip(category == id, { category = id }, label = { Text(stringResource(label)) }) }
            }
        }
        item { AppCard(onClick = onTraining) { SectionTitle(stringResource(R.string.ui_training)); SupportingText(stringResource(R.string.ui_training_placeholder)) } }
        state.content.error?.let { error -> item {
            InfoCard(stringResource(error)); TextButton(onRetry) { Text(stringResource(R.string.ui_retry)) }
        } }
        if (filtered.isEmpty()) item { SupportingText(stringResource(if (category == "saved") R.string.ui_empty_bookmarks else R.string.ui_empty_lessons)) }
        items(filtered, key = { it.id }) { lesson ->
            val steps = lesson.variant(settings.school, settings.language)?.steps.orEmpty()
            val completed = steps.count { "${lesson.id}:${it.id}" in settings.completedSteps }
            AppCard(onClick = { onLesson(lesson.id) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(lesson.title(settings.language), Modifier.weight(1f).padding(top = 8.dp), style = MaterialTheme.typography.titleLarge)
                    val saved = lesson.id in settings.bookmarks
                    IconButton({ onBookmark(lesson.id) }) {
                        Icon(Icons.Default.Star, stringResource(if (saved) R.string.ui_bookmark_remove else R.string.ui_bookmark_add),
                            tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    }
                }
                SupportingText(stringResource(R.string.ui_progress, completed, steps.size))
                if (completed > 0) LinearProgressIndicator(progress = { completed.toFloat() / steps.size }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
