package com.example.restaurantapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E5BFF),
    onPrimary = Color.White,

    secondary = Color(0xFF1E5BFF),
    onSecondary = Color.White,

    tertiary = Color(0xFF1E5BFF),
    onTertiary = Color.White,

    background = Color.White,
    onBackground = Color(0xFF1A1A1A),

    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),

    surfaceVariant = Color(0xFFF4F6FA),
    onSurfaceVariant = Color(0xFF6B7280),

    outline = Color(0xFFE0E0E0)
)

@Composable
fun RestaurantAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}