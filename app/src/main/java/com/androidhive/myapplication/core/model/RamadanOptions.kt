package com.androidhive.myapplication.core.model

data class RamadanOptions(
    val suhurEnabled: Boolean = false,
    val minutesBeforeFajr: Int = 90,
    val iftarEnabled: Boolean = false,
    val onlyRamadan: Boolean = true,
    val calendarAdjustment: Int = 0,
) {
    init { require(minutesBeforeFajr in 5..240); require(calendarAdjustment in -2..2) }
}
