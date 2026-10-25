package com.androidhive.myapplication.ui.qibla

import android.hardware.GeomagneticField
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidhive.myapplication.R
import com.androidhive.myapplication.core.model.AppSettings
import com.androidhive.myapplication.domain.qibla.QiblaMath
import com.androidhive.myapplication.platform.location.Heading
import com.androidhive.myapplication.platform.location.HeadingRepository
import com.androidhive.myapplication.ui.components.*
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(settings: AppSettings, headings: HeadingRepository, onLocation: () -> Unit) {
    val location = settings.location
    if (location == null) { PageColumn { InfoCard(stringResource(R.string.ui_qibla_location)); Button(onLocation) { Text(stringResource(R.string.ui_set_location)) } }; return }
    val rotation = LocalView.current.display?.rotation ?: 0
    val flow = remember(headings, rotation) { headings.observe(rotation) }
    val heading by flow.collectAsStateWithLifecycle(initialValue = Heading(true))
    val bearing = remember(location) { QiblaMath.bearing(location.latitude, location.longitude) }
    val declination = remember(location) { GeomagneticField(location.latitude.toFloat(), location.longitude.toFloat(), 0f, System.currentTimeMillis()).declination }
    val trueHeading = if (heading.available) QiblaMath.normalize(heading.magneticDegrees + declination) else 0f
    val relative = QiblaMath.relativeBearing(bearing, trueHeading)
    var target by remember { mutableFloatStateOf(relative) }
    LaunchedEffect(relative) { target += QiblaMath.shortestDelta(QiblaMath.normalize(target), relative) }
    val angle by animateFloatAsState(target, animationSpec = if (settings.reduceMotion) snap() else tween(180), label = "Qibla needle")
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outlineVariant
    val description = stringResource(R.string.ui_compass_description)
    PageColumn(Modifier.testTag("qibla_screen")) {
        IllustratedHeading(stringResource(R.string.ui_qibla), R.drawable.heritage_qibla, stringResource(R.string.ui_qibla_body))
        AppCard {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = -trueHeading }, contentAlignment = Alignment.TopCenter) {
                    Canvas(Modifier.fillMaxSize().padding(20.dp)) {
                        val radius = size.minDimension / 2
                        drawCircle(primary.copy(alpha = .06f), radius = radius)
                        drawCircle(outline, radius = radius, style = Stroke(3.dp.toPx()))
                        drawCircle(primary.copy(alpha = .15f), radius = radius * .72f, style = Stroke(1.dp.toPx()))
                        repeat(36) { i ->
                            val a = Math.toRadians(i * 10.0)
                            val inner = radius - if (i % 9 == 0) 18.dp.toPx() else 8.dp.toPx()
                            drawLine(outline, androidx.compose.ui.geometry.Offset(center.x + (sin(a) * inner).toFloat(), center.y - (cos(a) * inner).toFloat()),
                                androidx.compose.ui.geometry.Offset(center.x + (sin(a) * radius).toFloat(), center.y - (cos(a) * radius).toFloat()), 2.dp.toPx())
                        }
                    }
                    Text(stringResource(R.string.ui_north), Modifier.padding(top = 32.dp), color = primary, style = MaterialTheme.typography.labelLarge)
                }
                Canvas(Modifier.fillMaxSize().padding(55.dp).graphicsLayer { rotationZ = angle }) {
                    val radius = size.minDimension / 2
                    val path = Path().apply {
                        moveTo(center.x, center.y - radius * .8f)
                        lineTo(center.x - radius * .18f, center.y + radius * .3f)
                        lineTo(center.x, center.y + radius * .12f)
                        lineTo(center.x + radius * .18f, center.y + radius * .3f); close()
                    }
                    drawPath(path, primary)
                    drawCircle(primary, radius = 7.dp.toPx())
                }
            }
            Text(stringResource(R.string.ui_qibla_bearing, String.format(Locale.forLanguageTag(settings.language.tag), "%.1f", bearing)), style = MaterialTheme.typography.titleMedium)
            if (!heading.available) InfoCard(stringResource(R.string.ui_qibla_no_sensor))
            else if (heading.accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) SupportingText(stringResource(R.string.ui_qibla_calibration))
        }
        SupportingText(location.label.ifBlank { stringResource(R.string.ui_device_location) })
        TextButton(onLocation) { Text(stringResource(R.string.ui_set_location)) }
    }
}
