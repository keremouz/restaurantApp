package com.example.restaurantapp.presentation.restaurant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.domain.model.Restaurant
import java.util.Locale

private val DetailBlue = Color(0xFF2F5BFF)
private val DetailBlueDark = Color(0xFF1E4AE9)
private val DetailBg = Color(0xFFF4F7FF)
private val DetailCardBg = Color.White
private val DetailTextPrimary = Color(0xFF162033)
private val DetailTextSecondary = Color(0xFF6F7C99)
private val DetailBorder = Color(0xFFD9E4FF)
private val EmptyStarColor = Color(0xFFB7C4E6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    onBackClick: () -> Unit,
    onRequireLogin: () -> Unit
) {
    val viewModel: RestaurantDetailViewModel = viewModel(
        factory = RestaurantDetailViewModelFactory(restaurant)
    )

    val uiState by viewModel.uiState.collectAsState()

    val fillReviewFieldsText = stringResource(R.string.fill_review_fields)
    val commentAddedText = stringResource(R.string.comment_added)
    val favoriteAddedText = stringResource(R.string.favorite_added)
    val favoriteRemovedText = stringResource(R.string.favorite_removed)

    LaunchedEffect(uiState.requireLogin) {
        if (uiState.requireLogin) {
            onRequireLogin()
            viewModel.clearRequireLogin()
        }
    }

    Scaffold(
        containerColor = DetailBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.write_review_title),
                        color = DetailTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.back),
                            tint = DetailBlue
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(
                                favoriteAddedText = favoriteAddedText,
                                favoriteRemovedText = favoriteRemovedText
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = stringResource(R.string.add_to_favorites),
                            tint = DetailBlue
                        )
                    }

                    TextButton(
                        onClick = {
                            viewModel.submitReview(
                                fillReviewFieldsText = fillReviewFieldsText,
                                commentAddedText = commentAddedText
                            )
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.submit),
                            color = DetailBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DetailBg,
                    titleContentColor = DetailTextPrimary,
                    navigationIconContentColor = DetailBlue,
                    actionIconContentColor = DetailBlue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = UiConstants.ScreenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DetailTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.size(UiConstants.SmallSpacing))

                Text(
                    text = uiState.averageRating?.let { rating ->
                        "${String.format(Locale.getDefault(), "%.1f", rating)} puan"
                    } ?: "-",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DetailBlue,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(UiConstants.ExtraSmallSpacing)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = DetailBlue,
                    modifier = Modifier.size(UiConstants.SmallIconSize)
                )

                Text(
                    text = restaurant.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = DetailTextSecondary
                )
            }

            uiState.message?.let { message ->
                Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

                Text(
                    text = message,
                    color = DetailBlueDark,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(UiConstants.LargeSpacing))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(UiConstants.CardRadius),
                colors = CardDefaults.cardColors(containerColor = DetailCardBg),
                border = BorderStroke(
                    width = UiConstants.ReviewCardBorderWidth,
                    color = DetailBorder
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = UiConstants.FavoriteCardElevation
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UiConstants.ScreenPadding)
                ) {
                    Text(
                        text = stringResource(R.string.rate_your_experience),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DetailBlueDark
                    )

                    Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

                    RatingSection(
                        title = stringResource(R.string.criterion_taste),
                        rating = uiState.tasteRating,
                        label = getTasteLabel(uiState.tasteRating),
                        onRatingChanged = viewModel::updateTasteRating
                    )

                    Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

                    RatingSection(
                        title = stringResource(R.string.criterion_service),
                        rating = uiState.serviceRating,
                        label = getDefaultRatingLabel(uiState.serviceRating),
                        onRatingChanged = viewModel::updateServiceRating
                    )

                    Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

                    RatingSection(
                        title = stringResource(R.string.criterion_price_performance),
                        rating = uiState.pricePerformanceRating,
                        label = getPricePerformanceLabel(uiState.pricePerformanceRating),
                        onRatingChanged = viewModel::updatePricePerformanceRating
                    )

                    Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

                    RatingSection(
                        title = stringResource(R.string.criterion_atmosphere),
                        rating = uiState.atmosphereRating,
                        label = getAtmosphereLabel(uiState.atmosphereRating),
                        onRatingChanged = viewModel::updateAtmosphereRating
                    )

                    Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

                    RatingSection(
                        title = stringResource(R.string.criterion_location),
                        rating = uiState.locationRating,
                        label = getLocationLabel(uiState.locationRating),
                        onRatingChanged = viewModel::updateLocationRating
                    )
                }
            }

            Spacer(modifier = Modifier.height(UiConstants.LargeSpacing))

            Text(
                text = stringResource(R.string.comment_label),
                style = MaterialTheme.typography.bodyMedium,
                color = DetailBlueDark,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

            OutlinedTextField(
                value = uiState.commentText,
                onValueChange = viewModel::updateCommentText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(UiConstants.ReviewCommentFieldHeight),
                placeholder = {
                    Text(
                        text = stringResource(R.string.comment_hint),
                        color = Color(0xFFAEB8D3)
                    )
                },
                minLines = UiConstants.COMMENT_FIELD_MIN_LINES,
                maxLines = UiConstants.COMMENT_FIELD_MIN_LINES + 3,
                shape = RoundedCornerShape(UiConstants.TextFieldRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DetailBlue,
                    unfocusedBorderColor = DetailBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = DetailBlue,
                    focusedLabelColor = DetailBlue,
                    unfocusedLabelColor = DetailTextSecondary
                )
            )

            Spacer(modifier = Modifier.height(UiConstants.LargeSpacing))
        }
    }
}

