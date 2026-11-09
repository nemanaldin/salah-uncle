package com.androidhive.myapplication.platform.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import com.androidhive.myapplication.domain.qibla.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

typealias Heading = CompassHeading

class HeadingRepository(context: Context) {
    private val manager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    fun observe(displayRotation: Int) = callbackFlow {
        trySend(Heading())
        val vector = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val gravity=FloatArray(3); val magnetic=FloatArray(3); val matrix=FloatArray(9)
        val adjusted=FloatArray(9); val orientation=FloatArray(3)
        var haveGravity=false; var haveMagnetic=false
        var vectorActive=false
        var confidence=HeadingConfidence.UNRELIABLE
        var lastDegrees: Float?=null
        fun quality(value: Int)=when(value) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> HeadingConfidence.HIGH
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> HeadingConfidence.MEDIUM
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> HeadingConfidence.LOW
            else -> HeadingConfidence.UNRELIABLE
        }
        val listener=object:SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type==Sensor.TYPE_ROTATION_VECTOR) {
                    confidence=quality(event.accuracy)
                    SensorManager.getRotationMatrixFromVector(matrix,event.values)
                } else {
                    val target=if(event.sensor.type==Sensor.TYPE_ACCELEROMETER) gravity else magnetic
                    repeat(3) { i -> target[i]=.85f*target[i]+.15f*event.values[i] }
                    if(event.sensor.type==Sensor.TYPE_ACCELEROMETER) haveGravity=true
                    else {haveMagnetic=true;confidence=quality(event.accuracy)}
                    if(!haveGravity || !haveMagnetic) return
                    if(!SensorManager.getRotationMatrix(matrix,null,gravity,magnetic)) {
                        trySend(Heading(HeadingStatus.INVALID));return
                    }
                }
                val axes=when(displayRotation) {
                    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                }
                if(!SensorManager.remapCoordinateSystem(matrix,axes.first,axes.second,adjusted)) {
                    trySend(Heading(HeadingStatus.INVALID));return
                }
                SensorManager.getOrientation(adjusted,orientation)
                lastDegrees=Math.toDegrees(orientation[0].toDouble()).toFloat()
                trySend(compassHeading(lastDegrees,confidence))
            }
            override fun onAccuracyChanged(sensor:Sensor?,value:Int) {
                if(sensor?.type==Sensor.TYPE_ROTATION_VECTOR || !vectorActive && sensor?.type==Sensor.TYPE_MAGNETIC_FIELD) {
                    confidence=quality(value)
                    trySend(compassHeading(lastDegrees,confidence))
                }
            }
        }
        vectorActive=vector!=null && manager.registerListener(listener,vector,SensorManager.SENSOR_DELAY_UI)
        val registered=if(vectorActive) true else {
            val a=accelerometer!=null && manager.registerListener(listener,accelerometer,SensorManager.SENSOR_DELAY_UI)
            val m=magnetometer!=null && manager.registerListener(listener,magnetometer,SensorManager.SENSOR_DELAY_UI)
            a && m
        }
        if(!registered) {
            manager.unregisterListener(listener)
            trySend(Heading(HeadingStatus.INVALID));close()
        }
        awaitClose {manager.unregisterListener(listener)}
    }
}
