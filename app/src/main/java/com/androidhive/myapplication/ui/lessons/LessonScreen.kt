package com.androidhive.myapplication.ui.lessons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.intl.LocaleList
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

@Composable
fun LessonScreen(lesson: Lesson, settings: AppSettings, onRead: (String, String) -> Unit,
    onComplete: (String, String) -> Unit, onBack: () -> Unit, onError: (Int) -> Unit) {
    var showOriginal by rememberSaveable(lesson.id, settings.school, settings.language) { mutableStateOf(false) }
    val translated = lesson.variant(settings.school, settings.language)
    val original = lesson.variant(settings.school)
    val variant = translated ?: original.takeIf { showOriginal }
    if (variant == null) {
        PageColumn(Modifier.testTag("translation_unavailable")) {
            SectionTitle(stringResource(R.string.ui_translation_unavailable))
            SupportingText(stringResource(if (original == null) R.string.ui_school_unavailable else R.string.ui_translation_body))
            if (original != null) OutlinedButton({ showOriginal = true }, Modifier.testTag("read_original")) {
                Text(stringResource(R.string.ui_read_original,
                    AppLanguage.entries.firstOrNull { it.tag == original.language }?.nativeName ?: original.language))
            }
            TextButton(onBack) { Text(stringResource(R.string.ui_back)) }
        }
        return
    }
    var index by rememberSaveable(lesson.id, settings.school) { mutableIntStateOf(
        if (settings.lastLessonId == lesson.id) variant.steps.indexOfFirst { it.id == settings.lastStepId }.coerceAtLeast(0) else 0) }
    var finished by rememberSaveable(lesson.id, settings.school) { mutableStateOf(false) }
    val step = variant.steps[index.coerceIn(variant.steps.indices)]
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val currentError by rememberUpdatedState(onError)
    val audio = remember(lesson.id, variant.language) { LessonAudioController(context) { currentError(R.string.ui_error_audio) } }
    val playback by audio.state.collectAsStateWithLifecycle()
    DisposableEffect(audio, lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) audio.stop() }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer); audio.release() }
    }
    LaunchedEffect(step.id, variant.language) { audio.stop(); onRead(lesson.id, step.id) }
    val externalError = { onError(R.string.ui_error_external_app) }
    val shareLabel = stringResource(R.string.ui_share_lesson)

    if (finished) {
        PageColumn {
            AppCard {
                SectionTitle(stringResource(R.string.ui_lesson_complete))
                SupportingText(stringResource(R.string.ui_lesson_complete_body))
                Button(onBack, Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_lessons)) }
                OutlinedButton({ index = 0; finished = false }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_review_lesson)) }
            }
        }
        return
    }
    // Keying the reader resets its scroll position when the learner moves to another step.
    key(step.id) {
        PageColumn(Modifier.testTag("lesson_reader")) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.ui_step_of, index + 1, variant.steps.size), style = MaterialTheme.typography.titleMedium)
                IconButton({ shareText(context, lesson.title(settings.language) + "\n\n" + variant.steps.joinToString("\n\n") { it.body }, shareLabel, externalError) }) {
                    Icon(Icons.Default.Share, shareLabel)
                }
            }
            IllustratedHeading(lesson.title(settings.language), lessonArtwork(lesson.id))
            LinearProgressIndicator(progress = { (index + 1f) / variant.steps.size }, modifier = Modifier.fillMaxWidth())
            if (variant.language != settings.language.tag) InfoCard(stringResource(R.string.ui_content_language,
                AppLanguage.entries.firstOrNull { it.tag == variant.language }?.nativeName ?: variant.language))
            if (step.audio != null || step.body.any { it in '\u0600'..'\u06ff' }) SupportingText(stringResource(R.string.ui_recitation_language))
            if (lesson.id == "wudu") InfoCard(stringResource(R.string.ui_wudu_review))
            if (step.illustrations.isNotEmpty()) AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    step.illustrations.forEach { name -> MediaResources.illustrations[name]?.let { drawable ->
                        Image(painterResource(drawable), stringResource(R.string.ui_illustration), Modifier.weight(1f).heightIn(min = 140.dp, max = 250.dp).aspectRatio(.65f))
                    } }
                }
            }
            AppCard {
                val direction = if (android.text.TextUtils.getLayoutDirectionFromLocale(java.util.Locale.forLanguageTag(variant.language)) == android.view.View.LAYOUT_DIRECTION_RTL) LayoutDirection.Rtl else LayoutDirection.Ltr
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    SelectionContainer { Text(step.body, modifier = Modifier.testTag("lesson_body"), style = MaterialTheme.typography.bodyLarge.copy(localeList = LocaleList(variant.language))) }
                }
            }
            step.audio?.let { name -> MediaResources.audio[name]?.let { resource ->
                AppCard {
                    Button({ audio.toggle(resource) }, Modifier.fillMaxWidth().testTag("lesson_audio")) {
                        Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp))
                        Text(stringResource(if (playback.playing) R.string.ui_pause_audio else R.string.ui_play_audio))
                    }
                    val repeatLabel = stringResource(R.string.ui_repeat_audio)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(repeatLabel, Modifier.padding(top = 11.dp))
                        Switch(playback.repeat, audio::repeat, modifier = Modifier.semantics { contentDescription = repeatLabel })
                    }
                }
            } }
            Button({
                audio.stop(); onComplete(lesson.id, step.id)
                if (index < variant.steps.lastIndex) index++ else finished = true
            }, Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("lesson_next")) {
                Text(stringResource(if (index == variant.steps.lastIndex) R.string.ui_finish_lesson else R.string.ui_next_step))
            }
            if (index > 0) OutlinedButton({ index-- }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.ui_previous_step)) }
            if (variant.sources.isEmpty()) TextButton({ openUrl(context, lesson.source, externalError) }) { Text(stringResource(R.string.ui_lesson_source)) }
            else variant.sources.forEach { source ->
                TextButton({ openUrl(context, source.url, externalError) }) { Text(source.title) }
            }
        }
    }
}
