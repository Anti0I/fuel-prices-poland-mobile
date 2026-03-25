package com.example.fuel_prices.data

import android.content.Context
import com.example.fuel_prices.R
import com.google.gson.Gson
import java.io.InputStreamReader

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
}
