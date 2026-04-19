package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gif_items")
data class GifItem(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val url: String, // URL to GIF (local or remote)
    val isLocal: Boolean = false, // true = local file, false = remote URL
    val repeat: Boolean = true, // loop on/off
    val caption: String = "",
    val sortOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
