package com.example.restaurantapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NearbySearchRequestDto(
    @SerialName("includedTypes")
    val includedTypes: List<String>,
    @SerialName("maxResultCount")
    val maxResultCount: Int,
    @SerialName("locationRestriction")
    val locationRestriction: LocationRestrictionDto
)