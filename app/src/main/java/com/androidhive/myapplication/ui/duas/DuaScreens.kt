package com.androidhive.myapplication.ui.duas

import androidx.core.net.toUri
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.*
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.duas.*
import com.androidhive.myapplication.platform.openUrl
import com.androidhive.myapplication.platform.shareText
import com.androidhive.myapplication.ui.components.*
import com.androidhive.myapplication.ui.theme.ReadingText
import com.androidhive.myapplication.ui.theme.readingTextStyle
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun DuaContent(catalog: DuaCatalog?, loading: Boolean, error: Int?, retry: () -> Unit, content: @Composable (DuaCatalog) -> Unit) {
    if (catalog != null) content(catalog)
    else PageColumn {
        if (loading) CircularProgressIndicator() else {
            InfoCard(stringResource(error ?: R.string.ui_dua_empty))
            TextButton(retry) { Text(stringResource(R.string.ui_retry)) }
        }
    }
}

/** New cards reuse the current theme; the missing reference image is not guessed. */
@Composable
fun DuaLibraryScreen(catalog: DuaCatalog, settings: AppSettings, onCategory: (String) -> Unit,
    onRead: (String, String) -> Unit, onSaved: () -> Unit, onRecent: () -> Unit, onReminders: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val tag = settings.language.tag
    val results = remember(query, catalog) { catalog.items.filter { it.matches(query) } }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().testTag("dua_search"),
            label = { Text(stringResource(R.string.ui_dua_search)) }, singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onSaved, Modifier.weight(1f).testTag("dua_saved")) { Icon(Icons.Default.Star, null); Text(stringResource(R.string.ui_bookmarks)) }
            OutlinedButton(onRecent, Modifier.weight(1f).testTag("dua_recent")) { Icon(Icons.Default.Refresh, null); Text(stringResource(R.string.ui_dua_recent)) }
        }
        LazyColumn(Modifier.weight(1f).testTag("dua_categories"), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            item { LanguageReadinessNotice(settings.language) }
            if (query.isBlank()) {
                items(catalog.categories, key = { it.id }) { category ->
                    val ids = catalog.sections.filter { it.category == category.id }.flatMap { it.duaIds }.distinct()
                    AppCard(Modifier.testTag("dua_category_${category.id}"), { onCategory(category.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(52.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(when (category.id) {
                                    "inside" -> Icons.Default.Info; "around" -> Icons.Default.Home; "daily" -> Icons.Default.Face
                                    "week" -> Icons.Default.DateRange; "dhikr" -> Icons.Default.FavoriteBorder
                                    "occasions" -> Icons.Default.Star; else -> Icons.Default.Favorite
                                }, null) }
                            }
                            Column(Modifier.weight(1f)) { Text(category.title(tag), style = MaterialTheme.typography.titleMedium)
                                SupportingText(NumberFormat.getIntegerInstance(Locale.forLanguageTag(tag)).format(ids.size)) }
                        }
                    }
                }
                item { SupportingText(stringResource(R.string.ui_dua_pending_count)) }
                item { OutlinedButton(onReminders, Modifier.fillMaxWidth().testTag("dua_reminders_link")) { Text(stringResource(R.string.ui_dua_reminders)) } }
            } else {
                if (results.isEmpty()) item { InfoCard(stringResource(R.string.ui_dua_empty)) }
                items(results, key = { it.id }) { dua -> DuaTitleRow(dua, tag) { onRead(dua.section, dua.id) } }
            }
        }
    }
}

@Composable
private fun DuaTitleRow(item: DuaItem, tag: String, onClick: () -> Unit) {
    // Only titles at the browsing level: no long Arabic/translation text on the category page.
    ListItem(headlineContent = { Text(item.title(tag)) }, modifier = Modifier.fillMaxWidth().testTag("dua_title_${item.id}")
        .clickable(role = Role.Button, onClick = onClick), colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface))
}

@Composable
fun DuaCategoryScreen(catalog: DuaCatalog, categoryId: String, settings: AppSettings,
    onRead: (String, String) -> Unit, onBack: () -> Unit) {
    val tag = settings.language.tag
    val category = catalog.categories.firstOrNull { it.id == categoryId } ?: return
    val sections = catalog.sections.filter { it.category == categoryId }
    var expanded by rememberSaveable(categoryId) { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp).testTag("dua_category_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { TextButton(onBack) { Text("${stringResource(R.string.ui_duas)} > ${category.title(tag)}") } }
        item { LanguageReadinessNotice(settings.language) }
        if (sections.any { tag !in it.titles }) item { InfoCard(stringResource(R.string.ui_dua_title_fallback)) }
        if (categoryId == "week") item {
            // Civil date in the device zone; not a made-up special formula for each day.
            val today = catalog.section(todayDuaSection(LocalDate.now().dayOfWeek))!!
            AppCard(Modifier.testTag("duas_today"), { expanded = today.id }) {
                SectionTitle(stringResource(R.string.ui_dua_today)); SupportingText(today.title(tag))
            }
        }
        sections.forEach { section ->
            item(section.id) { AppCard {
                val open = expanded == section.id
                ListItem(headlineContent = { Text(section.title(tag)) }, trailingContent = { Icon(if (open) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null) },
                    modifier = Modifier.fillMaxWidth().testTag("dua_section_${section.id}").clickable(role = Role.Button) { expanded = if (open) "" else section.id }
                        .semantics { stateDescription = if (open) "−" else "+" },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface))
                if (open) catalog.itemsIn(section.id).forEach { dua -> DuaTitleRow(dua, tag) { onRead(section.id, dua.id) } }
            } }
        }
    }
}

