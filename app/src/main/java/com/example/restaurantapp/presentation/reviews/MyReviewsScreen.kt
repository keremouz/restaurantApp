package com.example.restaurantapp.presentation.reviews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.UserComment
import com.example.restaurantapp.presentation.components.LottieLoadingContent
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign


private val ReviewBg = Color.White
private val ReviewCardBg = Color(0xFFFFFFFF)
private val ReviewCardBorder = Color(0xFFE1E6F5)
private val ReviewTitleColor = Color(0xFF123A9F)
private val ReviewBodyColor = Color(0xFF3F4A66)
private val ReviewMutedColor = Color(0xFF7A8299)
private val ReviewDividerColor = Color(0xFFE7EAF3)
private val ReviewBlue = Color(0xFF244ED8)
private val ReviewBlueSoft = Color(0xFFEAF0FF)
private val ReviewGold = Color(0xFF9B6B00)
private val ReviewGoldSoft = Color(0xFFFFF1C2)
private val ReviewMetaPillBg = Color(0xFFF1F4FA)
private val ReviewDanger = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    onBackClick: () -> Unit
) {
    val viewModel: MyReviewsViewModel = viewModel(
        factory = MyReviewsViewModelFactory()
    )

    val uiState by viewModel.uiState.collectAsState()
    val sortedReviews = viewModel.getSortedReviews()

    Scaffold(
        containerColor = ReviewBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.my_reviews),
                        color = ReviewTitleColor,
                        fontWeight = FontWeight.SemiBold
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LottieLoadingContent(
                        animationRes = R.raw.restaurant_loading,
                        text = stringResource(R.string.loading_reviews)
                    )
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(UiConstants.ScreenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ReviewTitleColor
                    )
                }
            }

            uiState.reviews.isEmpty() -> {
                EmptyReviewsContent(
                    modifier = Modifier.padding(paddingValues)
                )
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
                        ReviewsHeader(
                            reviewCount = sortedReviews.size,
                            selectedSort = uiState.selectedSort,
                            filterExpanded = uiState.filterExpanded,
                            onFilterClick = viewModel::onFilterClick,
                            onDismissFilter = viewModel::onDismissFilter,
                            onSortSelected = viewModel::onSortSelected
                        )
                    }

                    items(
                        items = sortedReviews,
                        key = { it.commentId }
                    ) { review ->
                        ReviewArchiveCard(
                            review = review,
                            onDeleteClick = {
                                viewModel.onDeleteClick(review)
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showDeleteSheet && uiState.selectedReview != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissDeleteSheet,
            containerColor = ReviewCardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UiConstants.BottomSheetPadding),
                verticalArrangement = Arrangement.spacedBy(UiConstants.BottomSheetSpacing)
            ) {
                Text(
                    text = stringResource(R.string.delete_review_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ReviewTitleColor
                )

                Text(
                    text = stringResource(R.string.delete_review_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReviewBodyColor
                )

                Button(
                    onClick = viewModel::deleteSelectedReview,
                    colors = ButtonDefaults.buttonColors(containerColor = ReviewDanger),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(UiConstants.ButtonRadius)
                ) {
                    Text(stringResource(R.string.delete))
                }

                OutlinedButton(
                    onClick = viewModel::dismissDeleteSheet,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(UiConstants.ButtonRadius)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
private fun ReviewsHeader(
    reviewCount: Int,
    selectedSort: ReviewSortType,
    filterExpanded: Boolean,
    onFilterClick: () -> Unit,
    onDismissFilter: () -> Unit,
    onSortSelected: (ReviewSortType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = UiConstants.SmallSpacing),
        verticalArrangement = Arrangement.spacedBy(UiConstants.TinySpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(UiConstants.TinySpacing)
            ) {
                Text(
                    text = stringResource(R.string.reviews_header_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ReviewTitleColor
                )

                Text(
                    text = stringResource(R.string.reviews_header_subtitle, reviewCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReviewMutedColor
                )
            }

            Box(contentAlignment = Alignment.TopEnd) {
                Text(
                    text = stringResource(R.string.sort_prefix, selectedSort.label),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReviewBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onFilterClick() }
                        .padding(
                            horizontal = UiConstants.PillHorizontalPadding,
                            vertical = UiConstants.SmallPillVerticalPadding
                        )
                )

                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = onDismissFilter,
                    containerColor = ReviewCardBg
                ) {
                    ReviewSortType.values().forEach { sortType ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = sortType.label,
                                    color = if (selectedSort == sortType) {
                                        ReviewTitleColor
                                    } else {
                                        ReviewBodyColor
                                    },
                                    fontWeight = if (selectedSort == sortType) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            },
                            onClick = { onSortSelected(sortType) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewArchiveCard(
    review: UserComment,
    onDeleteClick: () -> Unit
) {
    val isExpanded = rememberSaveable(review.commentId) {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded.value = !isExpanded.value },
        shape = RoundedCornerShape(UiConstants.CardRadius),
        colors = CardDefaults.cardColors(containerColor = ReviewCardBg),
        border = BorderStroke(
            width = UiConstants.ReviewCardBorderWidth,
            color = ReviewCardBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = UiConstants.ReviewCardElevation
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

                    SmallMutedText(text = review.district.ifBlank { "-" })
                }

                Column(
                    modifier = Modifier.padding(top = UiConstants.TinySpacing),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
                ) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(UiConstants.ReviewDeleteIconButtonSize)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = ReviewDanger,
                            modifier = Modifier.size(UiConstants.ReviewDeleteIconSize)
                        )
                    }

                    RatingBadge(rating = review.generalRating)
                }
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
                    text = if (isExpanded.value) {
                        stringResource(R.string.hide_details)
                    } else {
                        stringResource(R.string.details)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = ReviewBlue,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(UiConstants.ExtraSmallSpacing))

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = ReviewBlue,
                    modifier = Modifier.rotate(if (isExpanded.value) 90f else 0f)
                )
            }

            AnimatedVisibility(visible = isExpanded.value) {
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
                text = stringResource(R.string.rating_point, rating),
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
@Composable
private fun EmptyReviewsContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = UiConstants.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_empty_favorites),
            contentDescription = null,
            modifier = Modifier.size(UiConstants.EmptyFavoritesImageSize),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.size(UiConstants.ContentSpacing))

        Text(
            text = stringResource(R.string.my_reviews_empty),
            style = MaterialTheme.typography.titleMedium,
            color = ReviewTitleColor,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.size(UiConstants.SmallSpacing))

        Text(
            text = stringResource(R.string.my_reviews_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = ReviewMutedColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}