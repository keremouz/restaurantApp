package com.example.restaurantapp.presentation.account

import com.example.restaurantapp.R
import com.google.firebase.auth.FirebaseUser

data class AccountUiState(
    val isConnected: Boolean = true,
    val currentUser: FirebaseUser? = null,
    val fullName: String = "",
    val reviewCount: Int = 0,
    val favoriteCount: Int = 0,

    val showAvatarSheet: Boolean = false,
    val showLanguageSheet: Boolean = false,
    val showLevelSheet: Boolean = false,

    val selectedAvatar: Int = R.drawable.avatar_person,
    val selectedLanguage: String = "Türkçe (TR)"
)