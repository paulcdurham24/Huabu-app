package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.MediaTrack
import com.huabu.app.data.model.MediaTrackType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaTrackDao {
    @Query("SELECT * FROM media_tracks WHERE userId = :userId AND type = :type ORDER BY rank ASC LIMIT 5")
    fun getTracksForUser(userId: String, type: MediaTrackType): Flow<List<MediaTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: MediaTrack)

    @Update
    suspend fun updateTrack(track: MediaTrack)

    @Delete
    suspend fun deleteTrack(track: MediaTrack)
}
