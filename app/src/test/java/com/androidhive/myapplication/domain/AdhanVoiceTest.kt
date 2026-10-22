package com.androidhive.myapplication.domain

import com.androidhive.myapplication.core.model.*
import org.junit.Assert.*
import org.junit.Test

class AdhanVoiceTest {
    private val voice = AdhanVoice("recording", "My recording", "recording.audio", 1200)
    @Test fun importedAudioCannotAddressFilesOutsideThePrivateRecordingDirectory() {
        listOf("../preferences.audio", "/tmp/other.audio", "other.mp3", "a/b.audio").forEach { assertFalse(AdhanVoice.validFileName(it)) }
        assertTrue(AdhanVoice.validFileName("123-abc.audio"))
    }
    @Test fun removedOrUnknownSelectionUsesTheExistingDefaultRecording() {
        assertNull(AppSettings(adhanVoices = listOf(voice), selectedVoiceId = "missing").selectedVoice())
        assertNull(AppSettings().selectedVoice())
    }
    @Test fun changingSchoolKeepsTheChosenRecordingAndPrayerAlerts() {
        val initial = AppSettings(adhanVoices = listOf(voice), selectedVoiceId = voice.id, alertMode = AlertMode.ADHAN,
            prayerAlerts = mapOf(PrayerId.FAJR to AlertMode.NOTIFICATION))
        val changed = initial.withSchool(School.MALIKI)
        assertEquals(voice, changed.selectedVoice())
        assertEquals(initial.prayerAlerts, changed.prayerAlerts)
        assertEquals(AlertMode.ADHAN, changed.alertMode)
    }
}
