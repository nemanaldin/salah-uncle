package com.androidhive.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.androidhive.myapplication.data.PrayerTimeRepository
import com.androidhive.myapplication.data.PreferencesDataSource
import com.androidhive.myapplication.data.NotificationSettingsStore

class PrayerTimesViewModelFactory(
    private val prayerTimeRepository: PrayerTimeRepository,
    private val preferences: PreferencesDataSource,
    private val notificationSettings: NotificationSettingsStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(PrayerTimesViewModel::class.java)) {
            return PrayerTimesViewModel(prayerTimeRepository, preferences, notificationSettings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}