package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Place(
    val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    val priceLevel: Int, // 0 to 4
    val categories: List<String>,
    val photoUrl: String,
    val isOpenNow: Boolean,
    val openingHours: List<String>,
    val phone: String,
    val website: String,
    val distance: Double // calculated in km/meters relative to current location
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val placeId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    val priceLevel: Int,
    val categories: List<String>,
    val photoUrl: String,
    val isOpenNow: Boolean,
    val openingHours: List<String>,
    val phone: String,
    val website: String,
    val distance: Double,
    val note: String = "",
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toPlace() = Place(
        placeId = placeId,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        rating = rating,
        priceLevel = priceLevel,
        categories = categories,
        photoUrl = photoUrl,
        isOpenNow = isOpenNow,
        openingHours = openingHours,
        phone = phone,
        website = website,
        distance = distance
    )

    companion object {
        fun fromPlace(place: Place, note: String = "") = Favorite(
            placeId = place.placeId,
            name = place.name,
            address = place.address,
            latitude = place.latitude,
            longitude = place.longitude,
            rating = place.rating,
            priceLevel = place.priceLevel,
            categories = place.categories,
            photoUrl = place.photoUrl,
            isOpenNow = place.isOpenNow,
            openingHours = place.openingHours,
            phone = place.phone,
            website = place.website,
            distance = place.distance,
            note = note
        )
    }
}

@Entity(tableName = "location_history")
data class LocationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserPreferences(
    val latitude: Double,
    val longitude: Double,
    val manualLocationName: String? = null,
    val minBudget: Int = 0,
    val maxBudget: Int = 4,
    val enabledCategories: List<String> = emptyList(),
    val maxDistanceKm: Float = 10f,
    val minRating: Float = 3.0f,
    val openNowOnly: Boolean = false
)
