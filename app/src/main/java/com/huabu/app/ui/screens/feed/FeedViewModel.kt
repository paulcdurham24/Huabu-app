package com.huabu.app.ui.screens.feed

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.firebase.StorageService
import com.huabu.app.data.model.Comment
import com.huabu.app.data.model.Post
import com.huabu.app.data.model.Story
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class CommentsUiState(
    val postId: String = "",
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false
)

enum class FeedFilter { ALL, FRIENDS, MINE }

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val allPosts: List<Post> = emptyList(),
    val stories: List<Story> = emptyList(),
    val currentUserId: String = "",
    val currentUserName: String = "",
    val currentUserImageUrl: String = "",
    val selectedFilter: FeedFilter = FeedFilter.ALL,
    val onlineUserIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePosts: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val storageService: StorageService,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var friendIds: Set<String> = emptySet()
    private var blockedIds: Set<String> = emptySet()

    private val _commentsState = MutableStateFlow(CommentsUiState())
    val commentsState: StateFlow<CommentsUiState> = _commentsState.asStateFlow()

    init {
        val uid = authService.getCurrentUserId() ?: ""
        _uiState.value = _uiState.value.copy(currentUserId = uid)
        if (uid.isNotEmpty()) {
            loadCurrentUser(uid)
            trackOnlinePresence(uid)
            observeOnlinePresence()
            loadFriendsThenFeed()
            loadStories()
            loadBlockedUsers(uid)
        }
    }

    private fun loadBlockedUsers(userId: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            blockedIds = firebaseService.getBlockedUserIds(userId).toSet()
        }
    }

    private fun trackOnlinePresence(userId: String) {
        viewModelScope.launch { firebaseService.updatePresence(userId, true) }
    }

    private fun observeOnlinePresence() {
        viewModelScope.launch {
            firebaseService.getOnlineUsersFlow()
                .catch { }
                .collect { ids -> _uiState.update { it.copy(onlineUserIds = ids) } }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val uid = authService.getCurrentUserId() ?: return
        viewModelScope.launch { firebaseService.updatePresence(uid, false) }
    }

    private fun loadCurrentUser(userId: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            val user = firebaseService.getUser(userId).getOrNull()
            if (user != null) {
                _uiState.update { it.copy(
                    currentUserName = user.displayName,
                    currentUserImageUrl = user.profileImageUrl
                )}
            }
        }
    }

    private fun loadFriendsThenFeed() {
        val currentUserId = authService.getCurrentUserId() ?: return

        viewModelScope.launch {
            firebaseService.getFriendsFlow(currentUserId)
                .catch { }
                .collect { friends -> friendIds = friends.map { it.friendId }.toSet() }
        }

        viewModelScope.launch {
            firebaseService.getPostsFlow()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { posts ->
                    val visible = posts
                        .filter { post ->
                            post.authorId !in blockedIds &&
                            (post.visibility == "public" ||
                            post.authorId == currentUserId ||
                            post.authorId in friendIds)
                        }
                        .map { post -> post.copy(isLiked = currentUserId in post.likedBy).apply { likedBy = post.likedBy } }
                    val activeFilter = _uiState.value.selectedFilter
                    val display = applyFilter(visible, activeFilter, currentUserId)
                    _uiState.update { it.copy(posts = display, allPosts = visible, isLoading = false) }
                }
        }
    }

    private fun applyFilter(all: List<Post>, filter: FeedFilter, currentUserId: String): List<Post> = when (filter) {
        FeedFilter.ALL -> all
        FeedFilter.FRIENDS -> all.filter { it.authorId in friendIds }
        FeedFilter.MINE -> all.filter { it.authorId == currentUserId }
    }

    fun setFilter(filter: FeedFilter) {
        val uid = _uiState.value.currentUserId
        val filtered = applyFilter(_uiState.value.allPosts, filter, uid)
        _uiState.update { it.copy(selectedFilter = filter, posts = filtered) }
    }

    private fun loadStories() {
        viewModelScope.launch {
            firebaseService.getStoriesFlow()
                .catch { }
                .collect { stories -> _uiState.update { it.copy(stories = stories) } }
        }
    }

    fun postStory(imageUri: Uri, caption: String) {
        val userId = authService.getCurrentUserId() ?: return
        val s = _uiState.value
        viewModelScope.launch {
            val uploadResult = storageService.uploadImage(imageUri, "stories/$userId/${System.currentTimeMillis()}.jpg")
            val imageUrl = uploadResult.getOrNull() ?: return@launch
            val story = Story(
                authorId = userId,
                authorName = s.currentUserName,
                authorImageUrl = s.currentUserImageUrl,
                imageUrl = imageUrl,
                caption = caption
            )
            firebaseService.createStory(story)
        }
    }

    fun viewStory(storyId: String) {
        val userId = authService.getCurrentUserId() ?: return
        viewModelScope.launch { firebaseService.markStoryViewed(storyId, userId) }
    }

    fun deleteStory(storyId: String) {
        _uiState.update { it.copy(stories = it.stories.filter { s -> s.id != storyId }) }
        viewModelScope.launch { firebaseService.deleteStory(storyId) }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            withTimeoutOrNull(5000) {
                firebaseService.getPostsFlow().first()
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
        loadFriendsThenFeed()
    }

    fun likePost(postId: String) {
        val userId = authService.getCurrentUserId() ?: return
        // Optimistic update
        _uiState.update { state ->
            state.copy(posts = state.posts.map { post ->
                if (post.id == postId) {
                    val nowLiked = !post.isLiked
                    post.copy(
                        isLiked = nowLiked,
                        likesCount = if (nowLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
                    ).apply {
                        likedBy = if (nowLiked) post.likedBy + userId else post.likedBy - userId
                    }
                } else post
            })
        }
        viewModelScope.launch { firebaseService.likePost(postId, userId) }
    }

    fun openComments(postId: String) {
        _commentsState.value = CommentsUiState(postId = postId, isLoading = true)
        viewModelScope.launch {
            firebaseService.getCommentsFlow(postId)
                .catch { _commentsState.update { it.copy(isLoading = false) } }
                .collect { comments ->
                    _commentsState.update { it.copy(comments = comments, isLoading = false) }
                }
        }
    }

    fun closeComments() {
        _commentsState.value = CommentsUiState()
    }

    fun commentPost(postId: String, content: String) {
        val userId = authService.getCurrentUserId() ?: return
        val state = _uiState.value
        val comment = Comment(
            postId = postId,
            authorId = userId,
            authorName = state.currentUserName,
            authorUsername = "",
            authorImageUrl = state.currentUserImageUrl,
            content = content
        )
        // Optimistic count bump
        _uiState.update { s ->
            s.copy(posts = s.posts.map { p ->
                if (p.id == postId) p.copy(commentsCount = p.commentsCount + 1) else p
            })
        }
        viewModelScope.launch { firebaseService.addComment(comment) }
    }

    fun loadMorePosts() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePosts) return
        val oldest = state.allPosts.minByOrNull { it.createdAt }?.createdAt ?: return
        val currentUserId = authService.getCurrentUserId() ?: return
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            firebaseService.getMorePosts(oldest).onSuccess { more ->
                val filtered = more.filter { post ->
                    post.authorId !in blockedIds &&
                    (post.visibility == "public" || post.authorId == currentUserId || post.authorId in friendIds)
                }.map { post -> post.copy(isLiked = currentUserId in post.likedBy).apply { likedBy = post.likedBy; reactions = post.reactions; reactedBy = post.reactedBy } }
                val newAll = (state.allPosts + filtered).distinctBy { it.id }
                val newDisplay = applyFilter(newAll, state.selectedFilter, currentUserId)
                _uiState.update { it.copy(
                    allPosts = newAll,
                    posts = newDisplay,
                    isLoadingMore = false,
                    hasMorePosts = more.size >= 20
                )}
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun reactToPost(postId: String, emoji: String) {
        val userId = authService.getCurrentUserId() ?: return
        _uiState.update { state ->
            state.copy(posts = state.posts.map { post ->
                if (post.id != postId) post else {
                    val currentList = post.reactedBy[emoji]?.toMutableList() ?: mutableListOf()
                    val alreadyReacted = userId in currentList
                    if (alreadyReacted) currentList.remove(userId) else currentList.add(userId)
                    val newReactedBy = post.reactedBy.toMutableMap().also { it[emoji] = currentList }
                    val newReactions = newReactedBy.mapValues { it.value.size }
                    post.also { it.reactedBy = newReactedBy; it.reactions = newReactions }
                }
            })
        }
        viewModelScope.launch { firebaseService.reactToPost(postId, userId, emoji) }
    }

    fun bookmarkPost(postId: String) {
        val userId = authService.getCurrentUserId() ?: return
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { if (it.id == postId) it.copy(isBookmarked = !it.isBookmarked) else it },
                allPosts = state.allPosts.map { if (it.id == postId) it.copy(isBookmarked = !it.isBookmarked) else it }
            )
        }
        viewModelScope.launch { firebaseService.toggleBookmark(userId, postId) }
    }

    fun deletePost(postId: String) {
        val userId = authService.getCurrentUserId() ?: return
        val post = _uiState.value.posts.find { it.id == postId } ?: return
        if (post.authorId != userId) return
        _uiState.update { it.copy(posts = it.posts.filter { p -> p.id != postId }) }
        viewModelScope.launch { firebaseService.deletePost(postId, userId) }
    }

    fun sharePost(postId: String) {
        // Optimistic update
        _uiState.update { state ->
            state.copy(posts = state.posts.map { post ->
                if (post.id == postId) post.copy(sharesCount = post.sharesCount + 1) else post
            })
        }
        viewModelScope.launch { firebaseService.incrementPostField(postId, "sharesCount") }
    }
}
