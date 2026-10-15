package com.androidhive.myapplication

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.androidhive.myapplication.data.PrayerTimeRepository
import com.androidhive.myapplication.data.PreferencesDataSource
import com.androidhive.myapplication.data.ContextBoundPreferencesDataSource
import com.androidhive.myapplication.data.NotificationSettingsStore
import com.androidhive.myapplication.data.AppPrefsNotificationSettingsStore
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.HashMap
import java.util.Locale

class PrayerTimesActivity : AppCompatActivity() {

    private lateinit var textFajr: TextView
    private lateinit var textSunrise: TextView
    private lateinit var textDHUHR: TextView
    private lateinit var textAsr: TextView
    private lateinit var textMaghrib: TextView
    private lateinit var textIsha: TextView
    private lateinit var textNextPrayerTime: TextView
    private lateinit var textRemainingTime: TextView
    private lateinit var textCurrentLocation: TextView
    private lateinit var spinnerCalculationMethod: Spinner
    private lateinit var spinnerCountry: Spinner
    private lateinit var spinnerProvince: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var radioGroupLocation: RadioGroup
    private lateinit var layoutManualLocation: LinearLayout
    private lateinit var btnPlayAdhan: Button
    private lateinit var btnSetManualLocation: Button
    private lateinit var btnGetLocation: Button
    private lateinit var switchAutoAdhan: Switch
    private lateinit var switchPlayAdhanSound: Switch

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationDataMap = HashMap<String, HashMap<String, HashMap<String, LocationData>>>()
    private var selectedManualLocation: LocationData? = null
    private var mediaPlayer: MediaPlayer? = null

    // SharedPreferences برای انتخاب دستی کشور/استان/شهر
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // ViewModel
    private lateinit var viewModel: PrayerTimesViewModel

