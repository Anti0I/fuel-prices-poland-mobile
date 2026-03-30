package com.example.fuel_prices.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper(private val context: Context) {

    companion object {
        private const val TAG = "LocationHelper"
        private const val UPDATE_INTERVAL_MS = 30_000L
        private const val FASTEST_INTERVAL_MS = 10_000L

        // Warsaw center as default fallback
        const val DEFAULT_LAT = 52.2297
        const val DEFAULT_LNG = 21.0122
    }

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null

    val hasLocationPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    /**
     * Creates a default [Location] pointing to Warsaw center.
     */
    fun getDefaultLocation(): Location {
        return Location("default").apply {
            latitude = DEFAULT_LAT
            longitude = DEFAULT_LNG
        }
    }

    /**
     * Starts throttled location updates. Calls [onLocationUpdate] with each new location.
     * Falls back to last known location or Warsaw default if permission is missing.
     */
    fun startLocationUpdates(onLocationUpdate: (Location) -> Unit) {
        if (!hasLocationPermission) {
            Log.w(TAG, "Location permission not granted, using default location")
            onLocationUpdate(getDefaultLocation())
            return
        }

        // Try to get last known location first
        try {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationUpdate(location)
                } else {
                    onLocationUpdate(getDefaultLocation())
                }
            }.addOnFailureListener {
                Log.w(TAG, "Failed to get last location", it)
                onLocationUpdate(getDefaultLocation())
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException getting last location", e)
            onLocationUpdate(getDefaultLocation())
        }

        // Set up continuous updates
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            UPDATE_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onLocationUpdate(it) }
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException requesting location updates", e)
        }
    }

    /**
     * Stops location updates to conserve resources.
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }
}
