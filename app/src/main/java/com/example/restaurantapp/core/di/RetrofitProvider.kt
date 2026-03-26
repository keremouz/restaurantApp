package com.example.restaurantapp.core.di

import com.example.restaurantapp.data.remote.api.PlacesApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory


object RetrofitProvider {

    private const val BASE_URL = "https://places.googleapis.com/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    val placesApiService: PlacesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(PlacesApiService::class.java)
    }
}