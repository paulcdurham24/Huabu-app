package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.TravelWish
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelWishDao {
    @Query("SELECT * FROM travel_wishes WHERE userId = :userId ORDER BY priority ASC, sortOrder ASC")
    fun getTravelWishes(userId: String): Flow<List<TravelWish>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWish(wish: TravelWish)

    @Query("DELETE FROM travel_wishes WHERE id = :wishId")
    suspend fun deleteWish(wishId: String)
}
