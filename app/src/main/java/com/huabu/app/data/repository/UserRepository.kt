package com.huabu.app.data.repository

import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.local.dao.UserDao
import com.huabu.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseService: FirebaseService
) {
    // Get user - tries Firestore first, falls back to local
    fun getUser(userId: String): Flow<User?> = flow {
        // Emit local data first
        val localUser = userDao.getUserById(userId).first()
        emit(localUser)

        // Then sync from Firestore
        try {
            val remoteResult = firebaseService.getUser(userId)
            remoteResult.getOrNull()?.let { remoteUser ->
                // Update local cache
                userDao.insertUser(remoteUser)
                emit(remoteUser)
            }
        } catch (e: Exception) {
            // Keep local data if remote fails
        }
    }

    // Real-time user updates from Firestore
    fun getUserRealtime(userId: String): Flow<User?> =
        firebaseService.getUserFlow(userId)

    // Create user in both local and remote
    suspend fun createUser(user: User): Result<Unit> {
        // Save locally first
        userDao.insertUser(user)

        // Sync to Firestore
        return firebaseService.createUser(user.id, user)
    }

    // Update user
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        // Update local
        val currentUser = userDao.getUserById(userId).first()
        currentUser?.let {
            val updatedUser = it.copy(
                displayName = updates["displayName"] as? String ?: it.displayName,
                username = updates["username"] as? String ?: it.username,
                bio = updates["bio"] as? String ?: it.bio,
                profileImageUrl = updates["avatarUrl"] as? String ?: it.profileImageUrl,
                backgroundImageUrl = updates["backgroundImageUrl"] as? String ?: it.backgroundImageUrl,
                location = updates["location"] as? String ?: it.location,
                mood = updates["mood"] as? String ?: it.mood
            )
            userDao.updateUser(updatedUser)
        }

        // Update remote
        return firebaseService.updateUser(userId, updates)
    }

    // Search users
    suspend fun searchUsers(query: String): Result<List<User>> {
        return firebaseService.searchUsers(query)
    }

    // Get current cached users
    fun getAllCachedUsers(): Flow<List<User>> = userDao.getAllUsers()
}
