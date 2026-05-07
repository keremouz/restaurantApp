package com.example.restaurantapp.presentation.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyReviewsViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyReviewsViewModel::class.java)) {
            return MyReviewsViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}