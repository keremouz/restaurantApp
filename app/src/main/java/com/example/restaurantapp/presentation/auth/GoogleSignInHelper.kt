package com.example.restaurantapp.presentation.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.restaurantapp.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

fun createGoogleSignInRequest(): GetCredentialRequest {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .setAutoSelectEnabled(false)
        .build()

    return GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
}

fun createCredentialManager(context: Context): CredentialManager {
    return CredentialManager.create(context)
}