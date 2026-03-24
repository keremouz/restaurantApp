package com.example.restaurantapp.domain.repository

import com.example.restaurantapp.domain.model.Restaurant

interface RestaurantRepository {
    suspend fun getNearbyRestaurants(): List<Restaurant>
}