package com.example.restaurantapp.presentation.map

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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurantapp.BuildConfig
import com.example.restaurantapp.R
import com.example.restaurantapp.core.di.RetrofitProvider
import com.example.restaurantapp.data.repository.RestaurantRepositoryImpl
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
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
            apiKey = BuildConfig.PLACES_API_KEY
        )
    }

    val viewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(isConnected) {
        viewModel.updateConnectionState(isConnected)
    }

    val istanbul = LatLng(41.0082, 28.9784)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(istanbul, 11f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.map_title)) }
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
}