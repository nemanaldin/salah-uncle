package com.androidhive.myapplication.platform.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.androidhive.myapplication.core.model.AdhanVoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Files chosen through Android's document picker are copied once for reliable offline alarms. */
class AdhanRecordings(context: Context) {
    private val app = context.applicationContext
    private val directory get() = File(app.filesDir, "adhan_voices").apply { mkdirs() }
    fun file(name: String?): File? = name?.takeIf { AdhanVoice.validFileName(it) }
        ?.let { File(directory, it) }?.takeIf { it.isFile && it.length() in 1..MAX_BYTES }
    fun uri(voice: AdhanVoice): Uri? = file(voice.fileName)?.let(Uri::fromFile)
    fun delete(voice: AdhanVoice) { file(voice.fileName)?.delete() }

    suspend fun import(uri: Uri): AdhanVoice = withContext(Dispatchers.IO) {
        require(uri.scheme == "content")
        val id = UUID.randomUUID().toString()
        val target = File(directory, "$id.audio")
        try {
            val title = app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }?.take(160)?.takeIf { it.isNotBlank() } ?: id
            requireNotNull(app.contentResolver.openInputStream(uri)).use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var size = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        size += count
                        require(size <= MAX_BYTES)
                        output.write(buffer, 0, count)
                    }
                    require(size > 0)
                }
            }
            val metadata = MediaMetadataRetriever()
            try {
                metadata.setDataSource(target.absolutePath)
                require(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let { it > 0 } == true)
                require(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) == "yes")
            } finally { metadata.release() }
            AdhanVoice(id, title, target.name, target.length())
        } catch (error: Exception) { target.delete(); throw error }
    }
    companion object { const val MAX_BYTES = 20L * 1024 * 1024 }
}
