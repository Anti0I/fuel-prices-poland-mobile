package com.example.fuel_prices.app

import android.location.Location
import android.util.Log
import com.example.fuel_prices.data.FuelType
import com.example.fuel_prices.data.StationRepository
import com.example.fuel_prices.data.StationWithDistance
import com.example.fuel_prices.location.LocationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel-like state holder for station data, user location, and filter state.
 * Uses [StateFlow] for reactive updates. Not an actual AndroidX ViewModel since
 * Car App Library sessions don't use ViewModelStoreOwner.
 *
 * Network calls are dispatched on [Dispatchers.IO] via a dedicated [CoroutineScope].
 */
class StationsViewModel(
    private val repository: StationRepository,
    private val locationHelper: LocationHelper
) {

    companion object {
        private const val TAG = "StationsViewModel"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentLocation = MutableStateFlow<Location>(locationHelper.getDefaultLocation())
    val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()

    private val _currentFilter = MutableStateFlow<FuelType?>(null)
    val currentFilter: StateFlow<FuelType?> = _currentFilter.asStateFlow()

    private val _stations = MutableStateFlow<List<StationWithDistance>>(emptyList())
    val stations: StateFlow<List<StationWithDistance>> = _stations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var refreshJob: Job? = null

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

    /**
     * Cancels the coroutine scope. Call when the session is destroyed.
     */
    fun onDestroy() {
        scope.cancel()
    }

    private fun refreshStations() {
        // Cancel any previous in-flight request
        refreshJob?.cancel()

        refreshJob = scope.launch {
            _isLoading.value = true
            try {
                val loc = _currentLocation.value
                val filter = _currentFilter.value

                val result = withContext(Dispatchers.IO) {
                    repository.getStationsSortedByDistance(
                        lat = loc.latitude,
                        lng = loc.longitude,
                        fuelType = filter,
                        limit = 6
                    )
                }

                _stations.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing stations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
