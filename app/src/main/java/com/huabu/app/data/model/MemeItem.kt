package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meme_items")
data class MemeItem(
    @PrimaryKey val id: String,
    val userId: String,
    val caption: String,
    val imageUrl: String,
    val likes: Int = 0,
    val fire: Int = 0,
    val laugh: Int = 0,
    val mindblown: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)

@Entity(tableName = "meme_reactions", primaryKeys = ["memeId", "userId"])
data class MemeReaction(
    val memeId: String,
    val userId: String,
    val reactionType: String, // "like", "fire", "laugh", "mindblown"
    val reactedAt: Long = System.currentTimeMillis()
)
