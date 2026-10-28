package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.location.*
import org.junit.Assert.*
import org.junit.Test

class PlaceNameTest {
    private val point = City("ایران/گلستان/مراوه‌تپه", "ایران", "گلستان", "مراوه‌تپه", 37.9, 55.95, "Asia/Tehran", "IR",
        names = mapOf("fa" to "مراوه‌تپه", "en" to "Maraveh Tappeh", "tr" to "Merave Tepe"), provinceEn = "Golestan")
    @Test fun nameFollowsLanguageWithoutMovingTheStoredPoint() {
        val location = point.location()
        assertEquals("مراوه‌تپه", location.labelFor(AppLanguage.PERSIAN))
        assertEquals("Maraveh Tappeh", location.labelFor(AppLanguage.ENGLISH))
        assertEquals("Merave Tepe", location.labelFor(AppLanguage.TURKISH))
        assertEquals("Maraveh Tappeh", location.labelFor(AppLanguage.FRENCH))
        assertEquals(37.9, location.latitude, 0.0); assertEquals(55.95, location.longitude, 0.0)
        assertEquals("Asia/Tehran", location.zoneId); assertEquals(point.id, location.cityId)
        assertEquals("مراوه‌تپه", location.label)
    }
    @Test fun searchFindsNativeAndLatinNamesAndProvinceHasAnEnglishForm() {
        assertTrue(point.matches(normalizeCityQuery("Maraveh Golestan")))
        assertTrue(point.matches(normalizeCityQuery("مراوه تپه")))
        assertEquals("Golestan", point.provinceName(AppLanguage.ENGLISH))
    }
    @Test fun anUnknownPersianLabelIsNeverMasqueradedAsEnglish() {
        val oldGps = SavedLocation(35.7, 51.4, "نام قدیمی", "Asia/Tehran", LocationMode.GPS)
        assertEquals("", oldGps.labelFor(AppLanguage.ENGLISH))
        assertEquals("نام قدیمی", oldGps.labelFor(AppLanguage.PERSIAN))
    }
}
