package com.example.fuel_prices.app

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import com.example.fuel_prices.data.StationRepository

class FuelStationsSession : Session() {
    lateinit var repository: StationRepository
        private set

    override fun onCreateScreen(intent: Intent): Screen {
        repository = StationRepository(carContext)
        return MainMapScreen(carContext, repository)
    }
}
