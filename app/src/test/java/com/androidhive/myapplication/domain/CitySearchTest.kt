package com.androidhive.myapplication.domain

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
    @Test fun everyQueryWordMustMatchTheSameCity() {
        assertTrue(city.matches(normalizeCityQuery("istanbul TR")))
        assertFalse(city.matches(normalizeCityQuery("istanbul ID")))
    }
}
