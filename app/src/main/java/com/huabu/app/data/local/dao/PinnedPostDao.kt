package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.PinnedPost
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedPostDao {
    @Query("SELECT * FROM pinned_posts WHERE userId = :userId ORDER BY pinOrder ASC")
    fun getPinnedPostsForUser(userId: String): Flow<List<PinnedPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinPost(pinnedPost: PinnedPost)

    @Query("DELETE FROM pinned_posts WHERE postId = :postId AND userId = :userId")
    suspend fun unpinPost(postId: String, userId: String)

    @Query("SELECT COUNT(*) FROM pinned_posts WHERE userId = :userId")
    suspend fun getPinnedCount(userId: String): Int
}
