package com.example.restaurantapp.presentation.map

import com.example.restaurantapp.domain.model.Restaurant

data class MapUiState(
    val isLoading: Boolean = false,
    val restaurants: List<Restaurant> = emptyList(),
    val errorMessage: String? = null,
    val isConnected: Boolean = true
)