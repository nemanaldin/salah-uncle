package com.androidhive.myapplication.platform.ramadan

import android.app.*
import android.content.*
import android.media.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahApplication
import com.androidhive.myapplication.core.localizedContext
import com.androidhive.myapplication.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SuhurPlayback(val active: Boolean = false, val playing: Boolean = false, val error: Int? = null, val preview: Boolean = false)
class SuhurSession {
    private val mutable = MutableStateFlow(SuhurPlayback())
    val state = mutable.asStateFlow()
    internal fun starting(preview: Boolean) { mutable.value = SuhurPlayback(active = true, preview = preview) }
    internal fun playing() { mutable.value = mutable.value.copy(active = true, playing = true) }
    internal fun stopped() { mutable.value = mutable.value.copy(active = false, playing = false) }
    internal fun failed() { mutable.value = SuhurPlayback(error = R.string.ui_alarm_sound_error) }
}
/** Alarm-stream playback with explicit stop controls, audio focus and a bounded
 * two-minute lifetime. It never substitutes an adhan recording for an alarm. */
class SuhurAlarmService : Service() {
    private val container get() = (application as SalahApplication).container
    private var player: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeout = Runnable { stopSelf() }
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var focusRequest: AudioFocusRequest? = null
    private var language = AppLanguage.PERSIAN
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> stopSelf()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player?.setVolume(.25f, .25f)
            AudioManager.AUDIOFOCUS_GAIN -> player?.setVolume(1f, 1f)
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) { stopSelf(); return START_NOT_STICKY }
        if (player != null) return START_NOT_STICKY
        language = enumOrDefault(intent.getStringExtra(LANGUAGE), AppLanguage.PERSIAN)
        val c = localizedContext(this, language)
        container.ramadanNotifications.channels(language)
        container.suhurSession.starting(intent.getBooleanExtra(PREVIEW, false))
        val stop = PendingIntent.getBroadcast(this, 430, Intent(this, StopSuhurReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val show = PendingIntent.getActivity(this, 431, Intent(this, SuhurAlertActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LANGUAGE, language.name)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val preview = intent.getBooleanExtra(PREVIEW, false)
        val builder = NotificationCompat.Builder(this, RamadanNotifications.SUHUR_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_prayer).setContentTitle(c.getString(R.string.ui_suhur_alarm))
            .setContentText(c.getString(if (preview) R.string.ui_alarm_preview_note else R.string.ui_suhur_wake_now))
            .setContentIntent(if (preview) container.ramadanNotifications.open() else show)
            .setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_MAX).setOngoing(true)
            .addAction(0, c.getString(R.string.ui_stop_alarm), stop)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!preview && (Build.VERSION.SDK_INT < 34 || manager.canUseFullScreenIntent())) builder.setFullScreenIntent(show, true)
        startForeground(RamadanNotifications.ALARM_ID, builder.build())
        handler.postDelayed(timeout, 120_000)
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        @Suppress("DEPRECATION")
        val focus = if (Build.VERSION.SDK_INT >= 26) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes).setOnAudioFocusChangeListener(focusListener).build()
            audioManager.requestAudioFocus(focusRequest!!)
        } else audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        if (focus != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { fail(); return START_NOT_STICKY }
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(attributes); setWakeMode(this@SuhurAlarmService, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(this@SuhurAlarmService, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
                isLooping = true
                setOnPreparedListener { it.start(); container.suhurSession.playing() }
                setOnErrorListener { _, _, _ -> fail(); true }
                prepareAsync()
            }
        } catch (_: Exception) { fail() }
        return START_NOT_STICKY
    }
    private fun fail() { container.suhurSession.failed(); container.ramadanNotifications.soundError(language); stopSelf() }
    @Suppress("DEPRECATION")
    override fun onDestroy() {
        handler.removeCallbacks(timeout)
        player?.release(); player = null
        if (Build.VERSION.SDK_INT >= 26) focusRequest?.let(audioManager::abandonAudioFocusRequest) else audioManager.abandonAudioFocus(focusListener)
        container.suhurSession.stopped(); stopForeground(true)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object { const val LANGUAGE = "alarm_language"; const val PREVIEW = "preview_alarm" }
}
