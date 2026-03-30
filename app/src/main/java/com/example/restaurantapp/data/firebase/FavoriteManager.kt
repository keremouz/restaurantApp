package com.example.restaurantapp.data.firebase

import com.example.restaurantapp.domain.model.Restaurant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun addFavorite(
        restaurant: Restaurant,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Giriş yapmanız gerekiyor")
            return
        }

        val favorite = FavoriteRestaurant(
            placeId = restaurant.placeId,
            name = restaurant.name,
            address = restaurant.address,
            rating = restaurant.rating,
            latitude = restaurant.latitude,
            longitude = restaurant.longitude
        )

        firestore.collection("users")
            .document(uid)
            .collection("favorites")
            .document(restaurant.placeId)
            .set(favorite)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Favoriye eklenemedi")
            }
    }

    fun getFavorites(
        onSuccess: (List<FavoriteRestaurant>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Giriş yapmanız gerekiyor")
            return
        }

        firestore.collection("users")
            .document(uid)
            .collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                val favorites = documents.mapNotNull { document ->
                    document.toObject(FavoriteRestaurant::class.java)
                }
                onSuccess(favorites)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Favoriler alınamadı")
            }
    }
}