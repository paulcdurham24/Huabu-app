package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "minesweeper_games")
data class MinesweeperGame(
    @PrimaryKey val id: String,
    val hostId: String,
    val opponentId: String = "",
    val hostName: String = "",
    val opponentName: String = "",
    val rows: Int = 9,
    val cols: Int = 9,
    val mineCount: Int = 10,
    // Grid state: each cell is "hidden|revealed|flagged" + "0-8|mine"
    // Format: "state:value" for each cell, joined by ","
    // e.g., "hidden:0,revealed:1,flagged:mine,..."
    val hostGrid: String = "",
    val opponentGrid: String = "",
    val hostMines: String = "", // Comma-separated indices of mines
    val opponentMines: String = "",
    val hostRevealed: String = "", // Comma-separated revealed indices
    val opponentRevealed: String = "",
    val hostFinished: Boolean = false,
    val opponentFinished: Boolean = false,
    val hostWon: Boolean = false,
    val opponentWon: Boolean = false,
    val status: GameStatus = GameStatus.WAITING,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long = 0,
    val finishedAt: Long = 0
) {
    companion object {
        fun generateGrid(rows: Int, cols: Int, mineCount: Int): Pair<String, String> {
            val total = rows * cols
            val mines = (0 until total).shuffled().take(mineCount).toSortedSet()
            
            // Calculate numbers for each cell
            val numbers = IntArray(total) { 0 }
            for (mine in mines) {
                val r = mine / cols
                val c = mine % cols
                // Check all 8 neighbors
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) continue
                        val nr = r + dr
                        val nc = c + dc
                        if (nr in 0 until rows && nc in 0 until cols) {
                            numbers[nr * cols + nc]++
                        }
                    }
                }
            }
            
            // Build grid state string
            val grid = (0 until total).map { idx ->
                if (mines.contains(idx)) "hidden:mine" else "hidden:${numbers[idx]}"
            }.joinToString(",")
            
            return Pair(grid, mines.joinToString(","))
        }
    }
}
