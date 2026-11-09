package com.androidhive.myapplication.core.model

/** Labels, translated names and theme do not change the astronomical configuration. */
fun AppSettings.calculationKey(): String? = location?.let {
    listOf(it.latitude, it.longitude, it.zoneId, calculationPreset.name, asrConvention.name).joinToString("|")
}
fun AppSettings.needsCalculationConfirmation(): Boolean =
    calculationKey()?.let { it != confirmedCalculation } ?: false
fun AppSettings.confirmCalculation(): AppSettings = copy(confirmedCalculation = calculationKey())
