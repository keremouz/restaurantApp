package com.example.restaurantapp.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.restaurantapp.R
import com.example.restaurantapp.core.util.UiConstants
import com.example.restaurantapp.data.firebase.FavoriteRestaurant
import com.example.restaurantapp.data.firebase.FavoritesManager
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.presentation.components.ConnectionWarningContent
import com.example.restaurantapp.presentation.components.LottieLoadingContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

private val FavoritesBg = Color.White
private val FavoriteItemBg = Color(0xFFEFF4FF)
private val FavoriteCardBorder = Color(0xFFD6E2FF)
private val FavoriteBlue = Color(0xFF244ED8)
private val TitleBlue = Color(0xFF0B2F86)
private val SoftBlue = Color(0xFF66789E)

private data class FavoriteRatingInfo(
    val myRating: Double? = null,
    val generalRating: Double? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    isConnected: Boolean,
    onRestaurantClick: (Restaurant) -> Unit
) {
    val favoritesManager = remember { FavoritesManager() }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val lifecycleOwner = LocalLifecycleOwner.current

    val favorites = remember { mutableStateListOf<FavoriteRestaurant>() }

    var currentUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth.currentUser) }
    var favoriteRatings by remember { mutableStateOf<Map<String, FavoriteRatingInfo>>(emptyMap()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }

    var isFavoritesLoading by remember { mutableStateOf(false) }
    var hasLoadedFavorites by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            refreshKey++
        }

        firebaseAuth.addAuthStateListener(listener)

        onDispose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isConnected, currentUser?.uid, refreshKey) {
        if (!isConnected) {
            favorites.clear()
            favoriteRatings = emptyMap()
            errorMessage = null
            isFavoritesLoading = false
            hasLoadedFavorites = false
            return@LaunchedEffect
        }

        val user = currentUser

        if (user == null) {
            favorites.clear()
            favoriteRatings = emptyMap()
            errorMessage = null
            isFavoritesLoading = false
            hasLoadedFavorites = false
            return@LaunchedEffect
        }

        isFavoritesLoading = true
        hasLoadedFavorites = false

        favoritesManager.getFavorites(
            onSuccess = { list ->
                favorites.clear()
                favorites.addAll(list)
                errorMessage = null

                if (list.isEmpty()) {
                    favoriteRatings = emptyMap()
                    isFavoritesLoading = false
                    hasLoadedFavorites = true
                    return@getFavorites
                }

                loadFavoriteRatings(
                    firestore = firestore,
                    currentUserId = user.uid,
                    favorites = list,
                    onLoaded = { ratings ->
                        favoriteRatings = ratings
                        isFavoritesLoading = false
                        hasLoadedFavorites = true
                    }
                )
            },
            onError = { error ->
                favorites.clear()
                favoriteRatings = emptyMap()
                errorMessage = error
                isFavoritesLoading = false
                hasLoadedFavorites = true
            }
        )
    }

    Scaffold(
        containerColor = FavoritesBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FavoritesBg,
                    scrolledContainerColor = FavoritesBg
                ),
                title = {
                    Text(
                        text = stringResource(R.string.favorites_title),
                        color = TitleBlue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { paddingValues ->
        when {
            !isConnected -> {
                ConnectionWarningContent(
                    innerPadding = PaddingValues(),
                    contentPadding = paddingValues
                )
            }

            currentUser == null -> {
                FavoritesLoginRequiredContent(
                    modifier = Modifier.padding(paddingValues)
                )
            }

            isFavoritesLoading || !hasLoadedFavorites -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LottieLoadingContent(
                        animationRes = R.raw.restaurant_loading,
                        text = stringResource(R.string.loading_favorites)
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
                        color = TitleBlue,
                        textAlign = TextAlign.Center
                    )
                }
            }

            favorites.isEmpty() -> {
                EmptyFavoritesContent(
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                FavoritesContent(
                    favorites = favorites,
                    ratings = favoriteRatings,
                    modifier = Modifier.padding(paddingValues),
                    onRestaurantClick = onRestaurantClick
                )
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    favorites: List<FavoriteRestaurant>,
    ratings: Map<String, FavoriteRatingInfo>,
    modifier: Modifier = Modifier,
    onRestaurantClick: (Restaurant) -> Unit
) {
    val sortedFavorites = favorites.sortedBy { it.name.lowercase() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FavoritesBg)
            .padding(horizontal = UiConstants.ScreenPadding),
        contentPadding = PaddingValues(
            top = UiConstants.ContentSpacing,
            bottom = UiConstants.ContentSpacing
        ),
        verticalArrangement = Arrangement.spacedBy(UiConstants.MediumSpacing)
    ) {
        items(sortedFavorites) { favorite ->
            val ratingInfo = ratings[favorite.placeId]
                ?: ratings[favorite.name]
                ?: FavoriteRatingInfo()

            FavoriteItemCard(
                favorite = favorite,
                ratingInfo = ratingInfo,
                onClick = {
                    onRestaurantClick(
                        Restaurant(
                            placeId = favorite.placeId,
                            name = favorite.name,
                            latitude = favorite.latitude,
                            longitude = favorite.longitude,
                            address = favorite.address,
                            district = null,
                            rating = null
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun FavoriteItemCard(
    favorite: FavoriteRestaurant,
    ratingInfo: FavoriteRatingInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(UiConstants.FavoriteCardRadius),
        colors = CardDefaults.cardColors(
            containerColor = FavoriteItemBg
        ),
        border = BorderStroke(
            width = UiConstants.FavoriteCardBorderWidth,
            color = FavoriteCardBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = UiConstants.FavoriteCardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = UiConstants.ContentSpacing,
                    vertical = UiConstants.ContentSpacing
                )
        ) {
            Text(
                text = favorite.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TitleBlue,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(UiConstants.SmallSpacing))

            Text(
                text = favorite.address,
                style = MaterialTheme.typography.bodySmall,
                color = SoftBlue,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(UiConstants.ContentSpacing))

            HorizontalDivider(
                color = FavoriteCardBorder
            )

            Spacer(modifier = Modifier.height(UiConstants.MediumSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingInfoBlock(
                    title = "Benim Puanım",
                    value = formatRating(ratingInfo.myRating),
                    modifier = Modifier.weight(1f)
                )

                RatingInfoBlock(
                    title = "Genel Puan",
                    value = formatRating(ratingInfo.generalRating),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RatingInfoBlock(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(UiConstants.ExtraSmallSpacing)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = SoftBlue,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = FavoriteBlue,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FavoritesLoginRequiredContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = UiConstants.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_empty_favorites),
            contentDescription = stringResource(R.string.empty_favorites_image_desc),
            modifier = Modifier.size(UiConstants.EmptyFavoritesImageSize),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.size(UiConstants.ContentSpacing))

        Text(
            text = stringResource(R.string.favorites_login_required_title),
            style = MaterialTheme.typography.titleMedium,
            color = TitleBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(UiConstants.SmallSpacing))

        Text(
            text = stringResource(R.string.favorites_login_required_description),
            style = MaterialTheme.typography.bodyMedium,
            color = SoftBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

@Composable
private fun EmptyFavoritesContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = UiConstants.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_empty_favorites),
            contentDescription = stringResource(R.string.empty_favorites_image_desc),
            modifier = Modifier.size(UiConstants.EmptyFavoritesImageSize),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.size(UiConstants.ContentSpacing))

        Text(
            text = stringResource(R.string.empty_favorites_title),
            style = MaterialTheme.typography.titleMedium,
            color = TitleBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(UiConstants.SmallSpacing))

        Text(
            text = stringResource(R.string.empty_favorites_description),
            style = MaterialTheme.typography.bodyMedium,
            color = SoftBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

private fun loadFavoriteRatings(
    firestore: FirebaseFirestore,
    currentUserId: String,
    favorites: List<FavoriteRestaurant>,
    onLoaded: (Map<String, FavoriteRatingInfo>) -> Unit
) {
    if (favorites.isEmpty()) {
        onLoaded(emptyMap())
        return
    }

    firestore.collection("comments")
        .get()
        .addOnSuccessListener { documents ->
            val comments = documents.documents
            val result = mutableMapOf<String, FavoriteRatingInfo>()

            favorites.forEach { favorite ->
                val matchedComments = comments.filter { document ->
                    val commentRestaurantId = document.getString("restaurantId").orEmpty()
                    val commentRestaurantName = document.getString("restaurantName").orEmpty()

                    val favoritePlaceId = favorite.placeId
                    val favoriteName = favorite.name

                    commentRestaurantId == favoritePlaceId ||
                            normalizeText(commentRestaurantName) == normalizeText(favoriteName) ||
                            normalizeText(commentRestaurantName).contains(normalizeText(favoriteName)) ||
                            normalizeText(favoriteName).contains(normalizeText(commentRestaurantName))
                }

                val myRatings = matchedComments
                    .filter { document ->
                        document.getString("userId") == currentUserId
                    }
                    .mapNotNull { document ->
                        extractRatingFromComment(document)
                    }

                val myRating = if (myRatings.isNotEmpty()) {
                    myRatings.average()
                } else {
                    null
                }

                val allRatings = matchedComments.mapNotNull { document ->
                    extractRatingFromComment(document)
                }

                val generalRating = if (allRatings.isNotEmpty()) {
                    allRatings.average()
                } else {
                    null
                }

                val ratingInfo = FavoriteRatingInfo(
                    myRating = myRating,
                    generalRating = generalRating
                )

                result[favorite.placeId] = ratingInfo
                result[favorite.name] = ratingInfo
            }

            onLoaded(result)
        }
        .addOnFailureListener {
            onLoaded(emptyMap())
        }
}

private fun extractRatingFromComment(
    document: DocumentSnapshot
): Double? {
    document.getDouble("generalRating")?.let { return it }
    document.getDouble("rating")?.let { return it }

    val ratingsMap = document.get("ratings") as? Map<*, *>
    val ratingValues = ratingsMap
        ?.values
        ?.mapNotNull { value ->
            when (value) {
                is Number -> value.toDouble()
                else -> null
            }
        }
        .orEmpty()

    return if (ratingValues.isNotEmpty()) {
        ratingValues.average()
    } else {
        null
    }
}

private fun formatRating(value: Double?): String {
    return value?.let {
        String.format(Locale.getDefault(), "%.1f", it)
    } ?: "-"
}

private fun normalizeText(value: String): String {
    return value
        .lowercase()
        .replace("ı", "i")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ş", "s")
        .replace("ö", "o")
        .replace("ç", "c")
        .replace(Regex("\\s+"), " ")
        .trim()
}