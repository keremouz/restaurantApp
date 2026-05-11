package com.example.restaurantapp.presentation.account

import androidx.lifecycle.ViewModel
import com.example.restaurantapp.R
import com.example.restaurantapp.data.firebase.AuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AccountViewModel(
    private val authManager: AuthManager = AuthManager(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AccountUiState(
            currentUser = firebaseAuth.currentUser
        )
    )
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    val avatars = listOf(
        R.drawable.avatar_person,
        R.drawable.avatar_woman,
        R.drawable.avatar_chef,
        R.drawable.avatar_burger,
        R.drawable.avatar_crown
    )

    private var userProfileListener: ListenerRegistration? = null
    private var reviewCountListener: ListenerRegistration? = null
    private var favoriteCountListener: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        _uiState.update { currentState ->
            currentState.copy(
                currentUser = auth.currentUser
            )
        }

        startAccountListeners()
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
        startAccountListeners()
    }

    fun updateConnectionState(isConnected: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isConnected = isConnected)
        }

        if (isConnected) {
            startAccountListeners()
        } else {
            clearAccountListeners()
            resetAccountData()
        }
    }

    fun setSelectedLanguage(language: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedLanguage = language)
        }
    }

    fun showAvatarSheet() {
        _uiState.update { currentState ->
            currentState.copy(showAvatarSheet = true)
        }
    }

    fun hideAvatarSheet() {
        _uiState.update { currentState ->
            currentState.copy(showAvatarSheet = false)
        }
    }

    fun selectAvatar(avatar: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedAvatar = avatar,
                showAvatarSheet = false
            )
        }
    }

    fun showLanguageSheet() {
        _uiState.update { currentState ->
            currentState.copy(showLanguageSheet = true)
        }
    }

    fun hideLanguageSheet() {
        _uiState.update { currentState ->
            currentState.copy(showLanguageSheet = false)
        }
    }

    fun selectLanguage(language: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedLanguage = language,
                showLanguageSheet = false
            )
        }
    }

    fun showLevelSheet() {
        _uiState.update { currentState ->
            currentState.copy(showLevelSheet = true)
        }
    }

    fun hideLevelSheet() {
        _uiState.update { currentState ->
            currentState.copy(showLevelSheet = false)
        }
    }

    fun logout() {
        authManager.signOut()
    }

    private fun startAccountListeners() {
        clearAccountListeners()

        if (!_uiState.value.isConnected) {
            resetAccountData()
            return
        }

        val uid = firebaseAuth.currentUser?.uid

        if (uid == null) {
            resetAccountData()
            return
        }

        userProfileListener = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { document, error ->
                if (error != null) return@addSnapshotListener

                _uiState.update { currentState ->
                    currentState.copy(
                        fullName = document?.getString("fullName").orEmpty()
                    )
                }
            }

        reviewCountListener = firestore.collection("comments")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { documents, error ->
                if (error != null) return@addSnapshotListener

                _uiState.update { currentState ->
                    currentState.copy(
                        reviewCount = documents?.size() ?: 0
                    )
                }
            }

        favoriteCountListener = firestore.collection("users")
            .document(uid)
            .collection("favorites")
            .addSnapshotListener { documents, error ->
                if (error != null) return@addSnapshotListener

                _uiState.update { currentState ->
                    currentState.copy(
                        favoriteCount = documents?.size() ?: 0
                    )
                }
            }
    }

    private fun clearAccountListeners() {
        userProfileListener?.remove()
        reviewCountListener?.remove()
        favoriteCountListener?.remove()

        userProfileListener = null
        reviewCountListener = null
        favoriteCountListener = null
    }

    private fun resetAccountData() {
        _uiState.update { currentState ->
            currentState.copy(
                fullName = "",
                reviewCount = 0,
                favoriteCount = 0
            )
        }
    }

    override fun onCleared() {
        clearAccountListeners()
        firebaseAuth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }
}