package com.example.fuel_prices.app

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*

class MainMapScreen(
    carContext: CarContext,
    private val viewModel: StationsViewModel
) : Screen(carContext) {

    init {
        viewModel.onInvalidate = {
            invalidate()
        }
    }

    override fun onGetTemplate(): Template {
        val stationsWithDist = viewModel.stations.value
        val location = viewModel.currentLocation.value
        val filter = viewModel.currentFilter.value
        val loading = viewModel.isLoading.value
        val selectedStation = viewModel.selectedStation.value

        val itemListBuilder = ItemList.Builder()
            .setNoItemsMessage(
                if (loading) "Loading stations..." else "No stations found."
            )

        stationsWithDist.forEachIndexed { _, swd ->
            val station = swd.station
            val priceStr = buildString {
                station.prices.pb95?.let { append("PB95: $it | ") }
                station.prices.diesel?.let { append("Diesel: $it | ") }
                station.prices.lpg?.let { append("LPG: $it") }
            }.removeSuffix(" | ")

            val distance = Distance.create(swd.distanceKm, Distance.UNIT_KILOMETERS)
            val spannableTitle = android.text.SpannableString("  ${station.brand} - ${station.name}")
            spannableTitle.setSpan(
                DistanceSpan.create(distance),
                0,
                1,
                android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )

            val row = Row.Builder()
                .setTitle(spannableTitle)
                .addText(priceStr)
                .setOnClickListener {
                    screenManager.push(StationDetailScreen(carContext, swd, viewModel))
                }
                .setMetadata(
                    Metadata.Builder()
                        .setPlace(
                            Place.Builder(
                                CarLocation.create(station.lat, station.lng)
                            )
                            .setMarker(
                                PlaceMarker.Builder()
                                    .setColor(
                                        if (selectedStation?.name == station.name &&
                                            selectedStation.brand == station.brand
                                        ) CarColor.RED else CarColor.BLUE
                                    )
                                    .build()
                            )
                            .build()
                        )
                        .build()
                )
                .build()

            itemListBuilder.addItem(row)
        }

        // Action strip with Filter and Center buttons
        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Filter")
                    .setOnClickListener {
                        screenManager.push(FilterScreen(carContext, viewModel))
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Center")
                    .setOnClickListener {
                        viewModel.setSelectedStation(null)
                        viewModel.startLocationUpdates()
                        CarToast.makeText(
                            carContext,
                            "Centering on your location...",
                            CarToast.LENGTH_SHORT
                        ).show()
                        invalidate()
                    }
                    .build()
            )
            .build()

        // Jeśli użytkownik wybrał stację z ekranu szczegółów, wycentruj mapę na niej
        // W przeciwnym razie pokaż aktualną lokalizację użytkownika
        val anchor = if (selectedStation != null) {
            Place.Builder(CarLocation.create(selectedStation.lat, selectedStation.lng))
                .setMarker(
                    PlaceMarker.Builder()
                        .setColor(CarColor.RED)
                        .build()
                )
                .build()
        } else {
            Place.Builder(CarLocation.create(location.latitude, location.longitude))
                .setMarker(
                    PlaceMarker.Builder()
                        .setLabel("You")
                        .build()
                )
                .build()
        }

        val title = when {
            loading -> "Loading..."
            selectedStation != null -> "${selectedStation.brand} - ${selectedStation.name}"
            filter == null -> "Nearest Stations"
            else -> "Cheapest ${filter.name}"
        }

        val templateBuilder = PlaceListMapTemplate.Builder()
            .setTitle(title)
            .setActionStrip(actionStrip)
            .setAnchor(anchor)

        if (loading && stationsWithDist.isEmpty()) {
            templateBuilder.setLoading(true)
        } else {
            templateBuilder.setItemList(itemListBuilder.build())
        }

        return templateBuilder.build()
    }
}
