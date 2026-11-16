package com.androidhive.myapplication.domain.duas

import com.androidhive.myapplication.core.model.DuaReminderOptions
import com.androidhive.myapplication.data.duas.*
import java.time.*
import kotlin.random.Random

/** Evenly spaced real instants strictly inside the local window, including DST gaps/overlaps. */
fun duaSlots(date: LocalDate, zone: ZoneId, options: DuaReminderOptions): List<Instant> {
    if (!options.valid) return emptyList()
    fun boundary(minute: Int) = if (minute == 1440) date.plusDays(1).atStartOfDay(zone).toInstant()
        else date.atTime(minute / 60, minute % 60).atZone(zone).toInstant()
    val start = boundary(options.startMinute); val end = boundary(options.endMinute)
    val duration = Duration.between(start, end).toMillis()
    if (duration <= options.count) return emptyList()
    return (1..options.count).map { start.plusMillis(duration * it / (options.count + 1)) }
        .distinct().filter { insideDuaWindow(it, zone, options) }
}
fun insideDuaWindow(time: Instant, zone: ZoneId, options: DuaReminderOptions): Boolean {
    if (!options.valid) return false
    val local = time.atZone(zone)
    val minute = local.hour * 60 + local.minute
    return minute >= options.startMinute && minute < options.endMinute
}
fun nextDuaSlot(now: Instant, zone: ZoneId, options: DuaReminderOptions, lastDelivered: Long = 0): Instant? {
    if (!options.canSchedule) return null
    val day = now.atZone(zone).toLocalDate()
    return (0L..2L).asSequence().flatMap { duaSlots(day.plusDays(it), zone, options).asSequence() }
        .firstOrNull { it > now && it.toEpochMilli() > lastDelivered }
}
data class DuaDraw(val id: String, val seen: Set<String>)

/** Favorites get three tickets while remaining in the deck; a cycle still contains each dua only once. */
fun drawDua(pool: List<String>, favorites: Set<String>, seen: Set<String>, previous: String?, random: Random = Random.Default): DuaDraw? {
    val ids = pool.distinct()
    if (ids.isEmpty()) return null
    var retained = seen.intersect(ids.toSet())
    var remaining = ids.filterNot { it in retained }
    if (remaining.isEmpty()) {
        retained = emptySet()
        remaining = ids.filterNot { ids.size > 1 && it == previous }
    }
    val total = remaining.sumOf { if (it in favorites) 3 else 1 }
    var ticket = random.nextInt(total)
    val selected = remaining.first { id -> ticket -= if (id in favorites) 3 else 1; ticket < 0 }
    return DuaDraw(selected, retained + selected)
}

/** Select actual content, not page names. Missing meanings and unresolved source/grade/text fields are excluded. */
fun DuaCatalog.reminderCandidates(categories: Set<String>, tag: String, favorites: Set<String>): List<DuaItem> {
    val ids = sections.filter { it.category in categories }.flatMap { it.duaIds }.toSet()
    return items.filter { it.id in ids && !it.guidance && it.reminder && it.hasVerifiedText && it.translation(tag).isNotBlank() }
        .groupBy { normalizeDuaSearch(it.arabic) }.values.map { sameText -> sameText.firstOrNull { it.id in favorites } ?: sameText.first() }
}

/** A bookmark on an alias must not make an already-seen Arabic payload repeat within its cycle. */
val DuaItem.reminderKey: String get() = java.security.MessageDigest.getInstance("SHA-256")
    .digest(normalizeDuaSearch(arabic).toByteArray(Charsets.UTF_8)).joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }

/** Receiver intents may request only known catalog IDs in their own real subcategory. */
fun validatedDuaRoute(route: String, catalog: DuaCatalog): String? {
    val match = Regex("duas/read/([a-z0-9-]+)/([a-z0-9-]+)").matchEntire(route) ?: return null
    return route.takeIf { catalog.validReaderRoute(match.groupValues[2], match.groupValues[1]) }
}
