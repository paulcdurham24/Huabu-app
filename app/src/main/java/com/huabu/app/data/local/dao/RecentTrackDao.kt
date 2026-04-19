package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.RecentTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentTrackDao {
    @Query("SELECT * FROM recent_tracks WHERE userId = :userId ORDER BY playedAt DESC LIMIT 5")
    fun getRecentTracks(userId: String): Flow<List<RecentTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: RecentTrack)

    @Query("DELETE FROM recent_tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)
}
