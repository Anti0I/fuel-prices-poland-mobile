package com.example.fuel_prices.app

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.fuel_prices.data.StationRepository
import com.example.fuel_prices.location.LocationHelper

class FuelStationsSession : Session() {

    private lateinit var repository: StationRepository
    private lateinit var locationHelper: LocationHelper
    private lateinit var viewModel: StationsViewModel

    override fun onCreateScreen(intent: Intent): Screen {
        repository = StationRepository(carContext)
        locationHelper = LocationHelper(carContext)
        viewModel = StationsViewModel(repository, locationHelper)

        // Start listening for location updates
        viewModel.startLocationUpdates()

        // Stop location updates when session is destroyed
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                viewModel.stopLocationUpdates()
            }
        })

        return MainMapScreen(carContext, viewModel)
    }
}

