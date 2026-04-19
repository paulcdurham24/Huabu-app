package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.SpotifyTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotifyTrackDao {
    @Query("SELECT * FROM spotify_tracks WHERE userId = :userId LIMIT 1")
    fun getCurrentTrack(userId: String): Flow<SpotifyTrack?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrack(track: SpotifyTrack)

    @Query("DELETE FROM spotify_tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)
}
