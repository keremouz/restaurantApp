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
}