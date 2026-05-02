package com.huabu.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.model.MediaTrack
import com.huabu.app.data.model.Post
import com.huabu.app.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val results: List<User> = emptyList(),
    val trendingUsers: List<User> = emptyList(),
    val postResults: List<Post> = emptyList(),
    val tagResults: List<Post> = emptyList(),
    val trackResults: List<MediaTrack> = emptyList(),
    val trendingTracks: List<MediaTrack> = emptyList(),
    val trendingTags: List<Pair<String, Int>> = emptyList(),
    val currentUserId: String = "",
    val friendIds: Set<String> = emptySet(),
    val pendingSentIds: Set<String> = emptySet(),
    val followingIds: Set<String> = emptySet(),
    val isSearching: Boolean = false,
    val hasQuery: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        val uid = authService.getCurrentUserId() ?: ""
        _uiState.value = _uiState.value.copy(currentUserId = uid)
        if (uid.isNotEmpty()) loadFollowingIds(uid)
        loadTrending()
        loadTrendingTracks()
        loadTrendingTags()
    }

    private fun loadTrendingTags() {
        viewModelScope.launch {
            val tags = firebaseService.getTrendingTags().getOrDefault(emptyList())
            _uiState.update { it.copy(trendingTags = tags) }
        }
    }

    private fun loadTrendingTracks() {
        viewModelScope.launch {
            val tracks = firebaseService.getTrendingTracks().getOrDefault(emptyList())
            _uiState.update { it.copy(trendingTracks = tracks) }
        }
    }

    private fun loadTrending() {
        val currentUserId = authService.getCurrentUserId()
        viewModelScope.launch {
            val result = firebaseService.searchUsers("")
            val users = result.getOrDefault(emptyList()).filter { it.id != currentUserId }.take(10)
            _uiState.update { it.copy(trendingUsers = users) }
        }
    }

    fun onQueryChanged(query: String, category: String = "People") {
        if (query.isBlank()) {
            searchJob?.cancel()
            _uiState.update { it.copy(
                results = emptyList(), postResults = emptyList(),
                tagResults = emptyList(), hasQuery = false, isSearching = false
            )}
            return
        }

        _uiState.update { it.copy(hasQuery = true, isSearching = true) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            val currentUserId = authService.getCurrentUserId()
            when (category) {
                "Posts" -> {
                    val posts = firebaseService.searchPosts(query).getOrDefault(emptyList())
                    _uiState.update { it.copy(postResults = posts, isSearching = false) }
                }
                "Tags" -> {
                    val posts = firebaseService.getPostsByTag(query).getOrDefault(emptyList())
                    _uiState.update { it.copy(tagResults = posts, isSearching = false) }
                }
                "Music" -> {
                    val tracks = firebaseService.searchTracks(query).getOrDefault(emptyList())
                    _uiState.update { it.copy(trackResults = tracks, isSearching = false) }
                }
                else -> {
                    val users = firebaseService.searchUsers(query).getOrDefault(emptyList())
                        .filter { it.id != currentUserId }
                    _uiState.update { it.copy(results = users, isSearching = false) }
                }
            }
        }
    }

    private fun loadFollowingIds(userId: String) {
        viewModelScope.launch {
            val following = firebaseService.getFollowingIds(userId)
            _uiState.update { it.copy(followingIds = following) }
        }
    }

    fun toggleFollow(toUserId: String) {
        val fromUserId = authService.getCurrentUserId() ?: return
        val isFollowing = toUserId in _uiState.value.followingIds
        _uiState.update { it.copy(
            followingIds = if (isFollowing) it.followingIds - toUserId else it.followingIds + toUserId
        )}
        viewModelScope.launch {
            if (isFollowing) firebaseService.unfollowUser(fromUserId, toUserId)
            else firebaseService.followUser(fromUserId, toUserId)
        }
    }

    fun sendFriendRequest(toUserId: String) {
        val fromUserId = authService.getCurrentUserId() ?: return
        _uiState.update { it.copy(pendingSentIds = it.pendingSentIds + toUserId) }
        viewModelScope.launch { firebaseService.sendFriendRequest(fromUserId, toUserId) }
    }

    fun getConversationForUser(otherUserId: String, onResult: (String) -> Unit) {
        val currentUserId = authService.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = firebaseService.getOrCreateConversation(currentUserId, otherUserId)
            result.getOrNull()?.let { onResult(it) }
        }
    }

    fun reactToPost(postId: String, emoji: String) {
        val userId = authService.getCurrentUserId() ?: return
        fun toggle(posts: List<Post>) = posts.map { post ->
            if (post.id != postId) post else {
                val currentList = post.reactedBy[emoji]?.toMutableList() ?: mutableListOf()
                val alreadyReacted = userId in currentList
                if (alreadyReacted) currentList.remove(userId) else currentList.add(userId)
                val newReactedBy = post.reactedBy.toMutableMap().also { it[emoji] = currentList }
                post.also { it.reactedBy = newReactedBy; it.reactions = newReactedBy.mapValues { e -> e.value.size } }
            }
        }
        _uiState.update { it.copy(postResults = toggle(it.postResults), tagResults = toggle(it.tagResults)) }
        viewModelScope.launch { firebaseService.reactToPost(postId, userId, emoji) }
    }

    fun likePost(postId: String) {
        val userId = authService.getCurrentUserId() ?: return
        fun toggle(posts: List<Post>) = posts.map { p ->
            if (p.id != postId) p else {
                val nowLiked = userId !in p.likedBy
                p.copy(
                    isLiked = nowLiked,
                    likesCount = if (nowLiked) p.likesCount + 1 else (p.likesCount - 1).coerceAtLeast(0)
                ).apply { likedBy = if (nowLiked) p.likedBy + userId else p.likedBy - userId }
            }
        }
        _uiState.update { it.copy(postResults = toggle(it.postResults), tagResults = toggle(it.tagResults)) }
        viewModelScope.launch { firebaseService.likePost(postId, userId) }
    }
}
