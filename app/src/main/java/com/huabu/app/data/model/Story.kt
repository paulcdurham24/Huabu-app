package com.huabu.app.data.model

data class Story(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorImageUrl: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
    val viewerIds: List<String> = emptyList()
)
