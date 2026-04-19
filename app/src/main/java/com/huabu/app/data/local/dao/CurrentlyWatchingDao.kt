package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.CurrentlyWatching
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentlyWatchingDao {
    @Query("SELECT * FROM currently_watching WHERE userId = :userId LIMIT 1")
    fun getForUser(userId: String): Flow<CurrentlyWatching?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CurrentlyWatching)

    @Query("DELETE FROM currently_watching WHERE userId = :userId")
    suspend fun delete(userId: String)
}
