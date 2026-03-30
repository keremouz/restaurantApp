package com.example.restaurantapp.data.repository

import com.example.restaurantapp.data.remote.api.PlacesApiService
import com.example.restaurantapp.data.remote.dto.CircleDto
import com.example.restaurantapp.data.remote.dto.LatLngDto
import com.example.restaurantapp.data.remote.dto.LocationRestrictionDto
import com.example.restaurantapp.data.remote.dto.NearbySearchRequestDto
import com.example.restaurantapp.data.remote.mapper.toDomain
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.domain.repository.RestaurantRepository
class RestaurantRepositoryImpl(
    private val placesApiService: PlacesApiService,
    private val apiKey: String
) : RestaurantRepository {

    override suspend fun getNearbyRestaurants(): List<Restaurant> {
        val centers = listOf(
            LatLngDto(latitude = 41.0082, longitude = 28.9784), // Fatih / Eminönü
            LatLngDto(latitude = 41.0422, longitude = 29.0083), // Beşiktaş / Şişli
            LatLngDto(latitude = 41.0670, longitude = 28.9850), // Kağıthane
            LatLngDto(latitude = 41.0220, longitude = 28.8750), // Bakırköy
            LatLngDto(latitude = 41.0370, longitude = 28.6770), // Beylikdüzü
            LatLngDto(latitude = 41.0560, longitude = 28.9130), // Bayrampaşa / Eyüp
            LatLngDto(latitude = 40.9917, longitude = 29.0277), // Kadıköy
            LatLngDto(latitude = 41.0278, longitude = 29.0152), // Üsküdar
            LatLngDto(latitude = 41.0244, longitude = 29.1244), // Ümraniye
            LatLngDto(latitude = 40.9869, longitude = 29.1244), // Ataşehir
            LatLngDto(latitude = 40.8787, longitude = 29.2347), // Pendik
            LatLngDto(latitude = 40.8917, longitude = 29.1850)  // Kartal / Maltepe
        )

        val allRestaurants = mutableListOf<Restaurant>()

        centers.forEach { center ->
            val request = NearbySearchRequestDto(
                includedTypes = listOf("restaurant"),
                maxResultCount = 20,
                locationRestriction = LocationRestrictionDto(
                    circle = CircleDto(
                        center = center,
                        radius = 8000.0
                    )
                )
            )

            val response = placesApiService.searchNearbyRestaurants(
                apiKey = apiKey,
                fieldMask = "places.id,places.displayName,places.location,places.formattedAddress,places.rating",
                request = request
            )

            val restaurants = response.places
                .orEmpty()
                .map { it.toDomain() }
                .filter { it.placeId.isNotBlank() && it.name.isNotBlank() }

            allRestaurants.addAll(restaurants)
        }

        return allRestaurants.distinctBy { it.placeId }
    }
}