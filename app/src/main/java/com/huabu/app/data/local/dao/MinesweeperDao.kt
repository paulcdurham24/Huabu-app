package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.MinesweeperGame
import com.huabu.app.data.model.GameStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MinesweeperDao {
    @Query("SELECT * FROM minesweeper_games WHERE hostId = :userId OR opponentId = :userId ORDER BY createdAt DESC")
    fun getGamesForUser(userId: String): Flow<List<MinesweeperGame>>

    @Query("SELECT * FROM minesweeper_games WHERE status = 'ACTIVE' AND (hostId = :userId OR opponentId = :userId)")
    fun getActiveGames(userId: String): Flow<List<MinesweeperGame>>

    @Query("SELECT * FROM minesweeper_games WHERE id = :gameId LIMIT 1")
    suspend fun getGameById(gameId: String): MinesweeperGame?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGame(game: MinesweeperGame)

    @Query("UPDATE minesweeper_games SET opponentId = :opponentId, opponentName = :opponentName, status = 'ACTIVE', startedAt = :now WHERE id = :gameId")
    suspend fun joinGame(gameId: String, opponentId: String, opponentName: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM minesweeper_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)
}
