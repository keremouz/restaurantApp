package com.example.restaurantapp.data.repository

import com.example.restaurantapp.data.remote.chatbot.ChatBotRemoteDataSource
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.domain.repository.ChatBotRepository

class ChatBotRepositoryImpl(
    private val remoteDataSource: ChatBotRemoteDataSource = ChatBotRemoteDataSource()
) : ChatBotRepository {

    override suspend fun sendMessage(
        message: String,
        restaurants: List<Restaurant>
    ): String {
        return remoteDataSource.sendMessage(
            message = message,
            restaurants = restaurants
        )
    }
}