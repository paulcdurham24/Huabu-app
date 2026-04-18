package com.huabu.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.local.dao.FriendDao
import com.huabu.app.data.local.dao.PostDao
import com.huabu.app.data.local.dao.UserDao
import com.huabu.app.data.model.Friend
import com.huabu.app.data.model.Post
import com.huabu.app.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val topFriends: List<Friend> = emptyList(),
    val isLoading: Boolean = true,
    val isCurrentUser: Boolean = false,
    val isFollowing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val friendDao: FriendDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        val resolvedId = if (userId == "me") "current_user" else userId
        val isMe = userId == "me" || userId == "current_user"

        viewModelScope.launch {
            combine(
                postDao.getPostsByUser(resolvedId),
                friendDao.getTopFriends(resolvedId)
            ) { posts, friends ->
                val mockUser = getMockUser(resolvedId, isMe)
                _uiState.update {
                    it.copy(
                        user = mockUser,
                        posts = if (posts.isEmpty()) getMockPosts(resolvedId, mockUser) else posts,
                        topFriends = if (friends.isEmpty()) getMockFriends(resolvedId) else friends,
                        isLoading = false,
                        isCurrentUser = isMe
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }.collect()
        }
    }

    fun toggleFollow() {
        _uiState.update { it.copy(isFollowing = !it.isFollowing) }
    }

    private fun getMockUser(userId: String, isMe: Boolean): User = if (isMe) {
        User(
            id = "current_user",
            username = "you",
            displayName = "Your Name",
            bio = "★ living life one page at a time ★",
            mood = "😎 chillin",
            location = "Somewhere Cool",
            profileColor1 = "#6A0572",
            profileColor2 = "#FF006E",
            aboutMe = "Hey!! I'm on Huabu!! Add me as a friend and check out my page 🌟\n\nI love music, art, and vibing with cool people.",
            heroesSection = "My heroes are people who dare to be different ✨",
            interests = "Music, Art, Gaming, Anime, Fashion",
            profileSong = "Neon Dreams",
            profileSongArtist = "Synthwave Collective",
            followersCount = 248,
            followingCount = 183,
            postsCount = 42,
            isOnline = true
        )
    } else {
        User(
            id = userId,
            username = "user_$userId",
            displayName = "Huabu User",
            bio = "✨ just here vibing ✨",
            mood = "🎵 listening to music",
            location = "The Internet",
            profileColor1 = "#0077FF",
            profileColor2 = "#39FF14",
            aboutMe = "I'm on Huabu! Let's be friends 🤝",
            heroesSection = "People who keep it real 💯",
            interests = "Music, Movies, Friends",
            profileSong = "Unknown Horizons",
            profileSongArtist = "Artist Unknown",
            followersCount = 112,
            followingCount = 89,
            postsCount = 18
        )
    }

    private fun getMockPosts(userId: String, user: User): List<Post> = listOf(
        Post(
            id = "p1_$userId", authorId = userId,
            authorName = user.displayName, authorUsername = user.username,
            content = "Updated my profile song 🎵 check it out!!",
            likesCount = 34, commentsCount = 7, mood = "🎵",
            createdAt = System.currentTimeMillis() - 3_600_000
        ),
        Post(
            id = "p2_$userId", authorId = userId,
            authorName = user.displayName, authorUsername = user.username,
            content = "Just got my top 8 sorted out! You know who you are 💖",
            likesCount = 89, commentsCount = 22, mood = "💖",
            createdAt = System.currentTimeMillis() - 86_400_000
        )
    )

    private fun getMockFriends(userId: String): List<Friend> = listOf(
        Friend("f1", userId, "u_a", "Xena Starfire", "xenastar", isTopFriend = true, topFriendRank = 1),
        Friend("f2", userId, "u_b", "DJ Phantom", "djphantom", isTopFriend = true, topFriendRank = 2),
        Friend("f3", userId, "u_c", "Luna Eclipse", "lunaeclipse", isTopFriend = true, topFriendRank = 3),
        Friend("f4", userId, "u_d", "Retro Kid", "retrokid2k", isTopFriend = true, topFriendRank = 4),
        Friend("f5", userId, "u_e", "Glitter Queen", "glitterqueen99", isTopFriend = true, topFriendRank = 5),
        Friend("f6", userId, "u_f", "Neon Ninja", "neonninja", isTopFriend = true, topFriendRank = 6),
        Friend("f7", userId, "u_g", "Star Chaser", "starchaser", isTopFriend = true, topFriendRank = 7),
        Friend("f8", userId, "u_h", "Pixel Prince", "pixelprince", isTopFriend = true, topFriendRank = 8)
    )
}
