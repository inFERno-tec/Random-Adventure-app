package com.example.data.repository

import android.location.Location
import android.util.Log
import com.example.BuildConfig
import com.example.data.database.FavoriteDao
import com.example.data.database.LocationHistoryDao
import com.example.data.api.GeminiAdventureService
import com.example.data.models.Favorite
import com.example.data.models.LocationHistory
import com.example.data.models.Place
import kotlinx.coroutines.flow.Flow
import java.util.Random

class AdventureRepository(
    private val favoriteDao: FavoriteDao,
    private val locationHistoryDao: LocationHistoryDao,
    private val geminiService: GeminiAdventureService = GeminiAdventureService()
) {
    val favorites: Flow<List<Favorite>> = favoriteDao.getAllFavorites()
    val locationHistory: Flow<List<LocationHistory>> = locationHistoryDao.getRecentHistory()

    fun isFavorite(placeId: String): Flow<Boolean> = favoriteDao.isFavorite(placeId)

    suspend fun getFavoriteById(placeId: String): Favorite? {
        return favoriteDao.getFavoriteById(placeId)
    }

    suspend fun addFavorite(place: Place, note: String = "") {
        favoriteDao.insertFavorite(Favorite.fromPlace(place, note))
    }

    suspend fun removeFavorite(placeId: String) {
        favoriteDao.deleteFavoriteById(placeId)
    }

    suspend fun updateNote(placeId: String, note: String) {
        favoriteDao.updateFavoriteNote(placeId, note)
    }

    suspend fun addLocationHistory(name: String, latitude: Double, longitude: Double) {
        locationHistoryDao.insertHistory(
            LocationHistory(
                name = name,
                latitude = latitude,
                longitude = longitude
            )
        )
    }

    suspend fun clearHistory() {
        locationHistoryDao.clearHistory()
    }

    suspend fun getAdventures(
        manualLocation: String?,
        latitude: Double,
        longitude: Double,
        minBudget: Int,
        maxBudget: Int,
        enabledCategories: List<String>,
        maxDistanceKm: Float,
        minRating: Float,
        openNowOnly: Boolean
    ): List<Place> {
        val hasKey = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

        if (hasKey) {
            try {
                return geminiService.generateAdventures(
                    locationName = manualLocation,
                    latitude = latitude,
                    longitude = longitude,
                    minBudget = minBudget,
                    maxBudget = maxBudget,
                    enabledCategories = enabledCategories,
                    maxDistanceKm = maxDistanceKm,
                    minRating = minRating,
                    openNowOnly = openNowOnly
                )
            } catch (e: Exception) {
                Log.e("AdventureRepository", "Gemini API call failed, falling back to local adventures", e)
            }
        }

        // Falling back to robust pre-built offline local adventures
        return getOfflineAdventures(
            manualLocation = manualLocation ?: "Current Location",
            latitude = latitude,
            longitude = longitude,
            minBudget = minBudget,
            maxBudget = maxBudget,
            enabledCategories = enabledCategories,
            maxDistanceKm = maxDistanceKm,
            minRating = minRating,
            openNowOnly = openNowOnly
        )
    }

    private fun getOfflineAdventures(
        manualLocation: String,
        latitude: Double,
        longitude: Double,
        minBudget: Int,
        maxBudget: Int,
        enabledCategories: List<String>,
        maxDistanceKm: Float,
        minRating: Float,
        openNowOnly: Boolean
    ): List<Place> {
        val categories = if (enabledCategories.isEmpty()) {
            listOf("Restaurants & Cafes", "Events & Activities", "Outdoors & Nature", "Entertainment", "Shopping", "Arts & Culture")
        } else {
            enabledCategories
        }

        val mockPool = listOf(
            // Restaurants & Cafes
            Place(
                placeId = "offline_rest_1",
                name = "The Wandering Cup",
                address = "1402 Sapphire Lane, $manualLocation",
                latitude = latitude + 0.0051,
                longitude = longitude - 0.0034,
                rating = 4.7,
                priceLevel = 1,
                categories = listOf("Restaurants & Cafes"),
                photoUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Mon-Sun: 7:00 AM - 6:00 PM"),
                phone = "+1 (555) 304-2041",
                website = "https://example.com/wanderingcup",
                distance = 0.6
            ),
            Place(
                placeId = "offline_rest_2",
                name = "Neon Lantern Sushi",
                address = "42 Electric Ave, $manualLocation",
                latitude = latitude - 0.0072,
                longitude = longitude + 0.0091,
                rating = 4.5,
                priceLevel = 2,
                categories = listOf("Restaurants & Cafes"),
                photoUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Daily: 12:00 PM - 10:00 PM"),
                phone = "+1 (555) 902-1834",
                website = "https://example.com/neonlantern",
                distance = 1.1
            ),
            Place(
                placeId = "offline_rest_3",
                name = "Chateau Truffle",
                address = "89 Velvet Blvd, $manualLocation",
                latitude = latitude + 0.012,
                longitude = longitude - 0.011,
                rating = 4.8,
                priceLevel = 4,
                categories = listOf("Restaurants & Cafes"),
                photoUrl = "https://images.unsplash.com/photo-1514933651103-005eec06c04b?auto=format&fit=crop&w=500&q=80",
                isOpenNow = false,
                openingHours = listOf("Tue-Sat: 5:00 PM - 11:30 PM"),
                phone = "+1 (555) 700-1928",
                website = "https://example.com/chateautruffle",
                distance = 1.8
            ),

            // Outdoors & Nature
            Place(
                placeId = "offline_nature_1",
                name = "Whispering Pines Observatory",
                address = "Summit Ridge Overlook, $manualLocation",
                latitude = latitude + 0.021,
                longitude = longitude + 0.018,
                rating = 4.9,
                priceLevel = 0,
                categories = listOf("Outdoors & Nature"),
                photoUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Opened 24 Hours"),
                phone = "No Phone",
                website = "https://example.com/whisperingpines",
                distance = 2.4
            ),
            Place(
                placeId = "offline_nature_2",
                name = "Botanical Maze & Fountains",
                address = "Scenic Valley Road, $manualLocation",
                latitude = latitude - 0.014,
                longitude = longitude - 0.008,
                rating = 4.6,
                priceLevel = 1,
                categories = listOf("Outdoors & Nature"),
                photoUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Mon-Sun: 8:00 AM - 8:00 PM"),
                phone = "+1 (555) 123-0941",
                website = "https://example.com/botanicalmaze",
                distance = 1.5
            ),

            // Events & Activities
            Place(
                placeId = "offline_event_1",
                name = "The Escape Portal",
                address = "22B Clockwork Lane, $manualLocation",
                latitude = latitude - 0.004,
                longitude = longitude + 0.003,
                rating = 4.4,
                priceLevel = 3,
                categories = listOf("Events & Activities"),
                photoUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Daily: 10:00 AM - Midnight"),
                phone = "+1 (555) 835-1029",
                website = "https://example.com/escapeportal",
                distance = 0.5
            ),
            Place(
                placeId = "offline_event_2",
                name = "Infinite Clay Crafting Studio",
                address = "509 Artisan Quarter, $manualLocation",
                latitude = latitude + 0.007,
                longitude = longitude - 0.006,
                rating = 4.8,
                priceLevel = 2,
                categories = listOf("Events & Activities"),
                photoUrl = "https://images.unsplash.com/photo-1565192647048-f997ded87958?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Wed-Sun: 10:00 AM - 8:00 PM"),
                phone = "+1 (555) 755-2211",
                website = "https://example.com/infiniteclay",
                distance = 0.9
            ),

            // Entertainment
            Place(
                placeId = "offline_ent_1",
                name = "Arcade Horizon",
                address = "808 Cyber Alley, $manualLocation",
                latitude = latitude - 0.002,
                longitude = longitude + 0.006,
                rating = 4.6,
                priceLevel = 1,
                categories = listOf("Entertainment"),
                photoUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Daily: 2:00 PM - 2:00 AM"),
                phone = "+1 (555) 888-0111",
                website = "https://example.com/arcadehorizon",
                distance = 0.7
            ),
            Place(
                placeId = "offline_ent_2",
                name = "Starlight Retro Cinema",
                address = "303 Broadway St, $manualLocation",
                latitude = latitude + 0.015,
                longitude = longitude + 0.003,
                rating = 4.7,
                priceLevel = 2,
                categories = listOf("Entertainment"),
                photoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Daily: 1:30 PM - Midnight"),
                phone = "+1 (555) 345-6789",
                website = "https://example.com/starlightretro",
                distance = 1.6
            ),

            // Shopping
            Place(
                placeId = "offline_shop_1",
                name = "Bazaar Curious & Antique",
                address = "12 Clock tower Square, $manualLocation",
                latitude = latitude - 0.009,
                longitude = longitude - 0.005,
                rating = 4.3,
                priceLevel = 2,
                categories = listOf("Shopping"),
                photoUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Mon-Sat: 10:00 AM - 7:00 PM"),
                phone = "+1 (555) 432-1111",
                website = "https://example.com/curiousbazaar",
                distance = 1.0
            ),
            Place(
                placeId = "offline_shop_2",
                name = "Golden Brick Thrift Co.",
                address = "85 Vintage Alley, $manualLocation",
                latitude = latitude - 0.001,
                longitude = longitude + 0.002,
                rating = 4.5,
                priceLevel = 1,
                categories = listOf("Shopping"),
                photoUrl = "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Daily: 11:00 AM - 6:00 PM"),
                phone = "+1 (555) 919-2041",
                website = "https://example.com/goldenbrickthrift",
                distance = 0.3
            ),

            // Arts & Culture
            Place(
                placeId = "offline_art_1",
                name = "Prism Modern Art Gallery",
                address = "77 Creative Boulevard, $manualLocation",
                latitude = latitude + 0.011,
                longitude = longitude - 0.004,
                rating = 4.7,
                priceLevel = 0, // Free!
                categories = listOf("Arts & Culture"),
                photoUrl = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Tue-Sun: 10:00 AM - 6:00 PM"),
                phone = "+1 (555) 750-2010",
                website = "https://example.com/prismart",
                distance = 1.2
            ),
            Place(
                placeId = "offline_art_2",
                name = "Stonegate Ampitheatre & Archaeology",
                address = "Historic Pillar Field, $manualLocation",
                latitude = latitude + 0.025,
                longitude = longitude - 0.015,
                rating = 4.6,
                priceLevel = 2,
                categories = listOf("Arts & Culture"),
                photoUrl = "https://images.unsplash.com/photo-1460881680858-30d872d5b530?auto=format&fit=crop&w=500&q=80",
                isOpenNow = true,
                openingHours = listOf("Daily: 9:00 AM - 5:00 PM"),
                phone = "+1 (555) 606-2020",
                website = "https://example.com/stonegatearch",
                distance = 2.8
            )
        )

        // Filter the mock pool based on criteria
        val rand = Random(latitude.toLong() + longitude.toLong() + minBudget.toLong() + maxBudget.toLong())
        val filtered = mockPool.filter { place ->
            val matchesCategory = categories.any { place.categories.contains(it) }
            val matchesBudget = place.priceLevel in minBudget..maxBudget
            val matchesRating = place.rating >= minRating
            val matchesOpen = !openNowOnly || place.isOpenNow

            matchesCategory && matchesBudget && matchesRating && matchesOpen
        }.toMutableList()

        // Shuffle with seed to give a pseudo-random yet stable explore listing for the position, or random if they generate new
        filtered.shuffle(rand)

        // Dynamically compute the distance in km based on simple coordinate math for premium feel
        val userLoc = Location("user").apply {
            this.latitude = latitude
            this.longitude = longitude
        }

        return filtered.map { place ->
            val placeLoc = Location("place").apply {
                this.latitude = place.latitude
                this.longitude = place.longitude
            }
            val calculatedDistanceKm = userLoc.distanceTo(placeLoc) / 1000.0
            val formattedDistance = Math.round(calculatedDistanceKm * 10.0) / 10.0

            place.copy(distance = formattedDistance)
        }.filter {
            it.distance <= maxDistanceKm
        }
    }
}
