package com.androidhive.myapplication.core.model

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import java.time.ZoneId

enum class AppLanguage(val tag: String, val nativeName: String, val isRtl: Boolean = false) {
    PERSIAN("fa", "فارسی", true), ENGLISH("en", "English"), ARABIC("ar", "العربية", true),
    TURKISH("tr", "Türkçe"), URDU("ur", "اردو", true), INDONESIAN("id", "Bahasa Indonesia"),
    BENGALI("bn", "বাংলা"), MALAY("ms", "Bahasa Melayu"), PASHTO("ps", "پښتو", true), FRENCH("fr", "Français")
}
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AlertMode { OFF, NOTIFICATION, ADHAN }
enum class AsrConvention(val madhab: Madhab) { HANAFI(Madhab.HANAFI), STANDARD(Madhab.SHAFI) }

enum class School(val contentAvailable: Boolean = true) {
    HANAFI, SHAFII, MALIKI, HANBALI;
    val defaultAsr get() = if (this == HANAFI) AsrConvention.HANAFI else AsrConvention.STANDARD
    val guideId get() = "prayer-${name.lowercase(java.util.Locale.ROOT)}"
}

/** Stable IDs are persisted; presentation order never determines a calculation method. */
enum class CalculationPreset(val method: CalculationMethod) {
    KARACHI(CalculationMethod.KARACHI),
    MUSLIM_WORLD_LEAGUE(CalculationMethod.MUSLIM_WORLD_LEAGUE),
    EGYPTIAN(CalculationMethod.EGYPTIAN),
    UMM_AL_QURA(CalculationMethod.UMM_AL_QURA),
    DUBAI(CalculationMethod.DUBAI),
    MOON_SIGHTING_COMMITTEE(CalculationMethod.MOON_SIGHTING_COMMITTEE),
    NORTH_AMERICA(CalculationMethod.NORTH_AMERICA),
    SINGAPORE(CalculationMethod.SINGAPORE);

    fun parameters(asr: AsrConvention) = method.parameters.apply { madhab = asr.madhab }
}

enum class PrayerId(val canAlert: Boolean = true) {
    FAJR, SUNRISE(false), DHUHR, ASR, MAGHRIB, ISHA;
    companion object { val obligatory = entries.filter { it.canAlert } }
}

enum class LocationMode { GPS, MANUAL }

data class SavedLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String,
    val zoneId: String,
    val mode: LocationMode = LocationMode.MANUAL,
    val cityId: String? = null,
) {
    init {
        require(latitude.isFinite() && latitude in -90.0..90.0)
        require(longitude.isFinite() && longitude in -180.0..180.0)
        ZoneId.of(zoneId)
    }
    val zone: ZoneId get() = ZoneId.of(zoneId)
}

data class AdhanVoice(val id: String, val title: String, val fileName: String, val bytes: Long) {
    init { require(validFileName(fileName) && id.isNotBlank() && title.isNotBlank() && bytes in 1..20L*1024*1024) }
    companion object { fun validFileName(name: String) = Regex("[a-zA-Z0-9-]+\\.audio").matches(name) }
}

data class AppSettings(
    val language: AppLanguage = AppLanguage.PERSIAN,
    val school: School = School.HANAFI,
    val onboardingComplete: Boolean = false,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val textScale: Float = 1f,
    val reduceMotion: Boolean = false,
    val location: SavedLocation? = null,
    val calculationPreset: CalculationPreset = CalculationPreset.KARACHI,
    val asrConvention: AsrConvention = school.defaultAsr,
    val alertMode: AlertMode = AlertMode.OFF,
    val prayerAlerts: Map<PrayerId, AlertMode> = emptyMap(),
    val adhanVoices: List<AdhanVoice> = emptyList(),
    val selectedVoiceId: String? = null,
    val bookmarks: Set<String> = emptySet(),
    val completedSteps: Set<String> = emptySet(),
    val lastLessonId: String? = null,
    val lastStepId: String? = null,
    val hiddenNotificationUntil: Long = 0L,
) {
    fun selectedVoice(): AdhanVoice? = adhanVoices.firstOrNull { it.id == selectedVoiceId }
    fun withSchool(value: School): AppSettings = copy(
        school = value, asrConvention = value.defaultAsr,
        hiddenNotificationUntil = 0L,
    )
    fun alertFor(prayer: PrayerId): AlertMode = when {
        !prayer.canAlert || alertMode == AlertMode.OFF -> AlertMode.OFF
        else -> prayerAlerts[prayer] ?: alertMode
    }
}

inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: default
