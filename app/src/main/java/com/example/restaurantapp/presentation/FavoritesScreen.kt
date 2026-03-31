package com.example.restaurantapp.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.FavoriteRestaurant
import com.example.restaurantapp.data.firebase.FavoritesManager
import com.example.restaurantapp.domain.model.Restaurant

private val FavoritesBg = Color(0xFFF7F7F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    innerPadding: PaddingValues,
    onRestaurantClick: (Restaurant) -> Unit
) {
    val favoritesManager = remember { FavoritesManager() }
    val favorites = remember { mutableStateListOf<FavoriteRestaurant>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        favoritesManager.getFavorites(
            onSuccess = { list ->
                favorites.clear()
                favorites.addAll(list)
            },
            onError = { error ->
                errorMessage = error
            }
        )
    }

    Scaffold(
        containerColor = FavoritesBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Restoranlarım") }
            )
        }
    ) { paddingValues ->
        when {
            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(paddingValues)
                        .padding(UiConstants.ScreenPadding),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(errorMessage ?: "")
                }
            }

            favorites.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(paddingValues)
                        .padding(UiConstants.ScreenPadding),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Henüz favori restoran eklemediniz.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(paddingValues)
                        .padding(UiConstants.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(UiConstants.ContentSpacing)
                ) {
                    items(favorites) { favorite ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onRestaurantClick(
                                        Restaurant(
                                            placeId = favorite.placeId,
                                            name = favorite.name,
                                            latitude = favorite.latitude,
                                            longitude = favorite.longitude,
                                            address = favorite.address,
                                            district = null,
                                            rating = favorite.rating
                                        )
                                    )
                                },
                            shape = RoundedCornerShape(UiConstants.CardRadius),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = UiConstants.CardElevation
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(UiConstants.ScreenPadding),
                                verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
                            ) {
                                Text(text = favorite.name)
                                Text(text = favorite.address)
                                Text(text = "Puan: ${favorite.rating ?: "-"}")
                            }
                        }
                    }
                }
            }
        }
    }
}