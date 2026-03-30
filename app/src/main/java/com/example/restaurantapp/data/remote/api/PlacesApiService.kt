package com.example.restaurantapp.data.remote.api

import com.example.restaurantapp.data.remote.dto.NearbySearchRequestDto
import com.example.restaurantapp.data.remote.dto.PlacesTextSearchResponseDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PlacesApiService {

    @POST("v1/places:searchNearby")
    suspend fun searchNearbyRestaurants(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String,
        @Body request: NearbySearchRequestDto
    ): PlacesTextSearchResponseDto
}