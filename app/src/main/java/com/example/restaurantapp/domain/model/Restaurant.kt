package com.example.restaurantapp.domain.model

data class Restaurant(
    val placeId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val district: String? = null,
    val rating: Double? = null
)