package com.example.restaurantapp.domain.repository

interface ChatBotRepository {
    suspend fun sendMessage(message: String): String
}