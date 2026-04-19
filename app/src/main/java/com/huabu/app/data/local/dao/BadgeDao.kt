package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.Badge
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges WHERE userId = :userId AND isVisible = 1 ORDER BY earnedAt DESC")
    fun getBadgesForUser(userId: String): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badge)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<Badge>)

    @Query("DELETE FROM badges WHERE id = :badgeId AND userId = :userId")
    suspend fun deleteBadge(badgeId: String, userId: String)
}
