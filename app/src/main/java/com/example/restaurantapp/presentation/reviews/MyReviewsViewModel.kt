package com.example.restaurantapp.presentation.reviews

import androidx.lifecycle.ViewModel
import com.example.restaurantapp.data.firebase.CommentsManager
import com.example.restaurantapp.data.firebase.UserComment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MyReviewsViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val commentsManager: CommentsManager = CommentsManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyReviewsUiState())
    val uiState: StateFlow<MyReviewsUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener {
        loadReviews()
    }

    init {
        auth.addAuthStateListener(authStateListener)
        loadReviews()
    }

    fun loadReviews() {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                reviews = emptyList(),
                errorMessage = null
            )
        }

        if (auth.currentUser == null) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    errorMessage = "Yorumlarınızı görmek için giriş yapmanız gerekiyor"
                )
            }
            return
        }

        commentsManager.getCurrentUserComments(
            onSuccess = { commentList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        reviews = commentList,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            },
            onError = { error ->
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = error,
                        isLoading = false
                    )
                }
            }
        )
    }

    fun onFilterClick() {
        _uiState.update { currentState ->
            currentState.copy(filterExpanded = true)
        }
    }

    fun onDismissFilter() {
        _uiState.update { currentState ->
            currentState.copy(filterExpanded = false)
        }
    }

    fun onSortSelected(sortType: ReviewSortType) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedSort = sortType,
                filterExpanded = false
            )
        }
    }

    fun onDeleteClick(review: UserComment) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedReview = review,
                showDeleteSheet = true
            )
        }
    }

    fun dismissDeleteSheet() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedReview = null,
                showDeleteSheet = false
            )
        }
    }

    fun deleteSelectedReview() {
        val reviewToDelete = _uiState.value.selectedReview ?: return

        commentsManager.deleteComment(
            commentId = reviewToDelete.commentId,
            onSuccess = {
                _uiState.update { currentState ->
                    currentState.copy(
                        reviews = currentState.reviews.filterNot {
                            it.commentId == reviewToDelete.commentId
                        },
                        selectedReview = null,
                        showDeleteSheet = false,
                        errorMessage = null
                    )
                }
            },
            onError = { error ->
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = error,
                        selectedReview = null,
                        showDeleteSheet = false
                    )
                }
            }
        )
    }

    fun getSortedReviews(): List<UserComment> {
        val state = _uiState.value

        return when (state.selectedSort) {
            ReviewSortType.NEWEST -> state.reviews.sortedByDescending { it.createdAt }
            ReviewSortType.OLDEST -> state.reviews.sortedBy { it.createdAt }
            ReviewSortType.GENERAL -> state.reviews.sortedByDescending { it.generalRating }
            ReviewSortType.TASTE -> state.reviews.sortedByDescending { it.ratings.taste }
            ReviewSortType.SERVICE -> state.reviews.sortedByDescending { it.ratings.service }
            ReviewSortType.PRICE_PERFORMANCE -> {
                state.reviews.sortedByDescending { it.ratings.pricePerformance }
            }
            ReviewSortType.ATMOSPHERE -> state.reviews.sortedByDescending { it.ratings.atmosphere }
            ReviewSortType.LOCATION -> state.reviews.sortedByDescending { it.ratings.location }
        }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }
}