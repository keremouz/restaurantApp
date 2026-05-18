package com.example.restaurantapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.restaurantapp.core.util.UiConstants


private val ConnectionBlue = Color(0xFF2F5BFF)
private val ConnectionText = Color(0xFF162033)
private val ConnectionMuted = Color(0xFF6E7A99)

@Composable
fun ConnectionWarningContent(
    innerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onRetryClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(innerPadding)
            .padding(horizontal = UiConstants.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            tint = ConnectionBlue,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

        Text(
            text = "İnternet bağlantısı yok",
            style = MaterialTheme.typography.titleMedium,
            color = ConnectionText
        )

        Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

        Text(
            text = "Bu sayfayı görüntülemek için internet bağlantınızı kontrol edip tekrar deneyin.",
            style = MaterialTheme.typography.bodyMedium,
            color = ConnectionMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (onRetryClick != null) {
            Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ConnectionBlue
                ),
                shape = RoundedCornerShape(UiConstants.ButtonRadius)
            ) {
                Text(
                    text = "Tekrar Dene",
                    color = Color.White
                )
            }
        }
    }
}