@Composable
fun DuaSavedScreen(catalog: DuaCatalog, settings: AppSettings, recent: Boolean, onRead: (String, String) -> Unit) {
    val ids = if (recent) settings.duaRecent else catalog.items.map { it.id }.filter { it in settings.duaBookmarks }
    val items = ids.mapNotNull(catalog::item)
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { SectionTitle(stringResource(if (recent) R.string.ui_dua_recent else R.string.ui_bookmarks)) }
        if (items.isEmpty()) item { InfoCard(stringResource(R.string.ui_dua_empty)) }
        items(items, key = { it.id }) { dua -> DuaTitleRow(dua, settings.language.tag) { onRead(dua.section, dua.id) } }
    }
}

@Composable
fun DuaReaderScreen(catalog: DuaCatalog, sectionId: String, initialDuaId: String, settings: AppSettings,
    onView: (String) -> Unit, onBookmark: (String) -> Unit, onSize: (Float) -> Unit,
    onSection: (String) -> Unit, onError: (Int) -> Unit) {
    val entries = remember(catalog, sectionId) { catalog.itemsIn(sectionId) }
    if (entries.isEmpty()) { PageColumn { InfoCard(stringResource(R.string.ui_dua_empty)) }; return }
    val section = catalog.section(sectionId) ?: return
    val pager = key(sectionId, initialDuaId) {
        rememberPagerState(initialPage = entries.indexOfFirst { it.id == initialDuaId }.coerceAtLeast(0), pageCount = { entries.size })
    }
    val scope = rememberCoroutineScope()
    val currentOnView by rememberUpdatedState(onView)
    LaunchedEffect(pager.settledPage, sectionId) { currentOnView(entries[pager.settledPage].id) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TextButton({ onSection(section.category) }, Modifier.testTag("dua_breadcrumb")) {
            Text("${stringResource(R.string.ui_duas)} > ${section.title(settings.language.tag)}")
        }
        HorizontalPager(pager, Modifier.fillMaxWidth().weight(1f).testTag("dua_reader_pager"), key = { entries[it].id }) { index ->
            val item = entries[index]
            // Off-screen pager pages must not allocate or play audio.
            DuaDetail(item, settings, item.id in settings.duaBookmarks, { onBookmark(item.id) }, onSize, onError,
                active = pager.settledPage == index)
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton({ scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }, Modifier.weight(1f).testTag("dua_previous"), enabled = pager.currentPage > 0) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.ui_back)) }
            Text(NumberFormat.getIntegerInstance(Locale.forLanguageTag(settings.language.tag)).let { "${it.format(pager.currentPage + 1)} / ${it.format(entries.size)}" }, Modifier.testTag("dua_position"))
            OutlinedButton({ scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }, Modifier.weight(1f).testTag("dua_next"), enabled = pager.currentPage < entries.lastIndex) { Text(stringResource(R.string.ui_slide_next)) }
        }
    }
}

