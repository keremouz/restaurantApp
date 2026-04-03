package com.example.restaurantapp.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.AccountScreen
import com.example.restaurantapp.presentation.FavoritesScreen
import com.example.restaurantapp.presentation.map.MapScreen

private val WarningBannerBg = Color(0xFFD32F2F)
private val WarningBannerText = Color.White

@Composable
fun MainBottomBarScreen(
    isConnected: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onRestaurantClick: (Restaurant) -> Unit,
    onNavigateToMyReviews: () -> Unit
) {
    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorites,
        BottomNavItem.Account
    )

    Scaffold(
        topBar = {
            if (!isConnected) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarningBannerBg)
                        .padding(
                            horizontal = UiConstants.ScreenPadding,
                            vertical = UiConstants.SmallSpacing
                        )
                ) {
                    Text(
                        text = stringResource(R.string.no_internet_banner),
                        color = WarningBannerText
                    )
                }
            }
        },
        bottomBar = {
            val navBackStackEntry = bottomNavController.currentBackStackEntryAsState().value
            val currentDestination = navBackStackEntry?.destination

            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(text = item.title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.MAP
        ) {
            composable(Routes.MAP) {
                MapScreen(
                    isConnected = isConnected,
                    onRestaurantClick = onRestaurantClick
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    isConnected = isConnected,
                    innerPadding = innerPadding,
                    onRestaurantClick = onRestaurantClick
                )
            }

            composable(Routes.ACCOUNT) {
                AccountScreen(
                    isConnected = isConnected,
                    innerPadding = innerPadding,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    onNavigateToMyReviews = onNavigateToMyReviews,
                    onRateAppClick = { },
                    onLanguageClick = { },
                    onDeleteAccountClick = { }
                )
            }
        }
    }
}