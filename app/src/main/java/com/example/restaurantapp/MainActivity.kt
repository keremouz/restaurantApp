package com.example.restaurantapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.restaurantapp.presentation.navigation.AppNavGraph
import com.example.restaurantapp.ui.theme.RestaurantAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestaurantAppTheme {
                AppNavGraph()
            }
        }
    }
}