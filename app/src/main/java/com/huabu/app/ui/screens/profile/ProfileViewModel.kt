package com.huabu.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.local.dao.*
import com.huabu.app.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val topFriends: List<Friend> = emptyList(),
    val photos: List<ProfilePhoto> = emptyList(),
    val videoLinks: List<VideoLink> = emptyList(),
    val topMusic: List<MediaTrack> = emptyList(),
    val topFilms: List<MediaTrack> = emptyList(),
    val widgetSettings: ProfileWidgetSettings = ProfileWidgetSettings(""),
    val theme: ProfileTheme = ProfileTheme(""),
    val isLoading: Boolean = true,
    val isCurrentUser: Boolean = false,
    val isFollowing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val friendDao: FriendDao,
    private val profilePhotoDao: ProfilePhotoDao,
    private val videoLinkDao: VideoLinkDao,
    private val mediaTrackDao: MediaTrackDao,
    private val profileWidgetSettingsDao: ProfileWidgetSettingsDao,
    private val profileThemeDao: ProfileThemeDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        val resolvedId = if (userId == "me") "current_user" else userId
        val isMe = userId == "me" || userId == "current_user"

        viewModelScope.launch {
            combine(
                postDao.getPostsByUser(resolvedId),
                friendDao.getTopFriends(resolvedId),
                profilePhotoDao.getPhotosForUser(resolvedId),
                videoLinkDao.getVideoLinksForUser(resolvedId),
                profileWidgetSettingsDao.getSettingsForUser(resolvedId)
            ) { posts, friends, photos, videos, settings ->
                val mockUser = getMockUser(resolvedId, isMe)
                _uiState.update {
                    it.copy(
                        user = mockUser,
                        posts = if (posts.isEmpty()) getMockPosts(resolvedId, mockUser) else posts,
                        topFriends = if (friends.isEmpty()) getMockFriends(resolvedId) else friends,
                        photos = if (photos.isEmpty()) getMockPhotos(resolvedId) else photos,
                        videoLinks = if (videos.isEmpty()) getMockVideos(resolvedId) else videos,
                        widgetSettings = settings ?: ProfileWidgetSettings(resolvedId),
                        isLoading = false,
                        isCurrentUser = isMe
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }.collect()
        }

        viewModelScope.launch {
            combine(
                mediaTrackDao.getTracksForUser(resolvedId, MediaTrackType.MUSIC),
                mediaTrackDao.getTracksForUser(resolvedId, MediaTrackType.FILM)
            ) { music, films ->
                _uiState.update {
                    it.copy(
                        topMusic = if (music.isEmpty()) getMockMusic(resolvedId) else music,
                        topFilms = if (films.isEmpty()) getMockFilms(resolvedId) else films
                    )
                }
            }.catch { }.collect()
        }

        viewModelScope.launch {
            profileThemeDao.getThemeForUser(resolvedId).collect { theme ->
                _uiState.update { it.copy(theme = theme ?: ProfileTheme(resolvedId)) }
            }
        }
    }

    fun saveTheme(theme: ProfileTheme) {
        viewModelScope.launch {
            profileThemeDao.upsertTheme(theme)
        }
    }

    fun saveUser(user: User) {
        _uiState.update { it.copy(user = user) }
        viewModelScope.launch {
            userDao.insertUser(user)
        }
    }

    fun toggleFollow() {
        _uiState.update { it.copy(isFollowing = !it.isFollowing) }
    }

    fun toggleWidget(toggle: (ProfileWidgetSettings) -> ProfileWidgetSettings) {
        val current = _uiState.value.widgetSettings
        val updated = toggle(current)
        _uiState.update { it.copy(widgetSettings = updated) }
        viewModelScope.launch {
            profileWidgetSettingsDao.upsertSettings(updated)
        }
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

    private fun getMockPhotos(userId: String): List<ProfilePhoto> = listOf(
        ProfilePhoto("ph1", userId, "", "My favourite memory ✨", PhotoFrameStyle.RAINBOW_GLOW, 0),
        ProfilePhoto("ph2", userId, "", "Summer vibes 🌞", PhotoFrameStyle.NEON_PINK, 1),
        ProfilePhoto("ph3", userId, "", "Art I made 🎨", PhotoFrameStyle.COSMIC_PURPLE, 2)
    )

    private fun getMockVideos(userId: String): List<VideoLink> = listOf(
        VideoLink("v1", userId, "My Favourite Music Video", "https://youtube.com/watch?v=dQw4w9WgXcQ", "", "This track slaps 🔥", 0),
        VideoLink("v2", userId, "Best Movie Trailer Ever", "https://youtube.com/watch?v=abc123", "", "Cannot wait for this film 🎬", 1)
    )

    private fun getMockMusic(userId: String): List<MediaTrack> = listOf(
        MediaTrack("m1", userId, MediaTrackType.MUSIC, "Blinding Lights", "The Weeknd", "", "2019", 1),
        MediaTrack("m2", userId, MediaTrackType.MUSIC, "As It Was", "Harry Styles", "", "2022", 2),
        MediaTrack("m3", userId, MediaTrackType.MUSIC, "Levitating", "Dua Lipa", "", "2020", 3),
        MediaTrack("m4", userId, MediaTrackType.MUSIC, "Heat Waves", "Glass Animals", "", "2020", 4),
        MediaTrack("m5", userId, MediaTrackType.MUSIC, "Neon Dreams", "Synthwave Collective", "", "2023", 5)
    )

    private fun getMockFilms(userId: String): List<MediaTrack> = listOf(
        MediaTrack("fi1", userId, MediaTrackType.FILM, "Interstellar", "Christopher Nolan", "", "2014", 1),
        MediaTrack("fi2", userId, MediaTrackType.FILM, "Blade Runner 2049", "Denis Villeneuve", "", "2017", 2),
        MediaTrack("fi3", userId, MediaTrackType.FILM, "Everything Everywhere All at Once", "Daniels", "", "2022", 3),
        MediaTrack("fi4", userId, MediaTrackType.FILM, "Parasite", "Bong Joon-ho", "", "2019", 4),
        MediaTrack("fi5", userId, MediaTrackType.FILM, "Dune", "Denis Villeneuve", "", "2021", 5)
    )
}
