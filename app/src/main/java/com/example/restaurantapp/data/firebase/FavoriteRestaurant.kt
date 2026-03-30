package com.example.restaurantapp.data.firebase

data class FavoriteRestaurant(
    val placeId: String = "",
    val name: String = "",
    val address: String = "",
    val rating: Double? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)