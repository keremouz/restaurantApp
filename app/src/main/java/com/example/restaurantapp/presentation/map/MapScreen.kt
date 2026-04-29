package com.example.restaurantapp.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurantapp.BuildConfig
import com.example.restaurantapp.R
import com.example.restaurantapp.core.di.RetrofitProvider
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.repository.RestaurantRepositoryImpl
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val MapTopBarBg = Color(0xFFF5F8FF)
private val MapTitleColor = Color(0xFF2F5BFF)
private val MapSubtitleColor = Color(0xFF6E8BFF)
private val MapIconBg = Color(0xFFDCE6FF)
private val MapIconBlue = Color(0xFF2F5BFF)
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

    Log.d("KEY_CHECK", "Places key empty: ${BuildConfig.PLACES_API_KEY.isBlank()}")

    val viewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(isConnected) {
        viewModel.updateConnectionState(isConnected)
    }

    val context = LocalContext.current

    val restaurantMarkerIcon = remember(context) {
        bitmapDescriptorFromVectorOrNull(
            context = context,
            vectorResId = R.drawable.ic_restaurant_marker
        )
    }

    val istanbul = LatLng(41.0082, 28.9784)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(istanbul, 11f)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MapTopBar()
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
                    CircularProgressIndicator(
                        color = MapIconBlue,
                        strokeWidth = UiConstants.LoadingIndicatorStrokeWidth
                    )
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${stringResource(R.string.map_error_prefix)} ${uiState.errorMessage}",
                        color = MapTitleColor
                    )
                }
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

            else -> {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    cameraPositionState = cameraPositionState
                ) {
                    uiState.restaurants.forEach { restaurant ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    restaurant.latitude,
                                    restaurant.longitude
                                )
                            ),
                            title = restaurant.name,
                            snippet = restaurant.address,
                            icon = restaurantMarkerIcon,
                            onClick = {
                                onRestaurantClick(restaurant)
                                true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapTopBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MapTopBarBg,
        shadowElevation = UiConstants.MapTopBarElevation
    ){
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
    }
}

private fun bitmapDescriptorFromVectorOrNull(
    context: Context,
    @DrawableRes vectorResId: Int
): BitmapDescriptor? {
    return try {
        MapsInitializer.initialize(context)

        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
            ?: return null

        val width = if (vectorDrawable.intrinsicWidth > 0) {
            vectorDrawable.intrinsicWidth
        } else {
            DEFAULT_MARKER_WIDTH
        }

        val height = if (vectorDrawable.intrinsicHeight > 0) {
            vectorDrawable.intrinsicHeight
        } else {
            DEFAULT_MARKER_HEIGHT
        }

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        vectorDrawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        vectorDrawable.draw(canvas)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (exception: Exception) {
        Log.e("MapScreen", "Marker icon could not be created", exception)
        null
    }
}

private const val DEFAULT_MARKER_WIDTH = 80
private const val DEFAULT_MARKER_HEIGHT = 92