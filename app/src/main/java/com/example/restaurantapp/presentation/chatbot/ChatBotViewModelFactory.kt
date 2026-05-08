package com.example.restaurantapp.presentation.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.restaurantapp.data.remote.chatbot.ChatBotRemoteDataSource
import com.example.restaurantapp.data.repository.ChatBotRepositoryImpl

class ChatBotViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatBotViewModel::class.java)) {
            val repository = ChatBotRepositoryImpl(
                remoteDataSource = ChatBotRemoteDataSource()
            )

            return ChatBotViewModel(
                repository = repository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}