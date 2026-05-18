package com.example.restaurantapp

import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.example.restaurantapp.core.network.NetworkChangeReceiver
import com.example.restaurantapp.core.util.LocaleHelper
import com.example.restaurantapp.presentation.navigation.AppNavGraph
import com.example.restaurantapp.ui.theme.RestaurantAppTheme

class MainActivity : ComponentActivity() {

    private var isConnected by mutableStateOf(true)
    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.applySavedLanguage(this)
        installSplashScreen()

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        isConnected = NetworkChangeReceiver.isInternetAvailable(this)

        networkChangeReceiver = NetworkChangeReceiver { connected ->
            isConnected = connected
        }

        setContent {
            RestaurantAppTheme {
                AppNavGraph(
                    isConnected = isConnected
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(
            networkChangeReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkChangeReceiver)
    }
}