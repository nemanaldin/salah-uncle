package com.androidhive.myapplication.ui.lessons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.content.*
import com.androidhive.myapplication.platform.audio.LessonAudioController
import com.androidhive.myapplication.platform.audio.LessonAudioState
import com.androidhive.myapplication.platform.openUrl
import com.androidhive.myapplication.platform.shareText
import com.androidhive.myapplication.ui.components.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** Direct reference articles; only the three guides offer an optional standard RTL-aware pager. */
@Composable
fun LessonScreen(lesson: Lesson, settings: AppSettings, onRead: (String, String) -> Unit,
    onBack: () -> Unit, onError: (Int) -> Unit, initialSection: String? = null,
    onSaveSection: (SectionBookmark) -> Unit = {}, onRemoveSection: (SectionBookmark) -> Unit = {}, initialOffset: Int = 0) {
    var showOriginal by rememberSaveable(lesson.id, settings.school, settings.language) { mutableStateOf(false) }
    val translated = lesson.variant(settings.school, settings.language)
    val original = lesson.variant(settings.school)
    val variant = translated ?: original.takeIf { showOriginal }
    if (variant == null || !ActiveContent.policy.allows(settings)) {
        PageColumn(Modifier.testTag("translation_unavailable")) {
            SectionTitle(stringResource(R.string.ui_translation_unavailable))
            SupportingText(stringResource(if (original == null) R.string.ui_school_unavailable else R.string.ui_translation_body))
            if (original != null && ActiveContent.policy.allows(settings)) OutlinedButton({ showOriginal = true }, Modifier.testTag("read_original")) {
                Text(stringResource(R.string.ui_read_original, AppLanguage.entries.firstOrNull { it.tag == original.language }?.nativeName ?: original.language))
            }
            TextButton(onBack) { Text(stringResource(R.string.ui_back)) }
        }
        return
    }
    val slides = remember(variant) { readingSlides(variant.steps) }
    val eligible = isSlideGuide(lesson.id, settings.school)
    var textMode by rememberSaveable(lesson.id, variant.language) { mutableStateOf(!eligible) }
    var cursor by rememberSaveable(lesson.id, variant.language) { mutableStateOf(initialSection ?: settings.lastStepId.takeIf { settings.lastLessonId == lesson.id } ?: variant.steps.first().id) }
    var cursorOffset by rememberSaveable(lesson.id, variant.language) { mutableIntStateOf(initialOffset.coerceAtLeast(0)) }
    val scroll = rememberLazyListState()
    val pager = rememberPagerState(initialPage = targetSlide(slides, cursor, cursorOffset), pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val currentError by rememberUpdatedState(onError)
    val currentRead by rememberUpdatedState(onRead)
    var editing by remember { mutableStateOf<SectionBookmark?>(null) }
    editing?.let { bookmark -> SectionBookmarkDialog(bookmark, settings.sectionBookmarks.firstOrNull { it.sameSection(bookmark) },
        onSaveSection, onRemoveSection, { editing = null }) }
    val audio = remember(lesson.id, variant.language, variant.steps) {
        if (variant.steps.any { it.audio in MediaResources.audio }) LessonAudioController(context) { currentError(R.string.ui_error_audio) } else null
    }
    val playback = audio?.state?.collectAsStateWithLifecycle()?.value ?: LessonAudioState()
    DisposableEffect(audio, lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) audio?.stop() }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer); audio?.release() }
    }
    LaunchedEffect(initialSection, initialOffset, variant.language) {
        initialSection?.let { cursor = it; cursorOffset = initialOffset.coerceAtLeast(0) }
        if (textMode) scroll.scrollToItem(variant.steps.indexOfFirst { it.id == cursor }.coerceAtLeast(0).let { if (initialSection == null && it == 0) 0 else it + 1 })
        else pager.scrollToPage(targetSlide(slides, cursor, cursorOffset))
    }
    var modeInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(textMode) {
        if (!modeInitialized) { modeInitialized = true; return@LaunchedEffect }
        if (textMode) scroll.scrollToItem(variant.steps.indexOfFirst { it.id == cursor }.coerceAtLeast(0) + 1)
        else pager.scrollToPage(targetSlide(slides, cursor, cursorOffset))
    }
    LaunchedEffect(textMode, lesson.id, variant.language) {
        if (textMode) snapshotFlow { scroll.firstVisibleItemIndex }.distinctUntilChanged().collect { index ->
            val step = variant.steps[(index - 1).coerceIn(variant.steps.indices)]
            cursor = step.id; cursorOffset = 0; currentRead(lesson.id, step.id)
        } else snapshotFlow { pager.currentPage }.distinctUntilChanged().collect { index ->
            val slide = slides[index]
            cursor = slide.step.id; cursorOffset = slide.offset; currentRead(lesson.id, slide.step.id)
        }
    }
    val direction = if (AppLanguage.entries.firstOrNull { it.tag == variant.language }?.isRtl == true) LayoutDirection.Rtl else LayoutDirection.Ltr
    val externalError = { onError(R.string.ui_error_external_app) }
    val shareLabel = stringResource(R.string.ui_share_lesson)
    val jump: (String) -> Unit = { id -> scope.launch {
        cursor = id; cursorOffset = 0
        if (textMode) scroll.animateScrollToItem(variant.steps.indexOfFirst { it.id == id }.coerceAtLeast(0) + 1)
        else pager.animateScrollToPage(targetSlide(slides, id))
    } }
    val bookmark: (LessonStep, Int) -> Unit = { step, offset -> editing = SectionBookmark(lesson.id, step.id, settings.school, settings.language, offset = offset) }
    val header: @Composable () -> Unit = {
        TopicHeading(lesson.title(settings.language), lesson.id)
        if (variant.reviewStatus == ReviewStatus.EXPERIMENTAL || settings.language.reviewStatus == ReviewStatus.EXPERIMENTAL)
            Box(Modifier.testTag("lesson_review_notice")) { InfoCard(stringResource(R.string.ui_experimental_language_notice, settings.language.nativeName)) }
        if (variant.language != settings.language.tag) InfoCard(stringResource(R.string.ui_content_language, variant.language))
        if (lesson.id == "wudu") InfoCard(stringResource(R.string.ui_wudu_review))
        ArticleContents(variant.steps, jump)
        if (eligible) TextButton({ textMode = !textMode }, Modifier.testTag("reader_mode")) { Text(stringResource(if (textMode) R.string.ui_show_slides else R.string.ui_show_text)) }
        IconButton({ shareText(context, lesson.title(settings.language) + "\n\n" + variant.steps.joinToString("\n\n") { it.body }, shareLabel, externalError) }) { Icon(Icons.Default.Share, shareLabel) }
    }
    if (textMode) LazyColumn(Modifier.fillMaxSize().testTag("lesson_reader"), state = scroll,
        contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item("heading") { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { header() } }
        items(variant.steps, key = { it.id }) { step ->
            ReaderSection(lesson.id, step, step.body, direction, variant.language, settings, playback, { bookmark(step, 0) }, audio, step.id == variant.steps.first().id)
        }
        item("sources") { ReaderSources(lesson, variant, externalError) }
    } else Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton({ textMode = true }, Modifier.testTag("reader_mode")) { Text(stringResource(R.string.ui_show_text)) }
        LanguageReadinessNotice(settings.language)
        if (lesson.id == "wudu") InfoCard(stringResource(R.string.ui_wudu_review))
        ArticleContents(variant.steps, jump)
        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth().testTag("guide_pager"), key = { slides[it].key }) { index ->
            val slide = slides[index]
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                ReaderSection(lesson.id, slide.step, slide.text, direction, variant.language, settings, playback,
                    { bookmark(slide.step, slide.offset) }, audio, index == 0)
                if (index == slides.lastIndex) ReaderSources(lesson, variant, externalError)
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }, Modifier.weight(1f).testTag("slide_previous"), enabled = pager.currentPage > 0) { Text(stringResource(R.string.ui_previous_step)) }
            Text(stringResource(R.string.ui_step_of, pager.currentPage + 1, slides.size), Modifier.weight(1f).testTag("slide_position"))
            Button({ scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }, Modifier.weight(1f).testTag("slide_next"), enabled = pager.currentPage < slides.lastIndex) { Text(stringResource(R.string.ui_slide_next)) }
        }
    }
}

