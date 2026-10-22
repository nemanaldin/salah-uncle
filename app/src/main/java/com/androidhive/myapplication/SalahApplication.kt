package com.androidhive.myapplication

import android.app.Application
import android.content.Context
import android.app.NotificationManager
import android.os.Build
import com.androidhive.myapplication.data.content.LessonRepository
import com.androidhive.myapplication.data.location.CityRepository
import com.androidhive.myapplication.data.settings.SettingsRepository
import com.androidhive.myapplication.domain.prayer.PrayerEngine
import com.androidhive.myapplication.platform.alarm.AlarmCoordinator
import com.androidhive.myapplication.platform.alarm.PrayerNotifications
import com.androidhive.myapplication.platform.location.DeviceLocation
import com.androidhive.myapplication.platform.location.HeadingRepository
import com.androidhive.myapplication.platform.audio.AdhanSession

class SalahApplication : Application() {
    val container by lazy { AppContainer(this) }
}

class AppContainer(private val application: Application) {
    val settings = SettingsRepository(application)
    val lessons = LessonRepository(application)
    val cities = CityRepository(application)
    val prayerEngine = PrayerEngine()
    val notifications = PrayerNotifications(application)
    val alarms = AlarmCoordinator(application, settings, prayerEngine, notifications)
    val deviceLocation = DeviceLocation(application)
    val headings = HeadingRepository(application)
    val adhanSession = AdhanSession()
    val recordings = com.androidhive.myapplication.platform.audio.AdhanRecordings(application)
    fun capabilities(): DeviceCapabilities {
        val manager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return DeviceCapabilities(alarms.exactAllowed(), notifications.allowed(), Build.VERSION.SDK_INT < 34 || manager.canUseFullScreenIntent())
    }
}
