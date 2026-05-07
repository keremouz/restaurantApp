package com.example.restaurantapp.presentation.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.AuthManager
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.account.AccountScreen
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

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showDeleteSheet by remember { mutableStateOf(false) }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorites,
        BottomNavItem.Account
    )
    LaunchedEffect(Unit) {
        if (showDeleteSheet) {
            authManager.deleteAccount(
                onSuccess = {
                    Toast.makeText(context, "Hesap silindi", Toast.LENGTH_SHORT).show()

                    onNavigateToLogin()
                },
                onError = { error ->
                    if (error == "REAUTH_REQUIRED") {
                        onNavigateToLogin()
                    } else {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

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
                        horizontalArrangement = Arrangement.SpaceBetween,
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
            modifier = Modifier.padding(innerPadding)
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
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UiConstants.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(UiConstants.ContentSpacing)
            ) {

                Text(
                    text = stringResource(R.string.delete_account),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.delete_account_subtitle),
                    style = MaterialTheme.typography.bodyMedium
                )
                val message = stringResource(R.string.account_deleted)

                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }
                        showDeleteSheet = false

                        authManager.deleteAccount(
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                onNavigateToLogin()
                            },
                            onError = { error ->
                                if (error == "REAUTH_REQUIRED") {
                                    onNavigateToLogin() // 🔥 login'e gönder
                                } else {
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64B5F6)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.delete_account))
                }

                OutlinedButton(
                    onClick = {
                        scope.launch { sheetState.hide() }
                        showDeleteSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
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