package com.example.restaurantapp.presentation.reviews

import com.example.restaurantapp.data.firebase.UserComment

data class MyReviewsUiState(
    val isLoading: Boolean = true,
    val reviews: List<UserComment> = emptyList(),
    val errorMessage: String? = null,
    val selectedSort: ReviewSortType = ReviewSortType.NEWEST,
    val filterExpanded: Boolean = false,
    val showDeleteSheet: Boolean = false,
    val showEditSheet: Boolean = false,
    val selectedReview: UserComment? = null,
    val editCommentText: String = "",
    val editTasteRating: Int = 0,
    val editServiceRating: Int = 0,
    val editPricePerformanceRating: Int = 0,
    val editAtmosphereRating: Int = 0,
    val editLocationRating: Int = 0
)

enum class ReviewSortType(val label: String) {
    NEWEST("En Yeni"),
    OLDEST("En Eski"),
    GENERAL("Genel Puan"),
    TASTE("Lezzet"),
    SERVICE("Servis"),
    PRICE_PERFORMANCE("Fiyat / Performans"),
    ATMOSPHERE("Atmosfer"),
    LOCATION("Konum")
}