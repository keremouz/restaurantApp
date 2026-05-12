package com.example.restaurantapp.data.firebase

import androidx.annotation.Keep

@Keep
data class UserComment(
    val commentId: String = "",
    val userId: String = "",
    val userName: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val comment: String = "",
    val generalRating: Double = 0.0,
    val ratings: CommentRatings = CommentRatings(),
    val createdAt: Long = System.currentTimeMillis(),
    val district: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)