package com.example.fuel_prices.data

data class MockResponse(
    val updated_at: String,
    val currency: String,
    val unit: String,
    val cities: List<City>
)

data class City(
    val city: String,
    val voivodeship: String,
    val stations: List<Station>
)

data class Station(
    val name: String,
    val brand: String,
    val lat: Double,
    val lng: Double,
    val prices: Prices,
    val updated_at: String
)

data class Prices(
    val pb95: Double?,
    val diesel: Double?,
    val lpg: Double?
)

enum class FuelType {
    PB95, DIESEL, LPG
}
