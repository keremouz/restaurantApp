package com.example.restaurantapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.restaurantapp.presentation.account.AccountScreen
import com.example.restaurantapp.presentation.auth.LoginScreen
import com.example.restaurantapp.presentation.auth.RegisterScreen
import com.example.restaurantapp.presentation.FavoritesScreen
import com.example.restaurantapp.presentation.map.MapScreen
import com.example.restaurantapp.presentation.restaurant.RestaurantDetailScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController as rememberBottomNavController
import androidx.navigation.compose.NavHost as BottomNavHost

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN,
        modifier = modifier
    ) {
        composable(Routes.MAIN) {
            val bottomNavController = rememberBottomNavController()

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
                                    Text(item.title)
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                BottomNavHost(
                    navController = bottomNavController,
                    startDestination = Routes.MAP
                ) {
                    composable(Routes.MAP) {
                        MapScreen(
                            onRestaurantClick = { placeId ->
                                navController.navigate("${Routes.RESTAURANT_DETAIL}/$placeId")
                            },
                            onNavigateToLogin = {
                                navController.navigate(Routes.LOGIN)
                            }
                        )
                    }

                    composable(Routes.FAVORITES) {
                        FavoritesScreen(innerPadding = innerPadding)
                    }

                    composable(Routes.ACCOUNT) {
                        AccountScreen(
                            innerPadding = innerPadding,
                            onNavigateToLogin = {
                                navController.navigate(Routes.LOGIN)
                            },
                            onNavigateToRegister = {
                                navController.navigate(Routes.REGISTER)
                            }
                        )
                    }
                }
            }
        }

        composable(
            route = "${Routes.RESTAURANT_DETAIL}/{placeId}",
            arguments = listOf(
                navArgument("placeId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId").orEmpty()

            RestaurantDetailScreen(
                placeId = placeId,
                onBackClick = { navController.popBackStack() },
                onRequireLogin = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}