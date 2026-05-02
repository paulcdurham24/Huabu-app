package com.huabu.app.data.firebase

import android.util.Log
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
    private val commentsRef = db.collection("comments")
    private val mediaTracksRef = db.collection("media_tracks")
    private val storiesRef = db.collection("stories")

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

    suspend fun incrementPostField(postId: String, field: String, by: Long = 1): Result<Unit> = try {
        postsRef.document(postId)
            .update(field, com.google.firebase.firestore.FieldValue.increment(by))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateFollowCounts(followerId: String, followingId: String, isFollow: Boolean): Result<Unit> = try {
        val delta = if (isFollow) 1L else -1L
        usersRef.document(followerId)
            .update("followingCount", com.google.firebase.firestore.FieldValue.increment(delta))
            .await()
        usersRef.document(followingId)
            .update("followersCount", com.google.firebase.firestore.FieldValue.increment(delta))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun incrementProfileViews(profileUserId: String, viewerUserId: String): Result<Unit> {
        if (profileUserId == viewerUserId) return Result.success(Unit)
        return try {
            usersRef.document(profileUserId)
                .update("profileViewsCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setOnlinePresence(userId: String, online: Boolean): Result<Unit> = try {
        usersRef.document(userId).update(
            mapOf(
                "isOnline" to online,
                "lastSeen" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun blockUser(blockerId: String, blockedId: String): Result<Unit> = try {
        usersRef.document(blockerId)
            .collection("blockedUsers")
            .document(blockedId)
            .set(mapOf("blockedAt" to System.currentTimeMillis()))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun unblockUser(blockerId: String, blockedId: String): Result<Unit> = try {
        usersRef.document(blockerId)
            .collection("blockedUsers")
            .document(blockedId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getBlockedUserIds(userId: String): List<String> = try {
        val snapshot = usersRef.document(userId)
            .collection("blockedUsers")
            .get().await()
        snapshot.documents.map { it.id }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun isUserBlocked(blockerId: String, blockedId: String): Boolean = try {
        val doc = usersRef.document(blockerId)
            .collection("blockedUsers")
            .document(blockedId)
            .get()
            .await()
        doc.exists()
    } catch (e: Exception) {
        false
    }

    suspend fun reportUser(reporterId: String, reportedId: String, reason: String): Result<Unit> = try {
        db.collection("reports").add(
            mapOf(
                "reporterId" to reporterId,
                "reportedId" to reportedId,
                "reason" to reason,
                "timestamp" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun isUsernameTaken(username: String): Boolean = try {
        val snap = usersRef.whereEqualTo("username", username.lowercase()).limit(1).get().await()
        !snap.isEmpty
    } catch (_: Exception) { false }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> = try {
        usersRef.document(userId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        if (query.isBlank()) {
            return try {
                val snapshot = usersRef.orderBy("displayName").limit(30).get().await()
                Result.success(snapshot.documents.mapNotNull { it.toObject(User::class.java)?.copy(id = it.id) })
            } catch (e: Exception) { Result.failure(e) }
        }
        return try {
            val byName = usersRef
                .orderBy("displayName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(15)
                .get().await()
                .documents.mapNotNull { it.toObject(User::class.java)?.copy(id = it.id) }
            val byUsername = usersRef
                .orderBy("username")
                .startAt(query.trimStart('@'))
                .endAt(query.trimStart('@') + "\uf8ff")
                .limit(15)
                .get().await()
                .documents.mapNotNull { it.toObject(User::class.java)?.copy(id = it.id) }
            Result.success((byName + byUsername).distinctBy { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchTracks(query: String): Result<List<MediaTrack>> = try {
        val snapshot = mediaTracksRef
            .whereEqualTo("type", "MUSIC")
            .limit(40)
            .get()
            .await()
        val lower = query.lowercase()
        val tracks = snapshot.documents.mapNotNull { doc ->
            doc.toObject(MediaTrack::class.java)?.copy(id = doc.id)
        }.filter { it.title.contains(lower, ignoreCase = true) || it.subtitle.contains(lower, ignoreCase = true) }
        Result.success(tracks)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getTrendingTracks(): Result<List<MediaTrack>> = try {
        val snapshot = mediaTracksRef
            .whereEqualTo("type", "MUSIC")
            .orderBy("rank")
            .limit(20)
            .get()
            .await()
        val tracks = snapshot.documents.mapNotNull { doc ->
            doc.toObject(MediaTrack::class.java)?.copy(id = doc.id)
        }
        Result.success(tracks)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchPosts(query: String): Result<List<Post>> = try {
        val snapshot = postsRef
            .whereEqualTo("visibility", "public")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .await()
        val lower = query.lowercase()
        val posts = snapshot.documents.mapNotNull { doc ->
            @Suppress("UNCHECKED_CAST")
            val lb = doc.get("likedBy") as? List<String> ?: emptyList()
            doc.toObject(Post::class.java)?.copy(id = doc.id)?.apply { likedBy = lb }
        }.filter { it.content.contains(lower, ignoreCase = true) || it.authorName.contains(lower, ignoreCase = true) }
        Result.success(posts)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPostsByTag(tag: String): Result<List<Post>> = try {
        val normalized = tag.trimStart('#').lowercase()
        val snapshot = postsRef
            .whereEqualTo("visibility", "public")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
        val posts = snapshot.documents.mapNotNull { doc ->
            @Suppress("UNCHECKED_CAST")
            val lb = doc.get("likedBy") as? List<String> ?: emptyList()
            doc.toObject(Post::class.java)?.copy(id = doc.id)?.apply { likedBy = lb }
        }.filter { it.tags.contains(normalized, ignoreCase = true) }
        Result.success(posts)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getTrendingTags(): Result<List<Pair<String, Int>>> = try {
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        val snapshot = postsRef
            .whereGreaterThan("createdAt", cutoff)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get().await()
        val counts = mutableMapOf<String, Int>()
        snapshot.documents.forEach { doc ->
            val tags = doc.getString("tags") ?: ""
            tags.split(",", " ", "#")
                .map { it.trim().lowercase().trimStart('#') }
                .filter { it.length > 1 }
                .forEach { tag -> counts[tag] = (counts[tag] ?: 0) + 1 }
        }
        val sorted = counts.entries.sortedByDescending { it.value }.take(10).map { it.key to it.value }
        Result.success(sorted)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== POSTS ====================

    suspend fun getPost(postId: String): Result<Post?> {
        return try {
            val snap = postsRef.document(postId).get().await()
            if (!snap.exists()) return Result.success(null)
            @Suppress("UNCHECKED_CAST")
            val lb = snap.get("likedBy") as? List<String> ?: emptyList()
            val post = snap.toObject(Post::class.java)?.copy(id = snap.id)?.apply { likedBy = lb }
            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(post: Post): Result<String> = try {
        val docRef = postsRef.add(post.toMap()).await()
        // Increment author's postsCount
        try {
            usersRef.document(post.authorId)
                .update("postsCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (_: Exception) {}
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getPostsFlow(pageSize: Long = 20): Flow<List<Post>> = callbackFlow {
        val listener = postsRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(pageSize)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val lb = doc.get("likedBy") as? List<String> ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    val reactions = doc.get("reactions") as? Map<String, Long> ?: emptyMap()
                    @Suppress("UNCHECKED_CAST")
                    val reactedBy = doc.get("reactedBy") as? Map<String, List<String>> ?: emptyMap()
                    doc.toObject(Post::class.java)?.copy(id = doc.id)?.apply {
                        likedBy = lb
                        this.reactions = reactions.mapValues { it.value.toInt() }
                        this.reactedBy = reactedBy
                    }
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getMorePosts(afterTimestamp: Long, pageSize: Long = 20): Result<List<Post>> = try {
        val snapshot = postsRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .whereLessThan("createdAt", afterTimestamp)
            .limit(pageSize)
            .get().await()
        val posts = snapshot.documents.mapNotNull { doc ->
            @Suppress("UNCHECKED_CAST")
            val lb = doc.get("likedBy") as? List<String> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val reactions = doc.get("reactions") as? Map<String, Long> ?: emptyMap()
            @Suppress("UNCHECKED_CAST")
            val reactedBy = doc.get("reactedBy") as? Map<String, List<String>> ?: emptyMap()
            doc.toObject(Post::class.java)?.copy(id = doc.id)?.apply {
                likedBy = lb
                this.reactions = reactions.mapValues { it.value.toInt() }
                this.reactedBy = reactedBy
            }
        }
        Result.success(posts)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getPostFlow(postId: String): Flow<Post?> = callbackFlow {
        val listener = postsRef.document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot == null || !snapshot.exists()) { trySend(null); return@addSnapshotListener }
                @Suppress("UNCHECKED_CAST")
                val lb = snapshot.get("likedBy") as? List<String> ?: emptyList()
                val post = snapshot.toObject(Post::class.java)?.copy(id = snapshot.id)?.apply { likedBy = lb }
                trySend(post)
            }
        awaitClose { listener.remove() }
    }

    fun getUserPostsFlow(userId: String): Flow<List<Post>> = callbackFlow {
        val listener = postsRef
            .whereEqualTo("authorId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val lb = doc.get("likedBy") as? List<String> ?: emptyList()
                    doc.toObject(Post::class.java)?.copy(id = doc.id)?.apply { likedBy = lb }
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun likePost(postId: String, userId: String): Result<Unit> = try {
        val postRef = postsRef.document(postId)
        var isNewLike = false
        var postAuthorId = ""
        var postAuthorName = ""
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentLikes = snapshot.getLong("likesCount") ?: 0
            val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
            postAuthorId = snapshot.getString("authorId") ?: ""
            postAuthorName = snapshot.getString("authorName") ?: ""

            if (userId in likedBy) {
                transaction.update(postRef, "likesCount", currentLikes - 1)
                transaction.update(postRef, "likedBy", likedBy - userId)
                isNewLike = false
            } else {
                transaction.update(postRef, "likesCount", currentLikes + 1)
                transaction.update(postRef, "likedBy", likedBy + userId)
                isNewLike = true
            }
        }.await()
        // Send notification only when liking (not unliking), and not to yourself
        if (isNewLike && postAuthorId.isNotEmpty() && postAuthorId != userId) {
            val liker = getUser(userId).getOrNull()
            sendNotification(
                userId = postAuthorId,
                senderId = userId,
                senderName = liker?.displayName ?: "Someone",
                type = "like",
                title = "${liker?.displayName ?: "Someone"} liked your post",
                message = "They liked your post ❤️",
                targetId = postId
            )
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun reactToPost(postId: String, userId: String, emoji: String): Result<Unit> = try {
        val postRef = postsRef.document(postId)
        db.runTransaction { transaction ->
            val snap = transaction.get(postRef)
            @Suppress("UNCHECKED_CAST")
            val reactedBy = (snap.get("reactedBy") as? Map<String, List<String>> ?: emptyMap()).toMutableMap()
            val currentList = reactedBy[emoji]?.toMutableList() ?: mutableListOf()
            val alreadyReacted = userId in currentList
            if (alreadyReacted) currentList.remove(userId) else currentList.add(userId)
            reactedBy[emoji] = currentList
            // Rebuild counts map
            val counts = reactedBy.mapValues { it.value.size }
            transaction.update(postRef, mapOf("reactedBy" to reactedBy, "reactions" to counts))
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePost(postId: String, content: String, mood: String, tags: String, visibility: String): Result<Unit> = try {
        postsRef.document(postId).update(
            mapOf(
                "content" to content,
                "mood" to mood,
                "tags" to tags,
                "visibility" to visibility
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePost(postId: String, authorId: String): Result<Unit> = try {
        postsRef.document(postId).delete().await()
        try {
            usersRef.document(authorId)
                .update("postsCount", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()
        } catch (_: Exception) {}
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
        val sender = getUser(fromUserId).getOrNull()
        sendNotification(
            userId = toUserId,
            senderId = fromUserId,
            senderName = sender?.displayName ?: "Someone",
            type = "friend_request",
            title = "${sender?.displayName ?: "Someone"} sent you a friend request",
            message = "Tap to view their profile",
            targetId = fromUserId
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun removeFriend(userId: String, friendUserId: String): Result<Unit> = try {
        // Delete doc where fromUserId==userId && toUserId==friendUserId (accepted)
        val q1 = friendsRef
            .whereEqualTo("fromUserId", userId)
            .whereEqualTo("toUserId", friendUserId)
            .whereEqualTo("status", "accepted")
            .get().await()
        q1.documents.forEach { it.reference.delete().await() }
        // Also delete reverse direction
        val q2 = friendsRef
            .whereEqualTo("fromUserId", friendUserId)
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "accepted")
            .get().await()
        q2.documents.forEach { it.reference.delete().await() }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun respondToFriendRequest(requestId: String, accept: Boolean): Result<Unit> = try {
        val status = if (accept) "accepted" else "rejected"
        friendsRef.document(requestId).update("status", status).await()
        if (accept) {
            val doc = friendsRef.document(requestId).get().await()
            val fromUserId = doc.getString("fromUserId") ?: ""
            val toUserId = doc.getString("toUserId") ?: ""
            if (fromUserId.isNotEmpty()) {
                val accepter = getUser(toUserId).getOrNull()
                sendNotification(
                    userId = fromUserId,
                    senderId = toUserId,
                    senderName = accepter?.displayName ?: "Someone",
                    type = "follow",
                    title = "${accepter?.displayName ?: "Someone"} accepted your friend request",
                    message = "You are now friends 🎉",
                    targetId = toUserId
                )
            }
        }
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
                    val toUserId = doc.getString("toUserId") ?: return@mapNotNull null
                    Friend(id = doc.id, userId = userId, friendId = toUserId, friendName = "", friendUsername = "")
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
                    val fromUserId = doc.getString("fromUserId") ?: return@mapNotNull null
                    Friend(id = doc.id, userId = userId, friendId = fromUserId, friendName = "", friendUsername = "", status = "pending")
                } ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    // ==================== MESSAGES ====================

    suspend fun getOrCreateConversation(currentUserId: String, otherUserId: String): Result<String> = try {
        val existing = conversationsRef
            .whereArrayContains("participantIds", currentUserId)
            .get().await()
        val match = existing.documents.firstOrNull { doc ->
            val ids = doc.get("participantIds") as? List<*> ?: emptyList<Any>()
            otherUserId in ids
        }
        if (match != null) {
            Result.success(match.id)
        } else {
            val newConvo = hashMapOf(
                "participantIds" to listOf(currentUserId, otherUserId),
                "lastMessage" to "",
                "lastMessageTimestamp" to System.currentTimeMillis(),
                "lastMessageSenderId" to "",
                "unreadCount" to 0
            )
            val ref = conversationsRef.add(newConvo).await()
            Result.success(ref.id)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun sendMessage(conversationId: String, message: Message): Result<Unit> = try {
        messagesRef.document(conversationId)
            .collection("messages")
            .add(message)
            .await()

        conversationsRef.document(conversationId).update(
            mapOf(
                "lastMessage" to message.content,
                "lastMessageTimestamp" to message.timestamp,
                "lastMessageSenderId" to message.senderId,
                "unreadCount" to com.google.firebase.firestore.FieldValue.increment(1)
            )
        ).await()

        // Notify the receiver
        if (message.receiverId.isNotEmpty() && message.receiverId != message.senderId) {
            sendNotification(
                userId = message.receiverId,
                senderId = message.senderId,
                senderName = message.senderName,
                type = "message",
                title = "New message from ${message.senderName}",
                message = message.content.take(80),
                targetId = conversationId
            )
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getTypingFlow(conversationId: String, otherUserId: String): Flow<Boolean> = callbackFlow {
        val ref = conversationsRef.document(conversationId)
            .collection("typing").document(otherUserId)
        val listener = ref.addSnapshotListener { snap, _ ->
            val isTyping = snap?.getBoolean("isTyping") ?: false
            val updatedAt = snap?.getLong("updatedAt") ?: 0L
            val stale = System.currentTimeMillis() - updatedAt > 8_000L
            trySend(isTyping && !stale)
        }
        awaitClose { listener.remove() }
    }

    suspend fun setTypingStatus(conversationId: String, userId: String, isTyping: Boolean) {
        try {
            conversationsRef.document(conversationId)
                .collection("typing").document(userId)
                .set(mapOf("isTyping" to isTyping, "updatedAt" to System.currentTimeMillis()))
                .await()
        } catch (_: Exception) {}
    }

    suspend fun markConversationRead(conversationId: String): Result<Unit> = try {
        conversationsRef.document(conversationId).update("unreadCount", 0).await()
        // Mark individual messages as read so double-tick shows correctly
        val unreadMsgs = messagesRef
            .document(conversationId)
            .collection("messages")
            .whereEqualTo("isRead", false)
            .get().await()
        if (!unreadMsgs.isEmpty) {
            val batch = db.batch()
            unreadMsgs.documents.forEach { doc -> batch.update(doc.reference, "isRead", true) }
            batch.commit().await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteMessage(conversationId: String, messageId: String): Result<Unit> = try {
        messagesRef.document(conversationId)
            .collection("messages")
            .document(messageId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getUnreadMessagesCountFlow(userId: String): Flow<Int> = callbackFlow {
        val listener = conversationsRef
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val total = snapshot?.documents?.sumOf { doc ->
                    val senderId = doc.getString("lastMessageSenderId") ?: ""
                    val unread = doc.getLong("unreadCount")?.toInt() ?: 0
                    // Only count unread where we are the receiver (last sender is not us)
                    if (senderId != userId) unread else 0
                } ?: 0
                trySend(total)
            }
        awaitClose { listener.remove() }
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

    suspend fun getOlderMessages(conversationId: String, beforeTimestamp: Long, pageSize: Long = 30L): Result<List<Message>> = try {
        val snapshot = messagesRef
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .whereLessThan("timestamp", beforeTimestamp)
            .limit(pageSize)
            .get().await()
        val messages = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Message::class.java)?.copy(id = doc.id)
        }.reversed()
        Result.success(messages)
    } catch (e: Exception) {
        Result.failure(e)
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

    // ==================== COMMENTS ====================

    suspend fun addComment(comment: Comment): Result<String> = try {
        val map = hashMapOf(
            "postId" to comment.postId,
            "authorId" to comment.authorId,
            "authorName" to comment.authorName,
            "authorUsername" to comment.authorUsername,
            "authorImageUrl" to comment.authorImageUrl,
            "content" to comment.content,
            "likesCount" to comment.likesCount,
            "timestamp" to comment.timestamp
        )
        val ref = commentsRef.add(map).await()
        // Increment post commentsCount
        try {
            postsRef.document(comment.postId)
                .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (_: Exception) {}
        // Notify post author
        val post = postsRef.document(comment.postId).get().await()
        val authorId = post.getString("authorId") ?: ""
        if (authorId.isNotEmpty() && authorId != comment.authorId) {
            sendNotification(
                userId = authorId,
                senderId = comment.authorId,
                senderName = comment.authorName,
                type = "comment",
                title = "${comment.authorName} commented on your post",
                message = comment.content.take(80),
                targetId = comment.postId
            )
        }
        Result.success(ref.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun likeComment(commentId: String, userId: String): Result<Unit> = try {
        val ref = commentsRef.document(commentId)
        db.runTransaction { transaction ->
            val snap = transaction.get(ref)
            val likedBy = snap.get("likedBy") as? List<String> ?: emptyList()
            val count = snap.getLong("likesCount") ?: 0
            if (userId in likedBy) {
                transaction.update(ref, mapOf("likedBy" to likedBy - userId, "likesCount" to (count - 1).coerceAtLeast(0)))
            } else {
                transaction.update(ref, mapOf("likedBy" to likedBy + userId, "likesCount" to count + 1))
            }
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteComment(commentId: String, postId: String, authorId: String, currentUserId: String): Result<Unit> {
        if (authorId != currentUserId) return Result.failure(Exception("Not your comment"))
        return try {
            commentsRef.document(commentId).delete().await()
            try {
                postsRef.document(postId)
                    .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
            } catch (_: Exception) {}
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCommentsFlow(postId: String): Flow<List<Comment>> = callbackFlow {
        // Try with ordering first; if index missing, fall back to unordered
        var listener = commentsRef
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirebaseService", "Comments query failed (missing index?), trying unordered: ${error.message}")
                    // Fallback: query without orderBy
                    commentsRef.whereEqualTo("postId", postId)
                        .get()
                        .addOnSuccessListener { snap ->
                            val list = snap.documents.mapNotNull { d ->
                                @Suppress("UNCHECKED_CAST")
                                val lb = d.get("likedBy") as? List<String> ?: emptyList()
                                d.toObject(Comment::class.java)?.copy(id = d.id, likedBy = lb)
                            }.sortedBy { it.timestamp }
                            trySend(list)
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseService", "Fallback comments query also failed: ${e.message}")
                        }
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val lb = doc.get("likedBy") as? List<String> ?: emptyList()
                    doc.toObject(Comment::class.java)?.copy(id = doc.id, likedBy = lb)
                }?.sortedBy { it.timestamp } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    // ==================== POLLS ====================

    suspend fun createPoll(userId: String, poll: ProfilePoll): Result<String> = try {
        val map = hashMapOf(
            "userId" to poll.userId,
            "question" to poll.question,
            "optionA" to poll.optionA,
            "optionB" to poll.optionB,
            "optionC" to poll.optionC,
            "optionD" to poll.optionD,
            "votesA" to 0,
            "votesB" to 0,
            "votesC" to 0,
            "votesD" to 0,
            "createdAt" to poll.createdAt,
            "endsAt" to poll.endsAt,
            "isActive" to true
        )
        val ref = usersRef.document(userId).collection("polls").add(map).await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePoll(userId: String, pollId: String): Result<Unit> = try {
        usersRef.document(userId).collection("polls").document(pollId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPollsForUser(userId: String): Result<List<ProfilePoll>> = try {
        val snap = usersRef.document(userId).collection("polls")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        val polls = snap.documents.mapNotNull { doc ->
            doc.toObject(ProfilePoll::class.java)?.copy(id = doc.id)
        }
        Result.success(polls)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun voteOnPoll(ownerId: String, pollId: String, option: Char, voterId: String): Result<Unit> = try {
        val pollRef = usersRef.document(ownerId).collection("polls").document(pollId)
        val voteRef = usersRef.document(ownerId).collection("polls").document(pollId)
            .collection("votes").document(voterId)
        db.runTransaction { tx ->
            val existing = tx.get(voteRef)
            if (existing.exists()) return@runTransaction // already voted
            val field = "votes${option.uppercaseChar()}"
            tx.update(pollRef, field, com.google.firebase.firestore.FieldValue.increment(1))
            tx.set(voteRef, mapOf("option" to option.toString(), "votedAt" to System.currentTimeMillis()))
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== PROFILE VIEWS ====================

    suspend fun incrementProfileView(profileUserId: String, viewerUserId: String): Result<Unit> {
        if (profileUserId == viewerUserId) return Result.success(Unit)
        return try {
            usersRef.document(profileUserId)
                .update("profileViewsCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== PINNED POSTS ====================

    suspend fun pinPost(userId: String, postId: String, order: Int): Result<Unit> = try {
        usersRef.document(userId)
            .collection("pinnedPosts")
            .document(postId)
            .set(mapOf("postId" to postId, "pinOrder" to order, "pinnedAt" to System.currentTimeMillis()))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun unpinPost(userId: String, postId: String): Result<Unit> = try {
        usersRef.document(userId)
            .collection("pinnedPosts")
            .document(postId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPinnedPosts(userId: String): Result<List<Post>> = try {
        val pins = usersRef.document(userId)
            .collection("pinnedPosts")
            .orderBy("pinOrder")
            .get().await()
        val postIds = pins.documents.mapNotNull { it.getString("postId") }
        val posts = postIds.mapNotNull { postId ->
            getPost(postId).getOrNull()
        }
        Result.success(posts)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun isPinned(userId: String, postId: String): Boolean = try {
        usersRef.document(userId).collection("pinnedPosts").document(postId).get().await().exists()
    } catch (_: Exception) { false }

    // ==================== BOOKMARKS ====================

    suspend fun toggleBookmark(userId: String, postId: String): Result<Boolean> = try {
        val ref = usersRef.document(userId).collection("bookmarks").document(postId)
        val exists = ref.get().await().exists()
        if (exists) ref.delete().await() else ref.set(mapOf("savedAt" to System.currentTimeMillis())).await()
        Result.success(!exists)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun isBookmarked(userId: String, postId: String): Boolean = try {
        usersRef.document(userId).collection("bookmarks").document(postId).get().await().exists()
    } catch (_: Exception) { false }

    suspend fun getSavedPosts(userId: String): Result<List<Post>> = try {
        val bookmarkDocs = usersRef.document(userId).collection("bookmarks")
            .orderBy("savedAt", Query.Direction.DESCENDING).limit(50).get().await()
        val postIds = bookmarkDocs.documents.map { it.id }
        val posts = mutableListOf<Post>()
        for (pid in postIds) {
            val doc = postsRef.document(pid).get().await()
            if (doc.exists()) {
                @Suppress("UNCHECKED_CAST")
                val lb = doc.get("likedBy") as? List<String> ?: emptyList()
                doc.toObject(Post::class.java)?.copy(id = doc.id, isBookmarked = true)
                    ?.apply { likedBy = lb }
                    ?.let { posts.add(it) }
            }
        }
        Result.success(posts)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== FOLLOW ====================

    suspend fun followUser(fromUserId: String, toUserId: String): Result<Unit> = try {
        usersRef.document(fromUserId).collection("following").document(toUserId)
            .set(mapOf("followedAt" to System.currentTimeMillis())).await()
        usersRef.document(toUserId).collection("followers").document(fromUserId)
            .set(mapOf("followedAt" to System.currentTimeMillis())).await()
        usersRef.document(fromUserId).update("followingCount", com.google.firebase.firestore.FieldValue.increment(1)).await()
        usersRef.document(toUserId).update("followersCount", com.google.firebase.firestore.FieldValue.increment(1)).await()
        val me = getUser(fromUserId).getOrNull()
        sendFollowNotification(toUserId, fromUserId, me?.displayName ?: "Someone")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun unfollowUser(fromUserId: String, toUserId: String): Result<Unit> = try {
        usersRef.document(fromUserId).collection("following").document(toUserId).delete().await()
        usersRef.document(toUserId).collection("followers").document(fromUserId).delete().await()
        usersRef.document(fromUserId).update("followingCount", com.google.firebase.firestore.FieldValue.increment(-1)).await()
        usersRef.document(toUserId).update("followersCount", com.google.firebase.firestore.FieldValue.increment(-1)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun isFollowing(fromUserId: String, toUserId: String): Boolean = try {
        usersRef.document(fromUserId).collection("following").document(toUserId).get().await().exists()
    } catch (_: Exception) { false }

    suspend fun getFollowingIds(userId: String): Set<String> = try {
        usersRef.document(userId).collection("following").get().await().documents.map { it.id }.toSet()
    } catch (_: Exception) { emptySet() }

    // ==================== ONLINE PRESENCE ====================

    suspend fun updatePresence(userId: String, online: Boolean) {
        try {
            usersRef.document(userId).update(
                mapOf("isOnline" to online, "lastSeen" to System.currentTimeMillis())
            ).await()
        } catch (_: Exception) {}
    }

    fun getOnlineUsersFlow(): Flow<Set<String>> = callbackFlow {
        val listener = usersRef
            .whereEqualTo("isOnline", true)
            .addSnapshotListener { snap, _ ->
                val ids = snap?.documents?.map { it.id }?.toSet() ?: emptySet()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }

    // ==================== NOTIFICATIONS ====================

    suspend fun sendFollowNotification(toUserId: String, fromUserId: String, fromName: String) {
        sendNotification(
            userId = toUserId,
            senderId = fromUserId,
            senderName = fromName,
            type = "follow",
            title = "$fromName started following you",
            message = ""
        )
    }

    private suspend fun sendNotification(
        userId: String,
        senderId: String,
        senderName: String,
        type: String,
        title: String,
        message: String,
        targetId: String = ""
    ) {
        try {
            val notification = hashMapOf(
                "userId" to userId,
                "senderId" to senderId,
                "senderName" to senderName,
                "type" to type,
                "title" to title,
                "message" to message,
                "read" to false,
                "timestamp" to System.currentTimeMillis(),
                "targetId" to targetId
            )
            notificationsRef.add(notification).await()
        } catch (_: Exception) { /* best-effort */ }
    }

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

    fun getUnreadNotificationsCountFlow(userId: String): Flow<Int> = callbackFlow {
        val listener = notificationsRef
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.size() ?: 0)
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

    suspend fun saveFcmToken(userId: String, token: String) {
        try {
            usersRef.document(userId).update("fcmToken", token).await()
        } catch (_: Exception) {}
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

    // ==================== STORIES ====================

    suspend fun createStory(story: Story): Result<String> = try {
        val map = mapOf(
            "authorId" to story.authorId,
            "authorName" to story.authorName,
            "authorUsername" to story.authorUsername,
            "authorImageUrl" to story.authorImageUrl,
            "imageUrl" to story.imageUrl,
            "caption" to story.caption,
            "createdAt" to story.createdAt,
            "expiresAt" to story.expiresAt,
            "viewerIds" to story.viewerIds
        )
        val ref = storiesRef.add(map).await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteStory(storyId: String): Result<Unit> = try {
        storiesRef.document(storyId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markStoryViewed(storyId: String, viewerId: String): Result<Unit> = try {
        storiesRef.document(storyId)
            .update("viewerIds", com.google.firebase.firestore.FieldValue.arrayUnion(viewerId))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getStoriesFlow(): Flow<List<Story>> = callbackFlow {
        val cutoff = System.currentTimeMillis()
        val listener = storiesRef
            .whereGreaterThan("expiresAt", cutoff)
            .orderBy("expiresAt", Query.Direction.DESCENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val stories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Story::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(stories)
            }
        awaitClose { listener.remove() }
    }
}

// Extension functions for data conversion
private fun User.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "displayName" to displayName,
    "username" to username,
    "email" to email,
    "bio" to bio,
    "profileImageUrl" to profileImageUrl,
    "backgroundImageUrl" to backgroundImageUrl,
    "mood" to mood,
    "location" to location,
    "isVerified" to isVerified,
    "isOnline" to isOnline,
    "joinedDate" to joinedDate,
    "lastSeen" to lastSeen,
    "followersCount" to followersCount,
    "followingCount" to followingCount,
    "postsCount" to postsCount
)

private fun Post.toMap(): Map<String, Any?> = mapOf(
    "authorId" to authorId,
    "authorName" to authorName,
    "authorUsername" to authorUsername,
    "authorImageUrl" to authorImageUrl,
    "content" to content,
    "imageUrl" to imageUrl,
    "videoUrl" to videoUrl,
    "likesCount" to likesCount,
    "commentsCount" to commentsCount,
    "sharesCount" to sharesCount,
    "isLiked" to isLiked,
    "createdAt" to createdAt,
    "tags" to tags,
    "mood" to mood,
    "backgroundColor" to backgroundColor,
    "visibility" to visibility
)
