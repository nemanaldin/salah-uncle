package com.androidhive.myapplication.platform.audio

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.*
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.androidhive.myapplication.AdhanAlertActivity
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahApplication
import com.androidhive.myapplication.core.localizedContext
import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.core.model.enumOrDefault
import com.androidhive.myapplication.platform.alarm.AlarmCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdhanSession {
    private val mutable = MutableStateFlow(false)
    val active = mutable.asStateFlow()
    internal fun active(value: Boolean) { mutable.value = value }
}

class AdhanService : Service() {
    private var player: MediaPlayer? = null
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var focusRequest: AudioFocusRequest? = null
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) { stopSelf(); return START_NOT_STICKY }
        if (player != null) return START_NOT_STICKY
        val language = enumOrDefault(intent.getStringExtra(EXTRA_LANGUAGE), AppLanguage.PERSIAN)
        val c = localizedContext(this, language)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
            NotificationChannel(CHANNEL, c.getString(R.string.ui_adhan_channel), NotificationManager.IMPORTANCE_HIGH).apply { setSound(null, null) })
        val stop = PendingIntent.getBroadcast(this, 20, Intent(this, StopAdhanReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val show = PendingIntent.getActivity(this, 21, Intent(this, AdhanAlertActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmCoordinator.EXTRA_ID, intent.getStringExtra(AlarmCoordinator.EXTRA_ID))
            putExtra(EXTRA_LANGUAGE, language.name)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_stat_prayer)
            .setContentTitle(c.getString(R.string.ui_adhan_now)).setContentText(c.getString(R.string.ui_adhan_playing))
            .setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(show).setOngoing(true).setSilent(true)
            .addAction(0, c.getString(R.string.ui_stop_adhan), stop)
        if (Build.VERSION.SDK_INT < 34 || manager.canUseFullScreenIntent()) notification.setFullScreenIntent(show, true)
        (application as SalahApplication).container.adhanSession.active(true)
        startForeground(NOTIFICATION_ID, notification.build())

        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        @Suppress("DEPRECATION")
        val focus = if (Build.VERSION.SDK_INT >= 26) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(attributes).setOnAudioFocusChangeListener(focusListener).build()
            audioManager.requestAudioFocus(focusRequest!!)
        } else audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        if (focus != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { stopSelf(); return START_NOT_STICKY }
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(attributes)
                setWakeMode(this@AdhanService, PowerManager.PARTIAL_WAKE_LOCK)
                resources.openRawResourceFd(R.raw.dhan_sound).use { fd -> setDataSource(fd.fileDescriptor, fd.startOffset, fd.length) }
                setOnPreparedListener { it.start() }
                setOnCompletionListener { stopSelf() }
                setOnErrorListener { _, _, _ -> stopSelf(); true }
                prepareAsync()
            }
        } catch (_: Exception) { stopSelf() }
        return START_NOT_STICKY
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        player?.release(); player = null
        if (Build.VERSION.SDK_INT >= 26) focusRequest?.let(audioManager::abandonAudioFocusRequest)
        else audioManager.abandonAudioFocus(focusListener)
        stopForeground(true)
        (application as SalahApplication).container.adhanSession.active(false)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object {
        const val CHANNEL = "adhan_playback"
        const val NOTIFICATION_ID = 3001
        const val EXTRA_LANGUAGE = "language"
    }
}

class StopAdhanReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val service = Intent(context, AdhanService::class.java)
        context.stopService(service)
    }
}
