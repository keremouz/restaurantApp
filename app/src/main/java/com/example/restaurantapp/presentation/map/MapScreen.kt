package com.example.restaurantapp.presentation.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurantapp.BuildConfig
import com.example.restaurantapp.R
import com.example.restaurantapp.core.di.RetrofitProvider
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.repository.RestaurantRepositoryImpl
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import com.example.restaurantapp.presentation.components.LottieLoadingContent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private val MapTopBarBg = Color(0xFFF5F8FF)
private val MapTitleColor = Color(0xFF2F5BFF)
private val MapSubtitleColor = Color(0xFF6E8BFF)
private val MapIconBg = Color(0xFFDCE6FF)
private val MapIconBlue = Color(0xFF2F5BFF)

private val MapCategories = listOf(
    MapCategory(MAP_CATEGORY_ALL, "Tümü", Color(0xFF2F5BFF)),
    MapCategory("restaurant", "Restoran", Color(0xFF5E6CE7)),
    MapCategory("doner", "Döner", Color(0xFFFF7043)),
    MapCategory("fish", "Balık", Color(0xFF00A6D6)),
    MapCategory("burger", "Burger", Color(0xFFFFB300)),
    MapCategory("kebab", "Kebap", Color(0xFFD84315)),
    MapCategory("pide", "Pide", Color(0xFFFF8A65)),
    MapCategory("breakfast", "Kahvaltı", Color(0xFF43A047)),
    MapCategory("dessert", "Tatlı", Color(0xFFE91E63)),
    MapCategory("cafe", "Kafe", Color(0xFF8D6E63)),
    MapCategory("steak", "Steak", Color(0xFF795548)),
    MapCategory("pizza", "Pizza", Color(0xFFEF5350)),
    MapCategory("sushi", "Sushi", Color(0xFF26A69A)),
    MapCategory("meyhane", "Meyhane", Color(0xFF7E57C2))
)

