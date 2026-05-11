package com.example.restaurantapp.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainBottomBarViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainBottomBarViewModel::class.java)) {
            return MainBottomBarViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}