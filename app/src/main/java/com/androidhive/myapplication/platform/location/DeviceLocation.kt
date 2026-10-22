package com.androidhive.myapplication.platform.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import android.os.CancellationSignal
import com.androidhive.myapplication.core.model.LocationMode
import com.androidhive.myapplication.core.model.SavedLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationUnavailable : Exception()

class DeviceLocation(context: Context) {
    private val app = context.applicationContext
    private val manager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    suspend fun current(): SavedLocation {
        val precise = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val approximate = ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!precise && !approximate) throw SecurityException("Location permission required")
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { manager.allProviders.contains(it) && manager.isProviderEnabled(it) }
            .filter { precise || it != LocationManager.GPS_PROVIDER }
        if (providers.isEmpty()) throw LocationUnavailable()
        @Suppress("MissingPermission")
        val recent = providers.mapNotNull { manager.getLastKnownLocation(it) }
            .filter { SystemClock.elapsedRealtimeNanos() - it.elapsedRealtimeNanos in 0..600_000_000_000L }
            .minByOrNull { it.accuracy }
        val result = recent ?: withTimeout(25_000L) { request(providers.first()) }
        return SavedLocation(result.latitude, result.longitude, "", ZoneId.systemDefault().id, LocationMode.GPS)
    }

    @Suppress("MissingPermission")
    private suspend fun request(provider: String): Location = suspendCancellableCoroutine { continuation ->
        val signal = CancellationSignal()
        continuation.invokeOnCancellation { signal.cancel() }
        LocationManagerCompat.getCurrentLocation(manager, provider, signal, ContextCompat.getMainExecutor(app)) { location ->
            if (continuation.isActive) {
                if (location == null) continuation.resumeWithException(LocationUnavailable()) else continuation.resume(location)
            }
        }
    }
}
