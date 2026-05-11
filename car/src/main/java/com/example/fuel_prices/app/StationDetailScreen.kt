package com.example.fuel_prices.app

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.example.fuel_prices.data.StationWithDistance

class StationDetailScreen(
    carContext: CarContext,
    private val stationWithDistance: StationWithDistance,
    private val viewModel: StationsViewModel
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

        // "Show on Map" — ustawia wybraną stację jako kotwicę mapy i wraca do MainMapScreen.
        // Działa zarówno na emulatorze jak i na prawdziwym urządzeniu bez potrzeby
        // zewnętrznej aplikacji nawigacyjnej.
        val showOnMapAction = Action.Builder()
            .setTitle("Show on Map")
            .setOnClickListener {
                viewModel.setSelectedStation(station)
                screenManager.pop()
            }
            .build()

        paneBuilder.addAction(showOnMapAction)

        return PaneTemplate.Builder(paneBuilder.build())
            .setTitle("${station.brand} - ${station.name}")
            .setHeaderAction(Action.BACK)
            .build()
    }
}
