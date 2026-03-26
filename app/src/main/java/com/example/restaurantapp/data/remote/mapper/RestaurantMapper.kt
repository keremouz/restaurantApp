package com.example.restaurantapp.data.remote.mapper

import com.example.restaurantapp.data.remote.dto.PlaceDto
import com.example.restaurantapp.domain.model.Restaurant

fun PlaceDto.toDomain(): Restaurant {
    return Restaurant(
        placeId = id.orEmpty(),
        name = displayName?.text.orEmpty(),
        latitude = location?.latitude ?: 0.0,
        longitude = location?.longitude ?: 0.0,
        address = formattedAddress.orEmpty(),
        district = null,
        rating = rating
    )
}