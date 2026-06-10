package com.example.restaurantapp.presentation.restaurant

import androidx.lifecycle.ViewModel
import com.example.restaurantapp.data.firebase.CommentRatings
import com.example.restaurantapp.data.firebase.CommentsManager
import com.example.restaurantapp.data.firebase.FavoritesManager
import com.example.restaurantapp.domain.model.Restaurant
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RestaurantDetailViewModel(
    private val restaurant: Restaurant,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val commentsManager: CommentsManager = CommentsManager(),
    private val favoritesManager: FavoritesManager = FavoritesManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantDetailUiState())
    val uiState: StateFlow<RestaurantDetailUiState> = _uiState.asStateFlow()

    init {
        loadAverageRating()
        loadFavoriteStatus()
    }

    fun updateCommentText(value: String) {
        _uiState.update { currentState ->
            currentState.copy(commentText = value)
        }
    }

    fun updateTasteRating(value: Int) {
        _uiState.update { currentState ->
            currentState.copy(tasteRating = value)
        }
    }

    fun updateServiceRating(value: Int) {
        _uiState.update { currentState ->
            currentState.copy(serviceRating = value)
        }
    }

    fun updatePricePerformanceRating(value: Int) {
        _uiState.update { currentState ->
            currentState.copy(pricePerformanceRating = value)
        }
    }

    fun updateAtmosphereRating(value: Int) {
        _uiState.update { currentState ->
            currentState.copy(atmosphereRating = value)
        }
    }

    fun updateLocationRating(value: Int) {
        _uiState.update { currentState ->
            currentState.copy(locationRating = value)
        }
    }

    fun clearRequireLogin() {
        _uiState.update { currentState ->
            currentState.copy(requireLogin = false)
        }
    }

    fun toggleFavorite(
        favoriteAddedText: String,
        favoriteRemovedText: String
    ) {
        if (auth.currentUser == null) {
            _uiState.update { currentState ->
                currentState.copy(requireLogin = true)
            }
            return
        }

        if (_uiState.value.isFavorite) {
            favoritesManager.removeFavorite(
                restaurantId = restaurant.placeId,
                onSuccess = {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isFavorite = false,
                            message = favoriteRemovedText
                        )
                    }
                },
                onError = { error ->
                    _uiState.update { currentState ->
                        currentState.copy(message = error)
                    }
                }
            )
        } else {
            favoritesManager.addFavorite(
                restaurant = restaurant,
                onSuccess = {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isFavorite = true,
                            message = favoriteAddedText
                        )
                    }
                },
                onError = { error ->
                    _uiState.update { currentState ->
                        currentState.copy(message = error)
                    }
                }
            )
        }
    }

    fun submitReview(
        fillReviewFieldsText: String,
        commentAddedText: String
    ) {
        if (auth.currentUser == null) {
            _uiState.update { currentState ->
                currentState.copy(requireLogin = true)
            }
            return
        }

        val state = _uiState.value

        if (
            state.commentText.isBlank() ||
            state.tasteRating == 0 ||
            state.serviceRating == 0 ||
            state.pricePerformanceRating == 0 ||
            state.atmosphereRating == 0 ||
            state.locationRating == 0
        ) {
            _uiState.update { currentState ->
                currentState.copy(message = fillReviewFieldsText)
            }
            return
        }

        val ratings = CommentRatings(
            taste = state.tasteRating,
            service = state.serviceRating,
            pricePerformance = state.pricePerformanceRating,
            atmosphere = state.atmosphereRating,
            location = state.locationRating
        )

        commentsManager.addComment(
            restaurantId = restaurant.placeId,
            restaurantName = restaurant.name,
            district = restaurant.district.orEmpty(),
            latitude = restaurant.latitude,
            longitude = restaurant.longitude,
            comment = state.commentText,
            ratings = ratings,
            onSuccess = {
                _uiState.update { currentState ->
                    currentState.copy(
                        message = commentAddedText,
                        commentText = "",
                        tasteRating = 0,
                        serviceRating = 0,
                        pricePerformanceRating = 0,
                        atmosphereRating = 0,
                        locationRating = 0
                    )
                }

                loadAverageRating()
            },
            onError = { error ->
                _uiState.update { currentState ->
                    currentState.copy(message = error)
                }
            }
        )
    }

    private fun loadAverageRating() {
        commentsManager.getRestaurantAverageRating(
            restaurantId = restaurant.placeId,
            restaurantName = restaurant.name,
            onSuccess = { rating ->
                _uiState.update { currentState ->
                    currentState.copy(averageRating = rating)
                }
            },
            onError = {
                _uiState.update { currentState ->
                    currentState.copy(averageRating = null)
                }
            }
        )
    }

    private fun loadFavoriteStatus() {
        if (auth.currentUser == null) {
            _uiState.update { currentState ->
                currentState.copy(isFavorite = false)
            }
            return
        }

        favoritesManager.isFavorite(
            restaurantId = restaurant.placeId,
            onSuccess = { favorite ->
                _uiState.update { currentState ->
                    currentState.copy(isFavorite = favorite)
                }
            },
            onError = {
                _uiState.update { currentState ->
                    currentState.copy(isFavorite = false)
                }
            }
        )
    }

}