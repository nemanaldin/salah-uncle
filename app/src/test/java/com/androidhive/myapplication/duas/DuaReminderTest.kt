package com.androidhive.myapplication.duas

import com.androidhive.myapplication.domain.duas.*
import com.androidhive.myapplication.core.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*
import kotlin.random.Random

class DuaReminderTest {
    @Test fun defaultIsOffAndConsentIsRequired() {
        val defaults = DuaReminderOptions()
        assertFalse(defaults.enabled)
        assertNull(nextDuaSlot(Instant.now(), ZoneId.of("UTC"), defaults))
        assertNull(nextDuaSlot(Instant.now(), ZoneId.of("UTC"), defaults.copy(enabled = true)))
        assertNotNull(nextDuaSlot(Instant.now(), ZoneId.of("UTC"), defaults.copy(enabled = true, consent = true)))
    }
    @Test fun slotsAreInsideTheChosenWindowAndExactlyTheChosenCount() {
        val date = LocalDate.of(2026, 11, 16); val zone = ZoneId.of("Asia/Tehran")
        for (n in 1..8) {
            val options = DuaReminderOptions(enabled = true, consent = true, count = n)
            val slots = duaSlots(date, zone, options)
            assertEquals(n, slots.distinct().size)
            assertTrue(slots.all { it.atZone(zone).hour in 8..20 })
            assertEquals(slots.sorted(), slots)
        }
    }
    @Test fun nextSlotSkipsPastAndAlreadyDeliveredSlots() {
        val options = DuaReminderOptions(true, true, count = 1)
        val zone = ZoneId.of("UTC");val now = Instant.parse("2026-11-16T23:00:00Z")
        assertEquals(LocalDate.of(2026,11,17), nextDuaSlot(now, zone, options)!!.atZone(zone).toLocalDate())
        val slot = duaSlots(LocalDate.of(2026,11,16), zone, options).single()
        assertTrue(nextDuaSlot(slot.minusSeconds(1), zone, options, slot.toEpochMilli())!! > slot)
    }
    @Test fun dstGapAndOverlapProduceDistinctRealInstantsInTheWindow() {
        val zone=ZoneId.of("Europe/Berlin")
        val options=DuaReminderOptions(true,true,count=8,startMinute=60,endMinute=300)
        for (date in listOf(LocalDate.of(2026,3,29),LocalDate.of(2026,10,25))) {
            val slots=duaSlots(date,zone,options)
            assertEquals(8,slots.distinct().size)
            assertTrue(slots.all { insideDuaWindow(it,zone,options) })
        }
    }
    @Test fun invalidBoundsNeverScheduleAnAlarm() {
        val now=Instant.now();val zone=ZoneId.of("UTC")
        for (options in listOf(DuaReminderOptions(true,true,count=9),DuaReminderOptions(true,true,startMinute=1000,endMinute=900),DuaReminderOptions(true,true,categoryIds=emptySet()))) {
            assertNull(nextDuaSlot(now,zone,options))
        }
    }
    @Test fun favoritesCannotRepeatUntilTheEntirePoolHasBeenSeen() {
        var seen=emptySet<String>();var last:String?=null;val shown=mutableListOf<String>()
        repeat(6) {
            val pick=drawDua(listOf("a","b","c"),setOf("a"),seen,last,Random(it))!!
            shown+=pick.id;seen=pick.seen;last=pick.id
        }
        assertEquals(3,shown.take(3).distinct().size)
        assertEquals(3,shown.drop(3).distinct().size)
        assertNotEquals(shown[2],shown[3])
    }
    @Test fun favoritesAreMoreLikelyToAppearEarlierInANewCycle() {
        val random=Random(1831)
        val favoritesFirst=(1..2000).count { drawDua(listOf("a","b"),setOf("a"),emptySet(),null,random)!!.id=="a" }
        assertTrue(favoritesFirst in 1350..1650)
    }
    @Test fun changedPoolsAndAnEmptyPoolAreSafe() {
        assertNull(drawDua(emptyList(),emptySet(),setOf("old"),"old",Random(0)))
        val pick=drawDua(listOf("new"),emptySet(),setOf("old"),"old",Random(0))!!
        assertEquals(setOf("new"),pick.seen)
    }
}
