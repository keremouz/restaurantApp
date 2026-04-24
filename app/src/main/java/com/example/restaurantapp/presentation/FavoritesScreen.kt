package com.example.restaurantapp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.FavoriteRestaurant
import com.example.restaurantapp.data.firebase.FavoritesManager
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import com.google.firebase.auth.FirebaseAuth

private val FavoritesBg = Color(0xFFFBFBFB)
private val EmptyTextGray = Color(0xFF6B6B6B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    isConnected: Boolean,
    onRestaurantClick: (Restaurant) -> Unit
) {
    val favoritesManager = remember { FavoritesManager() }
    val favorites = remember { mutableStateListOf<FavoriteRestaurant>() }
    val currentUser = FirebaseAuth.getInstance().currentUser

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isConnected, currentUser?.uid) {
        if (!isConnected) {
            favorites.clear()
            errorMessage = null
            return@LaunchedEffect
        }

        if (currentUser == null) {
            favorites.clear()
            errorMessage = null
            return@LaunchedEffect
        }

        favoritesManager.getFavorites(
            onSuccess = { list ->
                favorites.clear()
                favorites.addAll(list)
                errorMessage = null
            },
            onError = { error ->
                errorMessage = error
            }
        )
    }

    Scaffold(
        containerColor = FavoritesBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.favorites_title)) }
            )
        }
    ) { paddingValues ->
        when {
            !isConnected -> {
                ConnectionWarningContent(
                    innerPadding = PaddingValues(),
                    contentPadding = paddingValues
                )
            }

            currentUser == null -> {
                FavoritesLoginRequiredContent(
                    modifier = Modifier.padding(paddingValues)
                )
            }

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
                EmptyFavoritesContent(
                    modifier = Modifier.padding(paddingValues)
                )
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

@Composable
private fun FavoritesLoginRequiredContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = UiConstants.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_empty_favorites),
            contentDescription = stringResource(R.string.empty_favorites_image_desc),
            modifier = Modifier.size(450.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.size(UiConstants.ContentSpacing))

        Text(
            text = stringResource(R.string.favorites_login_required_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(UiConstants.SmallSpacing))

        Text(
            text = stringResource(R.string.favorites_login_required_description),
            style = MaterialTheme.typography.bodyMedium,
            color = EmptyTextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

@Composable
private fun EmptyFavoritesContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = UiConstants.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_empty_favorites),
            contentDescription = stringResource(R.string.empty_favorites_image_desc),
            modifier = Modifier.size(450.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.size(UiConstants.ContentSpacing))

        Text(
            text = stringResource(R.string.empty_favorites_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(UiConstants.SmallSpacing))

        Text(
            text = stringResource(R.string.empty_favorites_description),
            style = MaterialTheme.typography.bodyMedium,
            color = EmptyTextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}