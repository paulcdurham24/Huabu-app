package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_posts")
data class PinnedPost(
    @PrimaryKey val id: String,
    val userId: String,
    val postId: String,
    val pinOrder: Int = 0        // 0, 1, 2 — up to 3 pinned posts
)
