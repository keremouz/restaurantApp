package com.example.restaurantapp.data.remote.api

import com.example.restaurantapp.data.remote.dto.PlacesTextSearchRequestDto
import com.example.restaurantapp.data.remote.dto.PlacesTextSearchResponseDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface PlacesApiService {

    @Headers(
        "Content-Type: application/json",
        "X-Goog-FieldMask: places.id,places.displayName,places.formattedAddress,places.location,places.rating"
    )
    @POST("v1/places:searchText")
    suspend fun searchRestaurants(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Body request: PlacesTextSearchRequestDto
    ): PlacesTextSearchResponseDto
}