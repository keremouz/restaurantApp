package com.example.restaurantapp.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.CommentsManager
import com.example.restaurantapp.data.firebase.UserComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale

private val ReviewBg = Color(0xFFF7F8FA)
private val ReviewCardBg = Color(0xFFFFFFFF)
private val ReviewTitleColor = Color(0xFF161616)
private val ReviewBodyColor = Color(0xFF4F4F4F)
private val ReviewMutedColor = Color(0xFF9A9A9A)
private val ReviewDividerColor = Color(0xFFEAEAEA)
private val ReviewBlue = Color(0xFF3D4BFF)
private val ReviewBlueSoft = Color(0xFFE9ECFF)
private val ReviewGold = Color(0xFFB88900)
private val ReviewGoldSoft = Color(0xFFFFF4CC)
private val ReviewMetaPillBg = Color(0xFFF4F4F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    onBackClick: () -> Unit
) {
    val commentsManager = remember { CommentsManager() }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }

    var currentUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth.currentUser) }
    var reviews by remember { mutableStateOf<List<UserComment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }

        firebaseAuth.addAuthStateListener(listener)

        onDispose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(currentUser?.uid) {
        reviews = emptyList()
        errorMessage = null
        isLoading = true

        if (currentUser == null) {
            errorMessage = "Giriş yapmanız gerekiyor"
            isLoading = false
            return@LaunchedEffect
        }

        commentsManager.getCurrentUserComments(
            onSuccess = { commentList ->
                reviews = commentList
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    Scaffold(
        containerColor = ReviewBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.my_reviews),
                        color = ReviewTitleColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = ReviewTitleColor
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = ReviewBlue,
                        strokeWidth = UiConstants.LoadingIndicatorStrokeWidth
                    )
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(UiConstants.ScreenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ReviewTitleColor
                    )
                }
            }

            reviews.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(UiConstants.ScreenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.my_reviews_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ReviewMutedColor
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(UiConstants.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(UiConstants.MediumSpacing)
                ) {
                    item {
                        ReviewsHeader(reviewCount = reviews.size)
                    }

                    items(
                        items = reviews,
                        key = { it.commentId }
                    ) { review ->
                        ReviewArchiveCard(review = review)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewsHeader(
    reviewCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = UiConstants.SmallSpacing),
        verticalArrangement = Arrangement.spacedBy(UiConstants.TinySpacing)
    ) {

        Text(
            text = "Deneyimleriniz.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ReviewTitleColor
        )

        Text(
            text = "Toplam $reviewCount inceleme paylaştınız.",
            style = MaterialTheme.typography.bodyMedium,
            color = ReviewMutedColor
        )
    }
}

@Composable
private fun ReviewArchiveCard(
    review: UserComment
) {
    var isExpanded by rememberSaveable(review.commentId) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(UiConstants.CardRadius),
        colors = CardDefaults.cardColors(containerColor = ReviewCardBg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = UiConstants.CardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiConstants.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(UiConstants.ExtraSmallSpacing)
                ) {
                    Text(
                        text = review.restaurantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ReviewTitleColor
                    )

                    SmallMutedText(
                        text = review.district.ifBlank { "-" }
                    )
                }

                RatingBadge(rating = review.generalRating)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.FormatQuote,
                    contentDescription = null,
                    tint = ReviewBlueSoft,
                    modifier = Modifier
                        .size(UiConstants.QuoteIconSize)
                        .padding(top = UiConstants.TinySpacing)
                )

                Spacer(modifier = Modifier.width(UiConstants.SmallSpacing))

                Text(
                    text = "\"${review.comment}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReviewBodyColor,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = ReviewDividerColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallMetaPill(text = scoreText(review.generalRating))

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (isExpanded) "Detayları Gizle" else "Detaylar",
                    style = MaterialTheme.typography.labelLarge,
                    color = ReviewBlue,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(UiConstants.ExtraSmallSpacing))

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = ReviewBlue,
                    modifier = Modifier.rotate(if (isExpanded) 90f else 0f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = UiConstants.SmallSpacing),
                    verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
                ) {
                    HorizontalDivider(color = ReviewDividerColor)

                    RatingRow(
                        title = stringResource(R.string.criterion_taste),
                        value = review.ratings.taste.toString()
                    )
                    RatingRow(
                        title = stringResource(R.string.criterion_service),
                        value = review.ratings.service.toString()
                    )
                    RatingRow(
                        title = stringResource(R.string.criterion_price_performance),
                        value = review.ratings.pricePerformance.toString()
                    )
                    RatingRow(
                        title = stringResource(R.string.criterion_atmosphere),
                        value = review.ratings.atmosphere.toString()
                    )
                    RatingRow(
                        title = stringResource(R.string.criterion_location),
                        value = review.ratings.location.toString()
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingBadge(
    rating: Double
) {
    val isHigh = rating >= 4.0
    val badgeBg = if (isHigh) ReviewGoldSoft else ReviewBlueSoft
    val badgeTextColor = if (isHigh) ReviewGold else ReviewBlue

    Surface(
        shape = RoundedCornerShape(UiConstants.PillRadius),
        color = badgeBg
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = UiConstants.PillHorizontalPadding,
                vertical = UiConstants.PillVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiConstants.ExtraSmallSpacing)
        ) {
            if (isHigh) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = badgeTextColor,
                    modifier = Modifier.size(UiConstants.SmallIconSize)
                )
            }

            Text(
                text = String.format(Locale.getDefault(), "%.1f puan", rating),
                style = MaterialTheme.typography.labelMedium,
                color = badgeTextColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SmallMetaPill(
    text: String
) {
    Box(
        modifier = Modifier
            .background(
                color = ReviewMetaPillBg,
                shape = CircleShape
            )
            .padding(
                horizontal = UiConstants.PillHorizontalPadding,
                vertical = UiConstants.SmallPillVerticalPadding
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = ReviewMutedColor
        )
    }
}

@Composable
private fun SmallMutedText(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = ReviewMutedColor
    )
}

@Composable
private fun RatingRow(
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
            color = ReviewMutedColor
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = ReviewTitleColor
        )
    }
}

private fun scoreText(rating: Double): String {
    return String.format(Locale.getDefault(), "%.1f/5", rating)
}
