package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.VisitedPlace
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitedPlaceDao {
    @Query("SELECT * FROM visited_places WHERE userId = :userId ORDER BY visitedAt DESC")
    fun getVisitedPlaces(userId: String): Flow<List<VisitedPlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlace(place: VisitedPlace)

    @Query("DELETE FROM visited_places WHERE id = :placeId")
    suspend fun deletePlace(placeId: String)
}
