package com.example.restaurantapp.presentation.restaurant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.FavoritesManager
import com.example.restaurantapp.domain.model.Restaurant
import com.google.firebase.auth.FirebaseAuth

private val DetailBlue = Color(0xFF3D4BFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    placeId: String,
    onBackClick: () -> Unit,
    onRequireLogin: () -> Unit
) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val favoritesManager = remember { FavoritesManager() }

    var message by remember { mutableStateOf<String?>(null) }

    val currentUser = firebaseAuth.currentUser

    // Şimdilik geçici restoran nesnesi
    // Sonra gerçek detail verisiyle değiştiririz
    val restaurant = Restaurant(
        placeId = placeId,
        name = "Restoran",
        latitude = 0.0,
        longitude = 0.0,
        address = "Adres bilgisi eklenecek",
        district = null,
        rating = null
    )

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

            Text(
                text = "Place ID: ${restaurant.placeId}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = restaurant.address,
                style = MaterialTheme.typography.bodyMedium
            )

            restaurant.rating?.let {
                Text(
                    text = "Puan: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            message?.let {
                Text(
                    text = it,
                    color = DetailBlue,
                    style = MaterialTheme.typography.bodyMedium
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

            OutlinedButton(
                onClick = {
                    if (currentUser == null) {
                        onRequireLogin()
                    } else {
                        message = "Yorum yapma alanı eklenecek"
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