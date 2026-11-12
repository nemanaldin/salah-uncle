package com.androidhive.myapplication.ui.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.androidhive.myapplication.data.content.visibleFor
import com.androidhive.myapplication.data.content.searchLessons
import com.androidhive.myapplication.data.content.LessonSearchHit
import com.androidhive.myapplication.data.content.sectionHeading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import com.androidhive.myapplication.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonsScreen(state: SalahUiState, onLesson: (String) -> Unit, onBookmark: (String) -> Unit, onTraining: () -> Unit, onRetry: () -> Unit, initialCategory: String = "all", onSection: (String, String) -> Unit = { id, _ -> onLesson(id) },
    onSearchHit: (LessonSearchHit) -> Unit = { onSection(it.lessonId, it.sectionId) },
    onSavedSection: (com.androidhive.myapplication.core.model.SectionBookmark) -> Unit = { onSection(it.lessonId, it.sectionId) }) {
    val settings = state.settings ?: return
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable(initialCategory) { mutableStateOf(initialCategory) }
    val categories = listOf("all" to R.string.ui_all_lessons, "basics" to R.string.ui_basics,
        "prayers" to R.string.ui_prayers, "remembrance" to R.string.ui_remembrance, "saved" to R.string.ui_bookmarks)
    val filtered = remember(state.content.lessons, settings.school, settings.language, settings.bookmarks, query, category) {
        state.content.lessons.filter { lesson ->
            lesson.visibleFor(settings) &&
                (category == "all" || category == lesson.category || category == "saved" && lesson.id in settings.bookmarks) &&
                (query.isBlank() || lesson.title(settings.language).contains(query, true) || lesson.titleFa.contains(query, true) || lesson.titleEn.contains(query, true))
        }
    }
    var hits by remember { mutableStateOf(emptyList<LessonSearchHit>()) }
    var searchedFor by remember { mutableStateOf<Triple<String, com.androidhive.myapplication.core.model.School, com.androidhive.myapplication.core.model.AppLanguage>?>(null) }
    LaunchedEffect(query, state.content.lessons, settings.school, settings.language) {
        if (query.isBlank()) hits = emptyList() else {
            delay(150)
            val results = withContext(Dispatchers.Default) { searchLessons(state.content.lessons, settings, query).take(100).toList() }
            hits = results
            searchedFor = Triple(query, settings.school, settings.language)
        }
    }
    val currentHits = hits.takeIf { searchedFor == Triple(query, settings.school, settings.language) }.orEmpty()
    val savedSections = settings.sectionBookmarks.filter { it.school == settings.school && it.language == settings.language }
    LazyColumn(Modifier.fillMaxSize().testTag("lesson_list"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { IllustratedHeading(stringResource(R.string.ui_lessons), R.drawable.heritage_prayer) }
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
        state.content.lessonsError?.let { error -> item {
            InfoCard(stringResource(error)); TextButton(onRetry) { Text(stringResource(R.string.ui_retry)) }
        } }
        if (state.content.lessonsLoading) item { CircularProgressIndicator() }
        if (!state.content.lessonsLoading && state.content.lessonsError == null && filtered.isEmpty() && (query.isBlank() || currentHits.isEmpty()) && (category != "saved" || savedSections.isEmpty())) item { SupportingText(stringResource(if (category == "saved") R.string.ui_empty_bookmarks else R.string.ui_empty_lessons)) }
        if (query.isNotBlank()) items(currentHits, key = { "hit:${it.lessonId}:${it.sectionId}" }) { hit ->
            AppCard(modifier = Modifier.testTag("search_${hit.lessonId}_${hit.sectionId}"), onClick = { onSearchHit(hit) }) {
                SectionTitle(hit.lessonTitle); SupportingText(hit.sectionTitle); Text(hit.excerpt)
            }
        }
        if (query.isBlank() && category == "saved") items(savedSections, key = { "saved:${it.lessonId}:${it.sectionId}" }) { bookmark ->
            val lesson = state.content.lessons.firstOrNull { it.id == bookmark.lessonId }
            val step = lesson?.variant(settings.school, settings.language)?.steps?.firstOrNull { it.id == bookmark.sectionId }
            if (lesson != null && step != null) AppCard(onClick = { onSavedSection(bookmark) }) {
                SectionTitle(lesson.title(settings.language)); SupportingText(sectionHeading(step))
                if (bookmark.note.isNotBlank()) Text(bookmark.note)
            }
        }
        items(if (query.isBlank()) filtered else emptyList(), key = { it.id }) { lesson ->
            AppCard(modifier = Modifier.testTag("lesson_${lesson.id}"), onClick = { onLesson(lesson.id) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TopicArtwork(lesson.id, Modifier.size(64.dp))
                    Text(lesson.title(settings.language), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    val saved = lesson.id in settings.bookmarks
                    IconButton({ onBookmark(lesson.id) }, Modifier.testTag("bookmark_${lesson.id}")) {
                        Icon(Icons.Default.Star, stringResource(if (saved) R.string.ui_bookmark_remove else R.string.ui_bookmark_add),
                            tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    }
                }
                if (lesson.variant(settings.school, settings.language) == null) SupportingText(stringResource(R.string.ui_translation_unavailable))
            }
        }
    }
}
