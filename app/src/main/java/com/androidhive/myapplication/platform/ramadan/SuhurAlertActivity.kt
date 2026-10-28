package com.androidhive.myapplication.platform.ramadan

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidhive.myapplication.R
import com.androidhive.myapplication.SalahApplication
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.ui.components.*
import com.androidhive.myapplication.ui.theme.SalahTheme

class SuhurAlertActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        else @Suppress("DEPRECATION") window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) { override fun handleOnBackPressed() = stop() })
        val language = enumOrDefault(intent.getStringExtra(SuhurAlarmService.LANGUAGE), AppLanguage.PERSIAN)
        setContent {
            val session by (application as SalahApplication).container.suhurSession.state.collectAsStateWithLifecycle()
            LaunchedEffect(session.active) { if (!session.active) finish() }
            SalahTheme(AppSettings(language = language, theme = ThemeMode.DARK)) {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.safeDrawingPadding().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        PrayerArtwork(PrayerSymbol.TARAWIH, Modifier.size(110.dp))
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.ui_suhur_alarm), style = MaterialTheme.typography.headlineMedium)
                        SupportingText(stringResource(R.string.ui_suhur_wake_now))
                        Spacer(Modifier.height(24.dp))
                        Button(::stop, Modifier.fillMaxWidth().heightIn(min = 60.dp)) { Text(stringResource(R.string.ui_stop_alarm)) }
                    }
                }
            }
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) { stop(); return true }
        return super.onKeyDown(keyCode, event)
    }
    private fun stop() { stopService(Intent(this, SuhurAlarmService::class.java)); finish() }
}
