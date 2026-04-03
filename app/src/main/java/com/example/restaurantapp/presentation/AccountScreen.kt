package com.example.restaurantapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.AuthManager
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

private val AccountBlue = Color(0xFF3D4BFF)
private val AccountBg = Color(0xFFF7F7F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    isConnected: Boolean,
    innerPadding: PaddingValues,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val authManager = remember { AuthManager() }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var currentUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth.currentUser) }
    var fullName by remember { mutableStateOf("") }
    val userComments = remember { mutableStateListOf<Pair<String, String>>() }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }

        firebaseAuth.addAuthStateListener(listener)

        onDispose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(currentUser?.uid, isConnected) {
        fullName = ""
        userComments.clear()

        if (!isConnected) return@LaunchedEffect

        val uid = currentUser?.uid ?: return@LaunchedEffect

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                fullName = document.getString("fullName").orEmpty()
            }

        firestore.collection("comments")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { documents ->
                userComments.clear()
                documents.forEach { doc ->
                    val restaurantName = doc.getString("restaurantName").orEmpty()
                    val commentText = doc.getString("comment").orEmpty()

                    if (restaurantName.isNotBlank() || commentText.isNotBlank()) {
                        userComments.add(restaurantName to commentText)
                    }
                }
            }
    }

    Scaffold(
        containerColor = AccountBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.account_title),
                        color = AccountBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        if (!isConnected) {
            ConnectionWarningContent(
                innerPadding = innerPadding,
                contentPadding = paddingValues
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AccountBg)
                    .padding(innerPadding)
                    .padding(paddingValues)
                    .padding(UiConstants.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(UiConstants.ContentSpacing)
            ) {
                if (currentUser == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(UiConstants.CardRadius),
                        elevation = CardDefaults.cardElevation(defaultElevation = UiConstants.CardElevation)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(UiConstants.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(UiConstants.MediumSpacing)
                        ) {
                            Button(
                                onClick = onNavigateToLogin,
                                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                                colors = ButtonDefaults.buttonColors(containerColor = AccountBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.login))
                            }

                            OutlinedButton(
                                onClick = onNavigateToRegister,
                                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.register),
                                    color = AccountBlue
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.my_information),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(UiConstants.CardRadius),
                        elevation = CardDefaults.cardElevation(defaultElevation = UiConstants.CardElevation)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(UiConstants.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.account_full_name_value,
                                    if (fullName.isBlank()) "-" else fullName
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = stringResource(
                                    R.string.account_email_value,
                                    currentUser?.email ?: "-"
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.my_reviews),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(UiConstants.CardRadius),
                        elevation = CardDefaults.cardElevation(defaultElevation = UiConstants.CardElevation)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(UiConstants.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
                        ) {
                            if (userComments.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_reviews_yet),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                userComments.forEach { (restaurantName, comment) ->
                                    Text(text = "• $restaurantName")
                                    Text(
                                        text = comment,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = UiConstants.SmallSpacing)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { authManager.logout() },
                        shape = RoundedCornerShape(UiConstants.ButtonRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.logout))
                    }
                }
            }
        }
    }
}