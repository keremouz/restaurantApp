package com.example.restaurantapp.presentation.navigation

data class MainBottomBarUiState(
    val showDeleteSheet: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteAccountEvent: DeleteAccountEvent? = null
)

sealed interface DeleteAccountEvent {
    data object Success : DeleteAccountEvent
    data object ReauthRequired : DeleteAccountEvent
    data class Error(val message: String) : DeleteAccountEvent
}