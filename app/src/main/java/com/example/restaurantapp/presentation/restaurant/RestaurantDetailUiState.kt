package com.example.restaurantapp.presentation.restaurant

data class RestaurantDetailUiState(
    val message: String? = null,
    val isFavorite: Boolean = false,
    val averageRating: Double? = null,

    val commentText: String = "",
    val tasteRating: Int = 0,
    val serviceRating: Int = 0,
    val pricePerformanceRating: Int = 0,
    val atmosphereRating: Int = 0,
    val locationRating: Int = 0,

    val requireLogin: Boolean = false
)