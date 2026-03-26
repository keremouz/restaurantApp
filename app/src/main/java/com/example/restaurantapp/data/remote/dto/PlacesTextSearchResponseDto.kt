package com.example.restaurantapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlacesTextSearchResponseDto(
    @SerialName("places")
    val places: List<PlaceDto>? = null
)