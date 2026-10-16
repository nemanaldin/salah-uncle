package com.androidhive.myapplication.data.settings

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import com.androidhive.myapplication.core.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.ZoneId

class SettingsRepository(context: Context) {
    private val app = context.applicationContext
    private val store = PreferenceDataStoreFactory.create(
        migrations = listOf(LegacyMigration(app)),
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        produceFile = { app.preferencesDataStoreFile("salah_settings") },
    )
    val settings: Flow<AppSettings> = store.data.map(::decode)
    suspend fun snapshot(): AppSettings = settings.first()
    suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.edit { preferences -> encode(transform(decode(preferences)), preferences) }
    }
    suspend fun hideNotification(untilMillis: Long) = update { it.copy(hiddenNotificationUntil = untilMillis) }

    private class LegacyMigration(private val context: Context) : DataMigration<Preferences> {
        override suspend fun shouldMigrate(currentData: Preferences) = currentData[Keys.migrated] != true
        override suspend fun migrate(currentData: Preferences): Preferences {
            val result = currentData.toMutablePreferences()
            val legacy = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val country = context.getSharedPreferences("ManualLocationPrefs", Context.MODE_PRIVATE).getString("country", null)
            LegacySettings.restore(legacy.all, country, ZoneId.systemDefault().id)?.let { encode(it, result) }
            result[Keys.migrated] = true
            return result
        }
        // Keep old preferences intact so an upgrade never discards the user's original data.
        override suspend fun cleanUp() = Unit
    }

    private object Keys {
        val migrated = booleanPreferencesKey("legacy_migrated_v1")
        val language = stringPreferencesKey("language")
        val school = stringPreferencesKey("school")
        val onboarded = booleanPreferencesKey("onboarded")
        val theme = stringPreferencesKey("theme")
        val scale = floatPreferencesKey("text_scale")
        val reduceMotion = booleanPreferencesKey("reduce_motion")
        val lat = doublePreferencesKey("latitude")
        val lon = doublePreferencesKey("longitude")
        val label = stringPreferencesKey("location_label")
        val zone = stringPreferencesKey("location_zone")
        val mode = stringPreferencesKey("location_mode")
        val cityId = stringPreferencesKey("city_id")
        val preset = stringPreferencesKey("calculation_preset")
        val asr = stringPreferencesKey("asr_convention")
        val alerts = stringPreferencesKey("alert_mode")
        val overrides = stringSetPreferencesKey("prayer_alerts")
        val bookmarks = stringSetPreferencesKey("bookmarks")
        val completed = stringSetPreferencesKey("completed_steps")
        val lastLesson = stringPreferencesKey("last_lesson")
        val lastStep = stringPreferencesKey("last_step")
        val hidden = longPreferencesKey("hidden_notification_until")
    }

    companion object {
        private fun decode(p: Preferences): AppSettings {
            val school = enumOrDefault(p[Keys.school], School.HANAFI)
            val location = if (p[Keys.lat] == null || p[Keys.lon] == null) null else runCatching {
                SavedLocation(p[Keys.lat]!!, p[Keys.lon]!!, p[Keys.label].orEmpty(), p[Keys.zone] ?: ZoneId.systemDefault().id,
                    enumOrDefault(p[Keys.mode], LocationMode.MANUAL), p[Keys.cityId])
            }.getOrNull()
            val overrides = p[Keys.overrides].orEmpty().mapNotNull { entry ->
                val pair = entry.split('=', limit = 2)
                if (pair.size != 2) null else runCatching { PrayerId.valueOf(pair[0]) to AlertMode.valueOf(pair[1]) }.getOrNull()
            }.toMap()
            return AppSettings(
                language = enumOrDefault(p[Keys.language], AppLanguage.PERSIAN),
                school = school,
                onboardingComplete = p[Keys.onboarded] ?: false,
                theme = enumOrDefault(p[Keys.theme], ThemeMode.SYSTEM),
                textScale = (p[Keys.scale] ?: 1f).coerceIn(.85f, 1.5f),
                reduceMotion = p[Keys.reduceMotion] ?: false,
                location = location,
                calculationPreset = enumOrDefault(p[Keys.preset], CalculationPreset.KARACHI),
                asrConvention = enumOrDefault(p[Keys.asr], school.defaultAsr),
                alertMode = enumOrDefault(p[Keys.alerts], AlertMode.OFF),
                prayerAlerts = overrides, bookmarks = p[Keys.bookmarks].orEmpty(), completedSteps = p[Keys.completed].orEmpty(),
                lastLessonId = p[Keys.lastLesson], lastStepId = p[Keys.lastStep], hiddenNotificationUntil = p[Keys.hidden] ?: 0L,
            )
        }
        private fun encode(s: AppSettings, p: MutablePreferences) {
            p[Keys.language] = s.language.name
            p[Keys.school] = s.school.name
            p[Keys.onboarded] = s.onboardingComplete
            p[Keys.theme] = s.theme.name
            p[Keys.scale] = s.textScale
            p[Keys.reduceMotion] = s.reduceMotion
            if (s.location != null) {
                p[Keys.lat] = s.location.latitude; p[Keys.lon] = s.location.longitude
                p[Keys.label] = s.location.label; p[Keys.zone] = s.location.zoneId; p[Keys.mode] = s.location.mode.name
                s.location.cityId?.let { p[Keys.cityId] = it } ?: p.remove(Keys.cityId)
            } else {
                p.remove(Keys.lat); p.remove(Keys.lon); p.remove(Keys.label); p.remove(Keys.zone); p.remove(Keys.mode); p.remove(Keys.cityId)
            }
            p[Keys.preset] = s.calculationPreset.name; p[Keys.asr] = s.asrConvention.name; p[Keys.alerts] = s.alertMode.name
            p[Keys.overrides] = s.prayerAlerts.map { "${it.key.name}=${it.value.name}" }.toSet()
            p[Keys.bookmarks] = s.bookmarks; p[Keys.completed] = s.completedSteps
            s.lastLessonId?.let { p[Keys.lastLesson] = it } ?: p.remove(Keys.lastLesson)
            s.lastStepId?.let { p[Keys.lastStep] = it } ?: p.remove(Keys.lastStep)
            p[Keys.hidden] = s.hiddenNotificationUntil
        }
    }
}
