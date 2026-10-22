package com.androidhive.myapplication.core

import android.content.Context
import android.content.res.Configuration
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.*
import java.util.Locale

fun PrayerId.labelRes() = when (this) {
    PrayerId.FAJR -> R.string.ui_fajr; PrayerId.SUNRISE -> R.string.ui_sunrise
    PrayerId.DHUHR -> R.string.ui_dhuhr; PrayerId.ASR -> R.string.ui_asr
    PrayerId.MAGHRIB -> R.string.ui_maghrib; PrayerId.ISHA -> R.string.ui_isha
}
fun AlertMode.labelRes() = when (this) {
    AlertMode.OFF -> R.string.ui_alert_off; AlertMode.NOTIFICATION -> R.string.ui_alert_notification; AlertMode.ADHAN -> R.string.ui_alert_adhan
}
fun School.labelRes() = when (this) {
    School.HANAFI -> R.string.ui_school_hanafi; School.SHAFII -> R.string.ui_school_shafii
    School.MALIKI -> R.string.ui_school_maliki; School.HANBALI -> R.string.ui_school_hanbali
}
fun CalculationPreset.labelRes() = when (this) {
    CalculationPreset.KARACHI -> R.string.ui_calc_karachi; CalculationPreset.MUSLIM_WORLD_LEAGUE -> R.string.ui_calc_mwl
    CalculationPreset.EGYPTIAN -> R.string.ui_calc_egypt; CalculationPreset.UMM_AL_QURA -> R.string.ui_calc_umm
    CalculationPreset.DUBAI -> R.string.ui_calc_dubai; CalculationPreset.MOON_SIGHTING_COMMITTEE -> R.string.ui_calc_moonsighting
    CalculationPreset.NORTH_AMERICA -> R.string.ui_calc_isna
    CalculationPreset.SINGAPORE -> R.string.ui_calc_singapore
}
fun localizedContext(context: Context, language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.tag)
    val config = Configuration(context.resources.configuration).apply { setLocale(locale); setLayoutDirection(locale) }
    return context.createConfigurationContext(config)
}
