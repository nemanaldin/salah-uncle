package com.androidhive.myapplication

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.OpenableColumns

/** Reuses the existing recording to test SAF import; no synthetic adhan is shipped. */
class TestAudioProvider : ContentProvider() {
    override fun onCreate() = true
    override fun getType(uri: Uri) = "audio/mpeg"
    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor {
        require(mode == "r")
        return requireNotNull(context).createPackageContext("com.androidhive.myapplication", 0)
            .resources.openRawResourceFd(R.raw.dhan_sound)
    }
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME)
        return MatrixCursor(columns).apply { addRow(columns.map { if (it == OpenableColumns.DISPLAY_NAME) "QA-${uri.lastPathSegment}.mp3" else null }) }
    }
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
}
