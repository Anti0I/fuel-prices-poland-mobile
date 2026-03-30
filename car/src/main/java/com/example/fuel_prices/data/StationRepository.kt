package com.example.fuel_prices.data

import android.content.Context
import com.example.fuel_prices.R
import com.google.gson.Gson
import java.io.InputStreamReader
import kotlin.math.*

class StationRepository(private val context: Context) {

    private var cachedStations: List<Station> = emptyList()

    init {
        loadData()
    }

    private fun loadData() {
        try {
            val inputStream = context.resources.openRawResource(R.raw.mock_stations)
            val reader = InputStreamReader(inputStream)
            val response = Gson().fromJson(reader, MockResponse::class.java)
            reader.close()
            
            cachedStations = response.cities.flatMap { it.stations }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFilteredStations(fuelType: FuelType?, brand: String?): List<Station> {
        val filtered = cachedStations.filter { station ->
            val matchFuel = when (fuelType) {
                FuelType.PB95 -> station.prices.pb95 != null
                FuelType.DIESEL -> station.prices.diesel != null
                FuelType.LPG -> station.prices.lpg != null
                null -> true
            }
            val matchBrand = if (brand != null) station.brand == brand else true
            matchFuel && matchBrand
        }

        val sorted = when (fuelType) {
            FuelType.PB95 -> filtered.sortedBy { it.prices.pb95 }
            FuelType.DIESEL -> filtered.sortedBy { it.prices.diesel }
            FuelType.LPG -> filtered.sortedBy { it.prices.lpg }
            null -> filtered
        }

        return sorted.take(6)
    }

    /**
     * Returns stations sorted by distance from the given coordinates,
     * optionally filtered by fuel type, limited to [limit] results.
     */
    fun getStationsSortedByDistance(
        lat: Double,
        lng: Double,
        fuelType: FuelType?,
        limit: Int = 6
    ): List<StationWithDistance> {
        val filtered = cachedStations.filter { station ->
            when (fuelType) {
                FuelType.PB95 -> station.prices.pb95 != null
                FuelType.DIESEL -> station.prices.diesel != null
                FuelType.LPG -> station.prices.lpg != null
                null -> true
            }
        }

        return filtered
            .map { station ->
                StationWithDistance(
                    station = station,
                    distanceKm = haversineDistance(lat, lng, station.lat, station.lng)
                )
            }
            .sortedWith(
                if (fuelType != null) {
                    // Primary sort by price, secondary by distance
                    compareBy<StationWithDistance> {
                        when (fuelType) {
                            FuelType.PB95 -> it.station.prices.pb95
                            FuelType.DIESEL -> it.station.prices.diesel
                            FuelType.LPG -> it.station.prices.lpg
                        }
                    }.thenBy { it.distanceKm }
                } else {
                    // Sort by distance only
                    compareBy { it.distanceKm }
                }
            )
            .take(limit)
    }

    companion object {
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
}
