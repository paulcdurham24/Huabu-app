package com.huabu.app.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.huabu.app.data.local.dao.*
import com.huabu.app.data.model.*
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.StorageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileCommentsState(
    val postId: String = "",
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false
)

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
    val votedPollOptions: Map<String, Char> = emptyMap(),
    val codeSnippets: List<CodeSnippet> = emptyList(),
    val techStack: List<TechStackItem> = emptyList(),
    val gifs: List<GifItem> = emptyList(),
    val spotifyTrack: SpotifyTrack? = null,
    val memes: List<MemeItem> = emptyList(),
    val gameStats: List<GameStats> = emptyList(),
    val visitedPlaces: List<VisitedPlace> = emptyList(),
    val travelWishes: List<TravelWish> = emptyList(),
    val ticTacToeGames: List<TicTacToeGame> = emptyList(),
    val minesweeperGames: List<MinesweeperGame> = emptyList(),
    val pendingGameInvites: List<GameInvite> = emptyList(),
    val savedPosts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val isCurrentUser: Boolean = false,
    val isFollowing: Boolean = false,
    val isBlocked: Boolean = false,
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
    private val profilePollDao: ProfilePollDao,
    private val codeSnippetDao: CodeSnippetDao,
    private val techStackDao: TechStackDao,
    private val gifItemDao: GifItemDao,
    private val spotifyTrackDao: SpotifyTrackDao,
    private val memeItemDao: MemeItemDao,
    private val gameStatsDao: GameStatsDao,
    private val visitedPlaceDao: VisitedPlaceDao,
    private val travelWishDao: TravelWishDao,
    private val ticTacToeDao: TicTacToeDao,
    private val minesweeperDao: MinesweeperDao,
    private val gameInviteDao: GameInviteDao,
    private val firebaseService: FirebaseService,
    private val storageService: StorageService,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _commentsState = MutableStateFlow(ProfileCommentsState())
    val commentsState: StateFlow<ProfileCommentsState> = _commentsState.asStateFlow()

    fun loadProfile(userId: String) {
        val currentUserId = authService.getCurrentUserId()
        val resolvedId = if (userId == "me") currentUserId ?: userId else userId
        val isMe = userId == "me" || userId == currentUserId
        
        Log.d("ProfileViewModel", "loadProfile() called - userId=$userId, currentUserId=$currentUserId, resolvedId=$resolvedId, isMe=$isMe")

        // Load pinned posts
        viewModelScope.launch {
            val pinned = firebaseService.getPinnedPosts(resolvedId).getOrDefault(emptyList())
            _uiState.update { it.copy(pinnedPosts = pinned) }
        }

        // Load polls
        loadPolls(resolvedId)

        // Record profile view (increments counter for non-self visits)
        if (!isMe) recordProfileView(resolvedId)

        // Check if blocked (non-self only)
        if (!isMe) {
            val currentUid = authService.getCurrentUserId() ?: ""
            if (currentUid.isNotEmpty()) {
                viewModelScope.launch {
                    val blocked = firebaseService.isUserBlocked(currentUid, resolvedId)
                    _uiState.update { it.copy(isBlocked = blocked) }
                }
            }
        }

        // Stream live user from Firebase (also gets initial load + view count updates)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            firebaseService.getUserFlow(resolvedId)
                .catch { e ->
                    if (!isMe) {
                        val mockUser = getMockUser(resolvedId, isMe)
                        _uiState.update { it.copy(user = mockUser, isCurrentUser = isMe, isLoading = false, error = e.message) }
                    } else {
                        _uiState.update { it.copy(isCurrentUser = true, isLoading = false, error = e.message) }
                    }
                }
                .collect { user ->
                    if (user != null) {
                        _uiState.update { it.copy(user = user, isCurrentUser = isMe, isLoading = false) }
                    } else if (!isMe) {
                        val mockUser = getMockUser(resolvedId, isMe)
                        _uiState.update { it.copy(user = mockUser, isCurrentUser = isMe, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(isCurrentUser = true, isLoading = false) }
                    }
                }
        }

        // Posts from Firestore real-time
        viewModelScope.launch {
            firebaseService.getUserPostsFlow(resolvedId)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { posts -> _uiState.update { it.copy(posts = posts) } }
        }

        // Top friends from Firestore
        viewModelScope.launch {
            firebaseService.getFriendsFlow(resolvedId)
                .catch { }
                .collect { friends ->
                    val friendUsers = friends.take(8).mapNotNull { f ->
                        val u = firebaseService.getUser(f.friendId).getOrNull() ?: return@mapNotNull null
                        Friend(
                            id = f.id,
                            userId = resolvedId,
                            friendId = f.friendId,
                            friendName = u.displayName,
                            friendUsername = u.username,
                            friendImageUrl = u.profileImageUrl,
                            status = "accepted",
                            isTopFriend = true,
                            topFriendRank = friends.indexOf(f) + 1
                        )
                    }
                    _uiState.update { it.copy(topFriends = friendUsers) }
                }
        }

        viewModelScope.launch {
            combine(
                profilePhotoDao.getPhotosForUser(resolvedId),
                videoLinkDao.getVideoLinksForUser(resolvedId),
                profileWidgetSettingsDao.getSettingsForUser(resolvedId)
            ) { photos, videos, settings ->
                _uiState.update {
                    it.copy(
                        photos = photos,
                        videoLinks = videos,
                        widgetSettings = settings ?: ProfileWidgetSettings(resolvedId),
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(error = e.message) }
            }.collect()
        }

        viewModelScope.launch {
            combine(
                mediaTrackDao.getTracksForUser(resolvedId, MediaTrackType.MUSIC),
                mediaTrackDao.getTracksForUser(resolvedId, MediaTrackType.FILM)
            ) { music, films ->
                _uiState.update {
                    it.copy(
                        topMusic = if (music.isEmpty() && !isMe) getMockMusic(resolvedId) else music,
                        topFilms = if (films.isEmpty() && !isMe) getMockFilms(resolvedId) else films
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
                val shown = if (badges.isEmpty() && !isMe) getMockBadges(resolvedId) else badges
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
                val shown = if (tracks.isEmpty() && !isMe) getMockRecentTracks(resolvedId) else tracks
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
                val shown = if (nfts.isEmpty() && !isMe) getMockNfts(resolvedId) else nfts
                _uiState.update { it.copy(nfts = shown) }
            }
        }

        viewModelScope.launch {
            profilePollDao.getActivePolls(resolvedId).collect { polls ->
                val voterId = authService.getCurrentUserId() ?: ""
                val voted = if (voterId.isNotEmpty()) {
                    polls.mapNotNull { poll ->
                        val option = profilePollDao.getUserVote(poll.id, voterId)
                        if (option != null) poll.id to option else null
                    }.toMap()
                } else emptyMap()
                _uiState.update { it.copy(polls = polls, votedPollOptions = it.votedPollOptions + voted) }
            }
        }

        viewModelScope.launch {
            codeSnippetDao.getSnippetsForUser(resolvedId).collect { snippets ->
                val shown = if (snippets.isEmpty() && !isMe) getMockSnippets(resolvedId) else snippets
                _uiState.update { it.copy(codeSnippets = shown) }
            }
        }

        viewModelScope.launch {
            techStackDao.getTechStackForUser(resolvedId).collect { items ->
                val shown = if (items.isEmpty() && !isMe) getMockTechStack(resolvedId) else items
                _uiState.update { it.copy(techStack = shown) }
            }
        }

        viewModelScope.launch {
            gifItemDao.getGifsForUser(resolvedId).collect { gifs ->
                _uiState.update { it.copy(gifs = gifs) }
            }
        }

        viewModelScope.launch {
            spotifyTrackDao.getCurrentTrack(resolvedId).collect { track ->
                _uiState.update { it.copy(spotifyTrack = track) }
            }
        }

        viewModelScope.launch {
            memeItemDao.getMemesForUser(resolvedId).collect { memes ->
                _uiState.update { it.copy(memes = memes) }
            }
        }

        viewModelScope.launch {
            gameStatsDao.getAllGameStats(resolvedId).collect { stats ->
                _uiState.update { it.copy(gameStats = stats) }
            }
        }

        viewModelScope.launch {
            visitedPlaceDao.getVisitedPlaces(resolvedId).collect { places ->
                _uiState.update { it.copy(visitedPlaces = places) }
            }
        }

        viewModelScope.launch {
            travelWishDao.getTravelWishes(resolvedId).collect { wishes ->
                _uiState.update { it.copy(travelWishes = wishes) }
            }
        }

        viewModelScope.launch {
            ticTacToeDao.getGamesForUser(resolvedId).collect { games ->
                _uiState.update { it.copy(ticTacToeGames = games) }
            }
        }

        viewModelScope.launch {
            gameInviteDao.getPendingInvitesForUser(resolvedId).collect { invites ->
                _uiState.update { it.copy(pendingGameInvites = invites) }
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
            firebaseService.updateUser(
                user.id,
                mapOf(
                    "displayName" to user.displayName,
                    "username" to user.username,
                    "bio" to user.bio,
                    "location" to user.location,
                    "website" to user.website,
                    "mood" to user.mood,
                    "aboutMe" to user.aboutMe,
                    "heroesSection" to user.heroesSection,
                    "interests" to user.interests,
                    "profileSong" to user.profileSong,
                    "profileSongArtist" to user.profileSongArtist
                )
            )
        }
    }

    fun likePost(postId: String) {
        val userId = authService.getCurrentUserId() ?: return
        _uiState.update { s ->
            s.copy(posts = s.posts.map { p ->
                if (p.id == postId) {
                    val nowLiked = !p.isLiked
                    p.copy(
                        isLiked = nowLiked,
                        likesCount = if (nowLiked) p.likesCount + 1 else (p.likesCount - 1).coerceAtLeast(0)
                    ).apply { likedBy = if (nowLiked) p.likedBy + userId else p.likedBy - userId }
                } else p
            })
        }
        viewModelScope.launch { firebaseService.likePost(postId, userId) }
    }

    fun reactToPost(postId: String, emoji: String) {
        val userId = authService.getCurrentUserId() ?: return
        fun toggle(posts: List<com.huabu.app.data.model.Post>) = posts.map { post ->
            if (post.id != postId) post else {
                val currentList = post.reactedBy[emoji]?.toMutableList() ?: mutableListOf()
                val alreadyReacted = userId in currentList
                if (alreadyReacted) currentList.remove(userId) else currentList.add(userId)
                val newReactedBy = post.reactedBy.toMutableMap().also { it[emoji] = currentList }
                post.also { it.reactedBy = newReactedBy; it.reactions = newReactedBy.mapValues { e -> e.value.size } }
            }
        }
        _uiState.update { it.copy(posts = toggle(it.posts), pinnedPosts = toggle(it.pinnedPosts)) }
        viewModelScope.launch { firebaseService.reactToPost(postId, userId, emoji) }
    }

    fun bookmarkPost(postId: String) {
        val userId = authService.getCurrentUserId() ?: return
        _uiState.update { s ->
            s.copy(
                posts = s.posts.map { if (it.id == postId) it.copy(isBookmarked = !it.isBookmarked) else it },
                pinnedPosts = s.pinnedPosts.map { if (it.id == postId) it.copy(isBookmarked = !it.isBookmarked) else it }
            )
        }
        viewModelScope.launch { firebaseService.toggleBookmark(userId, postId) }
    }

    fun loadSavedPosts() {
        val userId = authService.getCurrentUserId() ?: return
        viewModelScope.launch {
            firebaseService.getSavedPosts(userId).getOrNull()?.let { saved ->
                _uiState.update { it.copy(savedPosts = saved) }
            }
        }
    }

    fun openComments(postId: String) {
        _commentsState.value = ProfileCommentsState(postId = postId, isLoading = true)
        viewModelScope.launch {
            firebaseService.getCommentsFlow(postId)
                .catch { _commentsState.update { it.copy(isLoading = false) } }
                .collect { comments ->
                    _commentsState.update { it.copy(comments = comments, isLoading = false) }
                }
        }
    }

    fun closeComments() {
        _commentsState.value = ProfileCommentsState()
    }

    fun incrementPostField(postId: String, field: String) {
        _uiState.update { s ->
            s.copy(posts = s.posts.map { p ->
                if (p.id == postId && field == "sharesCount") p.copy(sharesCount = p.sharesCount + 1) else p
            })
        }
        viewModelScope.launch { firebaseService.incrementPostField(postId, field) }
    }

    fun commentPost(postId: String, content: String) {
        val userId = authService.getCurrentUserId() ?: return
        val user = _uiState.value.user
        val comment = Comment(
            postId = postId,
            authorId = userId,
            authorName = user?.displayName ?: "",
            authorUsername = user?.username ?: "",
            authorImageUrl = user?.profileImageUrl ?: "",
            content = content
        )
        _uiState.update { s ->
            s.copy(posts = s.posts.map { p ->
                if (p.id == postId) p.copy(commentsCount = p.commentsCount + 1) else p
            })
        }
        viewModelScope.launch { firebaseService.addComment(comment) }
    }

    fun togglePin(post: Post) {
        val userId = authService.getCurrentUserId() ?: return
        if (post.authorId != userId) return
        val isPinned = _uiState.value.pinnedPosts.any { it.id == post.id }
        if (isPinned) {
            _uiState.update { s -> s.copy(pinnedPosts = s.pinnedPosts.filter { it.id != post.id }) }
            viewModelScope.launch { firebaseService.unpinPost(userId, post.id) }
        } else {
            val order = _uiState.value.pinnedPosts.size
            _uiState.update { s -> s.copy(pinnedPosts = s.pinnedPosts + post) }
            viewModelScope.launch { firebaseService.pinPost(userId, post.id, order) }
        }
    }

    fun blockUser() {
        val currentUserId = authService.getCurrentUserId() ?: return
        val profileUserId = _uiState.value.user?.id ?: return
        val nowBlocked = !_uiState.value.isBlocked
        _uiState.update { it.copy(isBlocked = nowBlocked) }
        viewModelScope.launch {
            if (nowBlocked) firebaseService.blockUser(currentUserId, profileUserId)
            else firebaseService.unblockUser(currentUserId, profileUserId)
        }
    }

    fun reportUser(reason: String) {
        val currentUserId = authService.getCurrentUserId() ?: return
        val profileUserId = _uiState.value.user?.id ?: return
        viewModelScope.launch { firebaseService.reportUser(currentUserId, profileUserId, reason) }
    }

    fun deletePost(postId: String) {
        val userId = authService.getCurrentUserId() ?: return
        val post = _uiState.value.posts.find { it.id == postId } ?: return
        if (post.authorId != userId) return
        _uiState.update { it.copy(posts = it.posts.filter { p -> p.id != postId }) }
        viewModelScope.launch { firebaseService.deletePost(postId, userId) }
    }

    fun toggleFollow() {
        val currentUserId = authService.getCurrentUserId() ?: return
        val profileUserId = _uiState.value.user?.id ?: return
        val nowFollowing = !_uiState.value.isFollowing
        _uiState.update { it.copy(isFollowing = nowFollowing) }
        viewModelScope.launch {
            firebaseService.updateFollowCounts(
                followerId = currentUserId,
                followingId = profileUserId,
                isFollow = nowFollowing
            )
            if (nowFollowing) {
                val me = firebaseService.getUser(currentUserId).getOrNull()
                val senderName = me?.displayName ?: "Someone"
                firebaseService.sendFollowNotification(
                    toUserId = profileUserId,
                    fromUserId = currentUserId,
                    fromName = senderName
                )
            }
        }
    }

    fun toggleWidget(toggle: (ProfileWidgetSettings) -> ProfileWidgetSettings) {
        val current = _uiState.value.widgetSettings
        val updated = toggle(current)
        _uiState.update { it.copy(widgetSettings = updated) }
        viewModelScope.launch {
            profileWidgetSettingsDao.upsertSettings(updated)
        }
    }

    fun updateProfileBackgroundImage(uri: Uri) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            // Upload to Firebase Storage
            val path = "users/$userId/profile_bg_${System.currentTimeMillis()}.jpg"
            val result = storageService.uploadImage(uri, path)
            result.fold(
                onSuccess = { downloadUrl ->
                    // Update widget settings with new background URL
                    toggleWidget { it.copy(backgroundImageUrl = downloadUrl) }
                },
                onFailure = { error ->
                    android.util.Log.e("ProfileViewModel", "Failed to upload background: ${error.message}")
                }
            )
        }
    }

    fun saveWidgetPositions(positionsJson: String) {
        toggleWidget { it.copy(widgetPositions = positionsJson) }
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

    fun reorderMediaTrack(track: MediaTrack, moveUp: Boolean) {
        val list = (if (track.type == MediaTrackType.MUSIC) _uiState.value.topMusic else _uiState.value.topFilms)
            .sortedBy { it.rank }.toMutableList()
        val idx = list.indexOfFirst { it.id == track.id }.takeIf { it >= 0 } ?: return
        val swapIdx = if (moveUp) idx - 1 else idx + 1
        if (swapIdx < 0 || swapIdx >= list.size) return
        val updated = list.toMutableList()
        val a = updated[idx].copy(rank = updated[swapIdx].rank)
        val b = updated[swapIdx].copy(rank = updated[idx].rank)
        updated[idx] = a; updated[swapIdx] = b
        val newList = updated.sortedBy { it.rank }
        if (track.type == MediaTrackType.MUSIC) _uiState.update { it.copy(topMusic = newList) }
        else _uiState.update { it.copy(topFilms = newList) }
        viewModelScope.launch { updated.forEach { mediaTrackDao.updateTrack(it) } }
    }

    fun reorderVideoLink(link: VideoLink, moveUp: Boolean) {
        val list = _uiState.value.videoLinks.sortedBy { it.sortOrder }.toMutableList()
        val idx = list.indexOfFirst { it.id == link.id }.takeIf { it >= 0 } ?: return
        val swapIdx = if (moveUp) idx - 1 else idx + 1
        if (swapIdx < 0 || swapIdx >= list.size) return
        val a = list[idx].copy(sortOrder = list[swapIdx].sortOrder)
        val b = list[swapIdx].copy(sortOrder = list[idx].sortOrder)
        list[idx] = a; list[swapIdx] = b
        _uiState.update { it.copy(videoLinks = list.sortedBy { v -> v.sortOrder }) }
        viewModelScope.launch { listOf(a, b).forEach { videoLinkDao.updateVideoLink(it) } }
    }

    fun reorderPlaylistItem(item: PlaylistItem, moveUp: Boolean) {
        val list = _uiState.value.playlist.sortedBy { it.sortOrder }.toMutableList()
        val idx = list.indexOfFirst { it.id == item.id }.takeIf { it >= 0 } ?: return
        val swapIdx = if (moveUp) idx - 1 else idx + 1
        if (swapIdx < 0 || swapIdx >= list.size) return
        val a = list[idx].copy(sortOrder = list[swapIdx].sortOrder)
        val b = list[swapIdx].copy(sortOrder = list[idx].sortOrder)
        list[idx] = a; list[swapIdx] = b
        _uiState.update { it.copy(playlist = list.sortedBy { p -> p.sortOrder }) }
        viewModelScope.launch { listOf(a, b).forEach { playlistItemDao.upsertItem(it) } }
    }

    fun reorderTechStackItem(item: TechStackItem, moveUp: Boolean) {
        val list = _uiState.value.techStack.sortedBy { it.sortOrder }.toMutableList()
        val idx = list.indexOfFirst { it.id == item.id }.takeIf { it >= 0 } ?: return
        val swapIdx = if (moveUp) idx - 1 else idx + 1
        if (swapIdx < 0 || swapIdx >= list.size) return
        val a = list[idx].copy(sortOrder = list[swapIdx].sortOrder)
        val b = list[swapIdx].copy(sortOrder = list[idx].sortOrder)
        list[idx] = a; list[swapIdx] = b
        _uiState.update { it.copy(techStack = list.sortedBy { t -> t.sortOrder }) }
        viewModelScope.launch { listOf(a, b).forEach { techStackDao.upsertItem(it) } }
    }

    fun addMediaTrack(track: MediaTrack) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = track.copy(userId = userId)
        viewModelScope.launch { mediaTrackDao.insertTrack(withUser) }
    }

    fun deleteMediaTrack(track: MediaTrack) {
        viewModelScope.launch { mediaTrackDao.deleteTrack(track) }
    }

    fun addVideoLink(link: VideoLink) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = link.copy(userId = userId, sortOrder = _uiState.value.videoLinks.size)
        viewModelScope.launch { videoLinkDao.insertVideoLink(withUser) }
    }

    fun deleteVideoLink(link: VideoLink) {
        viewModelScope.launch { videoLinkDao.deleteVideoLink(link) }
    }

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

    fun addCodeSnippet(snippet: CodeSnippet) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = snippet.copy(userId = userId)
        viewModelScope.launch { codeSnippetDao.insertSnippet(withUser) }
    }

    fun deleteCodeSnippet(snippet: CodeSnippet) {
        viewModelScope.launch { codeSnippetDao.deleteSnippet(snippet.id) }
    }

    fun addTechStackItem(item: TechStackItem) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = item.copy(userId = userId, sortOrder = _uiState.value.techStack.size)
        viewModelScope.launch { techStackDao.upsertItem(withUser) }
    }

    fun deleteTechStackItem(item: TechStackItem) {
        viewModelScope.launch { techStackDao.deleteItem(item.id) }
    }

    fun addGif(gif: GifItem) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = gif.copy(userId = userId, sortOrder = _uiState.value.gifs.size)
        viewModelScope.launch { gifItemDao.upsertGif(withUser) }
    }

    fun deleteGif(gif: GifItem) {
        viewModelScope.launch { gifItemDao.deleteGif(gif.id) }
    }

    fun toggleGifRepeat(gif: GifItem) {
        viewModelScope.launch {
            gifItemDao.upsertGif(gif.copy(repeat = !gif.repeat))
        }
    }

    fun setSpotifyTrack(track: SpotifyTrack) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = track.copy(userId = userId)
        viewModelScope.launch { spotifyTrackDao.upsertTrack(withUser) }
    }

    fun clearSpotifyTrack() {
        val userId = _uiState.value.user?.id ?: return
        _uiState.update { it.copy(spotifyTrack = null) }
        viewModelScope.launch { spotifyTrackDao.deleteTrack("spotify_$userId") }
    }

    fun addMeme(meme: MemeItem) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = meme.copy(userId = userId, sortOrder = _uiState.value.memes.size)
        viewModelScope.launch { memeItemDao.upsertMeme(withUser) }
    }

    fun deleteMeme(meme: MemeItem) {
        viewModelScope.launch { memeItemDao.deleteMeme(meme.id) }
    }

    fun reactToMeme(memeId: String, reaction: String) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            memeItemDao.recordReaction(MemeReaction(memeId, userId, reaction))
            when (reaction) {
                "like" -> memeItemDao.likeMeme(memeId)
                "fire" -> memeItemDao.fireMeme(memeId)
                "laugh" -> memeItemDao.laughMeme(memeId)
                "mindblown" -> memeItemDao.mindblownMeme(memeId)
            }
        }
    }

    fun addGameStats(stats: GameStats) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = stats.copy(userId = userId)
        viewModelScope.launch { gameStatsDao.upsertStats(withUser) }
    }

    fun addVisitedPlace(place: VisitedPlace) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = place.copy(userId = userId)
        viewModelScope.launch { visitedPlaceDao.upsertPlace(withUser) }
    }

    fun deleteVisitedPlace(place: VisitedPlace) {
        viewModelScope.launch { visitedPlaceDao.deletePlace(place.id) }
    }

    fun addTravelWish(wish: TravelWish) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = wish.copy(userId = userId, sortOrder = _uiState.value.travelWishes.size)
        viewModelScope.launch { travelWishDao.upsertWish(withUser) }
    }

    fun deleteTravelWish(wish: TravelWish) {
        viewModelScope.launch { travelWishDao.deleteWish(wish.id) }
    }

    // ─────────────────────────────────────────────
    // MULTIPLAYER GAMES
    // ─────────────────────────────────────────────

    fun createTicTacToeGame(opponentId: String, opponentName: String) {
        val userId = _uiState.value.user?.id ?: return
        val userName = _uiState.value.user?.displayName ?: "You"
        val gameId = "ttt_${System.currentTimeMillis()}"

        viewModelScope.launch {
            // Create game
            val game = TicTacToeGame(
                id = gameId,
                playerXId = userId,
                playerXName = userName,
                playerOId = opponentId,
                playerOName = opponentName,
                status = GameStatus.WAITING
            )
            ticTacToeDao.upsertGame(game)

            // Send invite
            gameInviteDao.sendInvite(GameInvite(
                id = "invite_${System.currentTimeMillis()}",
                gameType = GameType2P.TICTACTOE,
                gameId = gameId,
                fromUserId = userId,
                fromUserName = userName,
                toUserId = opponentId,
                toUserName = opponentName,
                message = "Want to play Tic Tac Toe?"
            ))
        }
    }

    fun acceptGameInvite(invite: GameInvite) {
        viewModelScope.launch {
            gameInviteDao.respondToInvite(invite.id, InviteStatus.ACCEPTED)

            // Update game to active
            when (invite.gameType) {
                GameType2P.TICTACTOE -> {
                    val userId = _uiState.value.user?.id ?: return@launch
                    val userName = _uiState.value.user?.displayName ?: "Opponent"
                    val game = ticTacToeDao.getGameById(invite.gameId) ?: return@launch
                    ticTacToeDao.upsertGame(game.copy(
                        playerOId = if (game.playerOId.isEmpty()) userId else game.playerOId,
                        playerOName = if (game.playerOName.isEmpty()) userName else game.playerOName,
                        status = GameStatus.ACTIVE
                    ))
                }
                GameType2P.MINESWEEPER -> {
                    val userId   = _uiState.value.user?.id ?: return@launch
                    val userName = _uiState.value.user?.displayName ?: "Opponent"
                    minesweeperDao.joinGame(invite.gameId, userId, userName)
                }
            }
        }
    }

    fun declineGameInvite(invite: GameInvite) {
        viewModelScope.launch {
            gameInviteDao.respondToInvite(invite.id, InviteStatus.DECLINED)
        }
    }

    fun makeTicTacToeMove(gameId: String, row: Int, col: Int) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            val game = ticTacToeDao.getGameById(gameId) ?: return@launch
            val playerSymbol = if (game.playerXId == userId) "X" else "O"

            game.makeMove(row, col, playerSymbol)?.let { updatedGame ->
                ticTacToeDao.upsertGame(updatedGame)
            }
        }
    }

    private fun getMockSnippets(userId: String): List<CodeSnippet> = listOf(
        CodeSnippet(
            id = "s1", userId = userId, title = "Retrofit Singleton",
            code = "object ApiClient {\n    val retrofit = Retrofit.Builder()\n        .baseUrl(BASE_URL)\n        .addConverterFactory(GsonConverterFactory.create())\n        .build()\n}",
            language = Language.KOTLIN,
            description = "Clean way to setup Retrofit"
        ),
        CodeSnippet(
            id = "s2", userId = userId, title = "StateFlow Collect",
            code = "viewModel.uiState\n    .flowWithLifecycle(lifecycle)\n    .collect { state ->\n        updateUi(state)\n    }",
            language = Language.KOTLIN,
            description = "Lifecycle-aware collection"
        ),
        CodeSnippet(
            id = "s3", userId = userId, title = "Compose Animation",
            code = "val alpha by animateFloatAsState(\n    targetValue = if (visible) 1f else 0f,\n    animationSpec = tween(300)\n)",
            language = Language.KOTLIN,
            description = "Simple fade animation"
        )
    )

    private fun getMockTechStack(userId: String): List<TechStackItem> = listOf(
        TechStackItem("ts1", userId, "Kotlin", "Mobile", 95, 4f),
        TechStackItem("ts2", userId, "Jetpack Compose", "Mobile", 90, 2f),
        TechStackItem("ts3", userId, "Android", "Mobile", 92, 6f),
        TechStackItem("ts4", userId, "Python", "Backend", 75, 3f),
        TechStackItem("ts5", userId, "TypeScript", "Frontend", 70, 2f),
        TechStackItem("ts6", userId, "Docker", "DevOps", 65, 2f)
    )

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

    // ── Polls ──────────────────────────────────────────────

    fun loadPolls(userId: String) {
        viewModelScope.launch {
            val polls = firebaseService.getPollsForUser(userId).getOrDefault(emptyList())
            _uiState.update { it.copy(polls = polls) }
        }
    }

    fun createPoll(poll: ProfilePoll) {
        val userId = authService.getCurrentUserId() ?: return
        viewModelScope.launch {
            val id = firebaseService.createPoll(userId, poll).getOrNull() ?: return@launch
            val saved = poll.copy(id = id)
            _uiState.update { it.copy(polls = listOf(saved) + it.polls) }
        }
    }

    fun deletePoll(poll: ProfilePoll) {
        val userId = authService.getCurrentUserId() ?: return
        _uiState.update { it.copy(polls = it.polls.filter { p -> p.id != poll.id }) }
        viewModelScope.launch { firebaseService.deletePoll(userId, poll.id) }
    }

    fun voteOnPoll(pollId: String, option: Char) {
        val voterId = authService.getCurrentUserId() ?: return
        if (_uiState.value.votedPollOptions.containsKey(pollId)) return
        val ownerId = _uiState.value.user?.id ?: return
        val upperOption = option.uppercaseChar()
        _uiState.update { s ->
            s.copy(
                polls = s.polls.map { p ->
                    if (p.id != pollId) p else when (upperOption) {
                        'A' -> p.copy(votesA = p.votesA + 1)
                        'B' -> p.copy(votesB = p.votesB + 1)
                        'C' -> p.copy(votesC = p.votesC + 1)
                        'D' -> p.copy(votesD = p.votesD + 1)
                        else -> p
                    }
                },
                votedPollOptions = s.votedPollOptions + (pollId to upperOption)
            )
        }
        viewModelScope.launch {
            profilePollDao.recordVote(PollVote(pollId = pollId, voterId = voterId, option = upperOption))
            when (upperOption) {
                'A' -> profilePollDao.voteA(pollId)
                'B' -> profilePollDao.voteB(pollId)
                'C' -> profilePollDao.voteC(pollId)
                'D' -> profilePollDao.voteD(pollId)
            }
            firebaseService.voteOnPoll(ownerId, pollId, upperOption, voterId)
        }
    }

    // ── Pinned post aliases (used by PinnedPostsWidget) ────

    fun pinPost(post: Post) {
        val userId = authService.getCurrentUserId() ?: return
        val order = _uiState.value.pinnedPosts.size
        _uiState.update { s -> s.copy(pinnedPosts = (s.pinnedPosts + post).distinctBy { it.id }) }
        viewModelScope.launch { firebaseService.pinPost(userId, post.id, order) }
    }

    fun unpinPost(post: Post) {
        val userId = authService.getCurrentUserId() ?: return
        _uiState.update { s -> s.copy(pinnedPosts = s.pinnedPosts.filter { it.id != post.id }) }
        viewModelScope.launch { firebaseService.unpinPost(userId, post.id) }
    }

    fun createMinesweeperGame(opponentId: String, opponentName: String) {
        val userId   = _uiState.value.user?.id ?: return
        val userName = _uiState.value.user?.displayName ?: "You"
        val gameId   = "ms_${System.currentTimeMillis()}"
        viewModelScope.launch {
            val (hostGrid, hostMines) = MinesweeperGame.generateGrid(9, 9, 10)
            val (oppGrid,  oppMines)  = MinesweeperGame.generateGrid(9, 9, 10)
            minesweeperDao.upsertGame(
                MinesweeperGame(
                    id            = gameId,
                    hostId        = userId,
                    opponentId    = opponentId,
                    hostName      = userName,
                    opponentName  = opponentName,
                    hostGrid      = hostGrid,
                    hostMines     = hostMines,
                    opponentGrid  = oppGrid,
                    opponentMines = oppMines,
                    status        = GameStatus.WAITING
                )
            )
            gameInviteDao.sendInvite(GameInvite(
                id           = "invite_ms_${System.currentTimeMillis()}",
                gameType     = GameType2P.MINESWEEPER,
                gameId       = gameId,
                fromUserId   = userId,
                fromUserName = userName,
                toUserId     = opponentId,
                toUserName   = opponentName,
                message      = "Want to race at Minesweeper?"
            ))
        }
    }

    fun addPhoto(photo: ProfilePhoto) {
        val userId = _uiState.value.user?.id ?: return
        val withUser = photo.copy(userId = userId, sortOrder = _uiState.value.photos.size)
        viewModelScope.launch { profilePhotoDao.insertPhoto(withUser) }
    }

    fun uploadAndAddPhoto(uri: Uri, caption: String) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            val path = "users/$userId/photos/photo_${System.currentTimeMillis()}.jpg"
            val result = storageService.uploadImage(uri, path)
            result.onSuccess { downloadUrl ->
                val photo = ProfilePhoto(
                    id = "photo_${System.currentTimeMillis()}",
                    userId = userId,
                    imageUrl = downloadUrl,
                    caption = caption,
                    sortOrder = _uiState.value.photos.size
                )
                profilePhotoDao.insertPhoto(photo)
            }
        }
    }

    fun deletePhoto(photo: ProfilePhoto) {
        viewModelScope.launch { profilePhotoDao.deletePhotoById(photo.id) }
    }

    fun updatePhotoFrame(photo: ProfilePhoto, newFrame: PhotoFrameStyle) {
        viewModelScope.launch {
            profilePhotoDao.updatePhoto(photo.copy(frameStyle = newFrame))
        }
    }

    // ── Profile view counter ───────────────────────────────

    fun recordProfileView(profileUserId: String) {
        val viewerId = authService.getCurrentUserId() ?: return
        viewModelScope.launch { firebaseService.incrementProfileView(profileUserId, viewerId) }
    }
}
