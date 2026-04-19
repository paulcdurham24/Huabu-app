package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.LiveStream
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveStreamDao {
    @Query("SELECT * FROM live_streams WHERE userId = :userId")
    fun getLiveStreamForUser(userId: String): Flow<LiveStream?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLiveStream(stream: LiveStream)
}
