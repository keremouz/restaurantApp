package com.example.restaurantapp.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.auth.LoginScreen
import com.example.restaurantapp.presentation.auth.RegisterScreen
import com.example.restaurantapp.presentation.restaurant.RestaurantDetailScreen

@Composable
fun AppNavGraph(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN,
        modifier = modifier
    ) {
        composable(Routes.MAIN) {
            MainBottomBarScreen(
                isConnected = isConnected,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onRestaurantClick = { restaurant ->
                    if (isConnected) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_restaurant", restaurant)

                        navController.navigate(Routes.RESTAURANT_DETAIL)
                    }
                },
                onNavigateToMyReviews = {
                    navController.navigate(Routes.MY_REVIEWS)
                }
            )
        }

        composable(Routes.RESTAURANT_DETAIL) {
            val restaurant = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Restaurant>("selected_restaurant")

            restaurant?.let {
                RestaurantDetailScreen(
                    restaurant = it,
                    onBackClick = { navController.popBackStack() },
                    onRequireLogin = {
                        navController.navigate(Routes.LOGIN)
                    }
                )
            }
        }
        composable(Routes.MY_REVIEWS) {
            Text("My Reviews Screen")
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