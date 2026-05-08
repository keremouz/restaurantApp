package com.example.restaurantapp.data.remote.chatbot

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

    suspend fun sendMessage(message: String): String {
        val restaurantContext = getRestaurantContextFromApp()

        val prompt = """
            Sen RestaurantApp içinde çalışan bir restoran öneri asistanısın.

            CEVAP KURALLARI:
            - Sadece aşağıdaki UYGULAMA_VERISI bölümündeki restoran yorumları ve puanlarına göre cevap ver.
            - Uygulama verisinde olmayan restoran, ilçe, mutfak türü veya kriter hakkında tahmin yapma.
            - Kullanıcı bir kriter sorarsa sadece uygulama içindeki kriterlere bak:
              Lezzet, Servis, Fiyat/Performans, Ortam, Konum, Genel Puan.
            - Eğer istenen bilgi uygulama verisinde yoksa açıkça şunu söyle:
              "Bu bilgi uygulamada verilmemiş."
            - Eğer yeterli veri yoksa kullanıcıya genel soru sorma; doğrudan hangi verinin eksik olduğunu söyle.
            - Cevap kısa, net ve Türkçe olsun.
            - Restoran önerirken mümkünse puanları da yaz.

            UYGULAMA_VERISI:
            $restaurantContext

            KULLANICI_MESAJI:
            $message
        """.trimIndent()

        val response = model.generateContent(prompt)

        return response.text ?: "Şu an cevap oluşturamadım."
    }

    private suspend fun getRestaurantContextFromApp(): String {
        val documents = firestore.collection("comments")
            .get()
            .await()

        if (documents.isEmpty) {
            return "Uygulamada henüz restoran yorumu veya puan verisi yok."
        }

        val restaurantMap = documents.documents.groupBy { document ->
            val restaurantId = document.getString("restaurantId").orEmpty()
            val restaurantName = document.getString("restaurantName").orEmpty()

            if (restaurantId.isNotBlank()) {
                restaurantId
            } else {
                restaurantName
            }
        }

        val restaurantSummaries = restaurantMap.values.mapNotNull { commentDocuments ->
            val firstDocument = commentDocuments.firstOrNull() ?: return@mapNotNull null

            val restaurantName = firstDocument.getString("restaurantName").orEmpty()
            val district = firstDocument.getString("district").orEmpty()

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

            """
            Restoran: ${restaurantName.ifBlank { "Ad bilgisi verilmemiş" }}
            İlçe: ${district.ifBlank { "Verilmemiş" }}
            Yorum sayısı: ${commentDocuments.size}
            Genel puan: ${formatRatingOrMissing(generalRatings)}
            Lezzet: ${formatRatingOrMissing(tasteRatings)}
            Servis: ${formatRatingOrMissing(serviceRatings)}
            Fiyat/Performans: ${formatRatingOrMissing(pricePerformanceRatings)}
            Ortam: ${formatRatingOrMissing(atmosphereRatings)}
            Konum: ${formatRatingOrMissing(locationRatings)}
            Kullanıcı yorumları: ${comments.ifEmpty { listOf("Yorum verilmemiş") }.joinToString(" | ")}
            """.trimIndent()
        }

        return if (restaurantSummaries.isEmpty()) {
            "Uygulamada henüz kullanılabilir restoran puan verisi yok."
        } else {
            restaurantSummaries.joinToString(separator = "\n\n---\n\n")
        }
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
}