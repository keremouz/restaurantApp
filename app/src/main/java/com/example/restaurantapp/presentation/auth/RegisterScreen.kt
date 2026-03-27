package com.example.restaurantapp.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val authManager = remember { AuthManager() }

    var fullName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.register_screen_title))
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
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    errorMessage = null
                },
                label = { Text(text = stringResource(R.string.full_name)) },
                singleLine = true
            )

            OutlinedTextField(
                value = birthDate,
                onValueChange = {
                    birthDate = it
                    errorMessage = null
                },
                label = { Text(text = stringResource(R.string.birth_date)) },
                placeholder = { Text(text = stringResource(R.string.birth_date_hint)) },
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text(text = stringResource(R.string.email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text(text = stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )

            errorMessage?.let {
                Text(text = it)
            }

            Button(
                enabled = !isLoading,
                onClick = {
                    if (fullName.isBlank() || birthDate.isBlank() || email.isBlank() || password.isBlank()) {
                        errorMessage = "Tüm alanları doldurun"
                        return@Button
                    }

                    isLoading = true

                    authManager.register(
                        fullName = fullName.trim(),
                        birthDate = birthDate.trim(),
                        email = email.trim(),
                        password = password,
                        onSuccess = {
                            isLoading = false
                            onRegisterSuccess()
                        },
                        onError = { message ->
                            isLoading = false
                            errorMessage = message
                        }
                    )
                }
            ) {
                Text(
                    text = if (isLoading) {
                        stringResource(R.string.loading)
                    } else {
                        stringResource(R.string.register)
                    }
                )
            }
        }
    }
}