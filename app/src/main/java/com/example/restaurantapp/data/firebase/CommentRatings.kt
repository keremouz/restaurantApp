package com.example.restaurantapp.data.firebase

import androidx.annotation.Keep


@Keep
data class CommentRatings(
    val taste: Int = 0,
    val service: Int = 0,
    val pricePerformance: Int = 0,
    val atmosphere: Int = 0,
    val location: Int = 0
) {
    fun average(): Double {
        return listOf(
            taste,
            service,
            pricePerformance,
            atmosphere,
            location
        ).average()
    }
}