@Composable
private fun DuaDetail(item: DuaItem, settings: AppSettings, saved: Boolean, onBookmark: () -> Unit,
    onSize: (Float) -> Unit, onError: (Int) -> Unit, active: Boolean) {
    val context = LocalContext.current
    val tag = settings.language.tag
    val meaning = item.translation(tag)
    val sourceLabel = stringResource(R.string.ui_dua_source)
    val review = stringResource(R.string.ui_dua_review)
    val shareLabel = stringResource(R.string.ui_dua_share)
    val export = remember(item, tag, review) { listOf(item.title(tag), item.arabic, meaning,
        item.sources.joinToString("\n") { "${it.book} ${it.reference} ${it.url}" }, review).filter { it.isNotBlank() }.joinToString("\n\n") }
    var scale by remember(settings.arabicTextScale) { mutableFloatStateOf(settings.arabicTextScale) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 10.dp).testTag("dua_detail_${item.id}"), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.title(tag), Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
            IconButton(onBookmark, Modifier.testTag("dua_bookmark_${item.id}").semantics { selected = saved }) {
                Icon(Icons.Default.Star, stringResource(if (saved) R.string.ui_bookmark_remove else R.string.ui_bookmark_add), tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            }
        }
        LanguageReadinessNotice(settings.language)
        if (tag !in item.titles) SupportingText(stringResource(R.string.ui_dua_title_fallback))
        if (item.reviewFields.isNotEmpty()) InfoCard(review)
        AppCard {
            SectionTitle(stringResource(R.string.ui_dua_arabic))
            if (item.arabic.isBlank()) SupportingText(stringResource(R.string.ui_dua_missing))
            else {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { SelectionContainer {
                    Text(item.arabic, Modifier.fillMaxWidth().testTag("dua_arabic_${item.id}"),
                        style = readingTextStyle(settings, AppLanguage.ARABIC).copy(fontSize = (28 * scale * settings.textScale).sp,
                            lineHeight = (48 * scale * settings.textScale).sp, textDirection = TextDirection.Rtl))
                } }
                val sizeLabel = stringResource(R.string.ui_arabic_text_size)
                Text(sizeLabel)
                Slider(scale, { scale = it }, valueRange = .85f..2f, onValueChangeFinished = { onSize(readingArabicScale(scale)) },
                    modifier = Modifier.testTag("dua_arabic_size").semantics { contentDescription = sizeLabel })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(item.title(tag), item.arabic)) }, Modifier.weight(1f).testTag("dua_copy")) { Text(stringResource(R.string.ui_dua_copy)) }
                    OutlinedButton({ shareText(context, export, shareLabel) { onError(R.string.ui_error_external_app) } }, Modifier.weight(1f).testTag("dua_share")) { Text(shareLabel) }
                }
            }
        }
        AppCard {
            SectionTitle(stringResource(R.string.ui_dua_pronunciation))
            val lines = item.pronunciations[tag].orEmpty()
            if (lines.isEmpty()) SupportingText(stringResource(R.string.ui_dua_missing))
            else lines.forEach { ReadingText(it, settings, settings.language) }
            SectionTitle(stringResource(R.string.ui_dua_meaning))
            if (meaning.isBlank()) SupportingText(stringResource(R.string.ui_dua_translation_missing))
            else { ReadingText(meaning, settings, settings.language); SupportingText(stringResource(R.string.ui_dua_translation_draft)) }
        }
        if (item.note(tag).isNotBlank()) InfoCard(item.note(tag))
        AppCard {
            SectionTitle(sourceLabel)
            if (item.sources.isEmpty()) SupportingText(stringResource(R.string.ui_dua_missing))
            item.sources.forEach { source ->
                Text("${source.book} ${source.reference}")
                SupportingText(stringResource(when (source.grade) { DuaGrade.SAHIH -> R.string.ui_dua_sahih; DuaGrade.HASAN -> R.string.ui_dua_hasan
                    DuaGrade.QURAN -> R.string.ui_dua_quran; DuaGrade.UNVERIFIED -> R.string.ui_dua_review }))
                if (source.grader.isNotBlank()) SupportingText(source.grader)
                if (!source.checked) SupportingText(review)
                if (source.url.startsWith("https://")) TextButton({ openUrl(context, source.url) { onError(R.string.ui_error_external_app) } }) { Text(sourceLabel) }
            }
            if (item.reviewFields.isNotEmpty()) {
                SupportingText(stringResource(R.string.ui_dua_fields_pending))
                item.reviewFields.map { when (it) {
                    "arabic" -> R.string.ui_dua_arabic; "source" -> R.string.ui_dua_source; "grade" -> R.string.ui_dua_source
                    "translation" -> R.string.ui_dua_meaning; "pronunciation" -> R.string.ui_dua_pronunciation
                    else -> R.string.ui_dua_review
                } }.distinct().forEach { SupportingText(stringResource(it)) }
            }
        }
        item.repeats.forEachIndexed { index, repeat -> DuaCounter(item.id, index, repeat, settings) }
        if (active && item.audio.isNotBlank()) DuaAudio(item.audio, onError)
    }
}

@Composable
private fun DuaCounter(id: String, index: Int, repeat: DuaRepeat, settings: AppSettings) {
    var count by rememberSaveable(id, index) { mutableIntStateOf(0) }
    val number = NumberFormat.getIntegerInstance(Locale.forLanguageTag(settings.language.tag))
    AppCard(Modifier.testTag("dua_counter_${id}_$index")) {
        SectionTitle(stringResource(R.string.ui_dua_counter))
        Text(repeat.arabic, style = readingTextStyle(settings, AppLanguage.ARABIC))
        Text("${number.format(count)} / ${number.format(repeat.count)}", Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Button({ count = advanceDuaCounter(count, repeat.count) }, Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("dua_count_${id}_$index"), enabled = count < repeat.count) { Text("+1") }
        TextButton({ count = 0 }) { Text(stringResource(R.string.ui_dua_reset_counter)) }
    }
}

/** The catalog currently supplies no recordings. A future bundled file is checked before allocating a player. */
@Composable
private fun DuaAudio(path: String, onError: (Int) -> Unit) {
    val context = LocalContext.current
    val exists by produceState(false, path) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            path.startsWith("duas/audio/") && !path.contains("..") && runCatching { context.assets.open(path).use { }; true }.getOrDefault(false)
        }
    }
    if (!exists) return
    val controller = remember(path) { com.androidhive.myapplication.platform.audio.LessonAudioController(context) { onError(R.string.ui_error_audio) } }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(controller, lifecycle) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event -> if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) controller.stop() }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer); controller.release() }
    }
    Button({ controller.toggle("asset:///$path".toUri()) }, Modifier.testTag("dua_audio")) { Text(stringResource(R.string.ui_play_audio)) }
}
