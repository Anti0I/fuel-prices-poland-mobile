package com.example.fuel_prices.data

/**
 * Response wrapper for /stations and /stations/near endpoints.
 */
data class StationsResponse(
    val stations: List<Station>
)

/**
 * Represents a single fuel station as returned by the API.
 * Used for both /stations and /stations/near endpoints.
 */
data class Station(
    val name: String,
    val brand: String,
    val lat: Double,
    val lng: Double,
    val prices: Prices,
    val distance_km: Double? = null
)

data class Prices(
    val pb95: Double?,
    val diesel: Double?,
    val lpg: Double?
)

enum class FuelType {
    PB95, DIESEL, LPG;

    /** Returns the lowercase key expected by the API query parameter. */
    fun toApiParam(): String = name.lowercase()
}
