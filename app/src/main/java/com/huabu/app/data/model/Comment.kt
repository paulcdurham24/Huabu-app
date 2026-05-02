package com.huabu.app.data.model

data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorImageUrl: String = "",
    val content: String = "",
    val likesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val likedBy: List<String> = emptyList(),
    val parentId: String = "",
    val replyToName: String = ""
)
