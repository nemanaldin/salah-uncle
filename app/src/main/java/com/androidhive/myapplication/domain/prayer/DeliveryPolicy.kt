package com.androidhive.myapplication.domain.prayer

import com.androidhive.myapplication.core.model.AlertMode
import java.time.Duration
import java.time.Instant

enum class PrayerDelivery { IGNORE, NOTIFY, PLAY_ADHAN }

/** A delayed notification remains useful; old audio must never unexpectedly ring. */
fun prayerDelivery(mode: AlertMode, scheduled: Instant, now: Instant): PrayerDelivery {
    if (mode == AlertMode.OFF || now.isBefore(scheduled)) return PrayerDelivery.IGNORE
    if (Duration.between(scheduled, now) > Duration.ofSeconds(600)) return PrayerDelivery.NOTIFY
    return if (mode == AlertMode.ADHAN) PrayerDelivery.PLAY_ADHAN else PrayerDelivery.NOTIFY
}
