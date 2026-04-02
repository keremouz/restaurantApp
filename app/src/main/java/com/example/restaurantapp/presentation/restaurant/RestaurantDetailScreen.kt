package com.example.restaurantapp.presentation.restaurant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.CommentRatings
import com.example.restaurantapp.data.firebase.CommentsManager
import com.example.restaurantapp.data.firebase.FavoritesManager
import com.example.restaurantapp.domain.model.Restaurant
import com.google.firebase.auth.FirebaseAuth

private val DetailBlue = Color(0xFF3D4BFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    onBackClick: () -> Unit,
    onRequireLogin: () -> Unit
) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val favoritesManager = remember { FavoritesManager() }
    val commentsManager = remember { CommentsManager() }

    val currentUser = firebaseAuth.currentUser

    var message by remember { mutableStateOf<String?>(null) }
    var commentText by remember { mutableStateOf("") }

    var tasteText by remember { mutableStateOf("") }
    var serviceText by remember { mutableStateOf("") }
    var pricePerformanceText by remember { mutableStateOf("") }
    var atmosphereText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }

    val favoriteAddedText = stringResource(R.string.favorite_added)
    val fillReviewFieldsText = stringResource(R.string.fill_review_fields)
    val commentAddedText = stringResource(R.string.comment_added)
    val ratingPlaceholderText = stringResource(R.string.rating_placeholder)

    fun parseRating(value: String): Int? {
        val number = value.toIntOrNull()
        return if (number != null && number in 1..5) number else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.restaurant_detail_title)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UiConstants.ScreenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(UiConstants.ContentSpacing)
        ) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(text = restaurant.address)

            message?.let {
                Text(
                    text = it,
                    color = DetailBlue
                )
            }

            Button(
                onClick = {
                    if (currentUser == null) {
                        onRequireLogin()
                    } else {
                        favoritesManager.addFavorite(
                            restaurant = restaurant,
                            onSuccess = {
                                message = favoriteAddedText
                            },
                            onError = { error ->
                                message = error
                            }
                        )
                    }
                },
                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                colors = ButtonDefaults.buttonColors(containerColor = DetailBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.add_to_favorites))
            }

            Text(
                text = stringResource(R.string.review_criteria_title),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
            ) {
                SmallRatingField(
                    value = tasteText,
                    onValueChange = { tasteText = it },
                    label = stringResource(R.string.criterion_taste),
                    placeholder = ratingPlaceholderText,
                    modifier = Modifier.weight(1f)
                )

                SmallRatingField(
                    value = serviceText,
                    onValueChange = { serviceText = it },
                    label = stringResource(R.string.criterion_service),
                    placeholder = ratingPlaceholderText,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
            ) {
                SmallRatingField(
                    value = pricePerformanceText,
                    onValueChange = { pricePerformanceText = it },
                    label = stringResource(R.string.criterion_price_performance),
                    placeholder = ratingPlaceholderText,
                    modifier = Modifier.weight(1f)
                )

                SmallRatingField(
                    value = atmosphereText,
                    onValueChange = { atmosphereText = it },
                    label = stringResource(R.string.criterion_atmosphere),
                    placeholder = ratingPlaceholderText,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
            ) {
                SmallRatingField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = stringResource(R.string.criterion_location),
                    placeholder = ratingPlaceholderText,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text(stringResource(R.string.comment_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = UiConstants.CommentFieldMinLines,
                maxLines = UiConstants.CommentFieldMinLines + 2
            )

            OutlinedButton(
                onClick = {
                    if (currentUser == null) {
                        onRequireLogin()
                    } else {
                        val taste = parseRating(tasteText)
                        val service = parseRating(serviceText)
                        val pricePerformance = parseRating(pricePerformanceText)
                        val atmosphere = parseRating(atmosphereText)
                        val location = parseRating(locationText)

                        if (
                            commentText.isBlank() ||
                            taste == null ||
                            service == null ||
                            pricePerformance == null ||
                            atmosphere == null ||
                            location == null
                        ) {
                            message = fillReviewFieldsText
                        } else {
                            val ratings = CommentRatings(
                                taste = taste,
                                service = service,
                                pricePerformance = pricePerformance,
                                atmosphere = atmosphere,
                                location = location
                            )

                            commentsManager.addComment(
                                restaurantId = restaurant.placeId,
                                restaurantName = restaurant.name,
                                comment = commentText.trim(),
                                ratings = ratings,
                                onSuccess = {
                                    message = commentAddedText
                                    commentText = ""
                                    tasteText = ""
                                    serviceText = ""
                                    pricePerformanceText = ""
                                    atmosphereText = ""
                                    locationText = ""
                                },
                                onError = { error ->
                                    message = error
                                }
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.submit_comment))
            }

            OutlinedButton(
                onClick = onBackClick,
                shape = RoundedCornerShape(UiConstants.ButtonRadius),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.go_back))
            }
        }
    }
}

@Composable
private fun SmallRatingField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = TextStyle(fontSize = 12.sp)
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(fontSize = 12.sp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.height(UiConstants.SmallFieldHeight),
        singleLine = true
    )
}