@Composable
private fun ReaderSection(lessonId: String, step: LessonStep, body: String, direction: LayoutDirection, language: String,
    settings: AppSettings, playback: LessonAudioState, onBookmark: () -> Unit, audio: LessonAudioController?, isFirst: Boolean) {
    AppCard(Modifier.testTag("topic_section_${step.id}")) {
        val value = SectionBookmark(lessonId, step.id, settings.school, settings.language)
        val saved = settings.sectionBookmarks.any { it.sameSection(value) }
        IconButton(onBookmark, Modifier.testTag("section_bookmark_${step.id}")) {
            Icon(Icons.Default.Star, stringResource(R.string.ui_section_bookmark), tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
        }
        if (step.illustrations.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            step.illustrations.forEach { name -> MediaResources.illustrations[name]?.let { drawable ->
                Image(painterResource(drawable), stringResource(R.string.ui_illustration), Modifier.weight(1f).heightIn(min = 140.dp, max = 250.dp).aspectRatio(.65f))
            } }
        } else if (isSlideGuide(lessonId, settings.school)) TopicArtwork(lessonId, Modifier.size(80.dp))
        CompositionLocalProvider(LocalLayoutDirection provides direction) { SelectionContainer {
            Text(body, Modifier.testTag(if (isFirst) "lesson_body" else "topic_body_${step.id}"), style = MaterialTheme.typography.bodyLarge.copy(localeList = LocaleList(language)))
        } }
        step.audio?.let { name -> MediaResources.audio[name]?.let { resource ->
            SupportingText(stringResource(R.string.ui_recitation_language))
            Button({ audio?.toggle(resource) }, Modifier.fillMaxWidth().testTag("topic_audio_${step.id}")) {
                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp))
                Text(stringResource(if (playback.playing && playback.resource == resource) R.string.ui_pause_audio else R.string.ui_play_audio))
            }
            val repeatLabel = stringResource(R.string.ui_repeat_audio)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(repeatLabel, Modifier.weight(1f).padding(top = 11.dp))
                Switch(playback.repeat, { audio?.repeat(it) }, Modifier.semantics { contentDescription = repeatLabel })
            }
        } }
    }
}

@Composable
private fun ReaderSources(lesson: Lesson, variant: LessonVariant, onError: () -> Unit) {
    val context = LocalContext.current
    if (variant.sources.isEmpty()) TextButton({ openUrl(context, lesson.source, onError) }) { Text(stringResource(R.string.ui_lesson_source)) }
    else variant.sources.forEach { source -> TextButton({ openUrl(context, source.url, onError) }) { Text(source.title) } }
}
