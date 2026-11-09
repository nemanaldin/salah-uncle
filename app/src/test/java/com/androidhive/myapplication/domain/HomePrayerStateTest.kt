package com.androidhive.myapplication.domain
import com.androidhive.myapplication.domain.prayer.*
import org.junit.Assert.assertEquals
import org.junit.Test
class HomePrayerStateTest {
    @Test fun unsetLocationHasItsOwnState() {assertEquals(HomePrayerState.LOCATION_NOT_SET,homePrayerState(false,false))}
    @Test fun missingNextWithSavedLocationIsNotALocationPrompt() {assertEquals(HomePrayerState.CALCULATION_UNAVAILABLE,homePrayerState(true,false))}
    @Test fun calculationFailureIsExplicit() {assertEquals(HomePrayerState.CALCULATION_UNAVAILABLE,homePrayerState(true,false,true))}
    @Test fun loadingFailureIsNotALocationPrompt() {assertEquals(HomePrayerState.LOADING_FAILED,homePrayerState(false,false,readFailed=true))}
    @Test fun initialLoadingIsNotAMissingLocation() {assertEquals(HomePrayerState.LOADING,homePrayerState(false,false,loading=true))}
    @Test fun aReadyPrayerHasItsOwnState() {assertEquals(HomePrayerState.READY,homePrayerState(true,true))}
}
