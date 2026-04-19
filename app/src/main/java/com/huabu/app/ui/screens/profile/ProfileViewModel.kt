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
    val liveStream: LiveStream = LiveStream(""),
    val events: List<ProfileEvent> = emptyList(),
    val badges: List<Badge> = emptyList(),
    val moodBoard: List<MoodBoardItem> = emptyList(),
    val pinnedPosts: List<Post> = emptyList(),
    val recentTracks: List<RecentTrack> = emptyList(),
    val playlist: List<PlaylistItem> = emptyList(),
    val currentlyReading: CurrentlyReading? = null,
    val currentlyWatching: CurrentlyWatching? = null,
    val nfts: List<NftItem> = emptyList(),
    val polls: List<ProfilePoll> = emptyList(),
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
    private val profileThemeDao: ProfileThemeDao,
    private val liveStreamDao: LiveStreamDao,
    private val profileEventDao: ProfileEventDao,
    private val badgeDao: BadgeDao,
    private val moodBoardDao: MoodBoardDao,
    private val pinnedPostDao: PinnedPostDao,
    private val recentTrackDao: RecentTrackDao,
    private val playlistItemDao: PlaylistItemDao,
    private val currentlyReadingDao: CurrentlyReadingDao,
    private val currentlyWatchingDao: CurrentlyWatchingDao,
    private val nftItemDao: NftItemDao,
    private val profilePollDao: ProfilePollDao
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

        viewModelScope.launch {
            liveStreamDao.getLiveStreamForUser(resolvedId).collect { stream ->
                _uiState.update { it.copy(liveStream = stream ?: LiveStream(resolvedId)) }
            }
        }

        viewModelScope.launch {
            profileEventDao.getEventsForUser(resolvedId).collect { events ->
                _uiState.update { it.copy(events = events) }
            }
        }

        viewModelScope.launch {
            badgeDao.getBadgesForUser(resolvedId).collect { badges ->
                val shown = if (badges.isEmpty()) getMockBadges(resolvedId) else badges
                _uiState.update { it.copy(badges = shown) }
            }
        }

        viewModelScope.launch {
            moodBoardDao.getMoodBoardForUser(resolvedId).collect { items ->
                _uiState.update { it.copy(moodBoard = items) }
            }
        }

        viewModelScope.launch {
            recentTrackDao.getRecentTracks(resolvedId).collect { tracks ->
                val shown = if (tracks.isEmpty()) getMockRecentTracks(resolvedId) else tracks
                _uiState.update { it.copy(recentTracks = shown) }
            }
        }

        viewModelScope.launch {
            playlistItemDao.getPlaylist(resolvedId).collect { items ->
                _uiState.update { it.copy(playlist = items) }
            }
        }

        viewModelScope.launch {
            currentlyReadingDao.getForUser(resolvedId).collect { book ->
                _uiState.update { it.copy(currentlyReading = book) }
            }
        }

        viewModelScope.launch {
            currentlyWatchingDao.getForUser(resolvedId).collect { show ->
                _uiState.update { it.copy(currentlyWatching = show) }
            }
        }

        viewModelScope.launch {
            nftItemDao.getNftsForUser(resolvedId).collect { nfts ->
                val shown = if (nfts.isEmpty()) getMockNfts(resolvedId) else nfts
                _uiState.update { it.copy(nfts = shown) }
            }
        }

        viewModelScope.launch {
            profilePollDao.getActivePolls(resolvedId).collect { polls ->
                _uiState.update { it.copy(polls = polls) }
            }
        }

        viewModelScope.launch {
            pinnedPostDao.getPinnedPostsForUser(resolvedId).collect { pins ->
                val posts = _uiState.value.posts
                val pinned = pins.mapNotNull { pin -> posts.find { it.id == pin.postId } }
                _uiState.update { it.copy(pinnedPosts = pinned) }
            }
        }
    }

    fun updateMoodBoardCell(item: MoodBoardItem) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = item.copy(userId = userId)
        viewModelScope.launch { moodBoardDao.upsertItem(withUser) }
    }

    fun pinPost(post: Post) {
        val userId = _uiState.value.user?.id ?: return
        val current = _uiState.value.pinnedPosts
        if (current.size >= 3) return
        val pin = PinnedPost(
            id = "pin_${userId}_${post.id}",
            userId = userId,
            postId = post.id,
            pinOrder = current.size
        )
        _uiState.update { it.copy(pinnedPosts = it.pinnedPosts + post) }
        viewModelScope.launch { pinnedPostDao.pinPost(pin) }
    }

    fun unpinPost(post: Post) {
        val userId = _uiState.value.user?.id ?: return
        _uiState.update { it.copy(pinnedPosts = it.pinnedPosts.filter { p -> p.id != post.id }) }
        viewModelScope.launch { pinnedPostDao.unpinPost(post.id, userId) }
    }

    fun goLive(title: String) {
        val userId = _uiState.value.liveStream.userId.ifEmpty {
            _uiState.value.user?.id ?: return
        }
        val stream = LiveStream(userId = userId, isLive = true, title = title, viewerCount = 0, startedAt = System.currentTimeMillis())
        _uiState.update { it.copy(liveStream = stream) }
        viewModelScope.launch { liveStreamDao.upsertLiveStream(stream) }
    }

    fun endLive() {
        val stream = _uiState.value.liveStream.copy(isLive = false, viewerCount = 0)
        _uiState.update { it.copy(liveStream = stream) }
        viewModelScope.launch { liveStreamDao.upsertLiveStream(stream) }
    }

    fun addEvent(event: ProfileEvent) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = event.copy(userId = userId)
        viewModelScope.launch { profileEventDao.upsertEvent(withUser) }
    }

    fun deleteEvent(event: ProfileEvent) {
        viewModelScope.launch { profileEventDao.deleteEvent(event) }
    }

    fun rsvpEvent(event: ProfileEvent) {
        val updated = event.copy(
            hasRsvped = !event.hasRsvped,
            rsvpCount = if (event.hasRsvped) (event.rsvpCount - 1).coerceAtLeast(0) else event.rsvpCount + 1
        )
        viewModelScope.launch { profileEventDao.upsertEvent(updated) }
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

    fun addPlaylistItem(item: PlaylistItem) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = item.copy(userId = userId, sortOrder = _uiState.value.playlist.size)
        viewModelScope.launch { playlistItemDao.upsertItem(withUser) }
    }

    fun deletePlaylistItem(item: PlaylistItem) {
        viewModelScope.launch { playlistItemDao.deleteItem(item.id) }
    }

    fun saveCurrentlyReading(book: CurrentlyReading) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = book.copy(userId = userId)
        _uiState.update { it.copy(currentlyReading = withUser) }
        viewModelScope.launch { currentlyReadingDao.upsert(withUser) }
    }

    fun clearCurrentlyReading() {
        val userId = _uiState.value.user?.id ?: return
        _uiState.update { it.copy(currentlyReading = null) }
        viewModelScope.launch { currentlyReadingDao.delete(userId) }
    }

    fun saveCurrentlyWatching(show: CurrentlyWatching) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = show.copy(userId = userId)
        _uiState.update { it.copy(currentlyWatching = withUser) }
        viewModelScope.launch { currentlyWatchingDao.upsert(withUser) }
    }

    fun clearCurrentlyWatching() {
        val userId = _uiState.value.user?.id ?: return
        _uiState.update { it.copy(currentlyWatching = null) }
        viewModelScope.launch { currentlyWatchingDao.delete(userId) }
    }

    fun addNft(nft: NftItem) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = nft.copy(userId = userId)
        viewModelScope.launch { nftItemDao.insertNft(withUser) }
    }

    fun deleteNft(nft: NftItem) {
        viewModelScope.launch { nftItemDao.deleteNft(nft.id) }
    }

    fun createPoll(poll: ProfilePoll) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = poll.copy(userId = userId)
        viewModelScope.launch { profilePollDao.upsertPoll(withUser) }
    }

    fun deletePoll(poll: ProfilePoll) {
        viewModelScope.launch { profilePollDao.deletePoll(poll.id) }
    }

    fun voteOnPoll(pollId: String, option: Char) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            profilePollDao.recordVote(PollVote(pollId, userId, option))
            when (option) {
                'A' -> profilePollDao.voteA(pollId)
                'B' -> profilePollDao.voteB(pollId)
                'C' -> profilePollDao.voteC(pollId)
                'D' -> profilePollDao.voteD(pollId)
            }
        }
    }

    private fun getMockNfts(userId: String): List<NftItem> = listOf(
        NftItem("nft1", userId, "Cosmic Voyager #1337", "Stargaze", NftChain.SOLANA, 2.5f, 1.8f),
        NftItem("nft2", userId, "Bored Ape #9999", "BAYC", NftChain.ETHEREUM, 45.0f, 42.0f),
        NftItem("nft3", userId, "Cyber Punk #2049", "CyberPunks", NftChain.POLYGON, 0.8f, 0.5f),
    )

    private fun getMockRecentTracks(userId: String): List<RecentTrack> = listOf(
        RecentTrack("rt1", userId, "Blinding Lights",      "The Weeknd",      "#8B0000", System.currentTimeMillis() - 600_000),
        RecentTrack("rt2", userId, "Levitating",           "Dua Lipa",        "#8B5CF6", System.currentTimeMillis() - 3_600_000),
        RecentTrack("rt3", userId, "Stay",                 "The Kid LAROI",   "#06B6D4", System.currentTimeMillis() - 7_200_000),
        RecentTrack("rt4", userId, "Heat Waves",           "Glass Animals",   "#F97316", System.currentTimeMillis() - 14_400_000),
        RecentTrack("rt5", userId, "As It Was",            "Harry Styles",    "#EC4899", System.currentTimeMillis() - 86_400_000),
    )

    private fun getMockBadges(userId: String): List<Badge> = listOf(
        Badge("og_member",       userId, "OG Member",       "⭐", "One of the first to join Huabu",       BadgeRarity.LEGENDARY),
        Badge("music_lover",     userId, "Music Lover",     "🎵", "Added a profile song",                 BadgeRarity.COMMON),
        Badge("customiser",      userId, "Customiser",      "🎨", "Customised their profile theme",       BadgeRarity.COMMON),
        Badge("social_butterfly",userId, "Social Butterfly","🦋", "Has 100+ friends",                     BadgeRarity.RARE),
        Badge("creator",         userId, "Creator",         "🎨", "Shared original creative content",     BadgeRarity.EPIC),
        Badge("popular",         userId, "Popular",         "💫", "Post with 50+ likes",                  BadgeRarity.EPIC),
    )
}
