package com.androidhive.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class PrayerTimesActivity extends AppCompatActivity {

    private TextView textFajr, textSunrise, textDHUHR, textAsr, textMaghrib, textIsha;
    private TextView textNextPrayerTime, textRemainingTime;
    private TextView textCurrentLocation;
    private Spinner spinnerCalculationMethod, spinnerCountry, spinnerProvince, spinnerCity;
    private Button btnPlayAdhan, btnSetManualLocation;
    private Switch switchAutoAdhan, switchPlayAdhanSound;
    private RadioGroup radioGroupLocation;
    private LinearLayout layoutManualLocation;
    private MediaPlayer mediaPlayer;

    private FusedLocationProviderClient fusedLocationClient;

    private PrayerTimes currentPrayerTimes;

    private Map<String, Map<String, Map<String, LocationData>>> locationDataMap;
    private LocationData selectedManualLocation;

    // Register the permissions request launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "مجوز نوتیفیکیشن داده شد.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "بدون مجوز، نوتیفیکیشن‌ها نمایش داده نمی‌شوند.", Toast.LENGTH_LONG).show();
                    switchAutoAdhan.setChecked(false);
                }
            });


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
        textCurrentLocation = findViewById(R.id.text_current_location);
        spinnerCalculationMethod = findViewById(R.id.spinner_calculation_method);
        btnPlayAdhan = findViewById(R.id.btn_play_adhan);
        switchAutoAdhan = findViewById(R.id.switch_auto_adhan);
        switchPlayAdhanSound = findViewById(R.id.switch_play_adhan_sound);
        radioGroupLocation = findViewById(R.id.radio_group_location);
        layoutManualLocation = findViewById(R.id.layout_manual_location);
        spinnerCountry = findViewById(R.id.spinner_country);
        spinnerProvince = findViewById(R.id.spinner_province);
        spinnerCity = findViewById(R.id.spinner_city);
        btnSetManualLocation = findViewById(R.id.btn_set_manual_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationDataMap = new HashMap<>();
        loadLocationDataFromJson();
        setupSpinners();

        radioGroupLocation.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_gps) {
                layoutManualLocation.setVisibility(View.GONE);
                updatePrayerTimesWithGPS();
            } else {
                layoutManualLocation.setVisibility(View.VISIBLE);
                textCurrentLocation.setText("مکان فعلی: دستی");
            }
        });

        btnSetManualLocation.setOnClickListener(v -> {
            if (selectedManualLocation != null) {
                calculateAndDisplayPrayerTimes(selectedManualLocation.latitude, selectedManualLocation.longitude);
                String selectedCountry = (String) spinnerCountry.getSelectedItem();
                String selectedProvince = (String) spinnerProvince.getSelectedItem();
                String selectedCity = (String) spinnerCity.getSelectedItem();
                textCurrentLocation.setText(String.format("مکان فعلی: %s، %s، %s", selectedCountry, selectedProvince, selectedCity));

                if (switchAutoAdhan.isChecked()) {
                    startAdhanService();
                }

            } else {
                Toast.makeText(this, "لطفاً یک شهر را انتخاب کنید.", Toast.LENGTH_SHORT).show();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.calculation_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCalculationMethod.setAdapter(adapter);

        spinnerCalculationMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (radioGroupLocation.getCheckedRadioButtonId() == R.id.radio_gps) {
                    updatePrayerTimesWithGPS();
                } else {
                    if (selectedManualLocation != null) {
                        calculateAndDisplayPrayerTimes(selectedManualLocation.latitude, selectedManualLocation.longitude);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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

        switchAutoAdhan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Request notification permission before starting the service
                checkAndRequestNotificationPermission();
            } else {
                stopService(new Intent(this, AdhanService.class));
            }
        });

        updatePrayerTimesWithGPS();
    }

    // New method to check and request notification permission
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                startAdhanService();
            } else {
                // Request permission from the user
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // For older Android versions, permission is not required
            startAdhanService();
        }
    }

    // A new method to encapsulate the logic for starting the AdhanService
    private void startAdhanService() {
        Intent serviceIntent = new Intent(this, AdhanService.class);
        // Pass the user's preference for playing the sound
        serviceIntent.putExtra("shouldPlayAdhanSound", switchPlayAdhanSound.isChecked());

        // Stop the existing service before starting a new one
        stopService(serviceIntent);

        if (radioGroupLocation.getCheckedRadioButtonId() == R.id.radio_gps) {
            // Service will get GPS location
            startService(serviceIntent);
        } else {
            if (selectedManualLocation != null) {
                // Pass manual location to the service
                serviceIntent.putExtra("latitude", selectedManualLocation.latitude);
                serviceIntent.putExtra("longitude", selectedManualLocation.longitude);
                startService(serviceIntent);
            } else {
                Toast.makeText(this, "لطفاً یک شهر را به صورت دستی انتخاب کنید.", Toast.LENGTH_LONG).show();
                switchAutoAdhan.setChecked(false);
            }
        }
    }

    private void loadLocationDataFromJson() {
        String jsonString = null;
        try {
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open("locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "خطا در بارگذاری فایل مکان‌ها.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> countryKeys = jsonObject.keys();
            while (countryKeys.hasNext()) {
                String countryName = countryKeys.next();
                JSONObject countryObject = jsonObject.getJSONObject(countryName);
                Map<String, Map<String, LocationData>> provincesMap = new HashMap<>();

                Iterator<String> provinceKeys = countryObject.keys();
                while (provinceKeys.hasNext()) {
                    String provinceName = provinceKeys.next();
                    JSONObject provinceObject = countryObject.getJSONObject(provinceName);
                    Map<String, LocationData> citiesMap = new HashMap<>();

                    Iterator<String> cityKeys = provinceObject.keys();
                    while (cityKeys.hasNext()) {
                        String cityName = cityKeys.next();
                        JSONObject cityObject = provinceObject.getJSONObject(cityName);
                        double latitude = cityObject.getDouble("latitude");
                        double longitude = cityObject.getDouble("longitude");
                        citiesMap.put(cityName, new LocationData(latitude, longitude));
                    }
                    provincesMap.put(provinceName, citiesMap);
                }
                locationDataMap.put(countryName, provincesMap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "خطا در پردازش فایل JSON.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupSpinners() {
        List<String> countries = new ArrayList<>(locationDataMap.keySet());
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);

        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateProvinceSpinner((String) parent.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCitySpinner((String) spinnerCountry.getSelectedItem(), (String) parent.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = (String) spinnerCountry.getSelectedItem();
                String selectedProvince = (String) spinnerProvince.getSelectedItem();
                String selectedCity = (String) parent.getItemAtPosition(position);
                selectedManualLocation = locationDataMap.get(selectedCountry).get(selectedProvince).get(selectedCity);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateProvinceSpinner(String country) {
        List<String> provinces = new ArrayList<>(locationDataMap.get(country).keySet());
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);
    }

    private void updateCitySpinner(String country, String province) {
        List<String> cities = new ArrayList<>(locationDataMap.get(country).get(province).keySet());
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);
    }

    private void updatePrayerTimesWithGPS() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    calculateAndDisplayPrayerTimes(location.getLatitude(), location.getLongitude());
                    textCurrentLocation.setText(String.format("مکان فعلی: GPS (%.4f, %.4f)", location.getLatitude(), location.getLongitude()));
                } else {
                    Toast.makeText(this, "مکان فعلی در دسترس نیست. لطفا GPS را روشن کنید.", Toast.LENGTH_LONG).show();
                    textCurrentLocation.setText("مکان فعلی: GPS (ناموفق)");
                }
            });
        } catch (SecurityException e) {
            Toast.makeText(this, "لطفاً مجوز دسترسی به مکان را به برنامه بدهید.", Toast.LENGTH_LONG).show();
            textCurrentLocation.setText("مکان فعلی: GPS (مجوز لازم)");
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
        if (radioGroupLocation.getCheckedRadioButtonId() == R.id.radio_gps) {
            updatePrayerTimesWithGPS();
        } else {
            // No need to update if manual location is set, unless the button is clicked
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private static class LocationData {
        public double latitude;
        public double longitude;

        public LocationData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
