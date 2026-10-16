package com.androidhive.myapplication.platform.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import com.androidhive.myapplication.domain.qibla.QiblaMath
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

data class Heading(val available: Boolean, val magneticDegrees: Float = 0f, val accuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE)

class HeadingRepository(context: Context) {
    private val manager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun observe(displayRotation: Int) = callbackFlow {
        val vector = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (vector == null && (accelerometer == null || magnetometer == null)) {
            trySend(Heading(false)); close(); return@callbackFlow
        }
        val gravity = FloatArray(3)
        val magnetic = FloatArray(3)
        val matrix = FloatArray(9)
        val adjusted = FloatArray(9)
        val orientation = FloatArray(3)
        var accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(matrix, event.values)
                } else {
                    val target = if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) gravity else magnetic
                    repeat(3) { index -> target[index] = .85f * target[index] + .15f * event.values[index] }
                    if (!SensorManager.getRotationMatrix(matrix, null, gravity, magnetic)) return
                }
                val axes = when (displayRotation) {
                    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                }
                SensorManager.remapCoordinateSystem(matrix, axes.first, axes.second, adjusted)
                SensorManager.getOrientation(adjusted, orientation)
                trySend(Heading(true, QiblaMath.normalize(Math.toDegrees(orientation[0].toDouble()).toFloat()), accuracy))
            }
            override fun onAccuracyChanged(sensor: Sensor?, value: Int) { accuracy = value }
        }
        if (vector != null) manager.registerListener(listener, vector, SensorManager.SENSOR_DELAY_UI)
        else {
            manager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            manager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
        awaitClose { manager.unregisterListener(listener) }
    }
}
