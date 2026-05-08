package com.example.restaurantapp.presentation.chatbot

data class ChatBotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)