package com.androidhive.myapplication.domain

import com.androidhive.myapplication.data.location.effectiveCityZone
import java.time.ZoneId
import java.time.Instant
import java.time.ZoneOffset
import com.androidhive.myapplication.data.location.City
import com.androidhive.myapplication.data.location.normalizeCityQuery
import org.junit.Assert.*
import org.junit.Test

class CitySearchTest {
    private val city = City("1", "TR", "", "İstanbul", 41.0, 29.0, "Europe/Istanbul", "TR", listOf("Istanbul", "استانبول"))
    @Test fun dottedTurkishNamesRemainSearchableWithAsciiInput() {
        assertTrue(city.matches(normalizeCityQuery("istanbul")))
    }
    @Test fun arabicAndPersianVariantsAndMarksMatch() {
        val tehran = City("2", "IR", "", "Tehran", 35.7, 51.4, "Asia/Tehran", "IR", listOf("تهران", "كاشان"))
        assertTrue(tehran.matches(normalizeCityQuery("تِهْران")))
        assertTrue(tehran.matches(normalizeCityQuery("کاشان")))
        assertFalse(tehran.matches(normalizeCityQuery("Jakarta")))
    }
    @Test fun newChileanZoneHasTheDocumentedCurrentOffsetOnOlderAndroid() {
        val fallback = ZoneId.of(effectiveCityZone("America/Coyhaique", emptySet()))
        for (date in listOf("2026-06-01T12:00:00Z", "2026-12-01T12:00:00Z")) {
            assertEquals(ZoneOffset.ofHours(-3), fallback.rules.getOffset(Instant.parse(date)))
            assertEquals(ZoneId.of("America/Coyhaique").rules.getOffset(Instant.parse(date)), fallback.rules.getOffset(Instant.parse(date)))
        }
    }
    @Test fun supportedZoneIdsAreNeverReplaced() {
        assertEquals("America/Coyhaique", effectiveCityZone("America/Coyhaique", setOf("America/Coyhaique")))
        assertEquals("Europe/London", effectiveCityZone("Europe/London", emptySet()))
    }
    @Test fun everyQueryWordMustMatchTheSameCity() {
        assertTrue(city.matches(normalizeCityQuery("istanbul TR")))
        assertFalse(city.matches(normalizeCityQuery("istanbul ID")))
    }
}
