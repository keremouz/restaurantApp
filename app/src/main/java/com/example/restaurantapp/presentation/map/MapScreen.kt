package com.example.restaurantapp.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurantapp.BuildConfig
import com.example.restaurantapp.R
import com.example.restaurantapp.core.di.RetrofitProvider
import com.example.restaurantapp.data.repository.RestaurantRepositoryImpl
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.map_title))
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
                    CircularProgressIndicator()
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
                        text = "${stringResource(R.string.map_error_prefix)} ${uiState.errorMessage}"
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
                    Text(text = stringResource(R.string.map_empty))
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

        com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (exception: Exception) {
        Log.e("MapScreen", "Marker icon could not be created", exception)
        null
    }
}

private const val DEFAULT_MARKER_WIDTH = 80
private const val DEFAULT_MARKER_HEIGHT = 92