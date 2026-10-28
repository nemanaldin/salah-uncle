package com.androidhive.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.PrayerId
import kotlin.math.min

enum class PrayerSymbol {
    FAJR, DHUHR, ASR, MAGHRIB, ISHA, FRIDAY, CONGREGATION, EID,
    FUNERAL, TRAVEL, ECLIPSE, QADA, TAHAJJUD, TARAWIH, TASBIH, MOSQUE,
    SUNRISE, QADR, ISHRAQ, AWWABIN;
}
private data class PrayerAtlases(val primary: ImageBitmap, val extra: ImageBitmap)
private val LocalAtlas = staticCompositionLocalOf<PrayerAtlases?> { null }

/** One decoded bitmap shared by all visible icons, not a player/bitmap per row. */
@Composable
fun PrayerArtworkProvider(content: @Composable () -> Unit) {
    val resources = LocalContext.current.resources
    val atlas = remember(resources) {
        // Thumbnails do not need full 1254px sheets in memory on older phones.
        fun decode(id: Int) = BitmapFactory.decodeResource(resources, id, BitmapFactory.Options().apply {
            inSampleSize = 2; inScaled = false
        }).asImageBitmap()
        PrayerAtlases(decode(R.drawable.prayer_atlas), decode(R.drawable.prayer_atlas_extra))
    }
    CompositionLocalProvider(LocalAtlas provides atlas, content = content)
}
fun PrayerId.artwork() = when (this) {
    PrayerId.FAJR -> PrayerSymbol.FAJR
    PrayerId.SUNRISE -> PrayerSymbol.SUNRISE
    PrayerId.DHUHR -> PrayerSymbol.DHUHR
    PrayerId.ASR -> PrayerSymbol.ASR
    PrayerId.MAGHRIB -> PrayerSymbol.MAGHRIB
    PrayerId.ISHA -> PrayerSymbol.ISHA
}
fun topicSymbol(id: String): PrayerSymbol? = when (id) {
    "friday" -> PrayerSymbol.FRIDAY
    "congregation" -> PrayerSymbol.CONGREGATION
    "eid" -> PrayerSymbol.EID
    "funeral" -> PrayerSymbol.FUNERAL
    "travel" -> PrayerSymbol.TRAVEL
    "signs" -> PrayerSymbol.ECLIPSE
    "missed-prayers" -> PrayerSymbol.QADA
    "night-prayer" -> PrayerSymbol.TAHAJJUD
    "qadr" -> PrayerSymbol.QADR
    "tarawih" -> PrayerSymbol.TARAWIH
    "tasbih-prayer" -> PrayerSymbol.TASBIH
    "greeting-mosque" -> PrayerSymbol.MOSQUE
    "ishraq" -> PrayerSymbol.ISHRAQ
    "awwabin" -> PrayerSymbol.AWWABIN
    "daily-prayers" -> PrayerSymbol.FAJR
    else -> null
}
@Composable
fun PrayerArtwork(symbol: PrayerSymbol, modifier: Modifier = Modifier) {
    val sheets = requireNotNull(LocalAtlas.current)
    val extra = symbol.ordinal >= 16
    val atlas = if (extra) sheets.extra else sheets.primary
    val index = if (extra) symbol.ordinal - 16 else symbol.ordinal
    val columns = if (extra) 2 else 4
    // Bounds describe the generated original; cropping happens only during drawing.
    val rows = if (extra) listOf(0f, .5f, 1f) else listOf(0f, .2496f, .4896f, .7289f, .9681f)
    val row = index / columns
    val col = index % columns
    Canvas(modifier.clip(CircleShape)) {
        val width = atlas.width / columns
        val top = (atlas.height * rows[row]).toInt()
        val height = (atlas.height * rows[row + 1]).toInt() - top
        val scale = min(size.width / width, size.height / height)
        val dw = (width * scale).toInt(); val dh = (height * scale).toInt()
        drawImage(atlas, srcOffset = IntOffset(col * width, top), srcSize = IntSize(width, height),
            dstOffset = IntOffset(((size.width - dw) / 2).toInt(), ((size.height - dh) / 2).toInt()), dstSize = IntSize(dw, dh))
    }
}
@Composable
fun TopicArtwork(id: String, modifier: Modifier = Modifier) {
    val symbol = topicSymbol(id)
    if (symbol != null) PrayerArtwork(symbol, modifier)
    else Image(painterResource(lessonArtwork(id)), null, modifier.clip(CircleShape))
}
@Composable
fun TopicHeading(title: String, id: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
        TopicArtwork(id, Modifier.size(76.dp))
    }
}
