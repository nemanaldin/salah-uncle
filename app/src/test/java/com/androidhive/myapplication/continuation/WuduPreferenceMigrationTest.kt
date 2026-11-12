package com.androidhive.myapplication.continuation

import androidx.datastore.preferences.core.*
import com.androidhive.myapplication.core.model.*
import com.androidhive.myapplication.data.settings.*
import org.junit.Assert.*
import org.junit.Test

class WuduPreferenceMigrationTest {
    private val legacyVoice = stringPreferencesKey("adhan_voices_v1")
    private val rawLocation = stringPreferencesKey("location_names_v1")
    @Test fun migrationOnlyAddsTheNewBookmarkFieldAndMarker() {
        val before=mutablePreferencesOf(legacyVoice to "unparsed legacy bytes", rawLocation to "unparsed legacy location",
            stringSetPreferencesKey("bookmarks") to setOf("wudu-remembrance","other"),stringPreferencesKey("future_key") to "keep")
        val settings=AppSettings(bookmarks=setOf("wudu-remembrance","other"),completedSteps=setOf("old:step"))
        val result=migrateWuduPreferences(before,settings) { it.joinToString { item -> item.sectionId } }
        before.asMap().forEach { (key,value) -> assertEquals(value,result.asMap()[key]) }
        assertEquals(before.asMap().keys+setOf(SectionBookmarkPreferences.bookmarks,SectionBookmarkPreferences.migrated),result.asMap().keys)
        assertEquals("after-wudu",result[SectionBookmarkPreferences.bookmarks])
    }
    @Test fun migrationWithoutOldTopicDoesNotNormalizeExistingJson() {
        val before=mutablePreferencesOf(SectionBookmarkPreferences.bookmarks to "keep exact JSON bytes")
        var writes=0
        val result=migrateWuduPreferences(before,AppSettings()) { writes++;"replacement" }
        assertEquals(0,writes);assertEquals("keep exact JSON bytes",result[SectionBookmarkPreferences.bookmarks])
        assertEquals(true,result[SectionBookmarkPreferences.migrated])
    }
    @Test fun alreadyMigratedNotesAndOffsetsAreNeverReplaced() {
        val bookmark=SectionBookmark("wudu-remembrance","after-wudu",School.HANAFI,AppLanguage.ENGLISH,"my note",120)
        val before=mutablePreferencesOf(SectionBookmarkPreferences.bookmarks to "original")
        val result=migrateWuduPreferences(before,AppSettings(language=AppLanguage.ENGLISH,bookmarks=setOf("wudu-remembrance"),sectionBookmarks=listOf(bookmark))) { fail("unchanged bookmarks must not be serialized");"" }
        assertEquals("original",result[SectionBookmarkPreferences.bookmarks])
    }
    @Test fun migrationRetainsTheOriginalHiddenScopeBeforeVisibilityRecovery() {
        val old=AppSettings(school=School.MALIKI,language=AppLanguage.FRENCH,bookmarks=setOf("wudu-remembrance"))
        var written=emptyList<SectionBookmark>()
        migrateWuduPreferences(emptyPreferences(),old) { written=it;"encoded" }
        val recovered=old.copy(sectionBookmarks=written).recoverVisibility(listOf("en"))
        assertEquals(School.HANAFI,recovered.school);assertEquals(AppLanguage.ENGLISH,recovered.language)
        assertEquals(School.MALIKI,recovered.sectionBookmarks.single().school)
        assertEquals(AppLanguage.FRENCH,recovered.sectionBookmarks.single().language)
    }
}
