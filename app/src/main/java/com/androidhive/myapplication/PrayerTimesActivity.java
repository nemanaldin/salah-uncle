package com.androidhive.myapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class PrayerTimesActivity extends AppCompatActivity {

    private TextView textFajr, textSunrise, textDHUHR, textAsr, textMaghrib, textIsha;
    private TextView textNextPrayerTime, textRemainingTime, textCurrentLocation;

    private Spinner spinnerCalculationMethod;
    private Spinner spinnerCountry, spinnerProvince, spinnerCity;

    private RadioGroup radioGroupLocation;
    private LinearLayout layoutManualLocation;

    private Button btnPlayAdhan, btnSetManualLocation, btnGetLocation;
    private Switch switchAutoAdhan, switchPlayAdhanSound;

    private FusedLocationProviderClient fusedLocationClient;
    private final Map<String, Map<String, Map<String, LocationData>>> locationDataMap = new HashMap<>();
    private LocationData selectedManualLocation;

    private PrayerTimes prayerTimes;

    private MediaPlayer mediaPlayer;

    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;

    // SharedPreferences برای انتخاب دستی کشور/استان/شهر
    private static final String PREFS_NAME = "ManualLocationPrefs";
    private static final String KEY_LOCATION_MODE = "location_mode"; // manual/gps/none
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_PROVINCE = "province";
    private static final String KEY_CITY = "city";

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_times);

        // Exact Alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            }
        }

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // حتی اگر granted نباشه هم ما UI رو آپدیت می‌کنیم
                    // فقط نوتیفیکیشن ممکنه نمایش داده نشه
                    PrayerScheduler.computeAndScheduleNext(this);
                    PrayerNotification.showOrUpdate(this, false);
                }
        );

        initializeUIComponents();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadLocationDataFromJson();
        setupSpinners();
        setupListeners();
        loadAndApplySavedPreferences();


        // هماهنگ‌سازی UI دکمه‌ها با حالت انتخاب‌شده (GPS/دستی)
        updateLocationModeUI(radioGroupLocation.getCheckedRadioButtonId() == R.id.radio_manual);
        // ✅ مهم: از این به بعد next prayer همیشه حساب می‌شود، مستقل از Auto
        PrayerScheduler.computeAndScheduleNext(this);
        PrayerNotification.showOrUpdate(this, false); // بدون دیرینگ
    }

    private void initializeUIComponents() {
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
        btnSetManualLocation = findViewById(R.id.btn_set_manual_location);
        btnGetLocation = findViewById(R.id.btn_get_location);

        radioGroupLocation = findViewById(R.id.radio_group_location);
        layoutManualLocation = findViewById(R.id.layout_manual_location);

        spinnerCountry = findViewById(R.id.spinner_country);
        spinnerProvince = findViewById(R.id.spinner_province);
        spinnerCity = findViewById(R.id.spinner_city);

        switchAutoAdhan = findViewById(R.id.switch_auto_adhan);
        switchPlayAdhanSound = findViewById(R.id.switch_play_adhan_sound);

        // مقدار اولیه سوئیچ‌ها
        switchAutoAdhan.setChecked(AppPrefs.isAutoAdhanEnabled(this));
        switchPlayAdhanSound.setChecked(AppPrefs.isPrayerNotificationsEnabled(this));
    }


    /**
     * کنترل UI بین حالت GPS و دستی
     * - در حالت دستی: اسپینرها و دکمه تنظیم مکان دستی نمایش داده شوند و دکمه گرفتن مکان (GPS) مخفی شود
     * - در حالت GPS: اسپینرها و دکمه دستی مخفی و دکمه گرفتن مکان نمایش داده شود
     */
    private void updateLocationModeUI(boolean isManual) {
        layoutManualLocation.setVisibility(isManual ? View.VISIBLE : View.GONE);
        btnSetManualLocation.setVisibility(isManual ? View.VISIBLE : View.GONE);
        btnGetLocation.setVisibility(isManual ? View.GONE : View.VISIBLE);
    }


    private void setupListeners() {

        // 🔊 پخش دستی اذان (فقط دکمه تست)
        btnPlayAdhan.setOnClickListener(v -> playOrPauseAdhan());

        // ✅ سوئیچ 1: پخش خودکار صدا
        switchAutoAdhan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPrefs.setAutoAdhanEnabled(this, isChecked);

            // اگر صدا روشن شد ولی نوتیف خاموش بود، نوتیف رو روشن کن (منطقی)
            if (isChecked && !AppPrefs.isPrayerNotificationsEnabled(this)) {
                AppPrefs.setPrayerNotificationsEnabled(this, true);
                switchPlayAdhanSound.setChecked(true);
            }

            checkAndRequestNotificationPermission();
            PrayerScheduler.computeAndScheduleNext(this);
            PrayerNotification.showOrUpdate(this, false);
        });

        // ✅ سوئیچ 2: فقط نوتیفیکیشن وقت اذان (بدون صدا)
        switchPlayAdhanSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPrefs.setPrayerNotificationsEnabled(this, isChecked);

            // اگر نوتیف خاموش شد، صدا هم خاموش بشه
            if (!isChecked && AppPrefs.isAutoAdhanEnabled(this)) {
                AppPrefs.setAutoAdhanEnabled(this, false);
                switchAutoAdhan.setChecked(false);
            }

            checkAndRequestNotificationPermission();
            PrayerScheduler.computeAndScheduleNext(this);
            PrayerNotification.showOrUpdate(this, false);
        });

        // ✅ انتخاب GPS/Manual
        radioGroupLocation.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_gps) {
                updateLocationModeUI(false);
                saveLocationMode("gps");

                // label برای main
                AppPrefs.setLocationMode(this, "gps");
                AppPrefs.setManualCityName(this, "");

                updatePrayerTimesWithGPS();
            } else if (checkedId == R.id.radio_manual) {
                updateLocationModeUI(true);
                saveLocationMode("manual");

                AppPrefs.setLocationMode(this, "manual");

                textCurrentLocation.setText("در انتظار انتخاب مکان دستی");
            }
        });

        // ✅ دریافت GPS
        btnGetLocation.setOnClickListener(v -> updatePrayerTimesWithGPS());

        // ✅ ست کردن مکان دستی
        btnSetManualLocation.setOnClickListener(v -> {
            if (selectedManualLocation == null) {
                Toast.makeText(this, "لطفاً شهر را انتخاب کنید.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveManualLocationSelection();

            double lat = selectedManualLocation.latitude;
            double lon = selectedManualLocation.longitude;

            AppPrefs.saveLocation(this, lat, lon);

            String cityName = String.valueOf(spinnerCity.getSelectedItem());
            AppPrefs.setLocationMode(this, "manual");
            AppPrefs.setManualCityName(this, cityName);

            calculateAndDisplayPrayerTimes(lat, lon);

            // ✅ همیشه next prayer ذخیره و برنامه‌ریزی شود
            PrayerScheduler.computeAndScheduleNext(this);
            PrayerNotification.showOrUpdate(this, false);

            Toast.makeText(this, "مکان دستی تنظیم شد: " + cityName, Toast.LENGTH_SHORT).show();
        });

        // ✅ تغییر روش محاسبه → محاسبه مجدد
        spinnerCalculationMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                double lat = AppPrefs.getLat(PrayerTimesActivity.this);
                double lon = AppPrefs.getLon(PrayerTimesActivity.this);

                if (lat != 0 || lon != 0) {
                    calculateAndDisplayPrayerTimes(lat, lon);
                    PrayerScheduler.computeAndScheduleNext(PrayerTimesActivity.this);
                    PrayerNotification.showOrUpdate(PrayerTimesActivity.this, false);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void checkAndRequestNotificationPermission() {
        // Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void updatePrayerTimesWithGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this, "مکان GPS یافت نشد.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    AppPrefs.saveLocation(this, lat, lon);
                    AppPrefs.setLocationMode(this, "gps");
                    AppPrefs.setManualCityName(this, "");

                    calculateAndDisplayPrayerTimes(lat, lon);

                    PrayerScheduler.computeAndScheduleNext(this);
                    PrayerNotification.showOrUpdate(this, false);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "خطا در دریافت مکان GPS", Toast.LENGTH_SHORT).show());
    }

    private void calculateAndDisplayPrayerTimes(double latitude, double longitude) {
        Coordinates coordinates = new Coordinates(latitude, longitude);
        DateComponents date = DateComponents.from(new Date());
        CalculationParameters params = getCalculationParamsFromSpinner();

        prayerTimes = new PrayerTimes(coordinates, date, params);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        textFajr.setText("صبح: " + sdf.format(prayerTimes.fajr));
        textSunrise.setText("طلوع: " + sdf.format(prayerTimes.sunrise));
        textDHUHR.setText("ظهر: " + sdf.format(prayerTimes.dhuhr));
        textAsr.setText("عصر: " + sdf.format(prayerTimes.asr));
        textMaghrib.setText("مغرب: " + sdf.format(prayerTimes.maghrib));
        textIsha.setText("عشاء: " + sdf.format(prayerTimes.isha));

        updateNextPrayerAndCountdown();

        // label
        String mode = AppPrefs.getLocationMode(this);
        if ("manual".equals(mode)) {
            String city = AppPrefs.getManualCityName(this);
            if (city == null || city.trim().isEmpty()) city = "مکان دستی";
            textCurrentLocation.setText("موقعیت فعلی: " + city);
        } else if ("gps".equals(mode)) {
            textCurrentLocation.setText(String.format(Locale.US, "موقعیت فعلی: %.5f, %.5f", latitude, longitude));
        } else {
            textCurrentLocation.setText("در انتظار انتخاب مکان");
            updateLocationModeUI(false);
        }
    }

    private void updateNextPrayerAndCountdown() {
        if (prayerTimes == null) return;

        long now = System.currentTimeMillis();
        Date nextTime = null;
        String nextName = "";

        if (prayerTimes.fajr != null && prayerTimes.fajr.getTime() > now) { nextTime = prayerTimes.fajr; nextName = "صبح"; }
        else if (prayerTimes.dhuhr != null && prayerTimes.dhuhr.getTime() > now) { nextTime = prayerTimes.dhuhr; nextName = "ظهر"; }
        else if (prayerTimes.asr != null && prayerTimes.asr.getTime() > now) { nextTime = prayerTimes.asr; nextName = "عصر"; }
        else if (prayerTimes.maghrib != null && prayerTimes.maghrib.getTime() > now) { nextTime = prayerTimes.maghrib; nextName = "مغرب"; }
        else if (prayerTimes.isha != null && prayerTimes.isha.getTime() > now) { nextTime = prayerTimes.isha; nextName = "عشاء"; }
        else if (prayerTimes.fajr != null) { nextTime = new Date(prayerTimes.fajr.getTime() + 24L * 60L * 60L * 1000L); nextName = "صبح"; }

        if (nextTime == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        textNextPrayerTime.setText("نماز بعدی: " + nextName + " - " + sdf.format(nextTime));

        if (countdownRunnable != null) countdownHandler.removeCallbacks(countdownRunnable);

        final Date finalNextTime = nextTime;
        countdownRunnable = new Runnable() {
            @Override public void run() {
                long diff = finalNextTime.getTime() - System.currentTimeMillis();
                if (diff < 0) diff = 0;

                long total = diff / 1000;
                long h = total / 3600;
                long m = (total % 3600) / 60;
                long s = total % 60;

                textRemainingTime.setText(String.format(Locale.getDefault(),
                        "زمان باقی‌مانده: %02d:%02d:%02d", h, m, s));

                if (diff > 0) countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    private CalculationParameters getCalculationParamsFromSpinner() {
        int position = spinnerCalculationMethod.getSelectedItemPosition();
        CalculationMethod method;
        switch (position) {
            case 1: method = CalculationMethod.MUSLIM_WORLD_LEAGUE; break;
            case 2: method = CalculationMethod.EGYPTIAN; break;
            case 3: method = CalculationMethod.KARACHI; break;
            case 4: method = CalculationMethod.UMM_AL_QURA; break;
            case 5: method = CalculationMethod.DUBAI; break;
            case 6: method = CalculationMethod.MOON_SIGHTING_COMMITTEE; break;
            default: method = CalculationMethod.KARACHI; break;
        }
        return method.getParameters();
    }

    // ========== JSON LOCATIONS ==========
    private void loadLocationDataFromJson() {
        try {
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open("locations.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);

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

                        double lat = cityObject.has("lat") ? cityObject.getDouble("lat") : cityObject.getDouble("latitude");
                        double lon = cityObject.has("lon") ? cityObject.getDouble("lon") : cityObject.getDouble("longitude");

                        citiesMap.put(cityName, new LocationData(lat, lon));
                    }
                    provincesMap.put(provinceName, citiesMap);
                }
                locationDataMap.put(countryName, provincesMap);
            }
        } catch (Exception e) {
            Toast.makeText(this, "خطا در خواندن locations.json", Toast.LENGTH_LONG).show();
        }
    }

    private void setupSpinners() {
        // calc methods
        ArrayAdapter<CharSequence> calcAdapter = ArrayAdapter.createFromResource(this,
                R.array.calculation_methods, R.layout.spinner_item_light);
        calcAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_light);
        spinnerCalculationMethod.setAdapter(calcAdapter);

        // countries
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_light, new java.util.ArrayList<>(locationDataMap.keySet()));
        countryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_light);
        spinnerCountry.setAdapter(countryAdapter);

        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String country = String.valueOf(spinnerCountry.getSelectedItem());
                loadProvinces(country);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String country = String.valueOf(spinnerCountry.getSelectedItem());
                String province = String.valueOf(spinnerProvince.getSelectedItem());
                loadCities(country, province);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String country = String.valueOf(spinnerCountry.getSelectedItem());
                String province = String.valueOf(spinnerProvince.getSelectedItem());
                String city = String.valueOf(spinnerCity.getSelectedItem());
                selectedManualLocation = getLocationData(country, province, city);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadProvinces(String country) {
        Map<String, Map<String, LocationData>> provinces = locationDataMap.get(country);
        if (provinces == null) provinces = new HashMap<>();

        java.util.ArrayList<String> provinceList = new java.util.ArrayList<>(provinces.keySet());
        java.util.Collections.sort(provinceList);

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_light, provinceList);
        provinceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_light);
        spinnerProvince.setAdapter(provinceAdapter);

        if (!provinceList.isEmpty()) spinnerProvince.setSelection(0);
    }

    private void loadCities(String country, String province) {
        Map<String, Map<String, LocationData>> provinces = locationDataMap.get(country);
        if (provinces == null) return;

        Map<String, LocationData> cities = provinces.get(province);
        if (cities == null) cities = new HashMap<>();

        java.util.ArrayList<String> cityList = new java.util.ArrayList<>(cities.keySet());
        java.util.Collections.sort(cityList);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_light, cityList);
        cityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_light);
        spinnerCity.setAdapter(cityAdapter);

        if (!cityList.isEmpty()) spinnerCity.setSelection(0);
    }

    private LocationData getLocationData(String country, String province, String city) {
        Map<String, Map<String, LocationData>> provinces = locationDataMap.get(country);
        if (provinces == null) return null;
        Map<String, LocationData> cities = provinces.get(province);
        if (cities == null) return null;
        return cities.get(city);
    }

    private void saveLocationMode(String mode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_LOCATION_MODE, mode).apply();
    }

    private void saveManualLocationSelection() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_COUNTRY, String.valueOf(spinnerCountry.getSelectedItem()))
                .putString(KEY_PROVINCE, String.valueOf(spinnerProvince.getSelectedItem()))
                .putString(KEY_CITY, String.valueOf(spinnerCity.getSelectedItem()))
                .apply();
    }

    private void loadAndApplySavedPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String locationMode = prefs.getString(KEY_LOCATION_MODE, "none");

        if ("manual".equals(locationMode)) {
            radioGroupLocation.check(R.id.radio_manual);
            updateLocationModeUI(true);

            String savedCountry = prefs.getString(KEY_COUNTRY, null);
            String savedProvince = prefs.getString(KEY_PROVINCE, null);
            String savedCity = prefs.getString(KEY_CITY, null);

            if (savedCountry != null && savedProvince != null && savedCity != null) {
                selectSpinnerByValue(spinnerCountry, savedCountry);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    selectSpinnerByValue(spinnerProvince, savedProvince);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        selectSpinnerByValue(spinnerCity, savedCity);
                        // بعد از restore، اگر لوکیشن هم داریم، محاسبه کن
                        double lat = AppPrefs.getLat(this);
                        double lon = AppPrefs.getLon(this);
                        if (lat != 0 || lon != 0) {
                            calculateAndDisplayPrayerTimes(lat, lon);
                        }
                    }, 300);
                }, 300);
            }
        } else if ("gps".equals(locationMode)) {
            radioGroupLocation.check(R.id.radio_gps);
            updateLocationModeUI(false);
            updatePrayerTimesWithGPS();
        } else {
            textCurrentLocation.setText("در انتظار انتخاب مکان");
            updateLocationModeUI(false);
        }
    }

    private void selectSpinnerByValue(Spinner spinner, String value) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i))) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void playOrPauseAdhan() {
        // ✅ این فقط دکمه تست است، ربطی به auto/notif ندارد
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                btnPlayAdhan.setText("پخش دستی اذان");
                return;
            }

            mediaPlayer = MediaPlayer.create(this, R.raw.dhan_sound);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
                btnPlayAdhan.setText("پخش دستی اذان");
            });
            mediaPlayer.start();
            btnPlayAdhan.setText("توقف اذان");

        } catch (Exception e) {
            Toast.makeText(this, "خطا در پخش اذان", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countdownRunnable != null) countdownHandler.removeCallbacks(countdownRunnable);

        // اگر اذان دستی در حال پخش بود، قطعش کن
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
            btnPlayAdhan.setText("پخش دستی اذان");
        }
    }

    private static class LocationData {
        final double latitude;
        final double longitude;
        LocationData(double lat, double lon) { this.latitude = lat; this.longitude = lon; }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
