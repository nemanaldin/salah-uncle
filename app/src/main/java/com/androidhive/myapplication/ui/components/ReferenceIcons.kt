package com.androidhive.myapplication.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Small original line icons; no added icon pack, downloaded asset or drawing of a prayer posture. */
object ReferenceIcons {
    val Learn: ImageVector = ImageVector.Builder("ReferenceLearn", 24.dp, 24.dp, 24f, 24f, autoMirror = true).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(5f, 20.5f); lineTo(5f, 4f); lineTo(19f, 4f); lineTo(19f, 19f); lineTo(7f, 19f)
            curveTo(4.3f, 19f, 4.3f, 22f, 7f, 22f)
            moveTo(9f, 8f); lineTo(15f, 8f)
        }
    }.build()
    val Chevron: ImageVector = ImageVector.Builder("ReferenceChevron", 24.dp, 24.dp, 24f, 24f, autoMirror = true).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f)
        }
    }.build()
}
