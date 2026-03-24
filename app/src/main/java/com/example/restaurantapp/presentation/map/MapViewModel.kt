package com.example.restaurantapp.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurantapp.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(isLoading = true))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadRestaurants()
    }

    fun loadRestaurants() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val restaurants = restaurantRepository.getNearbyRestaurants()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    restaurants = restaurants
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Bir hata oluştu"
                )
            }
        }
    }
}