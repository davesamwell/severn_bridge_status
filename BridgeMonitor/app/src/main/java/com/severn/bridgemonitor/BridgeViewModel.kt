package com.severn.bridgemonitor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BridgeViewModel : ViewModel() {
    
    private val apiClient = BridgeApiClient()
    private val weatherClient = WeatherApiClient()
    
    private val _bridgeData = MutableLiveData<BridgeData?>()
    val bridgeData: LiveData<BridgeData?> = _bridgeData
    
    private val _weatherData = MutableLiveData<WeatherData?>()
    val weatherData: LiveData<WeatherData?> = _weatherData
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        refreshData()
        startAutoRefresh()
    }
    
    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Fetch bridge status
            val bridgeResult = apiClient.fetchBridgeStatus()
            
            bridgeResult.fold(
                onSuccess = { data ->
                    _bridgeData.value = data
                },
                onFailure = { exception ->
                    _error.value = "Failed to load bridge data: ${exception.message}"
                }
            )
            
            // Fetch weather (don't block on weather failure)
            val weatherResult = weatherClient.fetchWeather()
            
            weatherResult.fold(
                onSuccess = { data ->
                    _weatherData.value = data
                },
                onFailure = { exception ->
                    // Weather failure is non-critical, just log it
                    _weatherData.value = null
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun reEvaluateStatus() {
        // Re-evaluate the status of existing data without fetching from API
        // This triggers the time-based logic to recalculate which closures are active
        viewModelScope.launch {
            val result = apiClient.fetchBridgeStatus()
            result.onSuccess { data ->
                _bridgeData.value = data
            }
        }
    }
    
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000) // 5 minutes
                refreshData()
            }
        }
    }
}
