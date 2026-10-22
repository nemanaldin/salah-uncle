package com.androidhive.myapplication.ui.lessons

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.data.content.TrainingVideo
import com.androidhive.myapplication.ui.components.*

@Composable
fun TrainingScreen(videos: List<TrainingVideo>, settings: AppSettings, onGuide: () -> Unit, onError: (Int) -> Unit) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    PageColumn(Modifier.testTag("training_screen")) {
        SupportingText(stringResource(R.string.ui_training_description))
        if (videos.isEmpty()) {
            Surface(Modifier.fillMaxWidth().aspectRatio(16f / 9f), shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            SectionTitle(stringResource(R.string.ui_training_placeholder))
            SupportingText(stringResource(R.string.ui_training_placeholder_body))
            Button(onGuide, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(stringResource(R.string.ui_open_prayer_guide)) }
        } else {
            val selected = videos.firstOrNull { it.id == selectedId } ?: videos.first()
            SectionTitle(selected.title(settings.language))
            key(selected.id) { TrainingPlayer(selected, onError) }
            videos.forEach { clip ->
                OutlinedButton({ selectedId = clip.id }, Modifier.fillMaxWidth()) { Text(clip.title(settings.language)) }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun TrainingPlayer(clip: TrainingVideo, onError: (Int) -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var position by rememberSaveable(clip.id) { mutableLongStateOf(0L) }
    val latestError by rememberUpdatedState(onError)
    val player = remember(clip.id) {
        ExoPlayer.Builder(context.applicationContext).build().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
            setHandleAudioBecomingNoisy(true)
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) { latestError(R.string.ui_video_unavailable) }
            })
            setMediaItem(MediaItem.fromUri(Uri.parse("asset:///${clip.assetPath}")))
            seekTo(position); prepare()
        }
    }
    DisposableEffect(player, owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) { position = player.currentPosition; player.pause() }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { position = player.currentPosition; owner.lifecycle.removeObserver(observer); player.release() }
    }
    AndroidView(factory = { PlayerView(it).apply { this.player = player; useController = true } },
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
}
