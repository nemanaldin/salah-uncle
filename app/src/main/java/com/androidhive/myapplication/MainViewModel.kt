package com.androidhive.myapplication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidhive.myapplication.data.PreferencesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val preferences: PreferencesDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state = _state.asStateFlow()

    private var _countdownJob: kotlinx.coroutines.Job? = null
    private var _onCountdownFinished: (() -> Unit)? = null
    private var _currentTargetMillis: Long = 0

    fun setOnCountdownFinished(callback: (() -> Unit)?) {
        _onCountdownFinished = callback
    }

    fun clearOnCountdownFinished() {
        _onCountdownFinished = null
    }

    fun loadFromPrefs(context: Context) {
        val lat = preferences.getLat(context)
        val lon = preferences.getLon(context)
        val mode = preferences.getLocationMode(context)
        val nextName = preferences.getNextPrayerName(context)
        val nextMillis = preferences.getNextPrayerTime(context)
        val city = preferences.getManualCityName(context)

        _state.value = _state.value.copy(
            currentLocationLabel = buildLocationLabel(lat, lon, mode, city),
            nextPrayerName = nextName,
            nextPrayerTime = if (nextMillis > 0) formatTime(nextMillis) else "--:--",
            remainingTime = if (nextMillis > 0) formatDuration(nextMillis - System.currentTimeMillis()) else "--:--:--",
            hasValidLocation = lat != 0.0 || lon != 0.0
        )

        if (nextMillis > 0 && nextName.isNotBlank()) {
            // Guard: اگر countdown برای همین زمان در حال اجراست، مجدداً راه‌اندازی نکن
            if (_currentTargetMillis == nextMillis && _countdownJob?.isActive == true) {
                return
            }
            startCountdown(nextMillis)
        } else {
            // نامعتبر بودن زمان → وضعیت countdown را پاک کن
            _currentTargetMillis = 0
            _countdownJob?.cancel()
            _countdownJob = null
        }
    }

    fun startCountdown(targetMillis: Long) {
        _countdownJob?.cancel()
        _currentTargetMillis = targetMillis
        _countdownJob = viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                val diff = targetMillis - System.currentTimeMillis()
                if (diff <= 0) {
                    _state.value = _state.value.copy(remainingTime = "00:00:00")
                    // تازه‌خوانی برای نماز بعدی (مشابه رفتار قبلی MainActivity)
                    _currentTargetMillis = 0
                    _onCountdownFinished?.invoke()
                    break
                }

                val total = diff / 1000
                val h = total / 3600
                val m = (total % 3600) / 60
                val s = total % 60

                _state.value = _state.value.copy(
                    remainingTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
                )

                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _countdownJob?.cancel()
    }

    private fun buildLocationLabel(lat: Double, lon: Double, mode: String, city: String): String {
        return if ((lat == 0.0 && lon == 0.0) || "none" == mode) {
            "در انتظار انتخاب مکان"
        } else if ("manual" == mode) {
            var c = city
            if (c.isNullOrBlank()) c = "مکان دستی"
            "موقعیت فعلی: $c"
        } else {
            String.format(Locale.US, "موقعیت فعلی: %.2f,%.2f", lat, lon)
        }
    }

    private fun formatTime(millis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    }

    private fun formatDuration(diff: Long): String {
        var d = diff
        if (d < 0) d = 0
        val total = d / 1000
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    }

    companion object {
        // این متد باید از Activity صدا زده شود که Context دارد
        // ViewModel نباید Context نگه دارد
        @JvmStatic
        fun loadFromPrefsStatic(preferences: PreferencesDataSource, context: Context): MainUiState {
            val lat = preferences.getLat(context)
            val lon = preferences.getLon(context)
            val mode = preferences.getLocationMode(context)
            val nextName = preferences.getNextPrayerName(context)
            val nextMillis = preferences.getNextPrayerTime(context)
            val city = preferences.getManualCityName(context)

            return MainUiState(
                currentLocationLabel = buildLocationLabelStatic(lat, lon, mode, city),
                nextPrayerName = nextName,
                nextPrayerTime = if (nextMillis > 0) formatTimeStatic(nextMillis) else "--:--",
                remainingTime = if (nextMillis > 0) formatDurationStatic(nextMillis - System.currentTimeMillis()) else "--:--:--",
                hasValidLocation = lat != 0.0 || lon != 0.0
            )
        }

        private fun buildLocationLabelStatic(lat: Double, lon: Double, mode: String, city: String): String {
            return if ((lat == 0.0 && lon == 0.0) || "none" == mode) {
                "در انتظار انتخاب مکان"
            } else if ("manual" == mode) {
                var c = city
                if (c.isNullOrBlank()) c = "مکان دستی"
                "موقعیت فعلی: $c"
            } else {
                String.format(Locale.US, "موقعیت فعلی: %.2f,%.2f", lat, lon)
            }
        }

        private fun formatTimeStatic(millis: Long): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
        }

        private fun formatDurationStatic(diff: Long): String {
            var d = diff
            if (d < 0) d = 0
            val total = d / 1000
            val h = total / 3600
            val m = (total % 3600) / 60
            val s = total % 60
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
        }
    }
}