package com.example.restaurantapp.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.AccountScreen
import com.example.restaurantapp.presentation.FavoritesScreen
import com.example.restaurantapp.presentation.map.MapScreen

@Composable
fun MainBottomBarScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onRestaurantClick: (Restaurant) -> Unit
) {
    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorites,
        BottomNavItem.Account
    )

    Scaffold(
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
                    onRestaurantClick = onRestaurantClick
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    innerPadding = innerPadding,
                    onRestaurantClick = onRestaurantClick
                )
            }

            composable(Routes.ACCOUNT) {
                AccountScreen(
                    innerPadding = innerPadding,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister
                )
            }
        }
    }
}