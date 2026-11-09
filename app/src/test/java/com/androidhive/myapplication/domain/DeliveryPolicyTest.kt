package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.AlertMode
import com.androidhive.myapplication.domain.prayer.*
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DeliveryPolicyTest {
    private val due = Instant.parse("2026-11-09T05:00:00Z")
    @Test fun futureEvenOneNanosecondIsIgnored() { assertEquals(PrayerDelivery.IGNORE, prayerDelivery(AlertMode.ADHAN, due, due.minusNanos(1))) }
    @Test fun exactTimeCanPlayAdhan() { assertEquals(PrayerDelivery.PLAY_ADHAN, prayerDelivery(AlertMode.ADHAN, due, due)) }
    @Test fun exactlyTenMinutesStillUsesTheExistingAudioWindow() { assertEquals(PrayerDelivery.PLAY_ADHAN, prayerDelivery(AlertMode.ADHAN, due, due.plusSeconds(600))) }
    @Test fun firstNanosecondBeyondTenMinutesStillNotifiesSilently() { assertEquals(PrayerDelivery.NOTIFY, prayerDelivery(AlertMode.ADHAN, due, due.plusSeconds(600).plusNanos(1))) }
    @Test fun elevenMinutesIsNotDropped() { assertEquals(PrayerDelivery.NOTIFY, prayerDelivery(AlertMode.ADHAN, due, due.plusSeconds(660))) }
    @Test fun oneHourLateNotifiesWithoutAudio() { assertEquals(PrayerDelivery.NOTIFY, prayerDelivery(AlertMode.ADHAN, due, due.plusSeconds(3600))) }
    @Test fun notificationModeNeverPlaysAudio() { assertEquals(PrayerDelivery.NOTIFY, prayerDelivery(AlertMode.NOTIFICATION, due, due.plusSeconds(3600))) }
    @Test fun disabledAlertsStayDisabledEvenWhenLate() { assertEquals(PrayerDelivery.IGNORE, prayerDelivery(AlertMode.OFF, due, due.plusSeconds(3600))) }
}
