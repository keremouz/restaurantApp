package com.example.restaurantapp.data.firebase

import androidx.annotation.Keep


@Keep
data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val birthDate: String = "",
    val email: String = ""
)