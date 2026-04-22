package com.example.restaurantapp.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.AuthManager
import kotlinx.coroutines.launch

private val PrimaryBlue = Color(0xFF3366FF)
private val ScreenBg = Color(0xFFF8F8FC)
private val FieldBg = Color(0xFFF1F3FF)
private val FieldBorder = Color(0xFFE8EBFB)
private val HintColor = Color(0xFFA7B2E3)
private val TextDark = Color(0xFF1E1E1E)
private val DescriptionGray = Color(0xFF8E8E93)
private val DividerGray = Color(0xFFE5E7F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val authManager = remember { AuthManager() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val emptyFieldsError = stringResource(R.string.login_fill_fields)

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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            tint = PrimaryBlue
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
                .padding(horizontal = UiConstants.ScreenPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(UiConstants.LoginTopSpacing))

            Text(
                text = stringResource(R.string.login_welcome),
                color = PrimaryBlue,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

            Text(
                text = stringResource(R.string.login_description),
                color = DescriptionGray,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(UiConstants.LargeSpacing))

            Text(
                text = stringResource(R.string.email),
                color = TextDark,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.email_hint),
                        color = HintColor
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(UiConstants.TextFieldRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FieldBorder,
                    unfocusedBorderColor = FieldBorder,
                    focusedContainerColor = FieldBg,
                    unfocusedContainerColor = FieldBg,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    cursorColor = PrimaryBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(UiConstants.LoginFieldSpacing))

            Text(
                text = stringResource(R.string.password),
                color = TextDark,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.password_hint),
                        color = HintColor
                    )
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = stringResource(R.string.password),
                            tint = DescriptionGray
                        )
                    }
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(UiConstants.TextFieldRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FieldBorder,
                    unfocusedBorderColor = FieldBorder,
                    focusedContainerColor = FieldBg,
                    unfocusedContainerColor = FieldBg,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    cursorColor = PrimaryBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = UiConstants.ExtraSmallSpacing),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { }) {
                    Text(
                        text = stringResource(R.string.forgot_password),
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(UiConstants.ExtraSmallSpacing))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.MediumSpacing))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = emptyFieldsError
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
                enabled = !isLoading && !isGoogleLoading,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(UiConstants.LoginButtonRadius),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(UiConstants.LoginButtonHeight)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = UiConstants.LoadingIndicatorStrokeWidth
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DividerGray
                )

                Text(
                    text = stringResource(R.string.login_with_google_divider),
                    color = DescriptionGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = UiConstants.MediumSpacing)
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DividerGray
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                GoogleCircleButton(
                    enabled = !isLoading && !isGoogleLoading,
                    onClick = {
                        errorMessage = null
                        isGoogleLoading = true

                        coroutineScope.launch {
                            authManager.signInWithGoogle(
                                context = context,
                                credentialRequest = createGoogleSignInRequest(),
                                onSuccess = {
                                    isGoogleLoading = false
                                    onLoginSuccess()
                                },
                                onError = { message ->
                                    isGoogleLoading = false
                                    errorMessage = message
                                }
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.LoginBottomSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.no_account_question),
                    color = DescriptionGray,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = " ${stringResource(R.string.register_short)}",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister)
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.ScreenPadding))
        }
    }
}

@Composable
private fun GoogleCircleButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(UiConstants.LoginSocialButtonSize)
            .background(Color.White, CircleShape)
            .border(
                width = 1.dp,
                color = DividerGray,
                shape = CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = stringResource(R.string.google_sign_in),
            tint = Color.Unspecified,
            modifier = Modifier.size(UiConstants.LoginSocialIconSize)
        )
    }
}