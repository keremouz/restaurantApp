package com.example.restaurantapp.presentation.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.domain.repository.ChatBotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatBotViewModel(
    private val repository: ChatBotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatBotUiState())
    val uiState: StateFlow<ChatBotUiState> = _uiState.asStateFlow()

    fun addWelcomeMessageIfNeeded(message: String) {
        if (_uiState.value.messages.isNotEmpty()) return

        _uiState.update { currentState ->
            currentState.copy(
                messages = listOf(
                    ChatMessage(
                        text = message,
                        isFromUser = false
                    )
                )
            )
        }
    }

    fun updateInputText(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                inputText = value,
                errorMessage = null
            )
        }
    }

    fun sendMessage(restaurants: List<Restaurant>) {
        val userMessageText = _uiState.value.inputText.trim()

        if (userMessageText.isBlank() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(
            text = userMessageText,
            isFromUser = true
        )

        _uiState.update { currentState ->
            currentState.copy(
                inputText = "",
                isLoading = true,
                errorMessage = null,
                messages = currentState.messages + userMessage
            )
        }

        viewModelScope.launch {
            try {
                val botAnswer = repository.sendMessage(
                    message = userMessageText,
                    restaurants = restaurants
                )

                val botMessage = ChatMessage(
                    text = botAnswer,
                    isFromUser = false
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        messages = currentState.messages + botMessage
                    )
                }
            } catch (exception: Exception) {
                val errorMessage = ChatMessage(
                    text = exception.message ?: "Cevap alınamadı.",
                    isFromUser = false
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Cevap alınamadı.",
                        messages = currentState.messages + errorMessage
                    )
                }
            }
        }
    }
}