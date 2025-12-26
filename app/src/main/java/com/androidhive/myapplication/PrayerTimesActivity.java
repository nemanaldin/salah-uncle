package com.androidhive.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private Button btnPlayAdhan, btnSetManualLocation, btnGetLocation;
    private Switch switchAutoAdhan, switchPlayAdhanSound;
    private RadioGroup radioGroupLocation;
    private LinearLayout layoutManualLocation;
    private MediaPlayer mediaPlayer;

    private FusedLocationProviderClient fusedLocationClient;
    private PrayerTimes currentPrayerTimes;
    private Map<String, Map<String, Map<String, LocationData>>> locationDataMap;
    private LocationData selectedManualLocation;

    private boolean isInitialSpinnerSetup = true;

    public static final String PREFS_NAME = "PrayerAppPrefs";
    public static final String KEY_LOCATION_MODE = "location_mode";
    public static final String KEY_COUNTRY = "manual_country";
    public static final String KEY_PROVINCE = "manual_province";
    public static final String KEY_CITY = "manual_city";
    public static final String KEY_MANUAL_LAT = "manual_lat";
    public static final String KEY_MANUAL_LON = "manual_lon";
    public static final String KEY_LAST_LAT = "last_lat";
    public static final String KEY_LAST_LON = "last_lon";
    public static final String KEY_LAST_SOURCE = "last_source";
    public static final String KEY_LAST_UPDATED = "last_updated";


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

        initializeUIComponents();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationDataMap = new HashMap<>();
        loadLocationDataFromJson();
        setupSpinners();

        loadAndApplySavedPreferences();

        setupListeners();
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
        switchAutoAdhan = findViewById(R.id.switch_auto_adhan);
        switchPlayAdhanSound = findViewById(R.id.switch_play_adhan_sound);
        radioGroupLocation = findViewById(R.id.radio_group_location);
        layoutManualLocation = findViewById(R.id.layout_manual_location);
        spinnerCountry = findViewById(R.id.spinner_country);
        spinnerProvince = findViewById(R.id.spinner_province);
        spinnerCity = findViewById(R.id.spinner_city);
        btnSetManualLocation = findViewById(R.id.btn_set_manual_location);
        btnGetLocation= findViewById(R.id.btn_get_location);
    }

    private void setupListeners() {
        radioGroupLocation.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_gps) {
                layoutManualLocation.setVisibility(View.GONE);
                updatePrayerTimesWithGPS();
                saveLocationMode("gps");
            } else {
                layoutManualLocation.setVisibility(View.VISIBLE);
                // کاربر حالت دستی را انتخاب کرده؛ ذخیره کن تا بعد از بستن اپ هم همین بماند
                saveLocationMode("manual");
                textCurrentLocation.setText("مکان فعلی: دستی");
            }
        });

        btnSetManualLocation.setOnClickListener(v -> {
            if (selectedManualLocation != null) {
                String selectedCountry = (String) spinnerCountry.getSelectedItem();
                String selectedProvince = (String) spinnerProvince.getSelectedItem();
                String selectedCity = (String) spinnerCity.getSelectedItem();

                // اطمینان از اینکه آیتم های انتخاب شده معتبر هستند
                if (selectedCountry == null || selectedProvince == null || selectedCity == null) {
                    Toast.makeText(this, "لطفاً مکان را به طور کامل انتخاب کنید.", Toast.LENGTH_SHORT).show();
                    return;
                }

                calculateAndDisplayPrayerTimes(selectedManualLocation.latitude, selectedManualLocation.longitude);
                textCurrentLocation.setText(String.format("مکان فعلی: %s، %s، %s", selectedCountry, selectedProvince, selectedCity));
                saveManualLocation(selectedCountry, selectedProvince, selectedCity);

                if (switchAutoAdhan.isChecked()) {
                    startAdhanService();
                }
            } else {
                Toast.makeText(this, "لطفاً یک شهر را انتخاب کنید.", Toast.LENGTH_SHORT).show();
            }
        });

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePrayerTimesWithGPS();

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.calculation_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCalculationMethod.setAdapter(adapter);

        spinnerCalculationMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialSpinnerSetup) return;
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

        btnPlayAdhan.setOnClickListener(v -> playOrPauseAdhan());

        switchAutoAdhan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestNotificationPermission();
            } else {
                stopService(new Intent(this, AdhanService.class));
            }
        });
    }


    private void loadAndApplySavedPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String locationMode = prefs.getString(KEY_LOCATION_MODE, "gps");

        // اگر آخرین مختصات ذخیره شده داریم، قبل از هرچیز نمایش اولیه بده (برای اینکه بعد از ری‌استارت صفحه خالی نباشد)
        if (prefs.contains(KEY_LAST_LAT) && prefs.contains(KEY_LAST_LON)) {
            double lat = prefs.getFloat(KEY_LAST_LAT, 0f);
            double lon = prefs.getFloat(KEY_LAST_LON, 0f);
            if (lat != 0f || lon != 0f) {
                calculateAndDisplayPrayerTimes(lat, lon);
                String src = prefs.getString(KEY_LAST_SOURCE, "saved");
                textCurrentLocation.setText(String.format("مکان فعلی: آخرین مکان ذخیره‌شده (%s)", src));
            }
        }

        if ("manual".equals(locationMode)) {
            String country = prefs.getString(KEY_COUNTRY, null);
            String province = prefs.getString(KEY_PROVINCE, null);
            String city = prefs.getString(KEY_CITY, null);

            radioGroupLocation.check(R.id.radio_manual);

            if (country != null && province != null && city != null) {
                restoreManualSelection(country, province, city);
            } else if (prefs.contains(KEY_MANUAL_LAT) && prefs.contains(KEY_MANUAL_LON)) {
                // اگر رشته‌ها نبودند ولی مختصات دستی داریم
                double lat = prefs.getFloat(KEY_MANUAL_LAT, 0f);
                double lon = prefs.getFloat(KEY_MANUAL_LON, 0f);
                calculateAndDisplayPrayerTimes(lat, lon);
                textCurrentLocation.setText("مکان فعلی: دستی (ذخیره‌شده)");
            } else {
                // اطلاعات دستی ناقص است؛ برو GPS
                radioGroupLocation.check(R.id.radio_gps);
                updatePrayerTimesWithGPS();
            }
        } else {
            // GPS: تلاش برای آپدیت؛ اگر null شد خودش fallback می‌کند
            radioGroupLocation.check(R.id.radio_gps);
            updatePrayerTimesWithGPS();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> isInitialSpinnerSetup = false, 500);
    }


    private void saveManualLocation(String country, String province, String city) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_LOCATION_MODE, "manual");
        editor.putString(KEY_COUNTRY, country);
        editor.putString(KEY_PROVINCE, province);
        editor.putString(KEY_CITY, city);

        // مختصات دستی را هم ذخیره کن تا سرویس/ری‌استارت بدون JSON هم کار کند
        if (selectedManualLocation != null) {
            editor.putFloat(KEY_MANUAL_LAT, (float) selectedManualLocation.latitude);
            editor.putFloat(KEY_MANUAL_LON, (float) selectedManualLocation.longitude);

            // آخرین مختصات موفق را هم برای fallback ذخیره کن
            editor.putFloat(KEY_LAST_LAT, (float) selectedManualLocation.latitude);
            editor.putFloat(KEY_LAST_LON, (float) selectedManualLocation.longitude);
            editor.putString(KEY_LAST_SOURCE, "manual");
            editor.putLong(KEY_LAST_UPDATED, System.currentTimeMillis());
        }

        editor.apply();
    }


    private void saveLocationMode(String mode) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_LOCATION_MODE, mode);
        editor.apply();
    }

    /**
     * متد جدید و قابل اعتماد برای بازیابی حالت دستی
     */
    private void restoreManualSelection(String country, String province, String city) {
        // 1. اسپینرها را به صورت ظاهری تنظیم کن
        ArrayAdapter<String> countryAdapter = (ArrayAdapter<String>) spinnerCountry.getAdapter();
        if (countryAdapter != null) {
            int countryPosition = countryAdapter.getPosition(country);
            if (countryPosition >= 0) spinnerCountry.setSelection(countryPosition);
        }

        updateProvinceSpinner(country);
        ArrayAdapter<String> provinceAdapter = (ArrayAdapter<String>) spinnerProvince.getAdapter();
        if (provinceAdapter != null) {
            int provincePosition = provinceAdapter.getPosition(province);
            if (provincePosition >= 0) spinnerProvince.setSelection(provincePosition);
        }

        updateCitySpinner(country, province);
        ArrayAdapter<String> cityAdapter = (ArrayAdapter<String>) spinnerCity.getAdapter();
        if (cityAdapter != null) {
            int cityPosition = cityAdapter.getPosition(city);
            if (cityPosition >= 0) spinnerCity.setSelection(cityPosition);
        }

        // 2. اطلاعات موقعیت را مستقیما بخوان و اوقات شرعی را محاسبه کن
        LocationData savedLocation = locationDataMap.get(country).get(province).get(city);
        if (savedLocation != null) {
            this.selectedManualLocation = savedLocation; // متغیر اصلی را هم ست می‌کنیم
            calculateAndDisplayPrayerTimes(savedLocation.latitude, savedLocation.longitude);
            textCurrentLocation.setText(String.format("مکان فعلی: %s، %s، %s", country, province, city));
        } else {
            // اگر به هر دلیلی مکان پیدا نشد، به حالت GPS برگرد
            Toast.makeText(this, "مکان ذخیره شده یافت نشد، از GPS استفاده می‌شود.", Toast.LENGTH_SHORT).show();
            radioGroupLocation.check(R.id.radio_gps);
            updatePrayerTimesWithGPS();
        }
    }


    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        startAdhanService();
    }

    private void startAdhanService() {
        Intent serviceIntent = new Intent(this, AdhanService.class);
        serviceIntent.putExtra("shouldPlayAdhanSound", switchPlayAdhanSound.isChecked());
        stopService(serviceIntent);

        if (radioGroupLocation.getCheckedRadioButtonId() == R.id.radio_gps) {
            startService(serviceIntent);
        } else {
            if (selectedManualLocation != null) {
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
        try {
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open("locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
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
                        double latitude = cityObject.getDouble("latitude");
                        double longitude = cityObject.getDouble("longitude");
                        citiesMap.put(cityName, new LocationData(latitude, longitude));
                    }
                    provincesMap.put(provinceName, citiesMap);
                }
                locationDataMap.put(countryName, provincesMap);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "خطا در پردازش فایل مکان‌ها.", Toast.LENGTH_LONG).show();
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
                if (isInitialSpinnerSetup) return;
                updateProvinceSpinner((String) parent.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialSpinnerSetup) return;
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

                if (selectedCountry != null && selectedProvince != null && selectedCity != null &&
                        locationDataMap.containsKey(selectedCountry) &&
                        locationDataMap.get(selectedCountry).containsKey(selectedProvince)) {
                    selectedManualLocation = locationDataMap.get(selectedCountry).get(selectedProvince).get(selectedCity);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateProvinceSpinner(String country) {
        if (country != null && locationDataMap.containsKey(country)) {
            List<String> provinces = new ArrayList<>(locationDataMap.get(country).keySet());
            ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinces);
            provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProvince.setAdapter(provinceAdapter);
        }
    }

    private void updateCitySpinner(String country, String province) {
        if (country != null && province != null &&
                locationDataMap.containsKey(country) &&
                locationDataMap.get(country).containsKey(province)) {
            List<String> cities = new ArrayList<>(locationDataMap.get(country).get(province).keySet());
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(cityAdapter);
        }
    }

    private void updatePrayerTimesWithGPS() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    // ذخیره‌ی آخرین مختصات موفق (برای اینکه بعد از ری‌استارت / null بودن GPS هم اوقات نمایش داده شود)
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putFloat(KEY_LAST_LAT, (float) lat);
                    editor.putFloat(KEY_LAST_LON, (float) lon);
                    editor.putString(KEY_LAST_SOURCE, "gps");
                    editor.putLong(KEY_LAST_UPDATED, System.currentTimeMillis());
                    editor.apply();

                    calculateAndDisplayPrayerTimes(lat, lon);
                    textCurrentLocation.setText(String.format("مکان فعلی: GPS (%.4f, %.4f)", lat, lon));
                } else {
                    // fallback: آخرین مختصات ذخیره شده
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    if (prefs.contains(KEY_LAST_LAT) && prefs.contains(KEY_LAST_LON)) {
                        double lat = prefs.getFloat(KEY_LAST_LAT, 0f);
                        double lon = prefs.getFloat(KEY_LAST_LON, 0f);

                        calculateAndDisplayPrayerTimes(lat, lon);

                        String src = prefs.getString(KEY_LAST_SOURCE, "saved");
                        textCurrentLocation.setText(String.format("مکان فعلی: آخرین مکان ذخیره‌شده (%s)", src));
                        Toast.makeText(this, "GPS فعلاً در دسترس نیست؛ از آخرین مکان ذخیره‌شده استفاده شد.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "مکان فعلی در دسترس نیست. لطفا GPS را روشن کنید.", Toast.LENGTH_LONG).show();
                        textCurrentLocation.setText("مکان فعلی: GPS (ناموفق)");
                    }
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

        updateNextPrayerInfo(prayerTimes, coordinates, params);
    }

        private void updateNextPrayerInfo(PrayerTimes prayerTimes, Coordinates coordinates, com.batoulapps.adhan.CalculationParameters params) {
        Prayer nextPrayer = prayerTimes.nextPrayer();

        Date nextPrayerTime;
        String nextPrayerNameFa;

        if (nextPrayer != Prayer.NONE) {
            nextPrayerTime = prayerTimes.timeForPrayer(nextPrayer);
            nextPrayerNameFa = mapPrayerName(nextPrayer);
        } else {
            // بعد از عشاء: نماز بعدی را فجرِ فردا نشان بده
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(java.util.Calendar.DATE, 1);
            DateComponents tomorrow = DateComponents.from(cal.getTime());
            PrayerTimes tomorrowTimes = new PrayerTimes(coordinates, tomorrow, params);

            nextPrayer = Prayer.FAJR;
            nextPrayerTime = tomorrowTimes.fajr;
            nextPrayerNameFa = "فجر";
        }

        long remainingMillis = nextPrayerTime.getTime() - System.currentTimeMillis();
        if (remainingMillis < 0) remainingMillis = 0;

        long hours = TimeUnit.MILLISECONDS.toHours(remainingMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60;

        textNextPrayerTime.setText("نماز بعدی: " + nextPrayerNameFa);
        String timeRemainingString = String.format(Locale.US, "زمان باقی‌مانده: %02d:%02d:%02d", hours, minutes, seconds);
        textRemainingTime.setText(timeRemainingString);

        // برای صفحه اصلی و سرویس، «نماز بعدی» را ذخیره کن
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putLong("next_prayer_time_millis", nextPrayerTime.getTime());
        editor.putString("next_prayer_name_fa", nextPrayerNameFa);
        editor.apply();
    }

    private CalculationMethod getSelectedCalculationMethod() {
        String selectedMethodName = (String) spinnerCalculationMethod.getSelectedItem();
        if (selectedMethodName == null) return CalculationMethod.KARACHI;
        switch (selectedMethodName) {
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
        if (date == null) return "--:--";
        return new java.text.SimpleDateFormat("HH:mm", Locale.US).format(date);
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

    private void playOrPauseAdhan() {
        if (mediaPlayer == null) {
            playAdhan();
        } else if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayAdhan.setText("پخش دستی اذان");
        } else {
            mediaPlayer.start();
            btnPlayAdhan.setText("مکث");
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
                Toast.makeText(this, "اذان در حال پخش است.", Toast.LENGTH_SHORT).show();
                btnPlayAdhan.setText("مکث");

                mediaPlayer.setOnCompletionListener(mp -> {
                    btnPlayAdhan.setText("پخش دستی اذان");
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                });
            } else {
                Toast.makeText(this, "خطا در بارگذاری فایل صوتی.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "خطا در پخش اذان.", Toast.LENGTH_LONG).show();
            btnPlayAdhan.setText("پخش دستی اذان");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // منطق اضافی از اینجا حذف شد تا با onCreate تداخل نکند
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