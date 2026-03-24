package com.example.restaurantapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.restaurantapp.presentation.auth.LoginScreen
import com.example.restaurantapp.presentation.auth.RegisterScreen
import com.example.restaurantapp.presentation.map.MapScreen
import com.example.restaurantapp.presentation.restaurant.RestaurantDetailScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MAP,
        modifier = modifier
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