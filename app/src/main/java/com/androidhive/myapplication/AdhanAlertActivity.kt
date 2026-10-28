package com.androidhive.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.androidhive.myapplication.core.labelRes
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.platform.alarm.AlarmCoordinator
import com.androidhive.myapplication.platform.audio.AdhanService
import com.androidhive.myapplication.ui.theme.SalahTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class AdhanAlertActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        else @Suppress("DEPRECATION") window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) { override fun handleOnBackPressed() = stopAndFinish() })
        val language = enumOrDefault(intent.getStringExtra(AdhanService.EXTRA_LANGUAGE), AppLanguage.PERSIAN)
        val prayer = enumOrDefault(intent.getStringExtra(AlarmCoordinator.EXTRA_ID), PrayerId.FAJR)
        setContent {
            SalahTheme(AppSettings(language = language, theme = ThemeMode.DARK)) {
                val playing by (application as SalahApplication).container.adhanSession.active.collectAsStateWithLifecycle()
                LaunchedEffect(playing) { if (!playing) finish() }
                var drag by remember { mutableFloatStateOf(0f) }
                Surface(Modifier.fillMaxSize().pointerInput(Unit) { detectVerticalDragGestures(onDragStart = { drag = 0f }, onDragCancel = { drag = 0f }, onDragEnd = {
                    if (drag > 100.dp.toPx()) stopAndFinish(); drag = 0f
                }, onVerticalDrag = { _, amount -> drag += amount }) }) {
                    Column(Modifier.safeDrawingPadding().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.ui_adhan_now), style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(22.dp))
                        Text(stringResource(prayer.labelRes()), style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(32.dp))
                        Button(::stopAndFinish, Modifier.fillMaxWidth().heightIn(min = 60.dp)) { Text(stringResource(R.string.ui_stop_adhan)) }
                    }
                }
            }
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) { stopAndFinish(); return true }
        return super.onKeyDown(keyCode, event)
    }
    private fun stopAndFinish() {
        val service = Intent(this, AdhanService::class.java)
        stopService(service)
        finish()
    }
}
