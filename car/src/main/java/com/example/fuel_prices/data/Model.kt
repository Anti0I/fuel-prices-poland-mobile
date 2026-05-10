package com.example.fuel_prices.data

/**
 * Represents a single fuel station as returned by the API.
 * Used for both /stations and /stations/near endpoints.
 */
data class Station(
    val name: String,
    val brand: String,
    val city: String? = null,
    val voivodeship: String? = null,
    val lat: Double,
    val lng: Double,
    val prices: Prices,
    val updated_at: String,
    val distance_km: Double? = null
)

data class Prices(
    val pb95: Double?,
    val diesel: Double?,
    val lpg: Double?
)

/**
 * Response from the /filters endpoint.
 * Contains all distinct values available for filtering.
 */
data class FiltersResponse(
    val cities: List<String>,
    val voivodeships: List<String>,
    val brands: List<String>
)

enum class FuelType {
    PB95, DIESEL, LPG;

    /** Returns the lowercase key expected by the API query parameter. */
    fun toApiParam(): String = name.lowercase()
}
