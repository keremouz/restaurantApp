package com.example.restaurantapp.presentation.favorites

import androidx.lifecycle.ViewModel
import com.example.restaurantapp.data.firebase.FavoriteRestaurant
import com.example.restaurantapp.data.firebase.FavoritesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FavoritesViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val favoritesManager: FavoritesManager = FavoritesManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FavoritesUiState(
            isLoggedIn = auth.currentUser != null
        )
    )
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser

        _uiState.update { currentState ->
            currentState.copy(
                isLoggedIn = user != null,
                favorites = if (user == null) emptyList() else currentState.favorites,
                ratings = if (user == null) emptyMap() else currentState.ratings,
                errorMessage = null,
                isLoading = false,
                hasLoadedFavorites = if (user == null) false else currentState.hasLoadedFavorites
            )
        }

        if (user != null && _uiState.value.isConnected) {
            loadFavorites()
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    fun updateConnectionState(isConnected: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isConnected = isConnected)
        }

        if (!isConnected) {
            _uiState.update { currentState ->
                currentState.copy(
                    favorites = emptyList(),
                    ratings = emptyMap(),
                    errorMessage = null,
                    isLoading = false,
                    hasLoadedFavorites = false
                )
            }
            return
        }

        if (auth.currentUser != null) {
            loadFavorites()
        }
    }

    fun refreshFavorites() {
        if (_uiState.value.isConnected && auth.currentUser != null) {
            loadFavorites()
        }
    }

    private fun loadFavorites() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoggedIn = false,
                    favorites = emptyList(),
                    ratings = emptyMap(),
                    errorMessage = null,
                    isLoading = false,
                    hasLoadedFavorites = false
                )
            }
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                isLoggedIn = true,
                isLoading = true,
                hasLoadedFavorites = false,
                errorMessage = null
            )
        }

        favoritesManager.getFavorites(
            onSuccess = { favoriteList ->
                if (favoriteList.isEmpty()) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            favorites = emptyList(),
                            ratings = emptyMap(),
                            isLoading = false,
                            hasLoadedFavorites = true,
                            errorMessage = null
                        )
                    }
                    return@getFavorites
                }

                loadFavoriteRatings(
                    currentUserId = currentUser.uid,
                    favorites = favoriteList,
                    onLoaded = { ratingMap ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                favorites = favoriteList,
                                ratings = ratingMap,
                                isLoading = false,
                                hasLoadedFavorites = true,
                                errorMessage = null
                            )
                        }
                    }
                )
            },
            onError = { error ->
                _uiState.update { currentState ->
                    currentState.copy(
                        favorites = emptyList(),
                        ratings = emptyMap(),
                        errorMessage = error,
                        isLoading = false,
                        hasLoadedFavorites = true
                    )
                }
            }
        )
    }

    private fun loadFavoriteRatings(
        currentUserId: String,
        favorites: List<FavoriteRestaurant>,
        onLoaded: (Map<String, FavoriteRatingInfo>) -> Unit
    ) {
        if (favorites.isEmpty()) {
            onLoaded(emptyMap())
            return
        }

        firestore.collection("comments")
            .get()
            .addOnSuccessListener { documents ->
                val comments = documents.documents
                val result = mutableMapOf<String, FavoriteRatingInfo>()

                favorites.forEach { favorite ->
                    val matchedComments = comments.filter { document ->
                        val commentRestaurantId = document.getString("restaurantId").orEmpty()
                        val commentRestaurantName = document.getString("restaurantName").orEmpty()

                        val favoritePlaceId = favorite.placeId
                        val favoriteName = favorite.name

                        commentRestaurantId == favoritePlaceId ||
                                normalizeText(commentRestaurantName) == normalizeText(favoriteName) ||
                                normalizeText(commentRestaurantName).contains(normalizeText(favoriteName)) ||
                                normalizeText(favoriteName).contains(normalizeText(commentRestaurantName))
                    }

                    val myRatings = matchedComments
                        .filter { document ->
                            document.getString("userId") == currentUserId
                        }
                        .mapNotNull { document ->
                            extractRatingFromComment(document)
                        }

                    val myRating = if (myRatings.isNotEmpty()) {
                        myRatings.average()
                    } else {
                        null
                    }

                    val allRatings = matchedComments.mapNotNull { document ->
                        extractRatingFromComment(document)
                    }

                    val generalRating = if (allRatings.isNotEmpty()) {
                        allRatings.average()
                    } else {
                        null
                    }

                    val ratingInfo = FavoriteRatingInfo(
                        myRating = myRating,
                        generalRating = generalRating
                    )

                    result[favorite.placeId] = ratingInfo
                    result[favorite.name] = ratingInfo
                }

                onLoaded(result)
            }
            .addOnFailureListener {
                onLoaded(emptyMap())
            }
    }

    private fun extractRatingFromComment(
        document: DocumentSnapshot
    ): Double? {
        document.getDouble("generalRating")?.let { return it }
        document.getDouble("rating")?.let { return it }

        val ratingsMap = document.get("ratings") as? Map<*, *>
        val ratingValues = ratingsMap
            ?.values
            ?.mapNotNull { value ->
                when (value) {
                    is Number -> value.toDouble()
                    else -> null
                }
            }
            .orEmpty()

        return if (ratingValues.isNotEmpty()) {
            ratingValues.average()
        } else {
            null
        }
    }

    private fun normalizeText(value: String): String {
        return value
            .lowercase()
            .replace("ı", "i")
            .replace("ğ", "g")
            .replace("ü", "u")
            .replace("ş", "s")
            .replace("ö", "o")
            .replace("ç", "c")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }
}