package com.example.restaurantapp.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantapp.core.location.AppRestaurantHolder
import com.example.restaurantapp.data.firebase.AuthManager
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.auth.LoginScreen
import com.example.restaurantapp.presentation.auth.RegisterScreen
import com.example.restaurantapp.presentation.chatbot.ChatBotScreen
import com.example.restaurantapp.presentation.restaurant.RestaurantDetailScreen
import com.example.restaurantapp.presentation.reviews.MyReviewsScreen

@Composable
fun AppNavGraph(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authManager = AuthManager()

    val startDestination = if (authManager.isUserLoggedIn()) {
        Routes.MAIN
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 1000)
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 900)
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = tween(durationMillis = 1000)
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = tween(durationMillis = 900)
            )
        }
    ) {
        composable(Routes.MAIN) {
            MainBottomBarScreen(
                isConnected = isConnected,

                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },

                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },

                onRestaurantClick = { restaurant ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_restaurant", restaurant)

                    navController.navigate(Routes.RESTAURANT_DETAIL)
                },

                onNavigateToMyReviews = {
                    navController.navigate(Routes.MY_REVIEWS)
                },

                onNavigateToChatBot = {
                    navController.navigate(Routes.CHATBOT)
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBackClick = {},

                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },

                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBackClick = {
                    navController.popBackStack()
                },

                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                },

                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
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
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRequireLogin = {
                        navController.navigate(Routes.LOGIN)
                    }
                )
            }
        }

        composable(Routes.MY_REVIEWS) {
            MyReviewsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CHATBOT) {
            ChatBotScreen(
                restaurants = AppRestaurantHolder.restaurants,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}