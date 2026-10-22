package com.androidhive.myapplication;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.FileNotFoundException;

/** Runs in the test APK's own process, without the target APK's Kotlin classes. */
public final class TestAudioProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }
    @Override public String getType(Uri uri) { return "audio/mpeg"; }
    @Override public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) throw new FileNotFoundException("Read only");
        try {
            Context target = getContext().createPackageContext("com.androidhive.myapplication", 0);
            int resource = target.getResources().getIdentifier("dhan_sound", "raw", target.getPackageName());
            return target.getResources().openRawResourceFd(resource);
        } catch (PackageManager.NameNotFoundException e) { throw new FileNotFoundException(e.toString()); }
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String[] columns = projection == null ? new String[]{OpenableColumns.DISPLAY_NAME} : projection;
        MatrixCursor cursor = new MatrixCursor(columns);
        Object[] row = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) if (OpenableColumns.DISPLAY_NAME.equals(columns[i])) row[i] = "QA-" + uri.getLastPathSegment() + ".mp3";
        cursor.addRow(row);
        return cursor;
    }
    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
