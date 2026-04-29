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

        allRestaurants.addAll(getNearbyRestaurantResults())
        allRestaurants.addAll(getTextSearchRestaurantResults())

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
            LatLngDto(latitude = 40.8917, longitude = 29.1850),  // Kartal / Maltepe
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
                    .map { it.toDomain() }
            )
        }

        return restaurants
    }

    private suspend fun getTextSearchRestaurantResults(): List<Restaurant> {
        val searchQueries = listOf(
            "İstanbul dönerci",
            "İstanbul balık restoranı",
            "İstanbul burgerci",
            "İstanbul kebapçı",
            "İstanbul pideci",
            "İstanbul lahmacun",
            "İstanbul kahvaltı mekanı",
            "İstanbul tatlıcı",
            "İstanbul kafe",
            "İstanbul steakhouse",
            "İstanbul pizza restoranı",
            "İstanbul sushi restoranı",
            "İstanbul makarna restoranı",
            "İstanbul vegan restoran",
            "İstanbul esnaf lokantası",
            "İstanbul kokoreç",
            "İstanbul mantıcı",
            "İstanbul köfteci",
            "İstanbul çorbacı",
            "İstanbul meyhane"
        )

        val restaurants = mutableListOf<Restaurant>()

        searchQueries.forEach { query ->
            val request = PlacesTextSearchRequestDto(
                textQuery = query,
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
                    .map { it.toDomain() }
            )
        }

        return restaurants
    }

    private companion object {
        const val MAX_RESULT_COUNT = 20
        const val SEARCH_RADIUS = 8000.0

        const val PLACE_FIELD_MASK =
            "places.id,places.displayName,places.location,places.formattedAddress,places.rating"
    }
}