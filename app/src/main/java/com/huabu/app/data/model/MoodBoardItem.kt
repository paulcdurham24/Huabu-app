package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_board_items")
data class MoodBoardItem(
    @PrimaryKey val id: String,
    val userId: String,
    val gridPosition: Int,       // 0-8 (3x3 grid)
    val imageUrl: String = "",
    val color: String = "",      // fallback solid colour hex if no image
    val caption: String = "",
    val emoji: String = ""
)