    // Flags to prevent listener execution during programmatic UI updates
    private var _isRestoringUI = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge for API 30+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer_times)

        // Exact Alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            // حتی اگر granted نباشه هم ما UI رو آپدیت می‌کنیم
            // فقط نوتیفیکیشن ممکنه نمایش داده نشه
            PrayerScheduler.computeAndScheduleNext(this)
            PrayerNotification.showOrUpdate(this, false)
        }

        initializeUIComponents()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize ViewModel
        val prayerTimeRepository = PrayerTimesCalculator
        val preferences = ContextBoundPreferencesDataSource(this, AppPrefs)
        val notificationSettings = AppPrefsNotificationSettingsStore(applicationContext)
        val factory = PrayerTimesViewModelFactory(prayerTimeRepository, preferences, notificationSettings)
        viewModel = ViewModelProvider(this, factory)[PrayerTimesViewModel::class.java]

        // Observe ViewModel state for prayer times
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Update prayer time displays from ViewModel
                    if (state.fajrTime != "--:--") textFajr.text = "صبح: ${state.fajrTime}"
                    if (state.sunriseTime != "--:--") textSunrise.text = "طلوع: ${state.sunriseTime}"
                    if (state.dhuhrTime != "--:--") textDHUHR.text = "ظهر: ${state.dhuhrTime}"
                    if (state.asrTime != "--:--") textAsr.text = "عصر: ${state.asrTime}"
                    if (state.maghribTime != "--:--") textMaghrib.text = "مغرب: ${state.maghribTime}"
                    if (state.ishaTime != "--:--") textIsha.text = "عشاء: ${state.ishaTime}"

                    // Update location label from ViewModel
                    textCurrentLocation.text = state.currentLocationLabel

                    // Update next prayer and countdown from ViewModel
                    if (state.nextPrayerName.isNotEmpty()) {
                        textNextPrayerTime.text = "نماز بعدی: ${state.nextPrayerName} - ${state.nextPrayerTime}"
                    }
                    textRemainingTime.text = "زمان باقی‌مانده: ${state.remainingTime}"

                    // Update notification switches from ViewModel state
                    switchAutoAdhan.isChecked = state.isAutoAdhanEnabled
                    switchPlayAdhanSound.isChecked = state.isPrayerNotificationsEnabled

                    // ✅ Bug 1 Fix: Sync RadioGroup with isManualMode
                    if (!_isRestoringUI) {
                        val shouldBeManual = state.isManualMode
                        val currentCheckedId = radioGroupLocation.checkedRadioButtonId
                        val expectedId = if (shouldBeManual) R.id.radio_manual else R.id.radio_gps
                        if (currentCheckedId != expectedId) {
                            _isRestoringUI = true
                            radioGroupLocation.check(expectedId)
                            updateLocationModeUI(shouldBeManual)
                            _isRestoringUI = false
                        }
                    }

                    // ✅ Bug 2 Fix: Sync Spinner with calculationMethodPosition
                    if (!_isRestoringUI) {
                        val adapter = spinnerCalculationMethod.adapter as ArrayAdapter<*>?
                        if (adapter != null) {
                            val expectedPos = state.calculationMethodPosition
                            if (expectedPos >= 0 && expectedPos < adapter.count) {
                                if (spinnerCalculationMethod.selectedItemPosition != expectedPos) {
                                    _isRestoringUI = true
                                    spinnerCalculationMethod.setSelection(expectedPos)
                                    _isRestoringUI = false
                                }
                            }
                        }
                    }
                }
            }
        }

        loadLocationDataFromJson()
        setupSpinners()
        setupListeners()
        loadAndApplyPreferencesToViewModel()

        // هماهنگ‌سازی UI دکمه‌ها با حالت انتخاب‌شده (GPS/دستی)
        updateLocationModeUI(radioGroupLocation.checkedRadioButtonId == R.id.radio_manual)
        // ✅ مهم: از این به بعد next prayer همیشه حساب می‌شود، مستقل از Auto
        PrayerScheduler.computeAndScheduleNext(this)
        PrayerNotification.showOrUpdate(this, false) // بدون دیرینگ
    }

    private fun initializeUIComponents() {
        textFajr = findViewById(R.id.text_fajr)
        textSunrise = findViewById(R.id.text_sunrise)
        textDHUHR = findViewById(R.id.text_dhuhr)
        textAsr = findViewById(R.id.text_asr)
        textMaghrib = findViewById(R.id.text_maghrib)
        textIsha = findViewById(R.id.text_isha)

        textNextPrayerTime = findViewById(R.id.text_next_prayer_time)
        textRemainingTime = findViewById(R.id.text_remaining_time)
        textCurrentLocation = findViewById(R.id.text_current_location)

        spinnerCalculationMethod = findViewById(R.id.spinner_calculation_method)

        btnPlayAdhan = findViewById(R.id.btn_play_adhan)
        btnSetManualLocation = findViewById(R.id.btn_set_manual_location)
        btnGetLocation = findViewById(R.id.btn_get_location)

        radioGroupLocation = findViewById(R.id.radio_group_location)
        layoutManualLocation = findViewById(R.id.layout_manual_location)

        spinnerCountry = findViewById(R.id.spinner_country)
        spinnerProvince = findViewById(R.id.spinner_province)
        spinnerCity = findViewById(R.id.spinner_city)

        switchAutoAdhan = findViewById(R.id.switch_auto_adhan)
        switchPlayAdhanSound = findViewById(R.id.switch_play_adhan_sound)
    }

    /**
     * کنترل UI بین حالت GPS و دستی
     * - در حالت دستی: اسپینرها و دکمه تنظیم مکان دستی نمایش داده شوند و دکمه گرفتن مکان (GPS) مخفی شود
     * - در حالت GPS: اسپینرها و دکمه دستی مخفی و دکمه گرفتن مکان نمایش داده شود
     */
    private fun updateLocationModeUI(isManual: Boolean) {
        layoutManualLocation.visibility = if (isManual) View.VISIBLE else View.GONE
        btnSetManualLocation.visibility = if (isManual) View.VISIBLE else View.GONE
        btnGetLocation.visibility = if (isManual) View.GONE else View.VISIBLE
    }

    private fun setupListeners() {

        // 🔊 پخش دستی اذان (فقط دکمه تست)
        btnPlayAdhan.setOnClickListener { playOrPauseAdhan() }

        // ✅ سوئیچ 1: پخش خودکار صدا
        switchAutoAdhan.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoAdhanEnabled(isChecked)
            checkAndRequestNotificationPermission()
            PrayerScheduler.computeAndScheduleNext(this)
            PrayerNotification.showOrUpdate(this, false)
        }

        // ✅ سوئیچ 2: فقط نوتیفیکیشن وقت اذان (بدون صدا)
        switchPlayAdhanSound.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPrayerNotificationsEnabled(isChecked)
            checkAndRequestNotificationPermission()
            PrayerScheduler.computeAndScheduleNext(this)
            if (isChecked) {
                PrayerNotification.showOrUpdate(this, false)
            } else {
                PrayerNotification.cancel(this)
            }
        }

        // ✅ انتخاب GPS/Manual
        radioGroupLocation.setOnCheckedChangeListener { _, checkedId ->
            if (_isRestoringUI) return@setOnCheckedChangeListener

            if (checkedId == R.id.radio_gps) {
                updateLocationModeUI(false)
                saveLocationMode("gps")

                // label برای main
                AppPrefs.setLocationMode(this, "gps")
                AppPrefs.setManualCityName(this, "")

                // Manual → GPS: cancel old alarm, clear stale manual coords
                AppPrefs.clearLocation(this)
                PrayerScheduler.cancelAlarm(this)
                PrayerNotification.cancel(this)

                viewModel.setLocationMode(false)

                updatePrayerTimesWithGPS()
            } else if (checkedId == R.id.radio_manual) {
                updateLocationModeUI(true)
                saveLocationMode("manual")

                AppPrefs.setLocationMode(this, "manual")

                // GPS → Manual: cancel old alarm, clear stale GPS coords
                AppPrefs.clearLocation(this)
                PrayerScheduler.cancelAlarm(this)
                PrayerNotification.cancel(this)

                viewModel.setLocationMode(true)

                textCurrentLocation.text = "در انتظار انتخاب مکان دستی"
            }
        }

        // ✅ دریافت GPS
        btnGetLocation.setOnClickListener { updatePrayerTimesWithGPS() }

        // ✅ ست کردن مکان دستی
        btnSetManualLocation.setOnClickListener {
            val selected = selectedManualLocation ?: run {
                Toast.makeText(this, "لطفاً شهر را انتخاب کنید.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveManualLocationSelection()

            val lat = selected.latitude
            val lon = selected.longitude

            AppPrefs.saveLocation(this, lat, lon)

            val cityName = spinnerCity.selectedItem.toString()
            AppPrefs.setLocationMode(this, "manual")
            AppPrefs.setManualCityName(this, cityName)

            viewModel.onManualLocationSelected(lat, lon, cityName)

            // ✅ همیشه next prayer ذخیره و برنامه‌ریزی شود
            PrayerScheduler.computeAndScheduleNext(this)
            PrayerNotification.showOrUpdate(this, false)

            Toast.makeText(this, "مکان دستی تنظیم شد: $cityName", Toast.LENGTH_SHORT).show()
        }

        // ✅ تغییر روش محاسبه → محاسبه مجدد
        spinnerCalculationMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (_isRestoringUI) return@onItemSelected

                // Persist user's selection
                val methodString = when (position) {
                    1 -> "MUSLIM_WORLD_LEAGUE"
                    2 -> "EGYPTIAN"
                    3 -> "KARACHI"
                    4 -> "UMM_AL_QURA"
                    5 -> "DUBAI"
                    6 -> "MOON_SIGHTING_COMMITTEE"
                    else -> "KARACHI"
                }
                AppPrefs.setCalculationMethod(this@PrayerTimesActivity, methodString)

                // Notify ViewModel
                if (viewModel.hasValidLocation()) {
                    viewModel.onCalculationMethodChanged(position)
                    PrayerScheduler.computeAndScheduleNext(this@PrayerTimesActivity)
                    PrayerNotification.showOrUpdate(this@PrayerTimesActivity, false)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun checkAndRequestNotificationPermission() {
        // Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updatePrayerTimesWithGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    Toast.makeText(this, "مکان GPS یافت نشد.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val lat = location.latitude
                val lon = location.longitude

                AppPrefs.saveLocation(this, lat, lon)
                AppPrefs.setLocationMode(this, "gps")
                AppPrefs.setManualCityName(this, "")

                viewModel.onLocationReceived(lat, lon)

                PrayerScheduler.computeAndScheduleNext(this)
                PrayerNotification.showOrUpdate(this, false)
            }
            .addOnFailureListener {
                Toast.makeText(this, "خطا در دریافت مکان GPS", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateAndDisplayPrayerTimes(latitude: Double, longitude: Double) {
        viewModel.onManualLocationSelected(latitude, longitude, spinnerCity.selectedItem.toString())
    }

    // ========== JSON LOCATIONS ==========
    private fun loadLocationDataFromJson() {
        try {
            val assetManager = assets
            val inputStream: InputStream = assetManager.open("locations.json")
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, StandardCharsets.UTF_8)

            val jsonObject = JSONObject(jsonString)
            val countryKeys = jsonObject.keys()

            while (countryKeys.hasNext()) {
                val countryName = countryKeys.next()
                val countryObject = jsonObject.getJSONObject(countryName)

                val provincesMap = HashMap<String, HashMap<String, LocationData>>()

                val provinceKeys = countryObject.keys()
                while (provinceKeys.hasNext()) {
                    val provinceName = provinceKeys.next()
                    val provinceObject = countryObject.getJSONObject(provinceName)

                    val citiesMap = HashMap<String, LocationData>()
                    val cityKeys = provinceObject.keys()

                    while (cityKeys.hasNext()) {
                        val cityName = cityKeys.next()
                        val cityObject = provinceObject.getJSONObject(cityName)

                        val lat: Double =
                            if (cityObject.has("lat")) cityObject.getDouble("lat")
                            else cityObject.getDouble("latitude")
                        val lon: Double =
                            if (cityObject.has("lon")) cityObject.getDouble("lon")
                            else cityObject.getDouble("longitude")

                        citiesMap[cityName] = LocationData(lat, lon)
                    }
                    provincesMap[provinceName] = citiesMap
                }
                locationDataMap[countryName] = provincesMap
            }
        } catch (e: Exception) {
            Toast.makeText(this, "خطا در خواندن locations.json", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupSpinners() {
        // calc methods
        val calcAdapter = ArrayAdapter.createFromResource(
            this, R.array.calculation_methods, R.layout.spinner_item_light
        ) as ArrayAdapter<CharSequence>
        calcAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_light)
        spinnerCalculationMethod.adapter = calcAdapter

        // countries
        val countryAdapter = ArrayAdapter(
            this, R.layout.spinner_item_light, ArrayList(locationDataMap.keys)
        )
        countryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_light)
        spinnerCountry.adapter = countryAdapter

        spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val country = spinnerCountry.selectedItem.toString()
                loadProvinces(country)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerProvince.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val country = spinnerCountry.selectedItem.toString()
                val province = spinnerProvince.selectedItem.toString()
                loadCities(country, province)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val country = spinnerCountry.selectedItem.toString()
                val province = spinnerProvince.selectedItem.toString()
                val city = spinnerCity.selectedItem.toString()
                selectedManualLocation = getLocationData(country, province, city)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadProvinces(country: String) {
        var provinces = locationDataMap[country] ?: HashMap()

        val provinceList = ArrayList(provinces.keys)
        Collections.sort(provinceList)

        val provinceAdapter = ArrayAdapter(
            this, R.layout.spinner_item_light, provinceList
        )
        provinceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_light)
        spinnerProvince.adapter = provinceAdapter

        if (provinceList.isNotEmpty()) spinnerProvince.setSelection(0)
    }

    private fun loadCities(country: String, province: String) {
        val provinces = locationDataMap[country] ?: return

        var cities = provinces[province] ?: HashMap()

        val cityList = ArrayList(cities.keys)
        Collections.sort(cityList)

        val cityAdapter = ArrayAdapter(
            this, R.layout.spinner_item_light, cityList
        )
        cityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_light)
        spinnerCity.adapter = cityAdapter

        if (cityList.isNotEmpty()) spinnerCity.setSelection(0)
    }

    private fun getLocationData(country: String, province: String, city: String): LocationData? {
        val provinces = locationDataMap[country] ?: return null
        val cities = provinces[province] ?: return null
        return cities[city]
    }

    private fun saveLocationMode(mode: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_LOCATION_MODE, mode).apply()
    }

    private fun saveManualLocationSelection() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_COUNTRY, spinnerCountry.selectedItem.toString())
            .putString(KEY_PROVINCE, spinnerProvince.selectedItem.toString())
            .putString(KEY_CITY, spinnerCity.selectedItem.toString())
            .apply()
    }

    private fun loadAndApplyPreferencesToViewModel() {
        // Use ViewModel's loadPreferences which reads from AppPrefs via PreferencesDataSource
        val isAutoAdhan = AppPrefs.isAutoAdhanEnabled(this)
        val isPrayerNotif = AppPrefs.isPrayerNotificationsEnabled(this)
        val locationMode = AppPrefs.getLocationMode(this)
        val manualCityName = AppPrefs.getManualCityName(this)
        val calculationMethod = AppPrefs.getCalculationMethod(this)
        val latitude = AppPrefs.getLat(this)
        val longitude = AppPrefs.getLon(this)

        viewModel.loadPreferences(
            isAutoAdhan = isAutoAdhan,
            isPrayerNotif = isPrayerNotif,
            locationMode = locationMode,
            manualCityName = manualCityName,
            calculationMethod = calculationMethod,
            latitude = latitude,
            longitude = longitude
        )

        // ✅ برای حالت دستی: محاسبه یک‌باره اوقات شرعی با مختصات و روش محاسبه ذخیره‌شده
        // انجام می‌شود تا پس از ورود مجدد، اوقات شرعی نمایش داده شوند.
        // این محاسبه زودتر از بازیابی Spinnerها انجام می‌شود تا listenerهای
        // برنامه‌ای Spinnerها محاسبه تکراری ایجاد نکنند.
        if ("manual" == locationMode && (latitude != 0.0 || longitude != 0.0)) {
            viewModel.onManualLocationSelected(latitude, longitude, manualCityName)
        }

        // Restore spinner selections for manual mode (UI-specific, stays in Activity)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if ("manual" == locationMode) {
            val savedCountry = prefs.getString(KEY_COUNTRY, null)
            val savedProvince = prefs.getString(KEY_PROVINCE, null)
            val savedCity = prefs.getString(KEY_CITY, null)

            if (savedCountry != null && savedProvince != null && savedCity != null) {
                _isRestoringUI = true
                selectSpinnerByValue(spinnerCountry, savedCountry)
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    selectSpinnerByValue(spinnerProvince, savedProvince)
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        selectSpinnerByValue(spinnerCity, savedCity)
                        // Do NOT trigger prayer calculation here on restore.
                        // Prayer times already calculated above via loadPreferences + onManualLocationSelected.
                        // If user explicitly changes location via button, calculation will be triggered there.
                        _isRestoringUI = false
                    }, 300)
                }, 300)
            }
        }
    }

    private fun selectSpinnerByValue(spinner: Spinner, value: String) {
        val adapter = spinner.adapter as ArrayAdapter<*>?
        if (adapter == null) return
        for (i in 0 until adapter.count) {
            if (value == adapter.getItem(i)) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun playOrPauseAdhan() {
        // ✅ این فقط دکمه تست است، ربطی به auto/notif ندارد
        try {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
                btnPlayAdhan.text = "پخش دستی اذان"
                return
            }

            mediaPlayer = MediaPlayer.create(this, R.raw.dhan_sound)
            mediaPlayer!!.setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
                btnPlayAdhan.text = "پخش دستی اذان"
            }
            mediaPlayer!!.start()
            btnPlayAdhan.text = "توقف اذان"

        } catch (e: Exception) {
            Toast.makeText(this, "خطا در پخش اذان", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()

        // اگر اذان دستی در حال پخش بود، قطعش کن
        if (mediaPlayer != null) {
            try {
                mediaPlayer!!.stop()
            } catch (ignored: Exception) {
            }
            mediaPlayer!!.release()
            mediaPlayer = null
            btnPlayAdhan.text = "پخش دستی اذان"
        }
    }

    private class LocationData(val latitude: Double, val longitude: Double)

    companion object {
        // SharedPreferences برای انتخاب دستی کشور/استان/شهر
        private const val PREFS_NAME = "ManualLocationPrefs"
        private const val KEY_LOCATION_MODE = "location_mode" // manual/gps/none
        private const val KEY_COUNTRY = "country"
        private const val KEY_PROVINCE = "province"
        private const val KEY_CITY = "city"
    }
}