package com.example.restaurantapp.presentation.components

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.restaurantapp.core.util.UiConstants

@Composable
fun LottieLoadingContent(
    @RawRes animationRes: Int,
    modifier: Modifier = Modifier,
    text: String? = null,
    textColor: Color = Color(0xFF0B2F86)
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(animationRes)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(UiConstants.LottieLoadingSize)
        )

        text?.let {
            Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}