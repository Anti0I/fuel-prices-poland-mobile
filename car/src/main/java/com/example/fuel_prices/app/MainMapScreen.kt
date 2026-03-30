package com.example.fuel_prices.app

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.example.fuel_prices.data.StationWithDistance

class MainMapScreen(
    carContext: CarContext,
    private val viewModel: StationsViewModel
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val stationsWithDist = viewModel.stations.value
        val location = viewModel.currentLocation.value
        val filter = viewModel.currentFilter.value

        val itemListBuilder = ItemList.Builder()
            .setNoItemsMessage("No stations found.")

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
                    screenManager.push(StationDetailScreen(carContext, swd))
                }
                .setMetadata(
                    Metadata.Builder()
                        .setPlace(
                            Place.Builder(
                                CarLocation.create(station.lat, station.lng)
                            )
                            .setMarker(PlaceMarker.Builder().build())
                            .build()
                        )
                        .build()
                )
                .build()
            
            itemListBuilder.addItem(row)
        }

        // Action strip with Filter and My Location buttons
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
                    .setTitle("My Location")
                    .setOnClickListener {
                        viewModel.startLocationUpdates()
                        CarToast.makeText(
                            carContext,
                            "Updating location...",
                            CarToast.LENGTH_SHORT
                        ).show()
                        invalidate()
                    }
                    .build()
            )
            .build()

        // Set user's current location as the map anchor
        val anchor = Place.Builder(
            CarLocation.create(location.latitude, location.longitude)
        )
            .setMarker(
                PlaceMarker.Builder()
                    .setLabel("You")
                    .build()
            )
            .build()

        val title = when {
            filter == null -> "Nearest Stations"
            else -> "Cheapest ${filter.name}"
        }

        return PlaceListMapTemplate.Builder()
            .setTitle(title)
            .setItemList(itemListBuilder.build())
            .setActionStrip(actionStrip)
            .setAnchor(anchor)
            .build()
    }
}
