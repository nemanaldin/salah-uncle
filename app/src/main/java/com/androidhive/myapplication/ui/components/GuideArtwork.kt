package com.androidhive.myapplication.ui.components

import android.content.res.Resources
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.AppLanguage
import com.androidhive.myapplication.data.content.*
import kotlin.math.min

private class GuideAtlas(resources: Resources) {
    // Decode only when a displayable stage is composed; share one bitmap across reader pages.
    private val atlas: ImageBitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.stage_art_draft,
            BitmapFactory.Options().apply { inScaled = false }).asImageBitmap()
    }
    private val prostration: ImageBitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.stage_prostration_draft,
            BitmapFactory.Options().apply { inScaled = false; inSampleSize = 2 }).asImageBitmap()
    }
    fun bitmap(asset: StageArtAsset) = when (asset) {
        StageArtAsset.ATLAS -> atlas
        StageArtAsset.PROSTRATION -> prostration
    }
}
private val LocalGuideAtlas = staticCompositionLocalOf<GuideAtlas?> { null }
@Composable
fun GuideArtworkProvider(content: @Composable () -> Unit) {
    val resources = LocalContext.current.resources
    val atlas = remember(resources) { GuideAtlas(resources) }
    CompositionLocalProvider(LocalGuideAtlas provides atlas, content = content)
}

@Composable
fun GuideStageIllustration(art: StageArt, language: AppLanguage, fallback: @Composable () -> Unit) {
    val description = art.alt(language)
    if (art.displayable) {
        val bitmap = requireNotNull(LocalGuideAtlas.current).bitmap(art.asset)
        val frame = if (art.asset == StageArtAsset.ATLAS) stageFrame(art.cell, bitmap.width, bitmap.height)
            else StageFrame(0, 0, bitmap.width, bitmap.height)
        Canvas(Modifier.fillMaxWidth().height(220.dp).clip(MaterialTheme.shapes.medium)
            .testTag("stage_art_${art.sourceLessonId}_${art.stepId}")
            .semantics { contentDescription = description; role = Role.Image }) {
            val scale = min(size.width / frame.width, size.height / frame.height)
            val width = (frame.width * scale).toInt(); val height = (frame.height * scale).toInt()
            drawImage(bitmap, srcOffset = IntOffset(frame.left, frame.top), srcSize = IntSize(frame.width, frame.height),
                dstOffset = IntOffset(((size.width - width) / 2).toInt(), ((size.height - height) / 2).toInt()), dstSize = IntSize(width, height))
        }
    } else {
        Box(Modifier.testTag("stage_art_placeholder_${art.stepId}").semantics {
            contentDescription = description; role = Role.Image
        }) { fallback() }
        SupportingText(stringResource(R.string.ui_art_placeholder))
    }
    SupportingText(stringResource(R.string.ui_art_draft))
}
