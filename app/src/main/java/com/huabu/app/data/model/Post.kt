package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorImageUrl: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: String = "",
    val mood: String = "",
    val backgroundColor: String = "#1A1A2E",
    val visibility: String = "public",
    val isBookmarked: Boolean = false
) {
    @androidx.room.Ignore
    var likedBy: List<String> = emptyList()
    @androidx.room.Ignore
    var reactions: Map<String, Int> = emptyMap()
    @androidx.room.Ignore
    var reactedBy: Map<String, List<String>> = emptyMap()
}
