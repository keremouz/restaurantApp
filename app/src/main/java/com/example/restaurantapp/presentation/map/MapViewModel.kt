package com.example.restaurantapp.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurantapp.domain.model.Restaurant
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
        _uiState.update { currentState ->
            currentState.copy(
                isConnected = isConnected,
                isLoading = false,
                errorMessage = null
            )
        }

        if (isConnected && _uiState.value.restaurants.isEmpty()) {
            loadRestaurants()
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                filteredRestaurants = filterRestaurants(
                    restaurants = currentState.restaurants,
                    selectedCategory = category
                )
            )
        }
    }

    fun retryLoadRestaurants() {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        if (!_uiState.value.isConnected) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    errorMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val restaurants = repository.getNearbyRestaurants()
                val selectedCategory = _uiState.value.selectedCategory

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        restaurants = restaurants,
                        filteredRestaurants = filterRestaurants(
                            restaurants = restaurants,
                            selectedCategory = selectedCategory
                        ),
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Bir hata oluştu"
                    )
                }
            }
        }
    }

    private fun filterRestaurants(
        restaurants: List<Restaurant>,
        selectedCategory: String
    ): List<Restaurant> {
        return if (selectedCategory == MAP_CATEGORY_ALL) {
            restaurants
        } else {
            restaurants.filter { restaurant ->
                restaurant.category == selectedCategory
            }
        }
    }
}