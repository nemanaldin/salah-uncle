package com.androidhive.myapplication.core.model

/** Additive preferences, separate from all adhan, Ramadan and lesson bookmark keys. */
data class DuaReminderOptions(
    val enabled: Boolean = false,
    val consent: Boolean = false,
    val count: Int = 3,
    val startMinute: Int = 8 * 60,
    val endMinute: Int = 21 * 60,
    val categoryIds: Set<String> = setOf("general", "dhikr"),
) {
    val valid get() = count in 1..8 && startMinute in 0..1439 && endMinute in 1..1440 && startMinute < endMinute && categoryIds.isNotEmpty()
    val canSchedule get() = enabled && consent && valid
}
data class DuaReminderHistory(
    val seen: Set<String> = emptySet(), val lastDuaId: String? = null,
    val lastSlot: Long = 0L, val scheduledSlot: Long = 0L,
)
