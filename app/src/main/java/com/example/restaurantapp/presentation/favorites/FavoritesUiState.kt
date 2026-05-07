package com.example.restaurantapp.presentation.favorites

import com.example.restaurantapp.data.firebase.FavoriteRestaurant

data class FavoritesUiState(
    val isConnected: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val hasLoadedFavorites: Boolean = false,
    val favorites: List<FavoriteRestaurant> = emptyList(),
    val ratings: Map<String, FavoriteRatingInfo> = emptyMap(),
    val errorMessage: String? = null
)

data class FavoriteRatingInfo(
    val myRating: Double? = null,
    val generalRating: Double? = null
)