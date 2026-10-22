package com.androidhive.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidhive.myapplication.platform.openExternal
import com.androidhive.myapplication.ui.DeviceActions
import com.androidhive.myapplication.ui.SalahApp
import com.androidhive.myapplication.ui.theme.SalahTheme
import com.androidhive.myapplication.core.model.ThemeMode

class MainActivity : AppCompatActivity() {
    private lateinit var model: SalahViewModel
    private var pendingRoute by mutableStateOf<String?>(null)
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.any { it }) model.currentLocation() else model.message(R.string.ui_error_location_permission)
    }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { model.onResume() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as SalahApplication).container
        model = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(SalahViewModel::class.java))
                return SalahViewModel(container) as T
            }
        })[SalahViewModel::class.java]
        pendingRoute = if (savedInstanceState == null) intent.getStringExtra(EXTRA_ROUTE) else savedInstanceState.getString(EXTRA_ROUTE)
        val actions = DeviceActions(::requestLocation, ::requestNotifications, ::requestExactAlarm, ::requestFullScreen)
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val dark = when (state.settings?.theme ?: ThemeMode.SYSTEM) {
                ThemeMode.SYSTEM -> systemDark; ThemeMode.LIGHT -> false; ThemeMode.DARK -> true
            }
            LaunchedEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(0xE6FFFFFF.toInt(), 0x801B1B1B.toInt()) { dark },
                )
            }
            LaunchedEffect(state.settings?.language) {
                val language = state.settings?.language ?: return@LaunchedEffect
                if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != language.tag)
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
            }
            SalahTheme(state.settings ?: com.androidhive.myapplication.core.model.AppSettings()) {
                SalahApp(state, model, container, actions, pendingRoute) { pendingRoute = null; intent.removeExtra(EXTRA_ROUTE) }
            }
        }
    }
    override fun onResume() { super.onResume(); if (::model.isInitialized) model.onResume() }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); pendingRoute = intent.getStringExtra(EXTRA_ROUTE) }
    override fun onSaveInstanceState(outState: Bundle) { outState.putString(EXTRA_ROUTE, pendingRoute); super.onSaveInstanceState(outState) }

    private fun requestLocation() {
        val granted = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
        if (granted) model.currentLocation()
        else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    private fun requestNotifications() {
        if ((application as SalahApplication).container.notifications.allowed()) return
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationSettings(); return
        }
        val prefs = getSharedPreferences("permission_prompts", MODE_PRIVATE)
        if (prefs.getBoolean("notifications", false) && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            notificationSettings()
        } else {
            prefs.edit().putBoolean("notifications", true).apply()
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    private fun notificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= 26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        openExternal(this, intent, ::externalError)
    }
    private fun requestExactAlarm() {
        if (Build.VERSION.SDK_INT >= 31) openExternal(this, Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")), ::externalError)
    }
    private fun requestFullScreen() {
        if (Build.VERSION.SDK_INT >= 34) openExternal(this, Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName")), ::externalError)
    }
    private fun externalError() { model.message(R.string.ui_error_external_app) }
    companion object { const val EXTRA_ROUTE = "route" }
}
