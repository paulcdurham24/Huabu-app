package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.GameStats
import com.huabu.app.data.model.DailyScore
import com.huabu.app.data.model.GameType
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatsDao {
    @Query("SELECT * FROM game_stats WHERE userId = :userId ORDER BY lastPlayed DESC")
    fun getAllGameStats(userId: String): Flow<List<GameStats>>

    @Query("SELECT * FROM game_stats WHERE userId = :userId AND gameType = :gameType LIMIT 1")
    fun getStatsForGame(userId: String, gameType: GameType): Flow<GameStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: GameStats)

    @Query("SELECT * FROM daily_scores WHERE userId = :userId AND date = :date ORDER BY playedAt DESC")
    fun getDailyScores(userId: String, date: String): Flow<List<DailyScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordDailyScore(score: DailyScore)
}
