package com.example.restaurantapp.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.presentation.AccountScreen
import com.example.restaurantapp.presentation.FavoritesScreen
import com.example.restaurantapp.presentation.map.MapScreen

private val BottomBarBlue = Color(0xFF2F5BFF)
private val BottomBarSelectedIcon = Color.White
private val BottomBarUnselectedIcon = Color(0xFFDCE4FF)
private val BottomBarSelectedBg = Color.White.copy(alpha = 0.16f)

@Composable
fun MainBottomBarScreen(
    isConnected: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onRestaurantClick: (com.example.restaurantapp.domain.model.Restaurant) -> Unit,
    onNavigateToMyReviews: () -> Unit
) {
    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorites,
        BottomNavItem.Account
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(
                        start = UiConstants.BottomBarOuterHorizontalPadding,
                        end = UiConstants.BottomBarOuterHorizontalPadding,
                        top = UiConstants.BottomBarOuterTopPadding,
                        bottom = UiConstants.BottomBarOuterBottomPadding
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(UiConstants.BottomBarCornerRadius),
                    color = BottomBarBlue,
                    shadowElevation = UiConstants.CardElevation * 2
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = UiConstants.BottomBarInnerHorizontalPadding,
                                vertical = UiConstants.BottomBarInnerVerticalPadding
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { destination ->
                                destination.route == item.route
                            } == true

                            Box(
                                modifier = Modifier
                                    .size(UiConstants.BottomBarItemCircleSize)
                                    .background(
                                        color = if (selected) BottomBarSelectedBg else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        bottomNavController.navigate(item.route) {
                                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (selected) BottomBarSelectedIcon else BottomBarUnselectedIcon,
                                    modifier = Modifier.size(UiConstants.BottomBarIconSize)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.MAP,
            modifier = Modifier.padding(innerPadding)
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
                    onRestaurantClick = onRestaurantClick
                )
            }

            composable(Routes.ACCOUNT) {
                AccountScreen(
                    isConnected = isConnected,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    onNavigateToMyReviews = onNavigateToMyReviews,
                    onRateAppClick = {},
                    onLanguageClick = {},
                    onDeleteAccountClick = {}
                )
            }
        }
    }
}