package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.TicTacToeGame
import com.huabu.app.data.model.GameStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TicTacToeDao {
    @Query("SELECT * FROM tictactoe_games WHERE playerXId = :userId OR playerOId = :userId ORDER BY lastMoveAt DESC")
    fun getGamesForUser(userId: String): Flow<List<TicTacToeGame>>

    @Query("SELECT * FROM tictactoe_games WHERE status = 'ACTIVE' AND (playerXId = :userId OR playerOId = :userId)")
    fun getActiveGames(userId: String): Flow<List<TicTacToeGame>>

    @Query("SELECT * FROM tictactoe_games WHERE id = :gameId LIMIT 1")
    suspend fun getGameById(gameId: String): TicTacToeGame?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGame(game: TicTacToeGame)

    @Query("UPDATE tictactoe_games SET status = :status WHERE id = :gameId")
    suspend fun updateStatus(gameId: String, status: GameStatus)

    @Query("DELETE FROM tictactoe_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)
}
