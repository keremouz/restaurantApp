package com.example.restaurantapp.presentation.chatbot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants

private val ChatBotBlue = Color(0xFF2F5BFF)
private val ChatBotBg = Color(0xFFF5F8FF)
private val ChatBotText = Color(0xFF162033)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = ChatBotBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chatbot_title),
                        color = ChatBotText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = null,
                            tint = ChatBotBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChatBotBg
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UiConstants.ScreenPadding)
        ) {
            Text(
                text = "Merhaba, sana restoran önerilerinde yardımcı olabilirim.",
                style = MaterialTheme.typography.bodyLarge,
                color = ChatBotText
            )
        }
    }
}