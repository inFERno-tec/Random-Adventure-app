package com.example.data.database

import android.content.Context
import androidx.room.*
import com.example.data.models.Favorite
import com.example.data.models.LocationHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE placeId = :placeId)")
    fun isFavorite(placeId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE placeId = :placeId LIMIT 1")
    suspend fun getFavoriteById(placeId: String): Favorite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE placeId = :placeId")
    suspend fun deleteFavoriteById(placeId: String)

    @Query("UPDATE favorites SET note = :note WHERE placeId = :placeId")
    suspend fun updateFavoriteNote(placeId: String, note: String)
}

@Dao
interface LocationHistoryDao {
    @Query("SELECT * FROM location_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<LocationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: LocationHistory)

    @Query("DELETE FROM location_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM location_history")
    suspend fun clearHistory()
}

@Database(entities = [Favorite::class, LocationHistory::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AdventureDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun locationHistoryDao(): LocationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AdventureDatabase? = null

        fun getDatabase(context: Context): AdventureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AdventureDatabase::class.java,
                    "adventure_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
