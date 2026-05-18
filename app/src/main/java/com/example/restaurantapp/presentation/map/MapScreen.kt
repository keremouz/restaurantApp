package com.example.restaurantapp.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurantapp.BuildConfig
import com.example.restaurantapp.R
import com.example.restaurantapp.core.di.RetrofitProvider
import com.example.restaurantapp.core.location.AppLocationHolder
import com.example.restaurantapp.core.location.AppRestaurantHolder
import com.example.restaurantapp.core.location.UserLocation
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.repository.RestaurantRepositoryImpl
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import com.example.restaurantapp.presentation.components.LottieLoadingContent
import com.google.android.gms.location.LocationServices
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val MapTopBarBg = Color(0xFFF5F8FF)
private val MapTitleColor = Color(0xFF2F5BFF)
private val MapSubtitleColor = Color(0xFF6E8BFF)
private val MapIconBg = Color(0xFFDCE6FF)
private val MapIconBlue = Color(0xFF2F5BFF)
private val ChatHintBorder = Color(0xFFDCE6FF)

private val MapCategories = listOf(
    MapCategory(MAP_CATEGORY_ALL, "Tümü", Color(0xFF2F5BFF)),
    MapCategory("restaurant", "Restoran", Color(0xFF2F5BFF)),
    MapCategory("doner", "Döner", Color(0xFF2F5BFF)),
    MapCategory("fish", "Balık", Color(0xFF2F5BFF)),
    MapCategory("burger", "Burger", Color(0xFF2F5BFF)),
    MapCategory("kebab", "Kebap", Color(0xFF2F5BFF)),
    MapCategory("pide", "Pide", Color(0xFF2F5BFF)),
    MapCategory("breakfast", "Kahvaltı", Color(0xFF2F5BFF)),
    MapCategory("dessert", "Tatlı", Color(0xFF2F5BFF)),
    MapCategory("cafe", "Kafe", Color(0xFF2F5BFF)),
    MapCategory("steak", "Steak", Color(0xFF2F5BFF)),
    MapCategory("pizza", "Pizza", Color(0xFF2F5BFF)),
    MapCategory("sushi", "Sushi", Color(0xFF2F5BFF)),
    MapCategory("meyhane", "Meyhane", Color(0xFF2F5BFF))
)

