package com.example.restaurantapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.AuthManager
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.WindowInsets

private val AccountBlue = Color(0xFF3D4BFF)
private val AccountBg = Color(0xFFF7F7F7)
private val AvatarBg = Color(0xFFE8ECFF)
private val DangerRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    isConnected: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToMyReviews: () -> Unit,
    onRateAppClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    val authManager = remember { AuthManager() }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var currentUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth.currentUser) }
    var fullName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var reviewCount by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }

        firebaseAuth.addAuthStateListener(listener)

        onDispose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(currentUser?.uid, isConnected) {
        fullName = ""
        birthDate = ""
        reviewCount = 0

        if (!isConnected) return@LaunchedEffect

        val uid = currentUser?.uid ?: return@LaunchedEffect

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                fullName = document.getString("fullName").orEmpty()
                birthDate = document.getString("birthDate").orEmpty()
            }

        firestore.collection("comments")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { documents ->
                reviewCount = documents.size()
            }
    }

    Scaffold(
        containerColor = AccountBg,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        color = AccountBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        if (!isConnected) {
            ConnectionWarningContent(
                innerPadding = PaddingValues(),
                contentPadding = paddingValues
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AccountBg)
                    .padding(paddingValues)
                    .padding(
                        start = UiConstants.ScreenPadding,
                        end = UiConstants.ScreenPadding,
                        top = 0.dp,
                        bottom = UiConstants.ScreenPadding
                    )
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiConstants.MediumSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentUser == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(UiConstants.CardRadius),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = UiConstants.CardElevation
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(UiConstants.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(UiConstants.MediumSpacing)
                        ) {
                            Text(
                                text = stringResource(R.string.account_guest_message),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Button(
                                onClick = onNavigateToLogin,
                                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                                colors = ButtonDefaults.buttonColors(containerColor = AccountBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.login))
                            }

                            OutlinedButton(
                                onClick = onNavigateToRegister,
                                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.register),
                                    color = AccountBlue
                                )
                            }
                        }
                    }
                } else {
                    ProfileHeader(
                        fullName = if (fullName.isBlank()) "-" else fullName,
                        email = currentUser?.email ?: "-"
                    )

                    Text(
                        text = stringResource(R.string.my_information),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(UiConstants.CardRadius),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = UiConstants.CardElevation
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(UiConstants.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(UiConstants.MediumSpacing)
                        ) {
                            InfoRow(
                                title = stringResource(R.string.birth_date),
                                value = if (birthDate.isBlank()) "-" else birthDate
                            )

                            InfoRow(
                                title = stringResource(R.string.review_count),
                                value = reviewCount.toString()
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(UiConstants.CardRadius),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = UiConstants.CardElevation
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AccountMenuItem(
                                title = stringResource(R.string.my_reviews),
                                onClick = onNavigateToMyReviews
                            )

                            HorizontalDivider()

                            AccountMenuItem(
                                title = stringResource(R.string.rate_us),
                                onClick = onRateAppClick
                            )

                            HorizontalDivider()

                            AccountMenuItem(
                                title = stringResource(R.string.language_selection),
                                onClick = onLanguageClick
                            )

                            HorizontalDivider()

                            AccountMenuItem(
                                title = stringResource(R.string.delete_account),
                                onClick = onDeleteAccountClick,
                                textColor = DangerRed
                            )
                        }
                    }

                    Button(
                        onClick = { authManager.logout() },
                        shape = RoundedCornerShape(UiConstants.ButtonRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.logout))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    fullName: String,
    email: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UiConstants.ProfileHeaderSpacing)
    ) {
        Box(
            modifier = Modifier
                .size(UiConstants.ProfileAvatarSize)
                .background(AvatarBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = AccountBlue,
                modifier = Modifier.size(UiConstants.ProfileAvatarIconSize)
            )
        }

        Text(
            text = fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun InfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AccountMenuItem(
    title: String,
    onClick: () -> Unit,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(UiConstants.ScreenPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = if (textColor == Color.Unspecified) Color.Gray else textColor
        )
    }
}