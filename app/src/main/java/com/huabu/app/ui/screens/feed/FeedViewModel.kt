package com.huabu.app.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.local.dao.PostDao
import com.huabu.app.data.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postDao: PostDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            postDao.getAllPosts()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { posts ->
                    _uiState.update {
                        if (posts.isEmpty()) {
                            it.copy(posts = mockPosts(), isLoading = false)
                        } else {
                            it.copy(posts = posts, isLoading = false)
                        }
                    }
                }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadFeed()
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            val post = _uiState.value.posts.find { it.id == postId } ?: return@launch
            val updated = post.copy(
                isLiked = !post.isLiked,
                likesCount = if (!post.isLiked) post.likesCount + 1 else post.likesCount - 1
            )
            postDao.updatePost(updated)
        }
    }

    private fun mockPosts(): List<Post> = listOf(
        Post(
            id = "1", authorId = "u1", authorName = "Xena Starfire", authorUsername = "xenastar",
            content = "Just customized my Huabu page with the new neon theme!! Check it out bestie ✨🌟",
            likesCount = 142, commentsCount = 23, sharesCount = 7,
            tags = "mypage,neonlife,huabu", mood = "😍",
            createdAt = System.currentTimeMillis() - 3_600_000
        ),
        Post(
            id = "2", authorId = "u2", authorName = "DJ Phantom", authorUsername = "djphantom",
            content = "New mixtape dropping midnight!! Been up all night cooking this one fr 🎵🔥 Who's staying up?",
            likesCount = 890, commentsCount = 156, sharesCount = 44,
            tags = "music,hiphop,midnight", mood = "🎵",
            createdAt = System.currentTimeMillis() - 7_200_000
        ),
        Post(
            id = "3", authorId = "u3", authorName = "Glitter Queen", authorUsername = "glitterqueen99",
            content = "Top 8 updated!! If you didn't make the cut... we can still be friends lol 💅✨\n\nJK ily all 🫶",
            likesCount = 211, commentsCount = 88, sharesCount = 12,
            tags = "top8,friends,huabu", mood = "💅",
            createdAt = System.currentTimeMillis() - 14_400_000
        ),
        Post(
            id = "4", authorId = "u4", authorName = "Retro Kid", authorUsername = "retrokid2k",
            content = "Nobody asked but my current song is stuck in my head ALL day. profile song updated 🎧",
            likesCount = 67, commentsCount = 14, sharesCount = 3,
            tags = "music,profilesong,vibes", mood = "🎧",
            createdAt = System.currentTimeMillis() - 86_400_000
        ),
        Post(
            id = "5", authorId = "u5", authorName = "Luna Eclipse", authorUsername = "lunaeclipse",
            content = "Mood: chaotic but make it aesthetic 🌙✨\n\nUpdated my About Me section if anyone cares lol",
            likesCount = 334, commentsCount = 41, sharesCount = 19,
            tags = "aesthetic,moody,vibes", mood = "🌙",
            createdAt = System.currentTimeMillis() - 43_200_000
        )
    )
}
