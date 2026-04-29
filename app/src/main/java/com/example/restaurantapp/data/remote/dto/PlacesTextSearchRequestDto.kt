package com.example.restaurantapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlacesTextSearchRequestDto(
    @SerialName("textQuery")
    val textQuery: String,

    @SerialName("languageCode")
    val languageCode: String = "tr",

    @SerialName("maxResultCount")
    val maxResultCount: Int = 20
)