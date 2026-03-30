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

private val RegisterBlue = Color(0xFF3D4BFF)
private val RegisterBg = Color(0xFFF7F7F7)

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
        containerColor = RegisterBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.register_screen_title),
                        color = RegisterBlue,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(
                            text = stringResource(R.string.back),
                            color = RegisterBlue
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RegisterBg)
                .padding(paddingValues)
                .padding(horizontal = UiConstants.ScreenPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.register_heading),
                color = RegisterBlue,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = UiConstants.LargeSpacing)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    errorMessage = null
                },
                placeholder = { Text(stringResource(R.string.full_name)) },
                singleLine = true,
                shape = RoundedCornerShape(UiConstants.TextFieldRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RegisterBlue,
                    unfocusedBorderColor = RegisterBlue,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = RegisterBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = UiConstants.MediumSpacing)
            )

            OutlinedTextField(
                value = birthDate,
                onValueChange = {
                    birthDate = it
                    errorMessage = null
                },
                placeholder = { Text(stringResource(R.string.birth_date)) },
                singleLine = true,
                shape = RoundedCornerShape(UiConstants.TextFieldRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RegisterBlue,
                    unfocusedBorderColor = RegisterBlue,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = RegisterBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = UiConstants.MediumSpacing)
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
                    focusedBorderColor = RegisterBlue,
                    unfocusedBorderColor = RegisterBlue,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = RegisterBlue
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
                    focusedBorderColor = RegisterBlue,
                    unfocusedBorderColor = RegisterBlue,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = RegisterBlue
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
            val emptyRegisterError = stringResource(R.string.empty_register_error)
            Button(
                onClick = {
                    if (fullName.isBlank() || birthDate.isBlank() || email.isBlank() || password.isBlank()) {
                        errorMessage = emptyRegisterError
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
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RegisterBlue
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
                        text = stringResource(R.string.register),
                        color = Color.White,
                        modifier = Modifier.padding(vertical = UiConstants.ButtonVerticalPadding)
                    )
                }
            }

            TextButton(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = UiConstants.SmallSpacing)
            ) {
                Text(
                    text = stringResource(R.string.go_to_login),
                    color = RegisterBlue
                )
            }
        }
    }
}