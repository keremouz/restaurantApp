package com.example.restaurantapp.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(
        route = Routes.MAP,
        title = "Anasayfa",
        icon = Icons.Default.Home
    )

    data object Favorites : BottomNavItem(
        route = Routes.FAVORITES,
        title = "Favoriler",
        icon = Icons.Default.Favorite
    )

    data object Account : BottomNavItem(
        route = Routes.ACCOUNT,
        title = "Hesap",
        icon = Icons.Default.Person
    )
}