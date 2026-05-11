package com.example.restaurantapp.presentation.navigation

import androidx.lifecycle.ViewModel
import com.example.restaurantapp.data.firebase.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainBottomBarViewModel(
    private val authManager: AuthManager = AuthManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainBottomBarUiState())
    val uiState: StateFlow<MainBottomBarUiState> = _uiState.asStateFlow()

    fun showDeleteAccountSheet() {
        _uiState.update { currentState ->
            currentState.copy(showDeleteSheet = true)
        }
    }

    fun hideDeleteAccountSheet() {
        _uiState.update { currentState ->
            currentState.copy(showDeleteSheet = false)
        }
    }

    fun deleteAccount() {
        if (_uiState.value.isDeletingAccount) return

        _uiState.update { currentState ->
            currentState.copy(
                isDeletingAccount = true,
                deleteAccountEvent = null
            )
        }

        authManager.deleteAccount(
            onSuccess = {
                _uiState.update { currentState ->
                    currentState.copy(
                        showDeleteSheet = false,
                        isDeletingAccount = false,
                        deleteAccountEvent = DeleteAccountEvent.Success
                    )
                }
            },
            onError = { error ->
                _uiState.update { currentState ->
                    currentState.copy(
                        showDeleteSheet = false,
                        isDeletingAccount = false,
                        deleteAccountEvent = if (error == "REAUTH_REQUIRED") {
                            DeleteAccountEvent.ReauthRequired
                        } else {
                            DeleteAccountEvent.Error(error)
                        }
                    )
                }
            }
        )
    }

    fun clearDeleteAccountEvent() {
        _uiState.update { currentState ->
            currentState.copy(deleteAccountEvent = null)
        }
    }
}