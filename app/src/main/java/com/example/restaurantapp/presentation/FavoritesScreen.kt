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
import androidx.compose.ui.res.stringResource
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.FavoriteRestaurant
import com.example.restaurantapp.data.firebase.FavoritesManager
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import androidx.compose.foundation.layout.WindowInsets

private val FavoritesBg = Color(0xFFF7F7F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    isConnected: Boolean,
    onRestaurantClick: (Restaurant) -> Unit
) {
    val favoritesManager = remember { FavoritesManager() }
    val favorites = remember { mutableStateListOf<FavoriteRestaurant>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            favoritesManager.getFavorites(
                onSuccess = { list ->
                    favorites.clear()
                    favorites.addAll(list)
                },
                onError = { error ->
                    errorMessage = error
                }
            )
        } else {
            favorites.clear()
            errorMessage = null
        }
    }

    Scaffold(
        containerColor = FavoritesBg,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.favorites_title)) }
            )
        }
    ) { paddingValues ->
        if (!isConnected) {
            ConnectionWarningContent(
                innerPadding = PaddingValues(),
                contentPadding = paddingValues
            )
        } else {
            when {
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
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
                            .padding(paddingValues)
                            .padding(UiConstants.ScreenPadding),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = stringResource(R.string.favorites_empty))
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
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
                                    Text(
                                        text = stringResource(
                                            R.string.favorites_rating_value,
                                            favorite.rating?.toString() ?: "-"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}