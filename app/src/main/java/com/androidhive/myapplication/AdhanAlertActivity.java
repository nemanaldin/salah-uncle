package com.androidhive.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;

public class AdhanAlertActivity extends Activity {

    private float downY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(R.layout.activity_adhan_alert);

        Button btnStop = findViewById(R.id.btnStopAdhan);
        btnStop.setOnClickListener(v -> stopAdhanAndFinish());
    }

    private void stopAdhanAndFinish() {
        Intent stop = new Intent(this, AdhanControlReceiver.class);
        stop.setAction(AdhanControlReceiver.ACTION_STOP);
        sendBroadcast(stop);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // بک هم اذان رو قطع کنه
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopAdhanAndFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float upY = event.getY();
                if (downY - upY > 200) { // سوایپ به بالا
                    stopAdhanAndFinish();
                    return true;
                }
        }
        return super.onTouchEvent(event);
    }
}
