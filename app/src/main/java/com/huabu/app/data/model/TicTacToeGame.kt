package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TicTacToeCell { EMPTY, X, O }
enum class GameStatus { WAITING, ACTIVE, FINISHED, CANCELLED }

@Entity(tableName = "tictactoe_games")
data class TicTacToeGame(
    @PrimaryKey val id: String,
    val playerXId: String, // Host/creator
    val playerOId: String = "", // Opponent (empty until accepted)
    val playerXName: String = "",
    val playerOName: String = "",
    // Board stored as 9 cells: 0-8 (top-left to bottom-right)
    val board: String = ".........", // . = empty, X = X, O = O
    val currentTurn: String = "X", // "X" or "O"
    val winner: String = "", // "X", "O", "DRAW", or empty
    val status: GameStatus = GameStatus.WAITING,
    val createdAt: Long = System.currentTimeMillis(),
    val lastMoveAt: Long = System.currentTimeMillis()
) {
    fun getBoardArray(): Array<Array<TicTacToeCell>> {
        val array = Array(3) { Array(3) { TicTacToeCell.EMPTY } }
        board.forEachIndexed { index, char ->
            val row = index / 3
            val col = index % 3
            array[row][col] = when (char) {
                'X' -> TicTacToeCell.X
                'O' -> TicTacToeCell.O
                else -> TicTacToeCell.EMPTY
            }
        }
        return array
    }

    fun makeMove(row: Int, col: Int, player: String): TicTacToeGame? {
        if (status != GameStatus.ACTIVE) return null
        if (currentTurn != player) return null
        
        val index = row * 3 + col
        if (board[index] != '.') return null
        
        val newBoard = board.toCharArray()
        newBoard[index] = player[0]
        
        // Check winner
        val newWinner = checkWinner(String(newBoard))
        val newStatus = if (newWinner.isNotEmpty()) GameStatus.FINISHED else GameStatus.ACTIVE
        val nextTurn = if (player == "X") "O" else "X"
        
        return copy(
            board = String(newBoard),
            currentTurn = if (newStatus == GameStatus.FINISHED) currentTurn else nextTurn,
            winner = newWinner,
            status = newStatus,
            lastMoveAt = System.currentTimeMillis()
        )
    }

    private fun checkWinner(board: String): String {
        val wins = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Cols
            listOf(0, 4, 8), listOf(2, 4, 6) // Diagonals
        )
        
        for (win in wins) {
            val a = board[win[0]]
            val b = board[win[1]]
            val c = board[win[2]]
            if (a != '.' && a == b && b == c) {
                return a.toString()
            }
        }
        
        return if (board.contains('.')) "" else "DRAW"
    }
}
