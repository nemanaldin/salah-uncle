package com.androidhive.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

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
import android.content.SharedPreferences;



public class AdhanService extends Service {

    private static final String TAG = "AdhanService";
    private static final String NOTIFICATION_CHANNEL_ID = "AdhanServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    public static final String PREFS_NAME = "PrayerAppPrefs";
    public static final String KEY_LOCATION_MODE = "location_mode";
    public static final String KEY_MANUAL_LAT = "manual_lat";
    public static final String KEY_MANUAL_LON = "manual_lon";
    public static final String KEY_LAST_LAT = "last_lat";
    public static final String KEY_LAST_LON = "last_lon";

    private MediaPlayer mediaPlayer;
    private Timer timer;
    private Handler handler;
    private FusedLocationProviderClient fusedLocationClient;

    private double latitude, longitude;
    private PrayerTimes prayerTimes;
    private boolean shouldPlayAdhanSound;
    private Runnable notificationTicker;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    
        // آپدیت دوره‌ای نوتیفیکیشن (هر 60 ثانیه) برای نمایش شمارش معکوس
        notificationTicker = new Runnable() {
            @Override
            public void run() {
                updateForegroundNotification();
                handler.postDelayed(this, 60_000);
            }
        };
        handler.post(notificationTicker);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the service as a foreground service to prevent it from being killed
        createNotificationChannel();
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);

        // Retrieve the user's preference for playing adhan sound
        if (intent != null && intent.hasExtra("shouldPlayAdhanSound")) {
            shouldPlayAdhanSound = intent.getBooleanExtra("shouldPlayAdhanSound", true);
        } else {
            shouldPlayAdhanSound = true; // Default to playing the sound if the preference is not passed
        }

        // Check for manual location in the intent
        // 1) اولویت: مختصات پاس داده شده از Activity (اگر وجود داشت)
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            this.latitude = intent.getDoubleExtra("latitude", 0.0);
            this.longitude = intent.getDoubleExtra("longitude", 0.0);
            Log.d(TAG, "Using intent location: " + latitude + ", " + longitude);
            calculateAndSchedulePrayerTimes();
            return START_STICKY;
        }

