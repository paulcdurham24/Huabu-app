package com.huabu.app.data.repository

import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.local.dao.FriendDao
import com.huabu.app.data.model.Friend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepository @Inject constructor(
    private val friendDao: FriendDao,
    private val firebaseService: FirebaseService
) {
    // Get friends from Firestore (real-time)
    fun getFriends(userId: String): Flow<List<Friend>> =
        firebaseService.getFriendsFlow(userId)

    // Get pending friend requests
    fun getFriendRequests(userId: String): Flow<List<Friend>> =
        firebaseService.getFriendRequestsFlow(userId)

    // Send friend request
    suspend fun sendFriendRequest(fromUserId: String, toUserId: String): Result<Unit> {
        return firebaseService.sendFriendRequest(fromUserId, toUserId)
    }

    // Accept or reject friend request
    suspend fun respondToRequest(requestId: String, accept: Boolean): Result<Unit> {
        return firebaseService.respondToFriendRequest(requestId, accept)
    }

    // Cache friends locally
    suspend fun cacheFriends(userId: String) {
        val friends = friendDao.getAllFriends(userId).first()
        // Sync logic can be added here
    }
}
