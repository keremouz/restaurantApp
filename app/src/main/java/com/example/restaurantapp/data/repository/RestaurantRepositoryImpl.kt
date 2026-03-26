package com.example.restaurantapp.data.repository

import com.example.restaurantapp.data.remote.mapper.toDomain
import com.example.restaurantapp.data.remote.api.PlacesApiService
import com.example.restaurantapp.data.remote.dto.PlacesTextSearchRequestDto
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.domain.repository.RestaurantRepository

class RestaurantRepositoryImpl(
    private val placesApiService: PlacesApiService,
    private val apiKey: String
) : RestaurantRepository {

    override suspend fun getNearbyRestaurants(): List<Restaurant> {
        val response = placesApiService.searchRestaurants(
            apiKey = apiKey,
            request = PlacesTextSearchRequestDto(
                textQuery = "restaurants in Istanbul"
            )
        )

        return response.places
            ?.map { it.toDomain() }
            ?.filter {
                it.placeId.isNotBlank() && it.name.isNotBlank()
            }
            ?: emptyList()
    }
}