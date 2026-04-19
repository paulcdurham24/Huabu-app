package com.huabu.app.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.huabu.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()

    // Collections
    private val usersRef = db.collection("users")
    private val postsRef = db.collection("posts")
    private val friendsRef = db.collection("friends")
    private val messagesRef = db.collection("messages")
    private val conversationsRef = db.collection("conversations")
    private val notificationsRef = db.collection("notifications")

    // ==================== USERS ====================

    suspend fun createUser(userId: String, user: User): Result<Unit> = try {
        usersRef.document(userId).set(user.toMap()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUser(userId: String): Result<User?> = try {
        val snapshot = usersRef.document(userId).get().await()
        val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getUserFlow(userId: String): Flow<User?> = callbackFlow {
        val listener = usersRef.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)?.copy(id = snapshot.id)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> = try {
        usersRef.document(userId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchUsers(query: String): Result<List<User>> = try {
        val snapshot = usersRef
            .orderBy("displayName")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(20)
            .get()
            .await()
        val users = snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }
        Result.success(users)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== POSTS ====================

    suspend fun createPost(post: Post): Result<String> = try {
        val docRef = postsRef.add(post).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getPostsFlow(): Flow<List<Post>> = callbackFlow {
        val listener = postsRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    fun getUserPostsFlow(userId: String): Flow<List<Post>> = callbackFlow {
        val listener = postsRef
            .whereEqualTo("authorId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun likePost(postId: String, userId: String): Result<Unit> = try {
        val postRef = postsRef.document(postId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentLikes = snapshot.getLong("likesCount") ?: 0
            val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()

            if (userId in likedBy) {
                // Unlike
                transaction.update(postRef, "likesCount", currentLikes - 1)
                transaction.update(postRef, "likedBy", likedBy - userId)
            } else {
                // Like
                transaction.update(postRef, "likesCount", currentLikes + 1)
                transaction.update(postRef, "likedBy", likedBy + userId)
            }
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== FRIENDS ====================

    suspend fun sendFriendRequest(fromUserId: String, toUserId: String): Result<Unit> = try {
        val request = hashMapOf(
            "fromUserId" to fromUserId,
            "toUserId" to toUserId,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )
        friendsRef.add(request).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun respondToFriendRequest(requestId: String, accept: Boolean): Result<Unit> = try {
        val status = if (accept) "accepted" else "rejected"
        friendsRef.document(requestId).update("status", status).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getFriendsFlow(userId: String): Flow<List<Friend>> = callbackFlow {
        val listener = friendsRef
            .whereEqualTo("fromUserId", userId)
            .whereEqualTo("status", "accepted")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val friends = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Friend::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(friends)
            }
        awaitClose { listener.remove() }
    }

    fun getFriendRequestsFlow(userId: String): Flow<List<Friend>> = callbackFlow {
        val listener = friendsRef
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Friend::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    // ==================== MESSAGES ====================

    suspend fun sendMessage(conversationId: String, message: Message): Result<Unit> = try {
        messagesRef.document(conversationId)
            .collection("messages")
            .add(message)
            .await()

        // Update conversation last message
        conversationsRef.document(conversationId).update(
            mapOf(
                "lastMessage" to message.content,
                "lastMessageTimestamp" to message.timestamp,
                "lastMessageSenderId" to message.senderId
            )
        ).await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getMessagesFlow(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listener = messagesRef
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    fun getConversationsFlow(userId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = conversationsRef
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Conversation::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(conversations)
            }
        awaitClose { listener.remove() }
    }

    // ==================== NOTIFICATIONS ====================

    suspend fun createNotification(notification: Notification): Result<Unit> = try {
        notificationsRef.add(notification).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getNotificationsFlow(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = notificationsRef
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> = try {
        notificationsRef.document(notificationId)
            .update("read", true)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== WIDGETS ====================

    suspend fun saveWidgetSettings(userId: String, settings: ProfileWidgetSettings): Result<Unit> = try {
        usersRef.document(userId)
            .collection("settings")
            .document("widgets")
            .set(settings)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getWidgetSettingsFlow(userId: String): Flow<ProfileWidgetSettings?> = callbackFlow {
        val listener = usersRef.document(userId)
            .collection("settings")
            .document("widgets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val settings = snapshot?.toObject(ProfileWidgetSettings::class.java)
                trySend(settings)
            }
        awaitClose { listener.remove() }
    }
}

// Extension functions for data conversion
private fun User.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "displayName" to displayName,
    "username" to username,
    "bio" to bio,
    "avatarUrl" to profileImageUrl,
    "backgroundImageUrl" to backgroundImageUrl,
    "mood" to mood,
    "location" to location,
    "verified" to isVerified,
    "isOnline" to isOnline,
    "joinedDate" to joinedDate,
    "lastSeen" to lastSeen,
    "followersCount" to followersCount,
    "followingCount" to followingCount,
    "postsCount" to postsCount
)
