package com.androidhive.myapplication;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androidhive.myapplication.R;
import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class PrayerTimesActivity extends AppCompatActivity {

    private TextView textFajr, textSunrise, textDHUHR, textAsr, textMaghrib, textIsha;
    private TextView textNextPrayerTime, textRemainingTime;
    private TextView textManualLocation;
    private Spinner spinnerCalculationMethod;
    private Button btnPlayAdhan;
    private Switch switchAutoAdhan;
    private MediaPlayer mediaPlayer;

    private FusedLocationProviderClient fusedLocationClient;

    private PrayerTimes currentPrayerTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_times);

        // Initialize UI components
        textFajr = findViewById(R.id.text_fajr);
        textSunrise = findViewById(R.id.text_sunrise);
        textDHUHR = findViewById(R.id.text_dhuhr);
        textAsr = findViewById(R.id.text_asr);
        textMaghrib = findViewById(R.id.text_maghrib);
        textIsha = findViewById(R.id.text_isha);
        textNextPrayerTime = findViewById(R.id.text_next_prayer_time);
        textRemainingTime = findViewById(R.id.text_remaining_time);
        textManualLocation = findViewById(R.id.text_manual_location);
        spinnerCalculationMethod = findViewById(R.id.spinner_calculation_method);
        btnPlayAdhan = findViewById(R.id.btn_play_adhan);
        switchAutoAdhan = findViewById(R.id.switch_auto_adhan);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // تنظیمات Spinner برای روش‌های محاسبه
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.calculation_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCalculationMethod.setAdapter(adapter);

        spinnerCalculationMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePrayerTimes(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // دکمه پخش دستی اذان
        btnPlayAdhan.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                playAdhan();
            } else if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayAdhan.setText("پخش دستی اذان");
            } else {
                mediaPlayer.start();
                btnPlayAdhan.setText("مکث");
            }
        });

        // کنترل پخش خودکار اذان
        switchAutoAdhan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent serviceIntent = new Intent(this, AdhanService.class);
            if (isChecked) {
                startService(serviceIntent);
            } else {
                stopService(serviceIntent);
            }
        });

        // به‌روزرسانی اولیه اوقات شرعی
        updatePrayerTimes(null);
    }

    private void updatePrayerTimes(Location manualLocation) {
        try {
            if (manualLocation != null) {
                calculateAndDisplayPrayerTimes(manualLocation.getLatitude(), manualLocation.getLongitude());
            } else {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        calculateAndDisplayPrayerTimes(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(this, "مکان فعلی در دسترس نیست. لطفا GPS را روشن کنید.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "لطفاً مجوز دسترسی به مکان را به برنامه بدهید.", Toast.LENGTH_LONG).show();
        }
    }


    private void calculateAndDisplayPrayerTimes(double latitude, double longitude) {
        DateComponents date = DateComponents.from(new Date());
        Coordinates coordinates = new Coordinates(latitude, longitude);
        CalculationMethod selectedMethod = getSelectedCalculationMethod();
        com.batoulapps.adhan.CalculationParameters params = selectedMethod.getParameters();
        PrayerTimes prayerTimes = new PrayerTimes(coordinates, date, params);

        currentPrayerTimes = prayerTimes;

        textFajr.setText("فجر: " + formatTime(prayerTimes.fajr));
        textSunrise.setText("طلوع: " + formatTime(prayerTimes.sunrise));
        textDHUHR.setText("ظهر: " + formatTime(prayerTimes.dhuhr));
        textAsr.setText("عصر: " + formatTime(prayerTimes.asr));
        textMaghrib.setText("مغرب: " + formatTime(prayerTimes.maghrib));
        textIsha.setText("عشا: " + formatTime(prayerTimes.isha));

        updateNextPrayerInfo(prayerTimes);
    }


    private void updateNextPrayerInfo(PrayerTimes prayerTimes) {
        Prayer nextPrayer = prayerTimes.nextPrayer();
        if (nextPrayer != Prayer.NONE) {
            Date nextPrayerTime = prayerTimes.timeForPrayer(nextPrayer);
            long remainingMillis = nextPrayerTime.getTime() - new Date().getTime();
            long hours = TimeUnit.MILLISECONDS.toHours(remainingMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60;

            textNextPrayerTime.setText("نماز بعدی: " + mapPrayerName(nextPrayer));
            String timeRemainingString = String.format(Locale.US, "زمان باقی‌مانده: %02d:%02d:%02d", hours, minutes, seconds);
            textRemainingTime.setText(timeRemainingString);
        } else {
            textNextPrayerTime.setText("اوقات شرعی به پایان رسیده است.");
            textRemainingTime.setText("");
        }
    }

    private CalculationMethod getSelectedCalculationMethod() {
        String selectedMethod = (String) spinnerCalculationMethod.getSelectedItem();
        switch (selectedMethod) {
            case "Dubai": return CalculationMethod.DUBAI;
            case "Muslim World League": return CalculationMethod.MUSLIM_WORLD_LEAGUE;
            case "Egyptian General Authority of Survey": return CalculationMethod.EGYPTIAN;
            case "University of Islamic Sciences, Karachi": return CalculationMethod.KARACHI;
            case "Islamic Society of North America": return CalculationMethod.NORTH_AMERICA;
            case "Umm Al-Qura University, Makkah": return CalculationMethod.UMM_AL_QURA;
            default: return CalculationMethod.KARACHI;
        }
    }

    private String formatTime(Date date) {
        if (date == null) {
            return "--:--";
        }
        return date.toString().substring(11, 16);
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

    private void playAdhan() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = MediaPlayer.create(this, R.raw.dhan_sound);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                Toast.makeText(this, "اذان در حال پخش است.", Toast.LENGTH_SHORT).show();
                btnPlayAdhan.setText("مکث");

                mediaPlayer.setOnCompletionListener(mp -> {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    btnPlayAdhan.setText("پخش دستی اذان");
                });
            } else {
                Toast.makeText(this, "خطا در بارگذاری فایل صوتی.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "خطا در پخش اذان. مطمئن شوید فایل صوتی در res/raw/adhan_sound.mp3 وجود دارد.", Toast.LENGTH_LONG).show();
            btnPlayAdhan.setText("پخش دستی اذان");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updatePrayerTimes(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
