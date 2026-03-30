package com.example.restaurantapp.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.AuthManager

private val PrimaryBlue = Color(0xFF3D4BFF)
private val ScreenBg = Color(0xFFF7F7F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val authManager = remember { AuthManager() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.login_screen_title),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(
                            text = stringResource(R.string.back),
                            color = PrimaryBlue
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(paddingValues)
                .padding(horizontal = UiConstants.ScreenPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_heading),
                color = PrimaryBlue,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = UiConstants.LargeSpacing)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                placeholder = { Text(stringResource(R.string.email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(UiConstants.TextFieldRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = PrimaryBlue,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = PrimaryBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = UiConstants.MediumSpacing)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(UiConstants.TextFieldRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = PrimaryBlue,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = PrimaryBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = UiConstants.SmallSpacing)
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = UiConstants.MediumSpacing)
                )
            }
            val emptyLoginError = stringResource(R.string.empty_login_error)
            Button(

                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = emptyLoginError
                        return@Button
                    }

                    isLoading = true

                    authManager.login(
                        email = email.trim(),
                        password = password,
                        onSuccess = {
                            isLoading = false
                            onLoginSuccess()
                        },
                        onError = { message ->
                            isLoading = false
                            errorMessage = message
                        }
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = UiConstants.SmallSpacing)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(vertical = UiConstants.ButtonVerticalPadding)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login),
                        color = Color.White,
                        modifier = Modifier.padding(vertical = UiConstants.ButtonVerticalPadding)
                    )
                }
            }

            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = UiConstants.SmallSpacing)
            ) {
                Text(
                    text = stringResource(R.string.go_to_register),
                    color = PrimaryBlue
                )
            }
        }
    }
}