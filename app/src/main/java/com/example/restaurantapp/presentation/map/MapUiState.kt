package com.example.restaurantapp.presentation.map

import com.example.restaurantapp.domain.model.Restaurant

const val MAP_CATEGORY_ALL = "all"

data class MapUiState(
    val isLoading: Boolean = false,
    val restaurants: List<Restaurant> = emptyList(),
    val filteredRestaurants: List<Restaurant> = emptyList(),
    val selectedCategory: String = MAP_CATEGORY_ALL,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val isConnected: Boolean = false
)