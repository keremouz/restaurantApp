package com.example.restaurantapp.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.AuthManager
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.restaurantapp.core.util.LocaleHelper

private val AccountBlue = Color(0xFF2F5BFF)
private val AccountBg = Color.White
private val AccountCardBg = Color.White
private val AvatarBg = Color(0xFFE8EEFF)
private val MenuIconBg = Color(0xFFE8EEFF)
private val LevelIconBg = Color(0xFFFFF4D8)
private val LevelIconColor = Color(0xFFF2A900)
private val DangerRed = Color(0xFFE53935)
private val DangerBg = Color(0xFFFFEAEA)
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF7B7B84)
private val DividerColor = Color(0xFFE9E9EF)
private val GuestTextGray = Color(0xFF4F4F4F)

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
    var reviewCount by remember { mutableIntStateOf(0) }
    var favoriteCount by remember { mutableIntStateOf(0) }

    var showAvatarSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showLevelSheet by remember { mutableStateOf(false) }

    var selectedAvatar by remember { mutableIntStateOf(R.drawable.avatar_person) }
    val context = LocalContext.current

    var selectedLanguage by remember {
        mutableStateOf(
            if (LocaleHelper.getLanguage(context) == "en") {
                "English (EN)"
            } else {
                "Türkçe (TR)"
            }
        )
    }

    val avatars = listOf(
        R.drawable.avatar_person,
        R.drawable.avatar_woman,
        R.drawable.avatar_chef,
        R.drawable.avatar_burger,
        R.drawable.avatar_crown
    )

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
        reviewCount = 0
        favoriteCount = 0

        if (!isConnected) return@LaunchedEffect

        val uid = currentUser?.uid ?: return@LaunchedEffect

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                fullName = document.getString("fullName").orEmpty()
            }

        firestore.collection("comments")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { documents ->
                reviewCount = documents.size()
            }

        firestore.collection("users")
            .document(uid)
            .collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                favoriteCount = documents.size()
            }
    }

    Scaffold(
        containerColor = AccountBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        when {
            !isConnected -> {
                ConnectionWarningContent(
                    innerPadding = PaddingValues(),
                    contentPadding = paddingValues
                )
            }

            currentUser == null -> {
                GuestAccountContent(
                    paddingValues = paddingValues,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister
                )
            }

            else -> {
                LoggedInAccountContent(
                    paddingValues = paddingValues,
                    fullName = fullName.ifBlank { stringResource(R.string.unknown_user) },
                    email = currentUser?.email ?: "-",
                    reviewCount = reviewCount,
                    favoriteCount = favoriteCount,
                    selectedAvatar = selectedAvatar,
                    selectedLanguage = selectedLanguage,
                    onAvatarClick = {
                        showAvatarSheet = true
                    },
                    onNavigateToMyReviews = onNavigateToMyReviews,
                    onRateAppClick = onRateAppClick,
                    onLanguageClick = {
                        onLanguageClick()
                        showLanguageSheet = true
                    },
                    onDeleteAccountClick = onDeleteAccountClick,
                    onLogoutClick = { authManager.signOut() },
                    onLevelClick = {
                        showLevelSheet = true
                    },
                )
            }
        }
    }

    if (showAvatarSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvatarSheet = false },
            containerColor = AccountBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UiConstants.BottomSheetPadding)
            ) {
                Text(
                    text = "Profil Fotoğrafı Seç",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(UiConstants.BottomSheetSpacing))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    avatars.forEach { avatar ->
                        Image(
                            painter = painterResource(id = avatar),
                            contentDescription = null,
                            modifier = Modifier
                                .size(UiConstants.AccountBottomSheetAvatarSize)
                                .clip(CircleShape)
                                .clickable {
                                    selectedAvatar = avatar
                                    showAvatarSheet = false
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(UiConstants.AccountBottomSheetBottomSpacing))
            }
        }
    }

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = AccountBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = UiConstants.AccountBottomSheetHorizontalPadding,
                        vertical = UiConstants.AccountBottomSheetVerticalPadding
                    )
            ) {
                Text(
                    text = stringResource(R.string.language_selection),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(UiConstants.AccountBottomSheetTitleBottomSpacing))

                LanguageOptionItem(
                    title = "Türkçe (TR)",
                    isSelected = selectedLanguage == "Türkçe (TR)",
                    onClick = {
                        selectedLanguage = "Türkçe (TR)"
                        LocaleHelper.setLanguage(context, "tr")
                        showLanguageSheet = false
                        (context as? Activity)?.recreate()
                    }
                )

                Spacer(modifier = Modifier.height(UiConstants.AccountBottomSheetSmallSpacing))

                LanguageOptionItem(
                    title = "English (EN)",
                    isSelected = selectedLanguage == "English (EN)",
                    onClick = {
                        selectedLanguage = "English (EN)"
                        LocaleHelper.setLanguage(context, "en")
                        showLanguageSheet = false
                        (context as? Activity)?.recreate()
                    }
                )

                Spacer(modifier = Modifier.height(UiConstants.AccountBottomSheetBottomSpacing))
            }
        }
    }
    if (showLevelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLevelSheet = false },
            containerColor = AccountBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = UiConstants.AccountBottomSheetHorizontalPadding,
                        vertical = UiConstants.AccountBottomSheetVerticalPadding
                    )
            ) {
                Text(
                    text = stringResource(R.string.account_level_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

                Text(
                    text = getNextLevelInfo(reviewCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(UiConstants.AccountBottomSheetTitleBottomSpacing))

                LevelInfoItem(stringResource(R.string.level_new_member), stringResource(R.string.level_range_new_member), reviewCount in 0..15)
                LevelInfoItem(stringResource(R.string.level_explorer), stringResource(R.string.level_range_explorer), reviewCount in 16..30)
                LevelInfoItem(stringResource(R.string.level_taste_hunter), stringResource(R.string.level_range_taste_hunter), reviewCount in 31..50)
                LevelInfoItem(stringResource(R.string.level_gourmet), stringResource(R.string.level_range_gourmet), reviewCount in 51..80)
                LevelInfoItem(stringResource(R.string.level_restaurant_expert), stringResource(R.string.level_range_restaurant_expert), reviewCount >= 81)

                Spacer(modifier = Modifier.height(UiConstants.AccountBottomSheetBottomSpacing))
            }
        }
    }
}
@Composable
private fun LevelInfoItem(
    title: String,
    subtitle: String,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isCurrent) AvatarBg else Color.Transparent,
                shape = RoundedCornerShape(UiConstants.AccountBottomSheetOptionRadius)
            )
            .padding(
                horizontal = UiConstants.AccountBottomSheetOptionHorizontalPadding,
                vertical = UiConstants.AccountBottomSheetOptionVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = TextPrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        if (isCurrent) {
            Text(
                text = stringResource(R.string.current_level),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AccountBlue
            )
        }
    }
}

