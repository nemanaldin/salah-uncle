package com.androidhive.myapplication.platform

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun openExternal(context: Context, intent: Intent, onError: () -> Unit) {
    try { context.startActivity(intent) }
    catch (_: ActivityNotFoundException) { onError() }
    catch (_: SecurityException) { onError() }
}
fun openUrl(context: Context, url: String, onError: () -> Unit) = openExternal(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)), onError)
fun shareText(context: Context, text: String, title: String, onError: () -> Unit) {
    openExternal(context, Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
    }, title), onError)
}