@Composable
fun MapScreen(
    isConnected: Boolean,
    onRestaurantClick: (Restaurant) -> Unit
) {
    val repository = remember {
        RestaurantRepositoryImpl(
            placesApiService = RetrofitProvider.placesApiService,
            apiKey = BuildConfig.PLACES_API_KEY,
        )
    }

    val viewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(isConnected) {
        viewModel.updateConnectionState(isConnected)
    }

    val clusterItems = remember(uiState.filteredRestaurants) {
        uiState.filteredRestaurants.map { restaurant ->
            RestaurantClusterItem(restaurant)
        }
    }

    val istanbul = LatLng(41.0082, 28.9784)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(istanbul, 11f)
    }

    val coroutineScope = rememberCoroutineScope()

    val mapProperties = remember {
        MapProperties(
            isBuildingEnabled = true,
            isIndoorEnabled = true,
            isTrafficEnabled = false
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            compassEnabled = true,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MapTopBar(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { category ->
                    viewModel.onCategorySelected(category)
                }
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

            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LottieLoadingContent(
                        animationRes = R.raw.restaurant_loading,
                        text = stringResource(R.string.loading_restaurants)
                    )
                }
            }

            uiState.errorMessage != null -> {
                MapErrorContent(
                    message = "${stringResource(R.string.map_error_prefix)} ${uiState.errorMessage}",
                    modifier = Modifier.padding(paddingValues),
                    onRetryClick = {
                        viewModel.retryLoadRestaurants()
                    }
                )
            }

            uiState.restaurants.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.map_empty),
                        color = MapTitleColor
                    )
                }
            }

            uiState.filteredRestaurants.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.map_category_empty),
                        color = MapTitleColor
                    )
                }
            }

            else -> {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings
                ) {
                    Clustering(
                        items = clusterItems,
                        onClusterClick = { cluster ->
                            val boundsBuilder = LatLngBounds.builder()

                            cluster.items.forEach { item ->
                                boundsBuilder.include(item.position)
                            }

                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngBounds(
                                        boundsBuilder.build(),
                                        MAP_CLUSTER_ZOOM_PADDING
                                    )
                                )
                            }

                            true
                        },
                        onClusterItemClick = { item ->
                            onRestaurantClick(item.restaurant)
                            true
                        },
                        clusterItemContent = { item ->
                            RestaurantMarker(
                                category = item.restaurant.category
                            )
                        },
                        clusterContent = { cluster ->
                            Box(
                                modifier = Modifier
                                    .size(UiConstants.MapClusterSize)
                                    .background(
                                        color = MapIconBlue,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cluster.size.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MapErrorContent(
    message: String,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(UiConstants.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MapTitleColor,
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MapIconBlue
            ),
            shape = RoundedCornerShape(UiConstants.ButtonRadius),
            modifier = Modifier.padding(top = UiConstants.ContentSpacing)
        ) {
            Text(
                text = stringResource(R.string.map_retry),
                color = Color.White
            )
        }
    }
}

@Composable
private fun MapTopBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MapTopBarBg,
        shadowElevation = UiConstants.MapTopBarElevation
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = UiConstants.ScreenPadding,
                        vertical = UiConstants.MapTopBarVerticalPadding
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(UiConstants.ExtraSmallSpacing)
                ) {
                    Text(
                        text = stringResource(R.string.map_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MapTitleColor
                    )

                    Text(
                        text = stringResource(R.string.map_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MapSubtitleColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(UiConstants.MapTopBarIconContainerSize)
                        .background(
                            color = MapIconBg,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MapIconBlue,
                        modifier = Modifier.size(UiConstants.MapTopBarIconSize)
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = UiConstants.ScreenPadding,
                        end = UiConstants.ScreenPadding,
                        bottom = UiConstants.MapCategoryBottomPadding
                    ),
                horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
            ) {
                items(MapCategories) { category ->
                    CategoryChip(
                        category = category,
                        selected = selectedCategory == category.id,
                        onClick = {
                            onCategorySelected(category.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: MapCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        category.color
    } else {
        Color.White
    }

    val textColor = if (selected) {
        Color.White
    } else {
        category.color
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(UiConstants.PillRadius)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = UiConstants.PillHorizontalPadding,
                vertical = UiConstants.PillVerticalPadding
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.title,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun RestaurantMarker(
    category: String
) {
    Image(
        painter = painterResource(
            id = getMarkerDrawableByCategory(category)
        ),
        contentDescription = null,
        modifier = Modifier.size(UiConstants.MapClusterItemSize)
    )
}

private fun getMarkerDrawableByCategory(
    category: String
): Int {
    return when (category) {
        "doner" -> R.drawable.ic_marker_doner
        "fish" -> R.drawable.ic_marker_fish
        "burger" -> R.drawable.ic_marker_burger
        "pide" -> R.drawable.ic_marker_pide
        "breakfast" -> R.drawable.ic_marker_breakfast
        "dessert" -> R.drawable.ic_marker_dessert
        "cafe" -> R.drawable.ic_marker_cafe
        "pizza" -> R.drawable.ic_marker_pizza
        "sushi" -> R.drawable.ic_marker_sushi
        "meyhane" -> R.drawable.ic_marker_meyhane
        "kebab" -> R.drawable.ic_restaurant_marker
        else -> R.drawable.ic_restaurant_marker
    }
}

private data class MapCategory(
    val id: String,
    val title: String,
    val color: Color
)

private data class RestaurantClusterItem(
    val restaurant: Restaurant
) : ClusterItem {

    override fun getPosition(): LatLng {
        return LatLng(
            restaurant.latitude,
            restaurant.longitude
        )
    }

    override fun getTitle(): String {
        return restaurant.name
    }

    override fun getSnippet(): String {
        return restaurant.address
    }

    override fun getZIndex(): Float? {
        return null
    }
}

private const val MAP_CLUSTER_ZOOM_PADDING = 120