package com.example.restaurantapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CircleDto (
    @SerialName("center")
    val center: LatLngDto,
    @SerialName("radius")
    val radius: Double
)