package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currently_reading")
data class CurrentlyReading(
    @PrimaryKey val userId: String,
    val title: String = "",
    val author: String = "",
    val coverColor: String = "#8B5CF6",
    val progressPercent: Int = 0,   // 0-100
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val rating: Float = 0f          // 0-5
)
