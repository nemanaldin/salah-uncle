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
import kotlinx.coroutines.withTimeoutOrNull
import com.androidhive.myapplication.domain.location.LocationFix
import com.androidhive.myapplication.domain.location.selectLocation
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
        val permitted = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .filter { manager.allProviders.contains(it) && (precise || it != LocationManager.GPS_PROVIDER) }
        val providers = permitted.filter { it != LocationManager.PASSIVE_PROVIDER && manager.isProviderEnabled(it) }
        @Suppress("MissingPermission")
        val cached = permitted.mapNotNull { provider ->
            try { manager.getLastKnownLocation(provider)?.fix() } catch (_: RuntimeException) { null }
        }
        val result = selectLocation(providers, cached, SystemClock::elapsedRealtimeNanos) { provider ->
            // A timeout belongs to this provider; outer cancellation still propagates.
            withTimeoutOrNull(8_000L) { request(provider).fix() }
        } ?: throw LocationUnavailable()
        return SavedLocation(result.latitude, result.longitude, "", ZoneId.systemDefault().id, LocationMode.GPS)
    }

    private fun Location.fix() = LocationFix(latitude, longitude,
        if (hasAccuracy()) accuracy else Float.NaN, elapsedRealtimeNanos)

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
