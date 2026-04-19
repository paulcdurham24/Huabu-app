package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GameType { WORDLE, CONNECTIONS, STRANDS, MINI_CROSSWORD, GEOGuessr, OTHER }

@Entity(tableName = "game_stats")
data class GameStats(
    @PrimaryKey val id: String,
    val userId: String,
    val gameType: GameType = GameType.WORDLE,
    val score: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val gamesPlayed: Int = 0,
    val winRate: Float = 0f,
    val lastPlayed: Long = System.currentTimeMillis(),
    val bestTimeSeconds: Int = 0
)

@Entity(tableName = "daily_scores")
data class DailyScore(
    @PrimaryKey val id: String,
    val userId: String,
    val gameType: GameType = GameType.WORDLE,
    val date: String, // YYYY-MM-DD
    val score: Int = 0,
    val guesses: Int = 0,
    val won: Boolean = false,
    val playedAt: Long = System.currentTimeMillis()
)
