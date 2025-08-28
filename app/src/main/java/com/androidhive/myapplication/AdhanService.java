package com.androidhive.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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


public class AdhanService extends Service {

    private static final String TAG = "AdhanService";
    private static final String NOTIFICATION_CHANNEL_ID = "AdhanServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private Handler handler;
    private FusedLocationProviderClient fusedLocationClient;

    private double latitude, longitude;
    private PrayerTimes prayerTimes;
    private boolean shouldPlayAdhanSound;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            this.latitude = intent.getDoubleExtra("latitude", 0.0);
            this.longitude = intent.getDoubleExtra("longitude", 0.0);
            Log.d(TAG, "Using manual location: " + latitude + ", " + longitude);
            calculateAndSchedulePrayerTimes();
        } else {
            // If no manual location, use GPS
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
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("سرویس اذان در حال اجرا است")
                .setContentText("اوقات شرعی به صورت خودکار رصد می‌شود.")
                .setSmallIcon(R.mipmap.ic_launcher) // Use your app's launcher icon
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
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
            Toast.makeText(this, "لطفاً مجوز دسترسی به مکان را به برنامه بدهید.", Toast.LENGTH_LONG).show();
            stopSelf();
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
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
