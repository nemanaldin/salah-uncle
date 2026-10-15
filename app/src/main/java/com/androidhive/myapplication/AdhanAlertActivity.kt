package com.androidhive.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher

class AdhanAlertActivity : Activity() {

    private var downY: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_adhan_alert)

        val btnStop = findViewById<Button>(R.id.btnStopAdhan)
        btnStop.setOnClickListener { stopAdhanAndFinish() }

        // Modern back handling for API 33+ (Android 13+)
        // Uses OnBackInvokedDispatcher (official API 33+ for back handling)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                object : OnBackInvokedCallback {
                    override fun onBackInvoked() {
                        stopAdhanAndFinish()
                    }
                }
            )
        }
        // For API < 33, onKeyDown handling is kept below
    }

    private fun stopAdhanAndFinish() {
        val stop = Intent(this, AdhanControlReceiver::class.java).apply {
            action = AdhanControlReceiver.ACTION_STOP
        }
        sendBroadcast(stop)
        finish()
    }

    // Legacy back handling for API < 33 (Android 12 and below)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                stopAdhanAndFinish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    downY = it.y
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val upY = it.y
                    if (downY - upY > 200) {
                        stopAdhanAndFinish()
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }
}