@Composable
private fun RatingSection(
    title: String,
    rating: Int,
    label: String,
    onRatingChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = DetailTextSecondary
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = DetailBlue,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

        Row(
            horizontalArrangement = Arrangement.spacedBy(UiConstants.ExtraSmallSpacing)
        ) {
            (1..5).forEach { index ->
                Icon(
                    imageVector = if (index <= rating) {
                        Icons.Filled.Star
                    } else {
                        Icons.Outlined.Star
                    },
                    contentDescription = null,
                    tint = if (index <= rating) {
                        DetailBlue
                    } else {
                        EmptyStarColor
                    },
                    modifier = Modifier
                        .size(UiConstants.ReviewStarSize)
                        .clickable { onRatingChanged(index) }
                )
            }
        }
    }
}

@Composable
private fun getDefaultRatingLabel(rating: Int): String {
    return when (rating) {
        1 -> stringResource(R.string.rating_bad)
        2 -> stringResource(R.string.rating_average)
        3 -> stringResource(R.string.rating_good)
        4 -> stringResource(R.string.rating_very_good)
        5 -> stringResource(R.string.rating_excellent)
        else -> stringResource(R.string.rating_select)
    }
}

@Composable
private fun getTasteLabel(rating: Int): String {
    return getDefaultRatingLabel(rating)
}

@Composable
private fun getPricePerformanceLabel(rating: Int): String {
    return when (rating) {
        1 -> stringResource(R.string.rating_weak)
        2 -> stringResource(R.string.rating_okay)
        3 -> stringResource(R.string.rating_reasonable)
        4 -> stringResource(R.string.rating_good)
        5 -> stringResource(R.string.rating_very_good)
        else -> stringResource(R.string.rating_select)
    }
}

@Composable
private fun getAtmosphereLabel(rating: Int): String {
    return when (rating) {
        1 -> stringResource(R.string.rating_weak)
        2 -> stringResource(R.string.rating_simple)
        3 -> stringResource(R.string.rating_good)
        4 -> stringResource(R.string.rating_nice)
        5 -> stringResource(R.string.rating_chic)
        else -> stringResource(R.string.rating_select)
    }
}

@Composable
private fun getLocationLabel(rating: Int): String {
    return when (rating) {
        1 -> stringResource(R.string.rating_far)
        2 -> stringResource(R.string.rating_average)
        3 -> stringResource(R.string.rating_good)
        4 -> stringResource(R.string.rating_easy)
        5 -> stringResource(R.string.rating_central)
        else -> stringResource(R.string.rating_select)
    }
}