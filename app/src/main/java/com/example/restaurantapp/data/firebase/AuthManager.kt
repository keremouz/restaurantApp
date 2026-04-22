package com.example.restaurantapp.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Giriş başarısız")
            }
    }

    fun register(
        fullName: String,
        birthDate: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid

                if (uid.isNullOrBlank()) {
                    onError("Kullanıcı kimliği alınamadı")
                    return@addOnSuccessListener
                }

                val userProfile = UserProfile(
                    uid = uid,
                    fullName = fullName,
                    birthDate = birthDate,
                    email = email
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userProfile)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Kullanıcı profili kaydedilemedi")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Kayıt başarısız")
            }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun signInWithGoogle(
        context: android.content.Context,
        credentialRequest: androidx.credentials.GetCredentialRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credentialManager = androidx.credentials.CredentialManager.create(context)
            val result = credentialManager.getCredential(
                context = context,
                request = credentialRequest
            )

            val credential = result.credential

            if (
                credential is androidx.credentials.CustomCredential &&
                credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential =
                    com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
                        .createFrom(credential.data)

                val firebaseCredential = com.google.firebase.auth.GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken,
                    null
                )

                auth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Google ile giriş başarısız")
                    }
            } else {
                onError("Google credential alınamadı")
            }
        } catch (e: Exception) {
            onError(e.message ?: "Google ile giriş başarısız")
        }
    }
}