@Composable
private fun getNextLevelInfo(reviewCount: Int): String {
    return when {
        reviewCount <= 15 -> stringResource(R.string.next_level_explorer, 16 - reviewCount)
        reviewCount <= 30 -> stringResource(R.string.next_level_taste_hunter, 31 - reviewCount)
        reviewCount <= 50 -> stringResource(R.string.next_level_gourmet, 51 - reviewCount)
        reviewCount <= 80 -> stringResource(R.string.next_level_restaurant_expert, 81 - reviewCount)
        else -> stringResource(R.string.highest_level_reached)
    }
}

@Composable
private fun LoggedInAccountContent(
    paddingValues: PaddingValues,
    fullName: String,
    email: String,
    reviewCount: Int,
    favoriteCount: Int,
    selectedAvatar: Int,
    selectedLanguage: String,
    onAvatarClick: () -> Unit,
    onLevelClick: () -> Unit,
    onNavigateToMyReviews: () -> Unit,
    onRateAppClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val levelTitle = getUserLevelTitle(reviewCount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccountBg)
            .padding(paddingValues)
            .padding(horizontal = UiConstants.AccountHorizontalPadding)
            .padding(bottom = UiConstants.AccountBottomPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = UiConstants.AccountTitleTopPadding)
        )

        Spacer(modifier = Modifier.height(UiConstants.AccountTitleBottomSpacing))

        ProfileHeader(
            fullName = fullName,
            email = email,
            selectedAvatar = selectedAvatar,
            onAvatarClick = onAvatarClick
        )

        Spacer(modifier = Modifier.height(UiConstants.AccountSectionSpacing))

        LevelCard(
            levelTitle = levelTitle,
            reviewCount = reviewCount,
            onClick = onLevelClick
        )

        Spacer(modifier = Modifier.height(UiConstants.AccountSectionSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UiConstants.AccountSectionSpacing)
        ) {
            ProfileInfoCard(
                icon = Icons.Default.RateReview,
                value = reviewCount.toString(),
                title = stringResource(R.string.review_count_profile),
                modifier = Modifier.weight(1f)
            )

            ProfileInfoCard(
                icon = Icons.Default.FavoriteBorder,
                value = favoriteCount.toString(),
                title = stringResource(R.string.favorite_count_profile),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(UiConstants.AccountProfileToMenuBlockSpacing))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(UiConstants.AccountMenuCardRadius),
            colors = CardDefaults.cardColors(containerColor = AccountCardBg),
            elevation = CardDefaults.cardElevation(
                defaultElevation = UiConstants.AccountCardElevation
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AccountMenuItem(
                    icon = Icons.Default.StarBorder,
                    title = stringResource(R.string.my_reviews),
                    subtitle = stringResource(R.string.review_count_text, reviewCount),
                    onClick = onNavigateToMyReviews
                )

                HorizontalDivider(color = DividerColor)

                AccountMenuItem(
                    icon = Icons.Default.RateReview,
                    title = stringResource(R.string.rate_us),
                    subtitle = stringResource(R.string.rate_us_subtitle),
                    onClick = onRateAppClick
                )

                HorizontalDivider(color = DividerColor)

                AccountMenuItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language_selection),
                    subtitle = selectedLanguage,
                    onClick = onLanguageClick
                )

                HorizontalDivider(color = DividerColor)

                AccountMenuItem(
                    icon = Icons.Default.DeleteOutline,
                    title = stringResource(R.string.delete_account),
                    subtitle = stringResource(R.string.delete_account_subtitle),
                    onClick = onDeleteAccountClick,
                    textColor = DangerRed,
                    iconBackgroundColor = DangerBg
                )

                HorizontalDivider(color = DividerColor)

                AccountMenuItem(
                    icon = Icons.Default.ExitToApp,
                    title = stringResource(R.string.logout),
                    subtitle = stringResource(R.string.logout_subtitle),
                    onClick = onLogoutClick
                )
            }
        }
    }
}

