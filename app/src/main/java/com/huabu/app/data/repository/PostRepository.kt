package com.huabu.app.data.repository

import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.local.dao.PostDao
import com.huabu.app.data.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val firebaseService: FirebaseService
) {
    // Get all posts - Firestore real-time with local fallback
    fun getAllPosts(): Flow<List<Post>> = firebaseService.getPostsFlow()

    // Get user's posts
    fun getUserPosts(userId: String): Flow<List<Post>> =
        firebaseService.getUserPostsFlow(userId)

    // Create post
    suspend fun createPost(post: Post): Result<String> {
        // Save locally first
        postDao.insertPost(post)

        // Sync to Firestore
        return firebaseService.createPost(post)
    }

    // Like/unlike post
    suspend fun likePost(postId: String, userId: String): Result<Unit> {
        return firebaseService.likePost(postId, userId)
    }

    // Refresh local cache from Firestore
    suspend fun refreshPosts() {
        // Posts are already real-time from Firestore
        // Local cache is handled by the service
    }
}
