package com.example.restaurantapp.data.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class AuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.message ?: "Giriş başarısız")
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
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userProfile = UserProfile(
                    uid = uid,
                    fullName = fullName,
                    birthDate = birthDate,
                    email = email
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userProfile)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener {
                        onError(it.message ?: "Profil kaydedilemedi")
                    }
            }
            .addOnFailureListener {
                onError(it.message ?: "Kayıt başarısız")
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser ?: return onError("Kullanıcı yok")
        val uid = user.uid

        user.delete()
            .addOnSuccessListener {
                firestore.collection("users").document(uid).delete()
                auth.signOut()
                onSuccess()
            }
            .addOnFailureListener {
                if (it.message?.contains("requires recent authentication") == true) {
                    onError("REAUTH_REQUIRED")
                } else {
                    onError(it.message ?: "Silinemedi")
                }
            }
    }

    suspend fun signInWithGoogle(
        context: Context,
        credentialRequest: GetCredentialRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, credentialRequest)

            val credential = result.credential

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken,
                    null
                )

                auth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener {
                        onError(it.message ?: "Google giriş başarısız")
                    }
            } else {
                onError("Google credential alınamadı")
            }
        } catch (e: Exception) {
            onError(e.message ?: "Google giriş başarısız")
        }
    }
}