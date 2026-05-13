package com.example.fuel_prices.data

import android.util.Log
import kotlin.math.*

/**
 * Repository that fetches fuel station data from the remote API
 * via [FuelApiService]. All methods are blocking and should be called
 * from a background thread / coroutine with Dispatchers.IO.
 */
class StationRepository(
    private val api: FuelApiService = FuelApiService()
) {

    companion object {
        private const val TAG = "StationRepository"

        /**
         * Calculates the great-circle distance between two GPS coordinates
         * using the Haversine formula. Returns distance in kilometers.
         */
        fun haversineDistance(
            lat1: Double, lng1: Double,
            lat2: Double, lng2: Double
        ): Double {
            val r = 6371.0 // Earth radius in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLng = Math.toRadians(lng2 - lng1)
            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLng / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }
    }

    /**
     * Returns stations sorted by distance from the given coordinates,
     * optionally filtered by fuel type, limited to [limit] results.
     * Uses the /stations/near API endpoint.
     */
    fun getStationsSortedByDistance(
        lat: Double,
        lng: Double,
        fuelType: FuelType?,
        limit: Int = 20
    ): List<StationWithDistance> {
        return try {
            val stations = api.fetchStationsNear(
                lat = lat,
                lng = lng,
                radius = 50.0,
                limit = limit
            )

            stations.map { station ->
                StationWithDistance(
                    station = station,
                    distanceKm = station.distance_km
                        ?: haversineDistance(lat, lng, station.lat, station.lng)
                )
            }.let { list ->
                if (fuelType != null) {
                    list.filter { swd ->
                        when (fuelType) {
                            FuelType.PB95 -> swd.station.prices.pb95 != null
                            FuelType.DIESEL -> swd.station.prices.diesel != null
                            FuelType.LPG -> swd.station.prices.lpg != null
                        }
                    }.sortedWith(
                        compareBy<StationWithDistance> {
                            when (fuelType) {
                                FuelType.PB95 -> it.station.prices.pb95
                                FuelType.DIESEL -> it.station.prices.diesel
                                FuelType.LPG -> it.station.prices.lpg
                            }
                        }.thenBy { it.distanceKm }
                    )
                } else {
                    list.sortedBy { it.distanceKm }
                }
            }.take(limit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch nearby stations", e)
            emptyList()
        }
    }
}
