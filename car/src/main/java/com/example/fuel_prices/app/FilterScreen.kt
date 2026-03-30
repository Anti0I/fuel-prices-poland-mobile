package com.example.fuel_prices.app

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.example.fuel_prices.data.FuelType

class FilterScreen(
    carContext: CarContext,
    private val viewModel: StationsViewModel
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()

        val filters = listOf(
            "Cheapest PB95" to FuelType.PB95,
            "Cheapest Diesel" to FuelType.DIESEL,
            "Cheapest LPG" to FuelType.LPG,
            "Nearest (no filter)" to null
        )

        filters.forEach { (title, fuelType) ->
            val row = Row.Builder()
                .setTitle(title)
                .setOnClickListener {
                    viewModel.setFilter(fuelType)
                    screenManager.pop()
                }
                .build()
            itemListBuilder.addItem(row)
        }

        return ListTemplate.Builder()
            .setTitle("Filter Stations")
            .setSingleList(itemListBuilder.build())
            .setHeaderAction(Action.BACK)
            .build()
    }
}
