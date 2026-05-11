package com.example.restaurantapp.presentation.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.AuthManager
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.account.AccountScreen
import com.example.restaurantapp.presentation.account.components.DeleteAccountSheet
import com.example.restaurantapp.presentation.favorites.FavoritesScreen
import com.example.restaurantapp.presentation.map.MapScreen
import kotlinx.coroutines.launch

private val BottomBarBlue = Color(0xFF2F5BFF)
private val BottomBarSelectedIcon = Color.White
private val BottomBarUnselectedIcon = Color(0xFFDCE4FF)
private val BottomBarSelectedBg = Color.White.copy(alpha = 0.16f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomBarScreen(
    isConnected: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onRestaurantClick: (Restaurant) -> Unit,
    onNavigateToMyReviews: () -> Unit,
    onNavigateToChatBot: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val context = LocalContext.current
    val authManager = remember { AuthManager() }
    val accountDeletedMessage = stringResource(R.string.account_deleted)

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showDeleteSheet by remember { mutableStateOf(false) }

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
                    modifier = Modifier
                        .fillMaxWidth(UiConstants.BottomBarWidthFraction)
                        .height(UiConstants.BottomBarHeight),
                    shape = RoundedCornerShape(UiConstants.BottomBarCornerRadius),
                    color = BottomBarBlue,
                    shadowElevation = UiConstants.BottomBarElevation
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = UiConstants.BottomBarInnerHorizontalPadding),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == item.route
                            } == true

                            BottomBarItem(
                                item = item,
                                selected = selected,
                                onClick = {
                                    bottomNavController.navigate(item.route) {
                                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.MAP,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(durationMillis = 700)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(durationMillis = 700)
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(durationMillis = 700)
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(durationMillis = 700)
                )
            }
        ) {
            composable(Routes.MAP) {
                MapScreen(
                    isConnected = isConnected,
                    onRestaurantClick = onRestaurantClick,
                    onChatBotClick = onNavigateToChatBot
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
                    onRateAppClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=${context.packageName}")
                            )
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                )
                            )
                        }
                    },
                    onLanguageClick = {
                        // sonra yapacağız
                    },
                    onDeleteAccountClick = {
                        showDeleteSheet = true
                    }
                )
            }
        }
    }

    if (showDeleteSheet) {
        DeleteAccountSheet(
            sheetState = sheetState,
            onConfirmClick = {
                scope.launch {
                    sheetState.hide()
                }

                showDeleteSheet = false

                authManager.deleteAccount(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            accountDeletedMessage,
                            Toast.LENGTH_SHORT
                        ).show()

                        onNavigateToLogin()
                    },
                    onError = { error ->
                        if (error == "REAUTH_REQUIRED") {
                            onNavigateToLogin()
                        } else {
                            Toast.makeText(
                                context,
                                error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            },
            onDismissClick = {
                scope.launch {
                    sheetState.hide()
                }

                showDeleteSheet = false
            }
        )
    }
}

@Composable
private fun BottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(UiConstants.BottomBarItemCircleSize)
            .background(
                color = if (selected) BottomBarSelectedBg else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when (item.route) {
            Routes.MAP -> {
                Icon(
                    painter = painterResource(R.drawable.ic_bottom_home),
                    contentDescription = null,
                    tint = if (selected) BottomBarSelectedIcon else BottomBarUnselectedIcon,
                    modifier = Modifier.size(UiConstants.BottomBarIconSize)
                )
            }

            Routes.ACCOUNT -> {
                Icon(
                    painter = painterResource(R.drawable.ic_bottom_profile),
                    contentDescription = null,
                    tint = if (selected) BottomBarSelectedIcon else BottomBarUnselectedIcon,
                    modifier = Modifier.size(UiConstants.BottomBarIconSize)
                )
            }

            else -> {
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