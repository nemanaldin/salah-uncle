package com.androidhive.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.androidhive.myapplication.R;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class AdhanService extends Service {

    private static final String TAG = "AdhanService";
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private Handler handler;
    private FusedLocationProviderClient fusedLocationClient;

    private double latitude, longitude;
    private PrayerTimes prayerTimes;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "سرویس پخش اذان شروع شد.", Toast.LENGTH_SHORT).show();
        // Get location and start the timer
        getLastKnownLocation();
        return START_STICKY;
    }

    private void getLastKnownLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    this.latitude = location.getLatitude();
                    this.longitude = location.getLongitude();
                    startAdhanTimer();
                } else {
                    Toast.makeText(this, "مکان فعلی در دسترس نیست. سرویس متوقف شد.", Toast.LENGTH_LONG).show();
                    stopSelf();
                }
            });
        } catch (SecurityException e) {
            Toast.makeText(this, "لطفاً مجوز دسترسی به مکان را به برنامه بدهید.", Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }


    private void startAdhanTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new Timer();
        // Use a self-rescheduling task with .schedule()
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    // Recalculate prayer times if not available or due for update
                    if (prayerTimes == null || shouldRecalculateTimes()) {
                        calculatePrayerTimes();
                    }

                    if (prayerTimes != null) {
                        long nowMillis = new Date().getTime();
                        if (isTimeForAdhan(prayerTimes.fajr, nowMillis)) {
                            playAdhanAndNotify("فجر");
                        } else if (isTimeForAdhan(prayerTimes.dhuhr, nowMillis)) {
                            playAdhanAndNotify("ظهر");
                        } else if (isTimeForAdhan(prayerTimes.asr, nowMillis)) {
                            playAdhanAndNotify("عصر");
                        } else if (isTimeForAdhan(prayerTimes.maghrib, nowMillis)) {
                            playAdhanAndNotify("مغرب");
                        } else if (isTimeForAdhan(prayerTimes.isha, nowMillis)) {
                            playAdhanAndNotify("عشا");
                        }
                    }
                    // Reschedule the task for the next minute
                    startAdhanTimer();
                });
            }
        }, 60 * 1000); // Check every minute
    }

    private void calculatePrayerTimes() {
        // Here you can set the calculation method based on user's preference
        // For simplicity, we use KARACHI method as default
        DateComponents date = DateComponents.from(new Date());
        com.batoulapps.adhan.CalculationParameters params = com.batoulapps.adhan.CalculationMethod.KARACHI.getParameters();
        prayerTimes = new PrayerTimes(new Coordinates(latitude, longitude), date, params);
        Log.d(TAG, "Prayer times recalculated.");
    }

    private boolean shouldRecalculateTimes() {
        // Simple logic to recalculate times at midnight to get new values for the next day
        Date now = new Date();
        return prayerTimes.fajr.before(now) && prayerTimes.isha.before(now);
    }

    private boolean isTimeForAdhan(Date prayerTime, long nowMillis) {
        if (prayerTime == null) {
            return false;
        }
        long diff = prayerTime.getTime() - nowMillis;
        return diff >= 0 && diff <= 60000; // Check for a 1-minute window
    }

    private void playAdhanAndNotify(String prayerName) {
        playAdhan();
        handler.post(() -> Toast.makeText(getApplicationContext(), "وقت اذان " + prayerName + " است.", Toast.LENGTH_LONG).show());
    }

    private void playAdhan() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, R.raw.dhan_sound);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    mediaPlayer.release();
                    mediaPlayer = null;
                });
            }
        } catch (Exception e) {
            handler.post(() -> Toast.makeText(getApplicationContext(), "خطا در پخش اذان.", Toast.LENGTH_SHORT).show());
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Toast.makeText(this, "سرویس پخش اذان متوقف شد.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
