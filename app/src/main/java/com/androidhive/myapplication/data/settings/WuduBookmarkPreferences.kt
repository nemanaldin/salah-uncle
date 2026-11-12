package com.androidhive.myapplication.data.settings

import androidx.datastore.preferences.core.*
import com.androidhive.myapplication.core.model.*

internal object SectionBookmarkPreferences {
    val bookmarks = stringPreferencesKey("section_bookmarks_v1")
    val migrated = booleanPreferencesKey("post_wudu_bookmarks_v1")
}

/** An additive migration must not round-trip unrelated legacy settings through their decoder. */
internal fun migrateWuduPreferences(data: Preferences, settings: AppSettings,
    serialize: (List<SectionBookmark>) -> String): Preferences {
    val migrated = settings.migratePostWuduBookmark()
    return data.toMutablePreferences().apply {
        if (migrated.sectionBookmarks != settings.sectionBookmarks)
            this[SectionBookmarkPreferences.bookmarks] = serialize(migrated.sectionBookmarks)
        this[SectionBookmarkPreferences.migrated] = true
    }
}
