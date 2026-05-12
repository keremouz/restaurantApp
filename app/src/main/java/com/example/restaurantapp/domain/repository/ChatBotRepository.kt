package com.example.restaurantapp.domain.repository

import com.example.restaurantapp.domain.model.Restaurant

interface ChatBotRepository {
    suspend fun sendMessage(
        message: String,
        restaurants: List<Restaurant>
    ): String
}