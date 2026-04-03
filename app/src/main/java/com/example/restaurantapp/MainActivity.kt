package com.example.restaurantapp

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.restaurantapp.core.network.NetworkChangeReceiver
import com.example.restaurantapp.presentation.navigation.AppNavGraph
import com.example.restaurantapp.ui.theme.RestaurantAppTheme

class MainActivity : ComponentActivity() {

    private var isConnected by mutableStateOf(true)
    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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