package com.androidhive.myapplication.ui.lessons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.data.content.Lesson
import com.androidhive.myapplication.data.content.MediaResources
import com.androidhive.myapplication.platform.audio.LessonAudioController
import com.androidhive.myapplication.platform.openUrl
import com.androidhive.myapplication.platform.shareText
import com.androidhive.myapplication.ui.components.*
import kotlinx.coroutines.flow.distinctUntilChanged

/** One continuous reference article. Source sections and media retain their IDs;
 * there is no completion gate, lesson sequence or course-progress UI. */
@Composable
fun LessonScreen(lesson: Lesson, settings: AppSettings, onRead: (String, String) -> Unit,
    onBack: () -> Unit, onError: (Int) -> Unit) {
    var showOriginal by rememberSaveable(lesson.id, settings.school, settings.language) { mutableStateOf(false) }
    val translated = lesson.variant(settings.school, settings.language)
    val original = lesson.variant(settings.school)
    val variant = translated ?: original.takeIf { showOriginal }
    if (variant == null) {
        PageColumn(Modifier.testTag("translation_unavailable")) {
            SectionTitle(stringResource(R.string.ui_translation_unavailable))
            SupportingText(stringResource(if (original == null) R.string.ui_school_unavailable else R.string.ui_translation_body))
            if (original != null) OutlinedButton({ showOriginal = true }, Modifier.testTag("read_original")) {
                Text(stringResource(R.string.ui_read_original, AppLanguage.entries.firstOrNull { it.tag == original.language }?.nativeName ?: original.language))
            }
            TextButton(onBack) { Text(stringResource(R.string.ui_back)) }
        }
        return
    }
    val initial = if (settings.lastLessonId == lesson.id) variant.steps.indexOfFirst { it.id == settings.lastStepId }.coerceAtLeast(0) else 0
    val scroll = rememberLazyListState(initialFirstVisibleItemIndex = if (initial > 0) initial + 1 else 0)
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val currentError by rememberUpdatedState(onError)
    val currentRead by rememberUpdatedState(onRead)
    val audio = remember(lesson.id, variant.language) { LessonAudioController(context) { currentError(R.string.ui_error_audio) } }
    val playback by audio.state.collectAsStateWithLifecycle()
    DisposableEffect(audio, lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) audio.stop() }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer); audio.release() }
    }
    LaunchedEffect(lesson.id, variant.language) {
        snapshotFlow { scroll.firstVisibleItemIndex }.distinctUntilChanged().collect { index ->
            currentRead(lesson.id, variant.steps[(index - 1).coerceIn(variant.steps.indices)].id)
        }
    }
    val externalError = { onError(R.string.ui_error_external_app) }
    val shareLabel = stringResource(R.string.ui_share_lesson)
    val direction = if (AppLanguage.entries.firstOrNull { it.tag == variant.language }?.isRtl == true) LayoutDirection.Rtl else LayoutDirection.Ltr
    LazyColumn(Modifier.fillMaxSize().testTag("lesson_reader"), state = scroll,
        contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item(key = "heading") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton({ shareText(context, lesson.title(settings.language) + "\n\n" + variant.steps.joinToString("\n\n") { it.body }, shareLabel, externalError) }) { Icon(Icons.Default.Share, shareLabel) }
                }
                TopicHeading(lesson.title(settings.language), lesson.id)
                if (variant.language != settings.language.tag) InfoCard(stringResource(R.string.ui_content_language,
                    AppLanguage.entries.firstOrNull { it.tag == variant.language }?.nativeName ?: variant.language))
                if (lesson.id == "wudu") InfoCard(stringResource(R.string.ui_wudu_review))
            }
        }
        items(variant.steps, key = { it.id }) { section ->
            AppCard(modifier = Modifier.testTag("topic_section_${section.id}")) {
                if (section.illustrations.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    section.illustrations.forEach { name -> MediaResources.illustrations[name]?.let { drawable ->
                        Image(painterResource(drawable), stringResource(R.string.ui_illustration), Modifier.weight(1f).heightIn(min = 140.dp, max = 250.dp).aspectRatio(.65f))
                    } }
                }
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    SelectionContainer {
                        Text(section.body, Modifier.testTag(if (section.id == variant.steps.first().id) "lesson_body" else "topic_body_${section.id}"),
                            style = MaterialTheme.typography.bodyLarge.copy(localeList = LocaleList(variant.language)))
                    }
                }
                section.audio?.let { name -> MediaResources.audio[name]?.let { resource ->
                    SupportingText(stringResource(R.string.ui_recitation_language))
                    Button({ audio.toggle(resource) }, Modifier.fillMaxWidth().testTag("topic_audio_${section.id}")) {
                        Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp))
                        Text(stringResource(if (playback.playing && playback.resource == resource) R.string.ui_pause_audio else R.string.ui_play_audio))
                    }
                    val repeatLabel = stringResource(R.string.ui_repeat_audio)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(repeatLabel, Modifier.weight(1f).padding(top = 11.dp))
                        Switch(playback.repeat, audio::repeat, Modifier.testTag("repeat_${section.id}").semantics { contentDescription = repeatLabel })
                    }
                } }
            }
        }
        item(key = "sources") {
            Column {
                if (variant.sources.isEmpty()) TextButton({ openUrl(context, lesson.source, externalError) }) { Text(stringResource(R.string.ui_lesson_source)) }
                else variant.sources.forEach { source -> TextButton({ openUrl(context, source.url, externalError) }) { Text(source.title) } }
            }
        }
    }
}
