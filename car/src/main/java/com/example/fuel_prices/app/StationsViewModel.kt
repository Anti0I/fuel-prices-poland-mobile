package com.example.fuel_prices.app

import android.location.Location
import com.example.fuel_prices.data.FuelType
import com.example.fuel_prices.data.StationRepository
import com.example.fuel_prices.data.StationWithDistance
import com.example.fuel_prices.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel-like state holder for station data, user location, and filter state.
 * Uses [StateFlow] for reactive updates. Not an actual AndroidX ViewModel since
 * Car App Library sessions don't use ViewModelStoreOwner.
 */
class StationsViewModel(
    private val repository: StationRepository,
    private val locationHelper: LocationHelper
) {

    private val _currentLocation = MutableStateFlow<Location>(locationHelper.getDefaultLocation())
    val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()

    private val _currentFilter = MutableStateFlow<FuelType?>(null)
    val currentFilter: StateFlow<FuelType?> = _currentFilter.asStateFlow()

    private val _stations = MutableStateFlow<List<StationWithDistance>>(emptyList())
    val stations: StateFlow<List<StationWithDistance>> = _stations.asStateFlow()

    private var isUpdating = false

    init {
        refreshStations()
    }

    fun setFilter(fuelType: FuelType?) {
        _currentFilter.value = fuelType
        refreshStations()
    }

    fun onLocationChanged(location: Location) {
        _currentLocation.value = location
        refreshStations()
    }

    fun startLocationUpdates() {
        locationHelper.startLocationUpdates { location ->
            onLocationChanged(location)
        }
    }

    fun stopLocationUpdates() {
        locationHelper.stopLocationUpdates()
    }

    private fun refreshStations() {
        if (isUpdating) return
        isUpdating = true

        val loc = _currentLocation.value
        val filter = _currentFilter.value

        _stations.value = repository.getStationsSortedByDistance(
            lat = loc.latitude,
            lng = loc.longitude,
            fuelType = filter,
            limit = 6
        )

        isUpdating = false
    }
}
