package com.example.restaurantapp.data.firebase

data class UserComment(
    val commentId: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val comment: String = "",
    val rating: Double = 0.0
)