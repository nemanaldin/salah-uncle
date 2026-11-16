package com.androidhive.myapplication.duas

import com.androidhive.myapplication.ui.home.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class ReferenceHomePresentationTest {
    private val now=Instant.parse("2026-11-16T07:00:00Z")
    @Test fun countdownUsesActualOneHourAndSixMinutes() {
        assertEquals(RemainingPrayerTime(1,6),remainingPrayerTime(now,now.plusSeconds(3960)))
    }
    @Test fun incompleteMinutesAreNotRoundedToAnInventedHour() {
        assertEquals(RemainingPrayerTime(0,59),remainingPrayerTime(now,now.plusSeconds(3599)))
        assertEquals(RemainingPrayerTime(1,0),remainingPrayerTime(now,now.plusSeconds(3600)))
    }
    @Test fun lessThanOneMinuteHasAnExplicitState() {
        assertTrue(remainingPrayerTime(now,now.plusSeconds(59)).lessThanMinute)
        assertFalse(remainingPrayerTime(now,now.plusSeconds(60)).lessThanMinute)
    }
    @Test fun lateTicksDoNotProduceNegativeTime() {
        assertEquals(RemainingPrayerTime(0,0),remainingPrayerTime(now,now.minusSeconds(30)))
    }
    @Test fun aNextDayPrayerRetainsItsRealDuration() {
        assertEquals(RemainingPrayerTime(25,5),remainingPrayerTime(now,now.plusSeconds(90300)))
    }
    @Test fun theFullPrayerPageRetainsItsSecondsDisplay() {
        assertEquals("01:06:00",countdown(now,now.plusSeconds(3960)))
    }
}
