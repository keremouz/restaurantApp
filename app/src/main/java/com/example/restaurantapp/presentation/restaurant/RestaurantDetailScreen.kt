package com.example.restaurantapp.presentation.restaurant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.CommentsManager
import com.example.restaurantapp.data.firebase.FavoritesManager
import com.example.restaurantapp.domain.model.Restaurant
import com.google.firebase.auth.FirebaseAuth

private val DetailBlue = Color(0xFF3D4BFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    onBackClick: () -> Unit,
    onRequireLogin: () -> Unit
) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val favoritesManager = remember { FavoritesManager() }
    val commentsManager = remember { CommentsManager() }

    val currentUser = firebaseAuth.currentUser

    var message by remember { mutableStateOf<String?>(null) }
    var commentText by remember { mutableStateOf("") }
    var ratingText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restoran Detayı") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UiConstants.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(UiConstants.ContentSpacing)
        ) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(text = restaurant.address)

            message?.let {
                Text(
                    text = it,
                    color = DetailBlue
                )
            }

            Button(
                onClick = {
                    if (currentUser == null) {
                        onRequireLogin()
                    } else {
                        favoritesManager.addFavorite(
                            restaurant = restaurant,
                            onSuccess = {
                                message = "Favorilere eklendi"
                            },
                            onError = { error ->
                                message = error
                            }
                        )
                    }
                },
                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                colors = ButtonDefaults.buttonColors(containerColor = DetailBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Favoriye Ekle")
            }

            OutlinedTextField(
                value = ratingText,
                onValueChange = { ratingText = it },
                label = { Text("Puan (1-5)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Yorumun") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    if (currentUser == null) {
                        onRequireLogin()
                    } else {
                        val rating = ratingText.toDoubleOrNull()
                        if (commentText.isBlank() || rating == null) {
                            message = "Puan ve yorum girmeniz gerekiyor"
                        } else {
                            commentsManager.addComment(
                                restaurantId = restaurant.placeId,
                                restaurantName = restaurant.name,
                                comment = commentText.trim(),
                                rating = rating,
                                onSuccess = {
                                    message = "Yorum eklendi"
                                    commentText = ""
                                    ratingText = ""
                                },
                                onError = { error ->
                                    message = error
                                }
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Yorum Yap")
            }

            OutlinedButton(
                onClick = onBackClick,
                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Geri Dön")
            }
        }
    }
}