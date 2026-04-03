package com.example.restaurantapp.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurantapp.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun updateConnectionState(isConnected: Boolean) {
        _uiState.update {
            it.copy(
                isConnected = isConnected,
                isLoading = false,
                errorMessage = null
            )
        }

        if (isConnected && _uiState.value.restaurants.isEmpty()) {
            loadRestaurants()
        }
    }

    fun loadRestaurants() {
        if (!_uiState.value.isConnected) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val restaurants = repository.getNearbyRestaurants()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        restaurants = restaurants,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Bir hata oluştu"
                    )
                }
            }
        }
    }
}