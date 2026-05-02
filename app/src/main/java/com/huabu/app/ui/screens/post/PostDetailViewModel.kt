package com.huabu.app.ui.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.model.Comment
import com.huabu.app.data.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val currentUserId: String = "",
    val currentUserName: String = "",
    val currentUserImageUrl: String = "",
    val replyingTo: Comment? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    fun loadPost(postId: String) {
        val userId = authService.getCurrentUserId() ?: ""
        viewModelScope.launch {
            val user = firebaseService.getUser(userId).getOrNull()
            _uiState.update {
                it.copy(
                    currentUserId = userId,
                    currentUserName = user?.displayName ?: "",
                    currentUserImageUrl = user?.profileImageUrl ?: ""
                )
            }
            // Stream post changes
            firebaseService.getPostFlow(postId)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { post ->
                    _uiState.update { it.copy(post = post, isLoading = false) }
                }
        }
        // Stream comments
        viewModelScope.launch {
            firebaseService.getCommentsFlow(postId)
                .catch { }
                .collect { comments ->
                    _uiState.update { it.copy(comments = comments) }
                }
        }
    }

    fun reactToPost(postId: String, emoji: String) {
        val userId = authService.getCurrentUserId() ?: return
        _uiState.update { s ->
            val post = s.post ?: return@update s
            val currentList = post.reactedBy[emoji]?.toMutableList() ?: mutableListOf()
            val alreadyReacted = userId in currentList
            if (alreadyReacted) currentList.remove(userId) else currentList.add(userId)
            val newReactedBy = post.reactedBy.toMutableMap().also { it[emoji] = currentList }
            val newReactions = newReactedBy.mapValues { it.value.size }
            s.copy(post = post.also { it.reactedBy = newReactedBy; it.reactions = newReactions })
        }
        viewModelScope.launch { firebaseService.reactToPost(postId, userId, emoji) }
    }

    fun likePost(postId: String) {
        val userId = authService.getCurrentUserId() ?: return
        _uiState.update { s ->
            val post = s.post ?: return@update s
            val nowLiked = !post.isLiked
            s.copy(
                post = post.copy(
                    isLiked = nowLiked,
                    likesCount = if (nowLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
                ).apply { likedBy = if (nowLiked) post.likedBy + userId else post.likedBy - userId }
            )
        }
        viewModelScope.launch { firebaseService.likePost(postId, userId) }
    }

    fun addComment(postId: String, content: String) {
        val userId = authService.getCurrentUserId() ?: return
        val state = _uiState.value
        val replyingTo = state.replyingTo
        val comment = Comment(
            postId = postId,
            authorId = userId,
            authorName = state.currentUserName,
            authorUsername = "",
            authorImageUrl = state.currentUserImageUrl,
            content = content,
            parentId = replyingTo?.id ?: "",
            replyToName = replyingTo?.authorName ?: ""
        )
        _uiState.update { s ->
            val post = s.post ?: return@update s
            s.copy(post = post.copy(commentsCount = post.commentsCount + 1), replyingTo = null)
        }
        viewModelScope.launch { firebaseService.addComment(comment) }
    }

    fun setReplyingTo(comment: Comment?) {
        _uiState.update { it.copy(replyingTo = comment) }
    }

    fun likeComment(comment: Comment) {
        val userId = authService.getCurrentUserId() ?: return
        val nowLiked = userId !in comment.likedBy
        _uiState.update { s ->
            s.copy(comments = s.comments.map { c ->
                if (c.id != comment.id) c else c.copy(
                    likedBy = if (nowLiked) c.likedBy + userId else c.likedBy - userId,
                    likesCount = if (nowLiked) c.likesCount + 1 else (c.likesCount - 1).coerceAtLeast(0)
                )
            })
        }
        viewModelScope.launch { firebaseService.likeComment(comment.id, userId) }
    }

    fun deleteComment(comment: Comment) {
        val userId = authService.getCurrentUserId() ?: return
        if (comment.authorId != userId) return
        _uiState.update { s ->
            val post = s.post
            s.copy(
                comments = s.comments.filter { it.id != comment.id },
                post = post?.copy(commentsCount = (post.commentsCount - 1).coerceAtLeast(0))
            )
        }
        viewModelScope.launch {
            firebaseService.deleteComment(comment.id, comment.postId, comment.authorId, userId)
        }
    }
}