// 2) اگر Intent چیزی نداشت (مثلاً بعد از ری‌استارت سیستم)، از SharedPreferences استفاده کن
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String mode = prefs.getString(KEY_LOCATION_MODE, "gps");

        if ("manual".equals(mode) && prefs.contains(KEY_MANUAL_LAT) && prefs.contains(KEY_MANUAL_LON)) {
            this.latitude = prefs.getFloat(KEY_MANUAL_LAT, 0f);
            this.longitude = prefs.getFloat(KEY_MANUAL_LON, 0f);
            Log.d(TAG, "Using saved manual location: " + latitude + ", " + longitude);
            calculateAndSchedulePrayerTimes();
        } else if (prefs.contains(KEY_LAST_LAT) && prefs.contains(KEY_LAST_LON)) {
            // fallback: آخرین مختصات موفق (GPS یا دستی)
            this.latitude = prefs.getFloat(KEY_LAST_LAT, 0f);
            this.longitude = prefs.getFloat(KEY_LAST_LON, 0f);
            Log.d(TAG, "Using saved last location: " + latitude + ", " + longitude);
            calculateAndSchedulePrayerTimes();
        } else {
            // 3) در نهایت تلاش با GPS
            Log.d(TAG, "Using GPS location.");
            getLastKnownLocation();
        }

        return START_STICKY;
    }
 
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "کانال اذان";
            String description = "نوتیفیکیشن برای پخش اذان و یادآوری اوقات شرعی";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

        private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, PrayerTimesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String nextName = prefs.getString("next_prayer_name_fa", null);
        long nextMillis = prefs.getLong("next_prayer_time_millis", 0L);

        String title;
        String text;

        if (nextName != null && nextMillis > 0L) {
            java.text.DateFormat df = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.US);
            String hhmm = df.format(new java.util.Date(nextMillis));

            long diff = nextMillis - System.currentTimeMillis();
            if (diff < 0) diff = 0;

            title = "نماز بعدی: " + nextName + " — " + hhmm;
            text = formatHms(diff) + " مانده تا اذان";
        } else {
            title = "سرویس اذان فعال است";
            text = "در حال دریافت زمان‌های بعدی…";
        }

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateForegroundNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    private String formatHms(long millis) {
        long totalSec = millis / 1000L;
        long h = totalSec / 3600L;
        long m = (totalSec % 3600L) / 60L;
        long s = totalSec % 60L;
        return String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, s);
    }


    // This method gets the last known GPS location and schedules the first prayer time.
    private void getLastKnownLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    this.latitude = location.getLatitude();
                    this.longitude = location.getLongitude();
                    calculateAndSchedulePrayerTimes();
                } else {
                    Toast.makeText(this, "مکان فعلی در دسترس نیست. سرویس متوقف شد.", Toast.LENGTH_LONG).show();
                    stopSelf();
                }
            });
        } catch (SecurityException e) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            if (prefs.contains(KEY_LAST_LAT) && prefs.contains(KEY_LAST_LON)) {
                this.latitude = prefs.getFloat(KEY_LAST_LAT, 0f);
                this.longitude = prefs.getFloat(KEY_LAST_LON, 0f);
                Log.d(TAG, "GPS null; using saved last location: " + latitude + ", " + longitude);
                calculateAndSchedulePrayerTimes();
            } else {
                Toast.makeText(this, "مکان فعلی در دسترس نیست. سرویس متوقف شد.", Toast.LENGTH_LONG).show();
                stopSelf();
            }

        }
    }

    // This is the core logic. It calculates prayer times and schedules a timer for the next prayer.
    private void calculateAndSchedulePrayerTimes() {
        // Cancel any existing timer to avoid conflicts
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // Calculate prayer times for the current date
        DateComponents date = DateComponents.from(new Date());
        com.batoulapps.adhan.CalculationParameters params = com.batoulapps.adhan.CalculationMethod.KARACHI.getParameters();
        prayerTimes = new PrayerTimes(new Coordinates(latitude, longitude), date, params);
        Log.d(TAG, "Prayer times recalculated for (" + latitude + ", " + longitude + ")");

        // Find the next prayer time to schedule
        Prayer nextPrayer = prayerTimes.nextPrayer();
        if (nextPrayer != Prayer.NONE) {
            Date nextPrayerTime = prayerTimes.timeForPrayer(nextPrayer);
            long delayMillis = nextPrayerTime.getTime() - new Date().getTime();

            // Log the time to help with debugging
            Log.d(TAG, "Next prayer: " + mapPrayerName(nextPrayer) + " at " + nextPrayerTime.toString());
            Log.d(TAG, "Scheduling adhan in " + TimeUnit.MILLISECONDS.toMinutes(delayMillis) + " minutes.");

            // ذخیره «نماز بعدی» برای نمایش در صفحه اصلی و نوتیفیکیشن
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putLong("next_prayer_time_millis", nextPrayerTime.getTime());
            editor.putString("next_prayer_name_fa", mapPrayerName(nextPrayer));
            editor.apply();
            updateForegroundNotification();


            // Schedule a one-shot timer for the next prayer time
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(() -> {
                        // Play adhan and notify the user
                        playAdhanAndNotify(mapPrayerName(nextPrayer));

                        // After playing adhan, reschedule for the next prayer
                        calculateAndSchedulePrayerTimes();
                    });
                }
            }, delayMillis);
        } else {
            // If all prayers for today have passed, schedule for Fajr of the next day
            DateComponents nextDay = DateComponents.from(new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(1)));
            PrayerTimes nextDayPrayerTimes = new PrayerTimes(new Coordinates(latitude, longitude), nextDay, params);
            Date nextDayFajr = nextDayPrayerTimes.fajr;
            long delayMillis = nextDayFajr.getTime() - new Date().getTime();

            Log.d(TAG, "All prayers passed for today. Scheduling for Fajr of next day.");
            Log.d(TAG, "Next Fajr: " + nextDayFajr.toString());

            // ذخیره نماز بعدی (فجرِ فردا) برای نمایش در صفحه اصلی و نوتیفیکیشن
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putLong("next_prayer_time_millis", nextDayFajr.getTime());
            editor.putString("next_prayer_name_fa", mapPrayerName(Prayer.FAJR));
            editor.apply();
            updateForegroundNotification();


            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(() -> {
                        playAdhanAndNotify(mapPrayerName(Prayer.FAJR));
                        calculateAndSchedulePrayerTimes();
                    });
                }
            }, delayMillis);
        }
    }

    private void playAdhanAndNotify(String prayerName) {
        // Create a separate notification for the adhan time
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, PrayerTimesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("وقت اذان " + prayerName)
                .setContentText("اذان " + prayerName + " هم‌اکنون است.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify(1, notification);

        // Play the adhan sound only if the user has enabled it
        if (shouldPlayAdhanSound) {
            playAdhan();
            // A toast is still a good idea for immediate user feedback
            handler.post(() -> Toast.makeText(getApplicationContext(), "اذان " + prayerName + " پخش می‌شود.", Toast.LENGTH_LONG).show());
        } else {
            handler.post(() -> Toast.makeText(getApplicationContext(), "وقت اذان " + prayerName + " است.", Toast.LENGTH_LONG).show());
        }
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

    private String mapPrayerName(Prayer prayer) {
        switch (prayer) {
            case FAJR: return "فجر";
            case SUNRISE: return "طلوع آفتاب";
            case DHUHR: return "ظهر";
            case ASR: return "عصر";
            case MAGHRIB: return "مغرب";
            case ISHA: return "عشا";
            default: return "نامعلوم";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        // Stop the foreground service
        stopForeground(true);
        Toast.makeText(this, "سرویس پخش اذان متوقف شد.", Toast.LENGTH_SHORT).show();
    
        if (notificationTicker != null) handler.removeCallbacks(notificationTicker);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}