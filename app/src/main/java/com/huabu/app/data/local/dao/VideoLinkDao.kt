package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.VideoLink
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoLinkDao {
    @Query("SELECT * FROM video_links WHERE userId = :userId ORDER BY sortOrder ASC")
    fun getVideoLinksForUser(userId: String): Flow<List<VideoLink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoLink(videoLink: VideoLink)

    @Update
    suspend fun updateVideoLink(videoLink: VideoLink)

    @Delete
    suspend fun deleteVideoLink(videoLink: VideoLink)
}
