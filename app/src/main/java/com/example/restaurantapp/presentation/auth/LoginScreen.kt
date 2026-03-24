package com.example.restaurantapp.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.login_screen_title))
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UiConstants.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(UiConstants.ContentPadding)
        ) {
            Text(text = stringResource(R.string.login_placeholder))

            Button(onClick = onLoginSuccess) {
                Text(text = stringResource(R.string.login))
            }

            TextButton(onClick = onNavigateToRegister) {
                Text(text = stringResource(R.string.go_to_register))
            }
        }
    }
}