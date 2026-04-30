package com.example.restaurantapp.data.repository

import com.example.restaurantapp.data.remote.api.PlacesApiService
import com.example.restaurantapp.data.remote.dto.CircleDto
import com.example.restaurantapp.data.remote.dto.LatLngDto
import com.example.restaurantapp.data.remote.dto.LocationRestrictionDto
import com.example.restaurantapp.data.remote.dto.NearbySearchRequestDto
import com.example.restaurantapp.data.remote.dto.PlacesTextSearchRequestDto
import com.example.restaurantapp.data.remote.mapper.toDomain
import com.example.restaurantapp.domain.model.Restaurant
import com.example.restaurantapp.domain.repository.RestaurantRepository

class RestaurantRepositoryImpl(
    private val placesApiService: PlacesApiService,
    private val apiKey: String
) : RestaurantRepository {

    override suspend fun getNearbyRestaurants(): List<Restaurant> {
        val allRestaurants = mutableListOf<Restaurant>()

        allRestaurants.addAll(getTextSearchRestaurantResults())
        allRestaurants.addAll(getNearbyRestaurantResults())

        return allRestaurants
            .filter { restaurant ->
                restaurant.placeId.isNotBlank() &&
                        restaurant.name.isNotBlank() &&
                        restaurant.latitude != 0.0 &&
                        restaurant.longitude != 0.0
            }
            .distinctBy { it.placeId }
    }

    private suspend fun getNearbyRestaurantResults(): List<Restaurant> {
        val centers = listOf(
            LatLngDto(latitude = 41.0082, longitude = 28.9784), // Fatih / Eminönü
            LatLngDto(latitude = 41.0422, longitude = 29.0083), // Beşiktaş / Şişli
            LatLngDto(latitude = 41.0670, longitude = 28.9850), // Kağıthane
            LatLngDto(latitude = 41.0220, longitude = 28.8750), // Bakırköy
            LatLngDto(latitude = 41.0370, longitude = 28.6770), // Beylikdüzü
            LatLngDto(latitude = 41.0560, longitude = 28.9130), // Bayrampaşa / Eyüp
            LatLngDto(latitude = 40.9917, longitude = 29.0277), // Kadıköy
            LatLngDto(latitude = 41.0278, longitude = 29.0152), // Üsküdar
            LatLngDto(latitude = 41.0244, longitude = 29.1244), // Ümraniye
            LatLngDto(latitude = 40.9869, longitude = 29.1244), // Ataşehir
            LatLngDto(latitude = 40.8787, longitude = 29.2347), // Pendik
            LatLngDto(latitude = 40.8917, longitude = 29.1850), // Maltepe
            LatLngDto(latitude = 41.1663, longitude = 29.0500), // Sarıyer
            LatLngDto(latitude = 41.1239, longitude = 29.1083), // Beykoz
            LatLngDto(latitude = 40.8170, longitude = 29.3000), // Tuzla
            LatLngDto(latitude = 41.1072, longitude = 28.8000), // Başakşehir
            LatLngDto(latitude = 41.1843, longitude = 28.7400), // Arnavutköy
            LatLngDto(latitude = 41.0333, longitude = 29.1753), // Çekmeköy
            LatLngDto(latitude = 40.9700, longitude = 29.2600), // Sultanbeyli
            LatLngDto(latitude = 41.0030, longitude = 28.5370), // Silivri
            LatLngDto(latitude = 41.0200, longitude = 28.5850), // Büyükçekmece
            LatLngDto(latitude = 41.1456, longitude = 28.4610)  // Çatalca
        )

        val restaurants = mutableListOf<Restaurant>()

        centers.forEach { center ->
            val request = NearbySearchRequestDto(
                includedTypes = listOf("restaurant"),
                maxResultCount = MAX_RESULT_COUNT,
                locationRestriction = LocationRestrictionDto(
                    circle = CircleDto(
                        center = center,
                        radius = SEARCH_RADIUS
                    )
                )
            )

            val response = placesApiService.searchNearbyRestaurants(
                apiKey = apiKey,
                fieldMask = PLACE_FIELD_MASK,
                request = request
            )

            restaurants.addAll(
                response.places
                    .orEmpty()
                    .map { place ->
                        place.toDomain().copy(category = CATEGORY_RESTAURANT)
                    }
            )
        }

        return restaurants
    }

    private suspend fun getTextSearchRestaurantResults(): List<Restaurant> {
        val searchQueries = listOf(
            SearchQuery("İstanbul dönerci", CATEGORY_DONER),
            SearchQuery("İstanbul balık restoranı", CATEGORY_FISH),
            SearchQuery("İstanbul burgerci", CATEGORY_BURGER),
            SearchQuery("İstanbul kebapçı", CATEGORY_KEBAB),
            SearchQuery("İstanbul pideci", CATEGORY_PIDE),
            SearchQuery("İstanbul lahmacun", CATEGORY_PIDE),
            SearchQuery("İstanbul kahvaltı mekanı", CATEGORY_BREAKFAST),
            SearchQuery("İstanbul tatlıcı", CATEGORY_DESSERT),
            SearchQuery("İstanbul kafe", CATEGORY_CAFE),
            SearchQuery("İstanbul steakhouse", CATEGORY_STEAK),
            SearchQuery("İstanbul pizza restoranı", CATEGORY_PIZZA),
            SearchQuery("İstanbul sushi restoranı", CATEGORY_SUSHI),
            SearchQuery("İstanbul makarna restoranı", CATEGORY_PASTA),
            SearchQuery("İstanbul esnaf lokantası", CATEGORY_RESTAURANT),
            SearchQuery("İstanbul kokoreç", CATEGORY_STREET_FOOD),
            SearchQuery("İstanbul mantıcı", CATEGORY_LOCAL),
            SearchQuery("İstanbul köfteci", CATEGORY_KOFTE),
            SearchQuery("İstanbul çorbacı", CATEGORY_SOUP),
            SearchQuery("İstanbul meyhane", CATEGORY_MEYHANE)
        )

        val restaurants = mutableListOf<Restaurant>()

        searchQueries.forEach { query ->
            val request = PlacesTextSearchRequestDto(
                textQuery = query.text,
                languageCode = "tr",
                maxResultCount = MAX_RESULT_COUNT
            )

            val response = placesApiService.searchTextRestaurants(
                apiKey = apiKey,
                fieldMask = PLACE_FIELD_MASK,
                request = request
            )

            restaurants.addAll(
                response.places
                    .orEmpty()
                    .map { place ->
                        place.toDomain().copy(category = query.category)
                    }
            )
        }

        return restaurants
    }

    private data class SearchQuery(
        val text: String,
        val category: String
    )

    private companion object {
        const val MAX_RESULT_COUNT = 20
        const val SEARCH_RADIUS = 8000.0

        const val CATEGORY_RESTAURANT = "restaurant"
        const val CATEGORY_DONER = "doner"
        const val CATEGORY_FISH = "fish"
        const val CATEGORY_BURGER = "burger"
        const val CATEGORY_KEBAB = "kebab"
        const val CATEGORY_PIDE = "pide"
        const val CATEGORY_BREAKFAST = "breakfast"
        const val CATEGORY_DESSERT = "dessert"
        const val CATEGORY_CAFE = "cafe"
        const val CATEGORY_STEAK = "steak"
        const val CATEGORY_PIZZA = "pizza"
        const val CATEGORY_SUSHI = "sushi"
        const val CATEGORY_PASTA = "pasta"
        const val CATEGORY_STREET_FOOD = "street_food"
        const val CATEGORY_LOCAL = "local"
        const val CATEGORY_KOFTE = "kofte"
        const val CATEGORY_SOUP = "soup"
        const val CATEGORY_MEYHANE = "meyhane"

        const val PLACE_FIELD_MASK =
            "places.id,places.displayName,places.location,places.formattedAddress,places.rating"
    }
}