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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Prayer;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PrayerTimesActivity extends AppCompatActivity {

    private Handler countdownHandler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;


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
                    // حالا که مجوز داده شد، فرآیند فعال‌سازی اذان خودکار را ادامه بده
                    enableAutoAdhanFlow();
                } else {
                    Toast.makeText(this, "بدون مجوز، نوتیفیکیشن‌ها نمایش داده نمی‌شوند.", Toast.LENGTH_LONG).show();
                    switchAutoAdhan.setChecked(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_times);

        // Android 12+ exact alarm permission (برای setExact*)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        initializeUIComponents();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationDataMap = new HashMap<>();

        loadLocationDataFromJson();
        setupSpinners();
        setupListeners();
        loadAndApplySavedPreferences();

        // اگر قبلاً auto روشن بوده، نوتیف و آلارم را یکبار آپدیت کن
        if (AppPrefs.isAutoAdhanEnabled(this)) {
            PrayerScheduler.computeAndScheduleNext(this);
            PrayerNotification.showOrUpdate(this, false);
        }
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
        btnGetLocation = findViewById(R.id.btn_get_location);
    }

    private void setupListeners() {
        radioGroupLocation.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_gps) {
                layoutManualLocation.setVisibility(View.GONE);
                updatePrayerTimesWithGPS();
                saveLocationMode("gps");
            } else {
                layoutManualLocation.setVisibility(View.VISIBLE);
                saveLocationMode("manual");
                textCurrentLocation.setText("مکان فعلی: دستی");
            }
        });

        btnSetManualLocation.setOnClickListener(v -> {
            if (selectedManualLocation != null) {
                saveManualLocationSelection();
                calculateAndDisplayPrayerTimes(selectedManualLocation.latitude, selectedManualLocation.longitude);

                // برای سیستم زمان‌بندی هم ذخیره کن
                AppPrefs.saveLocation(this, selectedManualLocation.latitude, selectedManualLocation.longitude);

                if (AppPrefs.isAutoAdhanEnabled(this)) {
                    PrayerScheduler.computeAndScheduleNext(this);
                    PrayerNotification.showOrUpdate(this, true);
                }

            } else {
                Toast.makeText(this, "لطفاً شهر را انتخاب کنید.", Toast.LENGTH_LONG).show();
            }
        });

        btnGetLocation.setOnClickListener(v -> updatePrayerTimesWithGPS());

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
            AppPrefs.setAutoAdhanEnabled(this, isChecked);
            if (isChecked) {
                // اگر لازم باشد مجوز نوتیفیکیشن می‌گیریم، سپس schedule انجام می‌شود
                checkAndRequestNotificationPermission();
            } else {
                disableAutoAdhanFlow();
            }
        });
    }

    private void checkAndRequestNotificationPermission() {
        // برای Android 13+ نیاز به اجازه نوتیفیکیشن داریم
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        enableAutoAdhanFlow();
    }

    /**
     * فعال‌سازی اذان خودکار:
     * - ذخیره‌ی لوکیشن (GPS یا دستی) داخل AppPrefs
     * - محاسبه و ست کردن آلارم نماز بعدی با AlarmManager
     * - نمایش/آپدیت نوتیفیکیشن نماز بعدی
     */
    private void enableAutoAdhanFlow() {
        // لوکیشن را داخل AppPrefs ذخیره کن (تا Scheduler روی همان کار کند)
        if (radioGroupLocation.getCheckedRadioButtonId() == R.id.radio_gps) {
            // در حالت GPS اگر هنوز موقعیت نگرفتیم، درخواست بزن
            if (!saveLastKnownLocationToAppPrefsIfAny()) {
                Toast.makeText(this, "در حال دریافت مکان از GPS…", Toast.LENGTH_SHORT).show();
                updatePrayerTimesWithGPS(); // این خودش بعداً AppPrefs را آپدیت می‌کند
                return;
            }
        } else {
            if (selectedManualLocation == null) {
                Toast.makeText(this, "لطفاً یک شهر را به صورت دستی انتخاب کنید.", Toast.LENGTH_LONG).show();
                switchAutoAdhan.setChecked(false);
                return;
            }
            AppPrefs.saveLocation(this, selectedManualLocation.latitude, selectedManualLocation.longitude);
        }

        PrayerScheduler.computeAndScheduleNext(this);
        PrayerNotification.showOrUpdate(this, true);
    }

    private void disableAutoAdhanFlow() {
        // اگر اذان در حال پخش است، قطعش کن
        Intent stop = new Intent(this, AdhanControlReceiver.class);
        stop.setAction(AdhanControlReceiver.ACTION_STOP);
        sendBroadcast(stop);

        PrayerScheduler.cancelAlarm(this);
        PrayerNotification.hide(this);
    }

    /**
     * تلاش می‌کند آخرین لوکیشن ذخیره‌شده را به AppPrefs منتقل کند.
     * اگر مقدار معتبر نبود، false برمی‌گرداند.
     */
    private boolean saveLastKnownLocationToAppPrefsIfAny() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        double lat = prefs.getFloat(KEY_LAST_LAT, 0f);
        double lon = prefs.getFloat(KEY_LAST_LON, 0f);
        if (lat == 0f && lon == 0f) return false;
        AppPrefs.saveLocation(this, lat, lon);
        return true;
    }

    private void loadAndApplySavedPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String locationMode = prefs.getString(KEY_LOCATION_MODE, "gps");

        // اگر آخرین مختصات ذخیره شده داریم، قبل از هر چیز نمایش بده
        double lat = prefs.getFloat(KEY_LAST_LAT, 0f);
        double lon = prefs.getFloat(KEY_LAST_LON, 0f);
        if (lat != 0f || lon != 0f) {
            AppPrefs.saveLocation(this, lat, lon);
            calculateAndDisplayPrayerTimes(lat, lon);
        }

        // Auto Adhan
        boolean autoEnabled = AppPrefs.isAutoAdhanEnabled(this);
        switchAutoAdhan.setChecked(autoEnabled);

        // حالت لوکیشن
        if ("manual".equals(locationMode)) {
            radioGroupLocation.check(R.id.radio_manual);
            layoutManualLocation.setVisibility(View.VISIBLE);

            String savedCountry = prefs.getString(KEY_COUNTRY, null);
            String savedProvince = prefs.getString(KEY_PROVINCE, null);
            String savedCity = prefs.getString(KEY_CITY, null);

            if (savedCountry != null && savedProvince != null && savedCity != null) {
                selectSpinnerByValue(spinnerCountry, savedCountry);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    selectSpinnerByValue(spinnerProvince, savedProvince);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        selectSpinnerByValue(spinnerCity, savedCity);

                        double mlat = prefs.getFloat(KEY_MANUAL_LAT, 0f);
                        double mlon = prefs.getFloat(KEY_MANUAL_LON, 0f);
                        if (mlat != 0f || mlon != 0f) {
                            selectedManualLocation = new LocationData(mlat, mlon);
                            AppPrefs.saveLocation(this, mlat, mlon);
                            calculateAndDisplayPrayerTimes(mlat, mlon);
                        }
                    }, 250);
                }, 250);
            }
        } else {
            radioGroupLocation.check(R.id.radio_gps);
            layoutManualLocation.setVisibility(View.GONE);
            updatePrayerTimesWithGPS();
        }

        // Spinner setup flag off
        isInitialSpinnerSetup = false;
    }

    private void selectSpinnerByValue(Spinner spinner, String value) {
        if (spinner.getAdapter() == null) return;
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (value.equals(spinner.getAdapter().getItem(i))) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void saveLocationMode(String mode) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_LOCATION_MODE, mode);
        editor.apply();
    }

    private void saveManualLocationSelection() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_COUNTRY, String.valueOf(spinnerCountry.getSelectedItem()));
        editor.putString(KEY_PROVINCE, String.valueOf(spinnerProvince.getSelectedItem()));
        editor.putString(KEY_CITY, String.valueOf(spinnerCity.getSelectedItem()));
        if (selectedManualLocation != null) {
            editor.putFloat(KEY_MANUAL_LAT, (float) selectedManualLocation.latitude);
            editor.putFloat(KEY_MANUAL_LON, (float) selectedManualLocation.longitude);
        }
        editor.apply();
    }

    private void updatePrayerTimesWithGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "لطفاً دسترسی موقعیت را فعال کنید.", Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();

                        saveLastLocation(lat, lon, "gps");
                        AppPrefs.saveLocation(this, lat, lon);

                        calculateAndDisplayPrayerTimes(lat, lon);

                        if (AppPrefs.isAutoAdhanEnabled(this)) {
                            PrayerScheduler.computeAndScheduleNext(this);
                            PrayerNotification.showOrUpdate(this, true);
                        }
                    } else {
                        Toast.makeText(this, "مکان یافت نشد. دوباره تلاش کنید.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "خطا در دریافت مکان.", Toast.LENGTH_LONG).show());
    }

    private void saveLastLocation(double lat, double lon, String source) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(KEY_LAST_LAT, (float) lat);
        editor.putFloat(KEY_LAST_LON, (float) lon);
        editor.putString(KEY_LAST_SOURCE, source);
        editor.putLong(KEY_LAST_UPDATED, System.currentTimeMillis());
        editor.apply();
    }

    private CalculationParameters getCalculationParamsFromSpinner() {
        int position = spinnerCalculationMethod.getSelectedItemPosition();
        CalculationMethod method;

        switch (position) {
            case 1:
                method = CalculationMethod.MUSLIM_WORLD_LEAGUE;
                break;
            case 2:
                method = CalculationMethod.EGYPTIAN;
                break;
            case 3:
                method = CalculationMethod.KARACHI;
                break;
            case 4:
                method = CalculationMethod.UMM_AL_QURA;
                break;
            case 5:
                method = CalculationMethod.DUBAI;
                break;
            case 6:
                method = CalculationMethod.MOON_SIGHTING_COMMITTEE;
                break;
            default:
                method = CalculationMethod.KARACHI;
                break;
        }
        return method.getParameters();
    }

    private void calculateAndDisplayPrayerTimes(double latitude, double longitude) {
        Coordinates coordinates = new Coordinates(latitude, longitude);
        DateComponents date = DateComponents.from(new Date());
        CalculationParameters params = getCalculationParamsFromSpinner();
        currentPrayerTimes = new PrayerTimes(coordinates, date, params);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        textFajr.setText("صبح: " + sdf.format(currentPrayerTimes.fajr));
        textSunrise.setText("طلوع: " + sdf.format(currentPrayerTimes.sunrise));
        textDHUHR.setText("ظهر: " + sdf.format(currentPrayerTimes.dhuhr));
        textAsr.setText("عصر: " + sdf.format(currentPrayerTimes.asr));
        textMaghrib.setText("مغرب: " + sdf.format(currentPrayerTimes.maghrib));
        textIsha.setText("عشاء: " + sdf.format(currentPrayerTimes.isha));

        updateNextPrayerAndCountdown();
        updateLocationText(latitude, longitude);

        // هر بار محاسبه شد، اگر auto روشن است، schedule را تازه کن
        if (AppPrefs.isAutoAdhanEnabled(this)) {
            AppPrefs.saveLocation(this, latitude, longitude);
            PrayerScheduler.computeAndScheduleNext(this);
            PrayerNotification.showOrUpdate(this, true);
        }
    }

    private void updateLocationText(double lat, double lon) {
        textCurrentLocation.setText(String.format(Locale.getDefault(),
                "مکان فعلی: %.5f , %.5f", lat, lon));
    }

    private void updateNextPrayerAndCountdown() {
        if (currentPrayerTimes == null) return;

        long now = System.currentTimeMillis();

        Date nextTime = null;
        String nextName = "";

        Date fajr = currentPrayerTimes.fajr;
        Date dhuhr = currentPrayerTimes.dhuhr;
        Date asr = currentPrayerTimes.asr;
        Date maghrib = currentPrayerTimes.maghrib;
        Date isha = currentPrayerTimes.isha;

        if (fajr != null && fajr.getTime() > now) {
            nextTime = fajr;
            nextName = "صبح";
        } else if (dhuhr != null && dhuhr.getTime() > now) {
            nextTime = dhuhr;
            nextName = "ظهر";
        } else if (asr != null && asr.getTime() > now) {
            nextTime = asr;
            nextName = "عصر";
        } else if (maghrib != null && maghrib.getTime() > now) {
            nextTime = maghrib;
            nextName = "مغرب";
        } else if (isha != null && isha.getTime() > now) {
            nextTime = isha;
            nextName = "عشاء";
        } else {
            // فردا صبح
            if (fajr != null) {
                nextTime = new Date(fajr.getTime() + 24 * 60 * 60 * 1000);
                nextName = "صبح";
            }
        }

        if (nextTime == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        textNextPrayerTime.setText("نماز بعدی: " + nextName + " - " + sdf.format(nextTime));

        if (countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }

        final Date finalNextTime = nextTime;
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long diff = finalNextTime.getTime() - System.currentTimeMillis();
                if (diff < 0) diff = 0;

                long h = diff / (1000 * 60 * 60);
                long m = (diff / (1000 * 60)) % 60;
                long s = (diff / 1000) % 60;

                textRemainingTime.setText(
                        String.format(Locale.getDefault(),
                                "زمان باقی‌مانده: %02d:%02d:%02d", h, m, s)
                );

                if (diff > 0) {
                    countdownHandler.postDelayed(this, 1000);
                }
            }
        };

        countdownHandler.post(countdownRunnable);
    }


    private String getPrayerNameFa(Prayer prayer) {
        switch (prayer) {
            case FAJR: return "صبح";
            case SUNRISE: return "طلوع";
            case DHUHR: return "ظهر";
            case ASR: return "عصر";
            case MAGHRIB: return "مغرب";
            case ISHA: return "عشاء";
            default: return "نامشخص";
        }
    }

    private void playOrPauseAdhan() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.dhan_sound);
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                });
                mediaPlayer.start();
            } else {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "خطا در پخش اذان", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpinners() {
        List<String> countries = new java.util.ArrayList<>(locationDataMap.keySet());
        java.util.Collections.sort(countries);
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(countryAdapter);

        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String country = String.valueOf(spinnerCountry.getSelectedItem());
                loadProvinces(country);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String country = String.valueOf(spinnerCountry.getSelectedItem());
                String province = String.valueOf(spinnerProvince.getSelectedItem());
                loadCities(country, province);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String country = String.valueOf(spinnerCountry.getSelectedItem());
                String province = String.valueOf(spinnerProvince.getSelectedItem());
                String city = String.valueOf(spinnerCity.getSelectedItem());
                selectedManualLocation = getLocationData(country, province, city);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadProvinces(String country) {
        Map<String, Map<String, LocationData>> provinces = locationDataMap.get(country);
        if (provinces == null) provinces = new HashMap<>();
        List<String> provinceList = new java.util.ArrayList<>(provinces.keySet());
        java.util.Collections.sort(provinceList);

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinceList);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        if (!provinceList.isEmpty()) {
            spinnerProvince.setSelection(0);
        }
    }

    private void loadCities(String country, String province) {
        Map<String, Map<String, LocationData>> provinces = locationDataMap.get(country);
        if (provinces == null) return;

        Map<String, LocationData> cities = provinces.get(province);
        if (cities == null) cities = new HashMap<>();
        List<String> cityList = new java.util.ArrayList<>(cities.keySet());
        java.util.Collections.sort(cityList);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityList);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);

        if (!cityList.isEmpty()) {
            spinnerCity.setSelection(0);
        }
    }

    private LocationData getLocationData(String country, String province, String city) {
        Map<String, Map<String, LocationData>> provinces = locationDataMap.get(country);
        if (provinces == null) return null;
        Map<String, LocationData> cities = provinces.get(province);
        if (cities == null) return null;
        return cities.get(city);
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

                        // ✅ پشتیبانی از هر دو ساختار
                        double lat = cityObject.has("lat")
                                ? cityObject.getDouble("lat")
                                : cityObject.getDouble("latitude");

                        double lon = cityObject.has("lon")
                                ? cityObject.getDouble("lon")
                                : cityObject.getDouble("longitude");

                        citiesMap.put(cityName, new LocationData(lat, lon));
                    }

                    provincesMap.put(provinceName, citiesMap);
                }

                locationDataMap.put(countryName, provincesMap);
            }

        } catch (Exception e) {
            Toast.makeText(this, "خطا در خواندن locations.json", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    private static class LocationData {
        double latitude;
        double longitude;

        LocationData(double lat, double lon) {
            this.latitude = lat;
            this.longitude = lon;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }

}
