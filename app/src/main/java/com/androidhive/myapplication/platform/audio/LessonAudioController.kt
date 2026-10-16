package com.androidhive.myapplication.platform.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LessonAudioState(val resource: Int? = null, val playing: Boolean = false, val repeat: Boolean = false)

/** One lazily prepared player per visible lesson; no eager players for every step. */
class LessonAudioController(context: Context, private val onError: () -> Unit) {
    private val app = context.applicationContext
    private val mutable = MutableStateFlow(LessonAudioState())
    val state = mutable.asStateFlow()
    private val player = ExoPlayer.Builder(app).build().apply {
        setAudioAttributes(AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).setUsage(C.USAGE_MEDIA).build(), true)
        setHandleAudioBecomingNoisy(true)
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { mutable.value = mutable.value.copy(playing = isPlaying) }
            override fun onPlayerError(error: PlaybackException) { stop(); onError() }
        })
    }
    fun toggle(resource: Int) {
        if (mutable.value.resource != resource) {
            player.setMediaItem(MediaItem.fromUri(Uri.parse("android.resource://${app.packageName}/$resource")))
            mutable.value = mutable.value.copy(resource = resource)
            player.prepare(); player.play()
        } else if (player.isPlaying) player.pause() else {
            if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
            player.play()
        }
    }
    fun repeat(value: Boolean) {
        player.repeatMode = if (value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        mutable.value = mutable.value.copy(repeat = value)
    }
    fun stop() { player.stop(); mutable.value = mutable.value.copy(resource = null, playing = false) }
    fun release() = player.release()
}
