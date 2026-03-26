package com.example.restaurantapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("displayName")
    val displayName: DisplayNameDto? = null,

    @SerialName("formattedAddress")
    val formattedAddress: String? = null,

    @SerialName("location")
    val location: LocationDto? = null,

    @SerialName("rating")
    val rating: Double? = null
)