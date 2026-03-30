package com.example.fuel_prices.app

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.example.fuel_prices.data.StationWithDistance

class StationDetailScreen(
    carContext: CarContext,
    private val stationWithDistance: StationWithDistance
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val station = stationWithDistance.station
        val distanceStr = String.format("%.1f km away", stationWithDistance.distanceKm)

        val paneBuilder = Pane.Builder()

        // Fuel prices rows
        station.prices.pb95?.let {
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("PB95")
                    .addText("$it PLN/L")
                    .build()
            )
        }
        station.prices.diesel?.let {
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("Diesel")
                    .addText("$it PLN/L")
                    .build()
            )
        }
        station.prices.lpg?.let {
            paneBuilder.addRow(
                Row.Builder()
                    .setTitle("LPG")
                    .addText("$it PLN/L")
                    .build()
            )
        }

        // Distance row
        paneBuilder.addRow(
            Row.Builder()
                .setTitle("Distance")
                .addText(distanceStr)
                .build()
        )

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("${station.brand} - ${station.name}")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
