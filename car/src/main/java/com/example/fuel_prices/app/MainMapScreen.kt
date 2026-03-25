package com.example.fuel_prices.app

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.example.fuel_prices.data.FuelType
import com.example.fuel_prices.data.StationRepository

class MainMapScreen(
    carContext: CarContext,
    private val repository: StationRepository
) : Screen(carContext) {

    var currentFilter: FuelType? = null

    override fun onGetTemplate(): Template {
        val stations = repository.getFilteredStations(currentFilter, null)

        val itemListBuilder = ItemList.Builder()
            .setNoItemsMessage("No stations found.")

        stations.forEachIndexed { index, station ->
            val priceStr = buildString {
                station.prices.pb95?.let { append("PB95: $it | ") }
                station.prices.diesel?.let { append("Diesel: $it | ") }
                station.prices.lpg?.let { append("LPG: $it") }
            }.removeSuffix(" | ")

            val distance = Distance.create(1.5 + index, Distance.UNIT_KILOMETERS)
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
                    CarToast.makeText(carContext, "Selected ${station.name}", CarToast.LENGTH_SHORT).show()
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

        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Filter")
                    .setOnClickListener {
                        screenManager.push(FilterScreen(carContext, this))
                    }
                    .build()
            )
            .build()

        return PlaceListMapTemplate.Builder()
            .setTitle(if (currentFilter == null) "Stations" else "Cheapest ${currentFilter!!.name}")
            .setItemList(itemListBuilder.build())
            .setActionStrip(actionStrip)
            .build()
    }
}
