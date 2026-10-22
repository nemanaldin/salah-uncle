package com.androidhive.myapplication.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahUiState
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.platform.audio.*
import com.androidhive.myapplication.ui.components.*
import android.net.Uri

@Composable
fun AdhanVoicesScreen(state: SalahUiState, onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onImport: (Uri) -> Unit, onRemove: (AdhanVoice) -> Unit, onError: (Int) -> Unit) {
    val settings = state.settings ?: return
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val error by rememberUpdatedState(onError)
    val recordings = remember { AdhanRecordings(context) }
    val preview = remember { LessonAudioController(context) { error(R.string.ui_error_audio) } }
    val playing by preview.state.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(onImport) }
    DisposableEffect(owner, preview) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) preview.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); preview.release() }
    }
    PageColumn(Modifier.testTag("adhan_voices_screen")) {
        SupportingText(stringResource(R.string.ui_adhan_import_body))
        AppCard {
            ListItem(headlineContent = { Text(stringResource(R.string.ui_adhan_default)) }, leadingContent = { RadioButton(settings.selectedVoiceId == null, null) },
                modifier = Modifier.clickable { preview.stop(); onUpdate { it.copy(selectedVoiceId = null) } }.testTag("voice_default"))
            OutlinedButton({ if (playing.playing && playing.resource == R.raw.dhan_sound) preview.stop() else preview.toggle(R.raw.dhan_sound) }, Modifier.fillMaxWidth().testTag("voice_preview_default")) {
                Text(stringResource(if (playing.playing && playing.resource == R.raw.dhan_sound) R.string.ui_stop_audio else R.string.ui_test_adhan))
            }
        }
        settings.adhanVoices.forEach { voice ->
            val uri = recordings.uri(voice)
            val isPlaying = playing.playing && uri != null && playing.source == uri.toString()
            AppCard {
                ListItem(headlineContent = { Text(voice.title) }, leadingContent = { RadioButton(settings.selectedVoiceId == voice.id, null) },
                    modifier = Modifier.clickable { preview.stop(); onUpdate { it.copy(selectedVoiceId = voice.id) } }.testTag("voice_${voice.id}"))
                OutlinedButton({
                    if (uri == null) onError(R.string.ui_error_audio)
                    else if (isPlaying) preview.stop() else preview.toggle(uri)
                }, Modifier.fillMaxWidth().testTag("preview_${voice.id}")) { Text(stringResource(if (isPlaying) R.string.ui_stop_audio else R.string.ui_test_adhan)) }
                TextButton({ preview.stop(); onRemove(voice) }, Modifier.testTag("remove_${voice.id}")) { Text(stringResource(R.string.ui_adhan_remove)) }
            }
        }
        Button({ preview.stop(); picker.launch(arrayOf("audio/*")) }, Modifier.fillMaxWidth().testTag("import_adhan"), enabled = !state.audioImportBusy) {
            Text(stringResource(if (state.audioImportBusy) R.string.ui_loading else R.string.ui_adhan_import))
        }
        if (state.audioImportBusy) LinearProgressIndicator(Modifier.fillMaxWidth())
        SupportingText(stringResource(R.string.ui_audio_size))
    }
}