@Composable
private fun GuestAccountContent(
    paddingValues: PaddingValues,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccountBg)
            .padding(paddingValues)
            .padding(
                start = UiConstants.ScreenPadding,
                end = UiConstants.ScreenPadding,
                bottom = UiConstants.ScreenPadding
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.img_guest_welcome),
                contentDescription = null,
                modifier = Modifier.size(UiConstants.AccountGuestImageSize),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(UiConstants.LargeSpacing))

            Text(
                text = stringResource(R.string.account_guest_message),
                style = MaterialTheme.typography.bodyMedium,
                color = GuestTextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.82f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(UiConstants.LoginButtonRadius),
                colors = ButtonDefaults.buttonColors(containerColor = AccountBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(UiConstants.LoginButtonHeight)
            ) {
                Text(
                    text = stringResource(R.string.login),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

            OutlinedButton(
                onClick = onNavigateToRegister,
                shape = RoundedCornerShape(UiConstants.LoginButtonRadius),
                border = BorderStroke(
                    width = UiConstants.AccountOutlinedButtonBorderWidth,
                    color = Color(0xFFD0D0D8)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(UiConstants.LoginButtonHeight)
            ) {
                Text(
                    text = stringResource(R.string.register),
                    color = AccountBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.LargeSpacing))
        }
    }
}

@Composable
private fun ProfileHeader(
    fullName: String,
    email: String,
    selectedAvatar: Int,
    onAvatarClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(UiConstants.AccountAvatarSize)
                    .background(AvatarBg, CircleShape)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = selectedAvatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(UiConstants.AccountAvatarSize)
                        .clip(CircleShape)
                        .clickable {
                            onAvatarClick()
                        }
                )
            }

            Box(
                modifier = Modifier
                    .size(UiConstants.AccountEditIconContainerSize)
                    .background(AccountBlue, CircleShape)
                    .border(
                        width = UiConstants.AccountOutlinedButtonBorderWidth,
                        color = Color.White,
                        shape = CircleShape
                    )
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(UiConstants.AccountEditIconSize)
                )
            }
        }

        Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

        Text(
            text = fullName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun LevelCard(
    levelTitle: String,
    reviewCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(UiConstants.AccountInfoCardRadius),
        colors = CardDefaults.cardColors(containerColor = AccountCardBg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = UiConstants.AccountCardElevation
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiConstants.AccountInfoCardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(UiConstants.AccountMenuIconContainerSize)
                    .background(
                        color = LevelIconBg,
                        shape = RoundedCornerShape(UiConstants.AccountMenuIconRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = LevelIconColor,
                    modifier = Modifier.size(UiConstants.AccountMenuIconSize)
                )
            }

            Spacer(modifier = Modifier.size(UiConstants.MediumSpacing))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.account_level_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Text(
                    text = levelTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = stringResource(R.string.account_level_description, reviewCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(
    icon: ImageVector,
    value: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(UiConstants.AccountInfoCardRadius),
        colors = CardDefaults.cardColors(containerColor = AccountCardBg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = UiConstants.AccountCardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiConstants.AccountInfoCardPadding),
            verticalArrangement = Arrangement.spacedBy(UiConstants.ExtraSmallSpacing)
        ) {
            Box(
                modifier = Modifier
                    .size(UiConstants.AccountMenuIconContainerSize)
                    .background(
                        color = MenuIconBg,
                        shape = RoundedCornerShape(UiConstants.AccountMenuIconRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccountBlue,
                    modifier = Modifier.size(UiConstants.AccountMenuIconSize)
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = TextPrimary,
    iconBackgroundColor: Color = MenuIconBg
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = UiConstants.AccountMenuItemHorizontalPadding,
                vertical = UiConstants.AccountMenuItemVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(UiConstants.AccountMenuIconContainerSize)
                .background(
                    color = iconBackgroundColor,
                    shape = RoundedCornerShape(UiConstants.AccountMenuIconRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(UiConstants.AccountMenuIconSize)
            )
        }

        Spacer(modifier = Modifier.size(UiConstants.MediumSpacing))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (textColor == DangerRed) {
                    DangerRed.copy(alpha = 0.8f)
                } else {
                    TextSecondary
                }
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = if (textColor == DangerRed) {
                DangerRed.copy(alpha = 0.7f)
            } else {
                Color(0xFFB8C4FF)
            },
            modifier = Modifier.size(UiConstants.AccountChevronIconSize)
        )
    }
}
@Composable
private fun LanguageOptionItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) AvatarBg else Color.Transparent,
                shape = RoundedCornerShape(UiConstants.AccountBottomSheetOptionRadius)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = UiConstants.AccountBottomSheetOptionHorizontalPadding,
                vertical = UiConstants.AccountBottomSheetOptionVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AccountBlue
            )
        }
    }
}

@Composable
private fun getUserLevelTitle(
    reviewCount: Int
): String {
    return when {
        reviewCount <= 15 -> stringResource(R.string.level_new_member)
        reviewCount <= 30 -> stringResource(R.string.level_explorer)
        reviewCount <= 50 -> stringResource(R.string.level_taste_hunter)
        reviewCount <= 80 -> stringResource(R.string.level_gourmet)
        else -> stringResource(R.string.level_restaurant_expert)
    }
}