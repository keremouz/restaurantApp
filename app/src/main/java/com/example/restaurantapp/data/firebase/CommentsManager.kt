package com.example.restaurantapp.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentsManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun addComment(
        restaurantId: String,
        restaurantName: String,
        district: String,
        latitude: Double?,
        longitude: Double?,
        comment: String,
        ratings: CommentRatings,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        if (uid == null) {
            onError("Giriş yapmanız gerekiyor")
            return
        }

        val docRef = firestore.collection("comments").document()

        val userName = currentUser.displayName
            ?: currentUser.email?.substringBefore("@")
            ?: "Kullanıcı"

        val userComment = UserComment(
            commentId = docRef.id,
            userId = uid,
            userName = userName,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            district = district,
            latitude = latitude,
            longitude = longitude,
            comment = comment,
            generalRating = ratings.average(),
            ratings = ratings,
            createdAt = System.currentTimeMillis()
        )

        docRef.set(userComment)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Yorum eklenemedi")
            }
    }

    fun getCurrentUserComments(
        onSuccess: (List<UserComment>) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError("Giriş yapmanız gerekiyor")
            return
        }

        firestore.collection("comments")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { documents ->
                val comments = documents.documents
                    .mapNotNull { document ->
                        document.toObject(UserComment::class.java)
                    }
                    .sortedByDescending { it.createdAt }

                onSuccess(comments)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Yorumlar getirilemedi")
            }
    }
    fun deleteComment(
        commentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("comments")
            .document(commentId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Yorum silinemedi")
            }
    }
    fun getRestaurantAverageRating(
        restaurantId: String,
        restaurantName: String,
        onSuccess: (Double?) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("comments")
            .get()
            .addOnSuccessListener { documents ->
                val ratings = documents.documents.mapNotNull { document ->
                    val commentRestaurantId = document.getString("restaurantId").orEmpty()
                    val commentRestaurantName = document.getString("restaurantName").orEmpty()

                    val isSameRestaurant =
                        commentRestaurantId == restaurantId ||
                                commentRestaurantName.equals(restaurantName, ignoreCase = true)

                    if (isSameRestaurant) {
                        document.getDouble("generalRating")
                    } else {
                        null
                    }
                }

                onSuccess(
                    if (ratings.isNotEmpty()) ratings.average() else null
                )
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Genel puan alınamadı")
            }
    }
    fun updateComment(
        commentId: String,
        comment: String,
        ratings: CommentRatings,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val updates = mapOf(
            "comment" to comment,
            "ratings" to ratings,
            "generalRating" to ratings.average(),
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("comments")
            .document(commentId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Yorum güncellenemedi")
            }
    }
}