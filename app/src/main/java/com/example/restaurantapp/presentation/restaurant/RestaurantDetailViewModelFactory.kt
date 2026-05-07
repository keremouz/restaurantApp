package com.example.restaurantapp.presentation.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.restaurantapp.domain.model.Restaurant

class RestaurantDetailViewModelFactory(
    private val restaurant: Restaurant
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantDetailViewModel::class.java)) {
            return RestaurantDetailViewModel(
                restaurant = restaurant
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}