@Composable
fun MapScreen(
    isConnected: Boolean,
    onRestaurantClick: (Restaurant) -> Unit,
    onChatBotClick: () -> Unit
) {
    val repository = remember {
        RestaurantRepositoryImpl(
            placesApiService = RetrofitProvider.placesApiService,
            apiKey = BuildConfig.PLACES_API_KEY
        )
    }

    val viewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.restaurants) {
        AppRestaurantHolder.restaurants = uiState.restaurants
    }

    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    var showChatHint by rememberSaveable { mutableStateOf(false) }
    var stopChatHintLoop by rememberSaveable { mutableStateOf(false) }

    var selectedRestaurant by remember {
        mutableStateOf<Restaurant?>(null)
    }

    LaunchedEffect(isConnected) {
        viewModel.updateConnectionState(isConnected)
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val userLocation = UserLocation(
                            latitude = it.latitude,
                            longitude = it.longitude
                        )

                        AppLocationHolder.userLocation = userLocation
                    }
                }
        }
    }

    LaunchedEffect(isConnected, stopChatHintLoop) {
        if (!isConnected || stopChatHintLoop) return@LaunchedEffect

        delay(1500)

        while (!stopChatHintLoop) {
            showChatHint = true
            delay(3500)

            showChatHint = false
            delay(12000)
        }
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
    fun resetMapZoom() {
        coroutineScope.launch {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    istanbul,
                    11f
                )
            )
        }
    }


    val mapProperties = remember(hasLocationPermission) {
        MapProperties(
            isBuildingEnabled = true,
            isIndoorEnabled = true,
            isTrafficEnabled = false,
            isMyLocationEnabled = hasLocationPermission
        )
    }

    val mapUiSettings = remember(hasLocationPermission) {
        MapUiSettings(
            compassEnabled = true,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = hasLocationPermission
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.White,
        topBar = {
            MapTopBar(
                selectedCategory = uiState.selectedCategory,
                searchQuery = uiState.searchQuery,
                onSearchQueryChanged = { query ->
                    selectedRestaurant = null
                    viewModel.onSearchQueryChanged(query)

                    if (query.isBlank()) {
                        resetMapZoom()
                    }
                },
                onCategorySelected = { category ->
                    selectedRestaurant = null
                    viewModel.onCategorySelected(category)
                    resetMapZoom()
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
                        .background(Color.White)
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
                        .background(Color.White)
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
                        .background(Color.White)
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(paddingValues)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = mapProperties,
                        uiSettings = mapUiSettings
                    ) {
                        Clustering(
                            items = clusterItems,
                            onClusterClick = { cluster ->
                                selectedRestaurant = null

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
                                selectedRestaurant = item.restaurant

                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(
                                            item.position,
                                            MAP_SELECTED_RESTAURANT_ZOOM
                                        )
                                    )
                                }

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

                    selectedRestaurant?.let { restaurant ->
                        SelectedRestaurantCard(
                            restaurant = restaurant,
                            onDetailClick = {
                                onRestaurantClick(restaurant)
                            },
                            onCloseClick = {
                                selectedRestaurant = null
                                resetMapZoom()
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(
                                    start = UiConstants.ScreenPadding,
                                    end = UiConstants.ScreenPadding,
                                    bottom = UiConstants.ScreenPadding
                                )
                        )
                    }

                    AnimatedVisibility(
                        visible = showChatHint && selectedRestaurant == null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(
                                start = UiConstants.ScreenPadding,
                                bottom = UiConstants.ScreenPadding +
                                        UiConstants.ChatBotFabSize +
                                        UiConstants.MediumSpacing
                            )
                    ) {
                        ChatBotHintBubble(
                            text = stringResource(R.string.chatbot_hint_message),
                            onClick = {
                                showChatHint = false
                                stopChatHintLoop = true
                                onChatBotClick()
                            }
                        )
                    }

                    if (selectedRestaurant == null) {
                        FloatingActionButton(
                            onClick = {
                                showChatHint = false
                                stopChatHintLoop = true
                                onChatBotClick()
                            },
                            containerColor = Color.White,
                            contentColor = MapIconBlue,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(UiConstants.ScreenPadding)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_ai_location),
                                contentDescription = stringResource(R.string.chatbot_title),
                                modifier = Modifier.size(UiConstants.ChatBotFabIconSize),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedRestaurantCard(
    restaurant: Restaurant,
    onDetailClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UiConstants.CardRadius),
        color = Color.White,
        shadowElevation = UiConstants.CardElevation,
        border = BorderStroke(
            width = UiConstants.ReviewCardBorderWidth,
            color = ChatHintBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(UiConstants.ContentSpacing),
            verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(UiConstants.ExtraSmallSpacing)
                ) {
                    Text(
                        text = restaurant.name,
                        color = MapTitleColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = restaurant.address,
                        color = MapSubtitleColor,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    restaurant.district?.takeIf { it.isNotBlank() }?.let { district ->
                        Text(
                            text = district,
                            color = MapIconBlue,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.map_selected_restaurant_close),
                    color = MapIconBlue,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onCloseClick)
                )
            }

            Button(
                onClick = onDetailClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MapIconBlue
                ),
                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.map_selected_restaurant_detail),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ChatBotHintBubble(
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(UiConstants.CardRadius),
            color = Color.White,
            shadowElevation = UiConstants.CardElevation,
            border = BorderStroke(
                width = UiConstants.ReviewCardBorderWidth,
                color = ChatHintBorder
            ),
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Text(
                text = text,
                color = MapTitleColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(
                    horizontal = UiConstants.ContentSpacing,
                    vertical = UiConstants.MediumSpacing
                )
            )
        }

        Box(
            modifier = Modifier
                .padding(start = UiConstants.LargeSpacing)
                .size(UiConstants.ChatBotHintArrowSize)
                .graphicsLayer {
                    rotationZ = 45f
                }
                .background(Color.White)
        )
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
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
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

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = UiConstants.ScreenPadding,
                        end = UiConstants.ScreenPadding,
                        bottom = UiConstants.SmallSpacing
                    ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.map_search_hint),
                        color = MapSubtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MapIconBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MapIconBlue,
                            modifier = Modifier.clickable {
                                onSearchQueryChanged("")
                            }
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(UiConstants.TextFieldRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = MapIconBlue,
                    unfocusedBorderColor = ChatHintBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = MapIconBlue,
                    focusedPlaceholderColor = MapSubtitleColor,
                    unfocusedPlaceholderColor = MapSubtitleColor,
                    focusedLeadingIconColor = MapIconBlue,
                    unfocusedLeadingIconColor = MapIconBlue,
                    focusedTrailingIconColor = MapIconBlue,
                    unfocusedTrailingIconColor = MapIconBlue
                )
            )

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
private const val MAP_SELECTED_RESTAURANT_ZOOM = 17f