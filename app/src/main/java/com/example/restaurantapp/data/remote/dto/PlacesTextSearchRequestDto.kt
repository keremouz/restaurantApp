package com.example.restaurantapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlacesTextSearchRequestDto(
    @SerialName("textQuery")
    val textQuery: String
)