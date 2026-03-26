package com.example.restaurantapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DisplayNameDto(
    @SerialName("text")
    val text: String? = null
)