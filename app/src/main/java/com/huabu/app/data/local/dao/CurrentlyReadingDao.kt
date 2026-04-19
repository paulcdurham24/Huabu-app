package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.CurrentlyReading
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentlyReadingDao {
    @Query("SELECT * FROM currently_reading WHERE userId = :userId LIMIT 1")
    fun getForUser(userId: String): Flow<CurrentlyReading?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CurrentlyReading)

    @Query("DELETE FROM currently_reading WHERE userId = :userId")
    suspend fun delete(userId: String)
}
