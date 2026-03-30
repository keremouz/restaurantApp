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
        comment: String,
        rating: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Giriş yapmanız gerekiyor")
            return
        }

        val docRef = firestore.collection("comments").document()

        val userComment = UserComment(
            commentId = docRef.id,
            userId = uid,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            comment = comment,
            rating = rating
        )

        docRef.set(userComment)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Yorum eklenemedi")
            }
    }
}