package com.example.restaurantapp.data.remote.chatbot

import com.example.restaurantapp.core.location.AppLocationHolder
import com.example.restaurantapp.domain.model.Restaurant
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Locale

class ChatBotRemoteDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val model = Firebase
        .ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    suspend fun sendMessage(
        message: String,
        restaurants: List<Restaurant>
    ): String {
        val reviewSummaries = getReviewSummariesFromFirestore()
        val restaurantContext = getRestaurantContextFromRestaurants(
            userMessage = message,
            restaurants = restaurants,
            reviewSummaries = reviewSummaries
        )
        val userLocationContext = getUserLocationContext()

        val prompt = """
    Sen RestaurantApp içinde çalışan bir restoran öneri asistanısın.

    CEVAP KURALLARI:
    - Sadece aşağıdaki UYGULAMA_VERISI bölümündeki restoranlara göre cevap ver.
    - Haritadaki restoran bilgileri latitude, longitude, adres, ilçe ve kategori için ana kaynaktır.
    - Uygulama içi puanlar ve yorum sayısı Firestore yorumlarından gelir.
    - API/Google puanını asla kullanma.
    - Uygulama Genel Puanı, kullanıcıların uygulama içinde verdiği puanların ortalamasıdır.

    KONUM KURALLARI:
    - Kullanıcı "yakınımda", "bana yakın", "en yakın", "konumuma yakın" gibi bir şey sorarsa KULLANICI_KONUMU bilgisini dikkate al.
    - En yakın restoranı seçerken kullanıcının latitude/longitude bilgisi ile restoranların latitude/longitude bilgisini karşılaştır.
    - Eğer kullanıcının konumu uygulamada yoksa "Konum bilgisi alınamadığı için yakındaki restoranları sıralayamıyorum." de.
    - Eğer hiçbir restoranın latitude ve longitude bilgisi yoksa "Restoranların konum bilgisi uygulamada verilmemiş." de.

    PUAN VE KRİTER KURALLARI:
    - Kullanıcı "genel puan", "puanı yüksek", "en iyi", "yüksek puanlı" gibi bir şey sorarsa sadece Uygulama Genel Puanı değerine göre sıralama yap.
    - Kullanıcı "lezzet" derse sadece Lezzet puanına göre sıralama yap ve cevapta Lezzet puanını mutlaka yaz.
    - Kullanıcı "servis" derse sadece Servis puanına göre sıralama yap ve cevapta Servis puanını mutlaka yaz.
    - Kullanıcı "fiyat performans", "fiyat/performans", "fiyat-performans" veya "uygun" derse sadece Fiyat/Performans puanına göre sıralama yap ve cevapta Fiyat/Performans puanını mutlaka yaz.
    - Kullanıcı "ortam", "ambiyans" veya "mekan" derse sadece Ortam puanına göre sıralama yap ve cevapta Ortam puanını mutlaka yaz.
    - Kullanıcı "konum puanı" derse sadece Konum Puanı değerine göre sıralama yap ve cevapta Konum Puanı değerini mutlaka yaz.
    - Puanı "Verilmemiş" olan restoranları ilgili puan/kriter sıralamasına dahil etme.
    - Yorum Sayısı 0 olan restoranları puan/kriter önerilerinde kullanma.
    - Eğer istenen türde veya ilçede uygulama içinde puanlanmış restoran yoksa "Bu tür için uygulama içinde puanlanmış restoran bulunamadı." de.

    FİLTRE KURALLARI:
    - Kullanıcı dönerci, kebapçı, burgerci, balıkçı, pizzacı, kafeci, meyhaneci, pideci, kahvaltıcı, tatlıcı, steak veya sushi gibi bir tür isterse restoran adı, adresi veya kategori bilgisine göre filtrele.
    - Kullanıcı ilçe belirtirse sadece o ilçeye uygun restoranları öner.
    - Uygulama verisinde olmayan restoran, ilçe veya mutfak türü hakkında tahmin yapma.
    - Eğer restoran listesi boşsa "Uygulamada restoran listesi bulunamadı." de.

    CEVAP FORMATI:
    - Cevap kısa, net ve Türkçe olsun.
    - Markdown kullanma. **, *, #, - gibi biçimlendirme karakterleri yazma.
    - Restoranları numaralı liste şeklinde yaz.
    - En fazla 3 restoran öner.
    - Kullanıcı özellikle daha fazla istemedikçe 3 restorandan fazla yazma.
    - Eğer uygun restoran sayısı 3'ten fazlaysa sadece en iyi 3 tanesini yaz.
    - "Bunlardan", "bunların içinden", "az önceki önerilerden" gibi ifadeler kullanılırsa önceki önerilerle aynı tür/kriter bağlamını koru ve yine en fazla 3 restoran yaz.
    - Her restoran için şu formatı kullan:

  1. Restoran adı
     Adres: ...
     Sorulan kriter puanı: ...
     Uygulama genel puanı: ...
     Yorum sayısı: ...

- Eğer kullanıcı genel puan sormuşsa "Sorulan kriter puanı" satırını yazma.
- Eğer kullanıcı lezzet, servis, fiyat/performans, ortam veya konum puanı sormuşsa "Sorulan kriter puanı" satırını ilgili kriter adıyla yaz.
- Örneğin kullanıcı lezzet sorduysa "Lezzet puanı: ..." yaz.
- Örneğin kullanıcı servis sorduysa "Servis puanı: ..." yaz.
- Örneğin kullanıcı fiyat performans sorduysa "Fiyat/Performans puanı: ..." yaz.

      1. Restoran adı
         Adres: ...
         Sorulan kriter puanı: ...
         Uygulama genel puanı: ...
         Yorum sayısı: ...

    - Eğer kullanıcı genel puan sormuşsa "Sorulan kriter puanı" satırını yazma.
    - Eğer kullanıcı lezzet, servis, fiyat/performans, ortam veya konum puanı sormuşsa "Sorulan kriter puanı" satırını ilgili kriter adıyla yaz.
    - Örneğin kullanıcı lezzet sorduysa "Lezzet puanı: ..." yaz.
    - Örneğin kullanıcı servis sorduysa "Servis puanı: ..." yaz.
    - Örneğin kullanıcı fiyat performans sorduysa "Fiyat/Performans puanı: ..." yaz.

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
            "Kullanıcının mevcut konumu uygulamada verilmemiş."
        } else {
            "Kullanıcının mevcut konumu: latitude=${location.latitude}, longitude=${location.longitude}"
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

            val districtMatches =
                when {
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

            val typeMatches =
                when {
                    lowerMessage.contains("döner") || lowerMessage.contains("doner") || lowerMessage.contains("dönerci") ->
                        restaurantText.contains("döner") ||
                                restaurantText.contains("doner") ||
                                restaurant.category == "doner"

                    lowerMessage.contains("kebap") || lowerMessage.contains("kebapçı") || lowerMessage.contains("kebapci") ->
                        restaurantText.contains("kebap") ||
                                restaurant.category == "kebab"

                    lowerMessage.contains("balık") || lowerMessage.contains("balik") || lowerMessage.contains("balıkçı") || lowerMessage.contains("balikci") ->
                        restaurantText.contains("balık") ||
                                restaurantText.contains("balik") ||
                                restaurant.category == "fish"

                    lowerMessage.contains("burger") ->
                        restaurantText.contains("burger") ||
                                restaurant.category == "burger"

                    lowerMessage.contains("pide") || lowerMessage.contains("pideci") ->
                        restaurantText.contains("pide") ||
                                restaurant.category == "pide"

                    lowerMessage.contains("kahvaltı") || lowerMessage.contains("kahvalti") ->
                        restaurantText.contains("kahvaltı") ||
                                restaurantText.contains("kahvalti") ||
                                restaurant.category == "breakfast"

                    lowerMessage.contains("tatlı") || lowerMessage.contains("tatli") || lowerMessage.contains("tatlıcı") || lowerMessage.contains("tatlici") ->
                        restaurantText.contains("tatlı") ||
                                restaurantText.contains("tatli") ||
                                restaurant.category == "dessert"

                    lowerMessage.contains("kafe") || lowerMessage.contains("cafe") ->
                        restaurantText.contains("kafe") ||
                                restaurantText.contains("cafe") ||
                                restaurant.category == "cafe"

                    lowerMessage.contains("steak") || lowerMessage.contains("etçi") || lowerMessage.contains("etci") ->
                        restaurantText.contains("steak") ||
                                restaurant.category == "steak"

                    lowerMessage.contains("pizza") || lowerMessage.contains("pizzacı") || lowerMessage.contains("pizzaci") ->
                        restaurantText.contains("pizza") ||
                                restaurant.category == "pizza"

                    lowerMessage.contains("sushi") ->
                        restaurantText.contains("sushi") ||
                                restaurant.category == "sushi"

                    lowerMessage.contains("meyhane") || lowerMessage.contains("meyhaneci") ->
                        restaurantText.contains("meyhane") ||
                                restaurant.category == "meyhane"

                    lowerMessage.contains("restoran") || lowerMessage.contains("restaurant") ->
                        restaurant.category == "restaurant" ||
                                restaurant.category == "all" ||
                                restaurantText.contains("restoran") ||
                                restaurantText.contains("restaurant")

                    else -> true
                }

            val hasApplicationReview =
                !wantsReviewBasedResult ||
                        (reviewSummary != null && reviewSummary.commentCount > 0)

            districtMatches && typeMatches && hasApplicationReview
        }

        val restaurantsToSend = filteredRestaurants
            .ifEmpty { restaurants }
            .take(50)

        return restaurantsToSend.joinToString(separator = "\n\n---\n\n") { restaurant ->
            val reviewSummary = findReviewSummary(
                restaurant = restaurant,
                reviewSummaries = reviewSummaries
            )

            """
        Restoran: ${restaurant.name}
        Adres: ${restaurant.address}
        İlçe: ${restaurant.district ?: "Verilmemiş"}
        Kategori: ${restaurant.category}
        Latitude: ${restaurant.latitude}
        Longitude: ${restaurant.longitude}
        Uygulama Yorum Sayısı: ${reviewSummary?.commentCount ?: 0}
        Uygulama Genel Puanı: ${reviewSummary?.generalRatingText ?: "Verilmemiş"}
        Lezzet: ${reviewSummary?.tasteRatingText ?: "Verilmemiş"}
        Servis: ${reviewSummary?.serviceRatingText ?: "Verilmemiş"}
        Fiyat/Performans: ${reviewSummary?.pricePerformanceRatingText ?: "Verilmemiş"}
        Ortam: ${reviewSummary?.atmosphereRatingText ?: "Verilmemiş"}
        Konum Puanı: ${reviewSummary?.locationRatingText ?: "Verilmemiş"}
        """.trimIndent()
        }
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

            val comments = commentDocuments.mapNotNull { document ->
                document.getString("comment")
                    ?.takeIf { it.isNotBlank() }
            }

            RestaurantReviewSummary(
                commentCount = commentDocuments.size,
                generalRatingText = formatRatingOrMissing(generalRatings),
                tasteRatingText = formatRatingOrMissing(tasteRatings),
                serviceRatingText = formatRatingOrMissing(serviceRatings),
                pricePerformanceRatingText = formatRatingOrMissing(pricePerformanceRatings),
                atmosphereRatingText = formatRatingOrMissing(atmosphereRatings),
                locationRatingText = formatRatingOrMissing(locationRatings),
                commentsText = comments.ifEmpty {
                    listOf("Yorum verilmemiş")
                }.joinToString(" | ")
            )
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

    private data class RestaurantReviewSummary(
        val commentCount: Int,
        val generalRatingText: String,
        val tasteRatingText: String,
        val serviceRatingText: String,
        val pricePerformanceRatingText: String,
        val atmosphereRatingText: String,
        val locationRatingText: String,
        val commentsText: String
    )
}