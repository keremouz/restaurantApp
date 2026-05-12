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
                    selectedCategory = category,
                    searchQuery = currentState.searchQuery
                )
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredRestaurants = filterRestaurants(
                    restaurants = currentState.restaurants,
                    selectedCategory = currentState.selectedCategory,
                    searchQuery = query
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
                val searchQuery = _uiState.value.searchQuery

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        restaurants = restaurants,
                        filteredRestaurants = filterRestaurants(
                            restaurants = restaurants,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery
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
        selectedCategory: String,
        searchQuery: String
    ): List<Restaurant> {
        return restaurants.filter { restaurant ->
            val categoryMatches =
                selectedCategory == MAP_CATEGORY_ALL ||
                        restaurant.category == selectedCategory

            val searchMatches =
                searchQuery.isBlank() ||
                        restaurant.name.contains(searchQuery, ignoreCase = true)

            categoryMatches && searchMatches
        }
    }
}