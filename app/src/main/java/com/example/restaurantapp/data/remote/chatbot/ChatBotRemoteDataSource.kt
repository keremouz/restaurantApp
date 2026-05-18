package com.example.restaurantapp.data.remote.chatbot

import com.example.restaurantapp.core.location.AppLocationHolder
import com.example.restaurantapp.domain.model.Restaurant
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ChatBotRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val model = Firebase
        .ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    private var cachedReviewSummaries: Map<String, RestaurantReviewSummary>? = null
    private var cachedReviewTime: Long = 0L

    suspend fun sendMessage(
        message: String,
        restaurants: List<Restaurant>
    ): String {
        val reviewSummaries = getReviewSummariesFromFirestoreCached()

        val restaurantContext = getRestaurantContextFromRestaurants(
            userMessage = message,
            restaurants = restaurants,
            reviewSummaries = reviewSummaries
        )

        val userLocationContext = getUserLocationContext()

        val prompt = """
Sen RestaurantApp içinde çalışan Türkçe restoran öneri asistanısın.

Kurallar:
Sadece UYGULAMA_VERISI içindeki restoranlara göre cevap ver.
Google/API puanını kullanma, sadece uygulama içi puanları kullan.
Cevap kısa ve net olsun.
Markdown kullanma.
En fazla 3 restoran öner.
Restoranları numaralı liste şeklinde yaz.
Kullanıcı yakın restoran isterse KULLANICI_KONUMU bilgisini dikkate al.
Puan sorarsa uygulama genel puanına göre cevap ver.
Lezzet, servis, fiyat/performans, ortam veya konum puanı sorarsa ilgili kriteri kullan.
Puanı verilmemiş restoranları puan önerisine dahil etme.

Cevap formatı:
1. Restoran adı
Adres: ...
Uygulama genel puanı: ...
Yorum sayısı: ...

KULLANICI_KONUMU:
$userLocationContext

UYGULAMA_VERISI:
$restaurantContext

KULLANICI_MESAJI:
$message
""".trimIndent()

        val response = model.generateContent(prompt)

        return response.text ?: "Şu an cevap oluşturamadım."
    }

    private fun getUserLocationContext(): String {
        val location = AppLocationHolder.userLocation

        return if (location == null) {
            "Konum bilgisi yok."
        } else {
            "latitude=${location.latitude}, longitude=${location.longitude}"
        }
    }

    private fun getRestaurantContextFromRestaurants(
        userMessage: String,
        restaurants: List<Restaurant>,
        reviewSummaries: Map<String, RestaurantReviewSummary>
    ): String {
        if (restaurants.isEmpty()) {
            return "Uygulamada restoran listesi bulunamadı."
        }

        val lowerMessage = userMessage.lowercase(Locale.getDefault())
        val userLocation = AppLocationHolder.userLocation

        val wantsReviewBasedResult =
            lowerMessage.contains("puan") ||
                    lowerMessage.contains("lezzet") ||
                    lowerMessage.contains("servis") ||
                    lowerMessage.contains("fiyat") ||
                    lowerMessage.contains("performans") ||
                    lowerMessage.contains("ortam") ||
                    lowerMessage.contains("konum puanı") ||
                    lowerMessage.contains("en iyi") ||
                    lowerMessage.contains("iyi")

        val wantsNearby =
            lowerMessage.contains("yakın") ||
                    lowerMessage.contains("yakınımda") ||
                    lowerMessage.contains("bana yakın") ||
                    lowerMessage.contains("en yakın")

        val filteredRestaurants = restaurants.filter { restaurant ->
            val reviewSummary = findReviewSummary(
                restaurant = restaurant,
                reviewSummaries = reviewSummaries
            )

            val restaurantText = buildString {
                append(restaurant.name.lowercase(Locale.getDefault()))
                append(" ")
                append(restaurant.address.lowercase(Locale.getDefault()))
                append(" ")
                append(restaurant.district?.lowercase(Locale.getDefault()).orEmpty())
                append(" ")
                append(restaurant.category.lowercase(Locale.getDefault()))
            }

            val districtMatches = districtMatches(
                lowerMessage = lowerMessage,
                restaurantText = restaurantText
            )

            val typeMatches = typeMatches(
                lowerMessage = lowerMessage,
                restaurantText = restaurantText,
                category = restaurant.category
            )

            val hasApplicationReview =
                !wantsReviewBasedResult ||
                        (reviewSummary != null && reviewSummary.commentCount > 0)

            districtMatches && typeMatches && hasApplicationReview
        }

        val sortedRestaurants = when {
            wantsNearby && userLocation != null -> {
                filteredRestaurants.sortedBy { restaurant ->
                    calculateDistanceKm(
                        firstLat = userLocation.latitude,
                        firstLng = userLocation.longitude,
                        secondLat = restaurant.latitude,
                        secondLng = restaurant.longitude
                    )
                }
            }

            wantsReviewBasedResult -> {
                filteredRestaurants.sortedByDescending { restaurant ->
                    findReviewSummary(
                        restaurant = restaurant,
                        reviewSummaries = reviewSummaries
                    )?.generalRatingValue ?: 0.0
                }
            }

            else -> filteredRestaurants
        }

        val restaurantsToSend = sortedRestaurants
            .ifEmpty { restaurants }
            .take(10)

        return restaurantsToSend.joinToString(separator = "\n") { restaurant ->
            val reviewSummary = findReviewSummary(
                restaurant = restaurant,
                reviewSummaries = reviewSummaries
            )

            val distanceText = if (userLocation != null) {
                val distance = calculateDistanceKm(
                    firstLat = userLocation.latitude,
                    firstLng = userLocation.longitude,
                    secondLat = restaurant.latitude,
                    secondLng = restaurant.longitude
                )

                String.format(Locale.getDefault(), "%.1f km", distance)
            } else {
                "Yok"
            }

            """
Restoran: ${restaurant.name}
Adres: ${restaurant.address}
İlçe: ${restaurant.district ?: "Verilmemiş"}
Kategori: ${restaurant.category}
Mesafe: $distanceText
Yorum Sayısı: ${reviewSummary?.commentCount ?: 0}
Genel Puan: ${reviewSummary?.generalRatingText ?: "Verilmemiş"}
Lezzet: ${reviewSummary?.tasteRatingText ?: "Verilmemiş"}
Servis: ${reviewSummary?.serviceRatingText ?: "Verilmemiş"}
Fiyat/Performans: ${reviewSummary?.pricePerformanceRatingText ?: "Verilmemiş"}
Ortam: ${reviewSummary?.atmosphereRatingText ?: "Verilmemiş"}
Konum Puanı: ${reviewSummary?.locationRatingText ?: "Verilmemiş"}
""".trimIndent()
        }
    }

    private suspend fun getReviewSummariesFromFirestoreCached(): Map<String, RestaurantReviewSummary> {
        val now = System.currentTimeMillis()
        val cached = cachedReviewSummaries

        if (cached != null && now - cachedReviewTime < REVIEW_CACHE_DURATION_MS) {
            return cached
        }

        val freshData = getReviewSummariesFromFirestore()

        cachedReviewSummaries = freshData
        cachedReviewTime = now

        return freshData
    }

    private suspend fun getReviewSummariesFromFirestore(): Map<String, RestaurantReviewSummary> {
        val documents = firestore.collection("comments")
            .get()
            .await()

        if (documents.isEmpty) {
            return emptyMap()
        }

        val groupedDocuments = documents.documents.groupBy { document ->
            val restaurantId = document.getString("restaurantId").orEmpty()
            val restaurantName = document.getString("restaurantName").orEmpty()

            if (restaurantId.isNotBlank()) {
                restaurantId
            } else {
                restaurantName.lowercase(Locale.getDefault())
            }
        }

        return groupedDocuments.mapValues { (_, commentDocuments) ->
            val generalRatings = commentDocuments.mapNotNull { document ->
                document.getDouble("generalRating")
            }

            val tasteRatings = commentDocuments.mapNotNull { document ->
                getNestedRating(document.get("ratings"), "taste")
            }

            val serviceRatings = commentDocuments.mapNotNull { document ->
                getNestedRating(document.get("ratings"), "service")
            }

            val pricePerformanceRatings = commentDocuments.mapNotNull { document ->
                getNestedRating(document.get("ratings"), "pricePerformance")
            }

            val atmosphereRatings = commentDocuments.mapNotNull { document ->
                getNestedRating(document.get("ratings"), "atmosphere")
            }

            val locationRatings = commentDocuments.mapNotNull { document ->
                getNestedRating(document.get("ratings"), "location")
            }

            RestaurantReviewSummary(
                commentCount = commentDocuments.size,
                generalRatingValue = generalRatings.averageOrZero(),
                generalRatingText = formatRatingOrMissing(generalRatings),
                tasteRatingText = formatRatingOrMissing(tasteRatings),
                serviceRatingText = formatRatingOrMissing(serviceRatings),
                pricePerformanceRatingText = formatRatingOrMissing(pricePerformanceRatings),
                atmosphereRatingText = formatRatingOrMissing(atmosphereRatings),
                locationRatingText = formatRatingOrMissing(locationRatings)
            )
        }
    }

    private fun districtMatches(
        lowerMessage: String,
        restaurantText: String
    ): Boolean {
        return when {
            lowerMessage.contains("kadıköy") || lowerMessage.contains("kadikoy") ->
                restaurantText.contains("kadıköy") || restaurantText.contains("kadikoy")

            lowerMessage.contains("ataşehir") || lowerMessage.contains("atasehir") ->
                restaurantText.contains("ataşehir") || restaurantText.contains("atasehir")

            lowerMessage.contains("beşiktaş") || lowerMessage.contains("besiktas") ->
                restaurantText.contains("beşiktaş") || restaurantText.contains("besiktas")

            lowerMessage.contains("fatih") ->
                restaurantText.contains("fatih")

            lowerMessage.contains("üsküdar") || lowerMessage.contains("uskudar") ->
                restaurantText.contains("üsküdar") || restaurantText.contains("uskudar")

            else -> true
        }
    }

    private fun typeMatches(
        lowerMessage: String,
        restaurantText: String,
        category: String
    ): Boolean {
        return when {
            lowerMessage.contains("döner") || lowerMessage.contains("doner") || lowerMessage.contains("dönerci") ->
                restaurantText.contains("döner") ||
                        restaurantText.contains("doner") ||
                        category == "doner"

            lowerMessage.contains("kebap") || lowerMessage.contains("kebapçı") || lowerMessage.contains("kebapci") ->
                restaurantText.contains("kebap") ||
                        category == "kebab"

            lowerMessage.contains("balık") || lowerMessage.contains("balik") || lowerMessage.contains("balıkçı") || lowerMessage.contains("balikci") ->
                restaurantText.contains("balık") ||
                        restaurantText.contains("balik") ||
                        category == "fish"

            lowerMessage.contains("burger") ->
                restaurantText.contains("burger") ||
                        category == "burger"

            lowerMessage.contains("pide") || lowerMessage.contains("pideci") ->
                restaurantText.contains("pide") ||
                        category == "pide"

            lowerMessage.contains("kahvaltı") || lowerMessage.contains("kahvalti") ->
                restaurantText.contains("kahvaltı") ||
                        restaurantText.contains("kahvalti") ||
                        category == "breakfast"

            lowerMessage.contains("tatlı") || lowerMessage.contains("tatli") || lowerMessage.contains("tatlıcı") || lowerMessage.contains("tatlici") ->
                restaurantText.contains("tatlı") ||
                        restaurantText.contains("tatli") ||
                        category == "dessert"

            lowerMessage.contains("kafe") || lowerMessage.contains("cafe") ->
                restaurantText.contains("kafe") ||
                        restaurantText.contains("cafe") ||
                        category == "cafe"

            lowerMessage.contains("steak") || lowerMessage.contains("etçi") || lowerMessage.contains("etci") ->
                restaurantText.contains("steak") ||
                        category == "steak"

            lowerMessage.contains("pizza") || lowerMessage.contains("pizzacı") || lowerMessage.contains("pizzaci") ->
                restaurantText.contains("pizza") ||
                        category == "pizza"

            lowerMessage.contains("sushi") ->
                restaurantText.contains("sushi") ||
                        category == "sushi"

            lowerMessage.contains("meyhane") || lowerMessage.contains("meyhaneci") ->
                restaurantText.contains("meyhane") ||
                        category == "meyhane"

            else -> true
        }
    }

    private fun findReviewSummary(
        restaurant: Restaurant,
        reviewSummaries: Map<String, RestaurantReviewSummary>
    ): RestaurantReviewSummary? {
        return reviewSummaries[restaurant.placeId]
            ?: reviewSummaries[restaurant.name.lowercase(Locale.getDefault())]
    }

    private fun getNestedRating(
        ratingsObject: Any?,
        key: String
    ): Double? {
        val ratingsMap = ratingsObject as? Map<*, *> ?: return null

        return when (val value = ratingsMap[key]) {
            is Number -> value.toDouble()
            else -> null
        }
    }

    private fun formatRatingOrMissing(
        ratings: List<Double>
    ): String {
        if (ratings.isEmpty()) {
            return "Verilmemiş"
        }

        return String.format(
            Locale.getDefault(),
            "%.1f",
            ratings.average()
        )
    }

    private fun List<Double>.averageOrZero(): Double {
        return if (isEmpty()) 0.0 else average()
    }

    private fun calculateDistanceKm(
        firstLat: Double,
        firstLng: Double,
        secondLat: Double,
        secondLng: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(secondLat - firstLat)
        val dLng = Math.toRadians(secondLng - firstLng)

        val lat1 = Math.toRadians(firstLat)
        val lat2 = Math.toRadians(secondLat)

        val a = sin(dLat / 2).pow(2.0) +
                sin(dLng / 2).pow(2.0) *
                cos(lat1) *
                cos(lat2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }

    private data class RestaurantReviewSummary(
        val commentCount: Int,
        val generalRatingValue: Double,
        val generalRatingText: String,
        val tasteRatingText: String,
        val serviceRatingText: String,
        val pricePerformanceRatingText: String,
        val atmosphereRatingText: String,
        val locationRatingText: String
    )

    private companion object {
        const val REVIEW_CACHE_DURATION_MS = 5 * 60 * 1000L
    }
}