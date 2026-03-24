package com.example.restaurantapp.presentation.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


data class MapRestaurantMarker(
    val placeId: String,
    val name: String,
    val district: String,
    val latLng: LatLng
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onRestaurantClick: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val istanbul = LatLng(41.0082, 28.9784)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            istanbul,
            UiConstants.MapZoomLevel
        )
    }

    val markers = remember {
        listOf(
            MapRestaurantMarker(
                placeId = "sample_1",
                name = "Örnek Restoran 1",
                district = "Ümraniye",
                latLng = LatLng(41.0339, 29.1223)
            ),
            MapRestaurantMarker(
                placeId = "sample_2",
                name = "Örnek Restoran 2",
                district = "Kadıköy",
                latLng = LatLng(40.9909, 29.0287)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.map_title))
                },
                actions = {
                    TextButton(onClick = onNavigateToLogin) {
                        Text(text = stringResource(R.string.login))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToLogin) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = stringResource(R.string.login_content_description)
                )
            }
        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier,
            cameraPositionState = cameraPositionState,
            contentPadding = paddingValues
        ) {
            markers.forEach { marker ->
                Marker(
                    state = MarkerState(position = marker.latLng),
                    title = marker.name,
                    snippet = marker.district,
                    onClick = {
                        onRestaurantClick(marker.placeId)
                        true
                    }
                )
            }
        }
    }
}