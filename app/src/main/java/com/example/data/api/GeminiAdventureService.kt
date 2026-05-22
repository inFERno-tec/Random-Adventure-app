package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.models.Place
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = "application/json"
)

@JsonClass(generateAdapter = true)
data class GeminiRequestPayload(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPartResponse(
    val text: String?
)

@JsonClass(generateAdapter = true)
data class GeminiContentResponse(
    val parts: List<GeminiPartResponse>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContentResponse?
)

@JsonClass(generateAdapter = true)
data class GeminiResponsePayload(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class PlaceJson(
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
    val distance: Double
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequestPayload
    ): GeminiResponsePayload
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    val moshiInstance: Moshi = moshi
}

class GeminiAdventureService {

    suspend fun generateAdventures(
        locationName: String?,
        latitude: Double,
        longitude: Double,
        minBudget: Int,
        maxBudget: Int,
        enabledCategories: List<String>,
        maxDistanceKm: Float,
        minRating: Float,
        openNowOnly: Boolean
    ): List<Place> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiAdventureService", "API Key is missing!")
            throw IllegalStateException("API Key is missing in BuildConfig. Please configure GEMINI_API_KEY in the Secrets panel.")
        }

        val locationString = if (locationName != null && locationName.isNotBlank()) {
            "near the city/address: '$locationName'"
        } else {
            "within $maxDistanceKm km of GPS coordinates ($latitude, $longitude)"
        }

        val categoriesFilter = if (enabledCategories.isEmpty()) {
            "Restaurants & Cafes, Events & Activities, Outdoors & Nature, Entertainment, Shopping, Arts & Culture"
        } else {
            enabledCategories.joinToString(", ")
        }

        val prompt = """
            Generate exactly 8 realistic, fun, and interesting places/things to do (adventures) matching these criteria:
            - Location: $locationString (Ensure the places are actual, real spots inside this region!)
            - Enabled Categories: $categoriesFilter  (ONLY include places that fit into these categories!)
            - Budget level (Price Level): $minBudget to $maxBudget (Price Level: 0 is Free, 1 is $, 2 is $$, 3 is $$$, 4 is $$$$)
            - Minimum Rating: $minRating (out of 5.0)
            - Open Now Toggle: $openNowOnly (if true, favor locations usually open now)
            
            Each place MUST have an illustrative photoUrl from unsplash. Use Unsplash Source/Search keywords related to the place type to build stable, clean image links, formatted as: https://images.unsplash.com/photo-[random_image_id]?auto=format&fit=crop&w=500&q=80 (example random ids: 1528605248601-3f6c8c441dfa for restaurants, 1470071459604-3b5ec3a7fe05 for parks, 1500835597721-e338f3f2ffd8 for entertainment, 1441986300917-64674bd600d8 for shopping).
            
            You MUST return a JSON array of objects representing these adventures with this exact keys and structure:
            [
              {
                "placeId": "unique_string_id",
                "name": "Name of Place",
                "address": "Full physical Street Address, City, State/Country",
                "latitude": double, 
                "longitude": double,
                "rating": double_value_between_3.0_and_5.0,
                "priceLevel": int_value_between_0_and_4,
                "categories": ["CategoryNameFromEnabled"],
                "photoUrl": "https://images.unsplash.com/... (must be a valid Unsplash photo URL)",
                "isOpenNow": boolean,
                "openingHours": ["Mon-Fri: 9:00 AM - 10:00 PM", "Sat-Sun: 10:00 AM - 11:00 PM"],
                "phone": "+1-xxx-xxx-xxxx",
                "website": "https://example.com/someplace",
                "distance": double_distance_in_km_relative_to_input_location
              }
            ]
            
            Do NOT include any markdown code blocks, backticks, or wrapping. Return ONLY the raw valid JSON array.
        """.trimIndent()

        val request = GeminiRequestPayload(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.6f,
                responseMimeType = "application/json"
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are a professional, accurate local adventure directory engine. Generate only factual-feeling, interesting places to do. Return strictly formatted valid JSON matching the schema requested. No conversational text."))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Empty response text from Gemini API")

            // Clean markdown blocks if any exist
            val cleanJson = cleanJsonString(rawText)

            val listType = Types.newParameterizedType(List::class.java, PlaceJson::class.java)
            val adapter = RetrofitClient.moshiInstance.adapter<List<PlaceJson>>(listType)
            val placeJsons = adapter.fromJson(cleanJson) ?: emptyList()

            return placeJsons.map { p ->
                Place(
                    placeId = p.placeId,
                    name = p.name,
                    address = p.address,
                    latitude = p.latitude,
                    longitude = p.longitude,
                    rating = p.rating,
                    priceLevel = p.priceLevel,
                    categories = p.categories,
                    photoUrl = p.photoUrl,
                    isOpenNow = p.isOpenNow,
                    openingHours = p.openingHours,
                    phone = p.phone,
                    website = p.website,
                    distance = p.distance
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiAdventureService", "Error calling Gemini: ${e.message}", e)
            throw e
        }
    }

    private fun cleanJsonString(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```")) {
            text = text.substringAfter("```")
            if (text.startsWith("json", ignoreCase = true)) {
                text = text.substring(4)
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length - 3)
            }
        }
        return text.trim()
    }
}
