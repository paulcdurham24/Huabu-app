package com.huabu.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.huabu.app.data.model.Friend
import com.huabu.app.data.model.User
import com.huabu.app.ui.components.GlitterCanvas
import com.huabu.app.ui.components.PostCard
import com.huabu.app.ui.theme.*

@Composable
fun ProfileScreen(
    userId: String,
    onNavigateToFriends: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToThemeEditor: (String) -> Unit = {},
    onNavigateToEditProfile: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showWidgetSettings by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    if (showWidgetSettings) {
        WidgetSettingsPanel(
            settings = uiState.widgetSettings,
            onToggle = { viewModel.toggleWidget(it) },
            onDismiss = { showWidgetSettings = false }
        )
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize().background(HuabuDarkBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HuabuHotPink)
        }
        return
    }

    val user = uiState.user ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HuabuDarkBg),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Profile Header Banner
        item { ProfileBanner(user = user) }

        // Profile Info Card
        item {
            ProfileInfoCard(
                user = user,
                isCurrentUser = uiState.isCurrentUser,
                isFollowing = uiState.isFollowing,
                onFollowClick = { viewModel.toggleFollow() },
                onMessageClick = onNavigateToMessages,
                onCustomiseClick = { showWidgetSettings = true },
                onEditThemeClick = { onNavigateToThemeEditor(userId) },
                onEditProfileClick = { onNavigateToEditProfile(userId) }
            )
        }

        // Widgets rendered in user-defined order
        val ws = uiState.widgetSettings
        items(ws.orderedWidgetIds()) { widgetId ->
            when (widgetId) {
                "profile_song" -> if (ws.showProfileSong && user.profileSong.isNotEmpty()) {
                    ProfileSongWidget(song = user.profileSong, artist = user.profileSongArtist)
                }
                "photo_gallery" -> if (ws.showPhotoGallery && uiState.photos.isNotEmpty()) {
                    PhotoGalleryWidget(
                        photos = uiState.photos,
                        isCurrentUser = uiState.isCurrentUser,
                        onPhotoClick = {},
                        onFrameChange = { _, _ -> }
                    )
                }
                "video_links" -> if (ws.showVideoLinks && uiState.videoLinks.isNotEmpty()) {
                    VideoLinksWidget(videos = uiState.videoLinks, isCurrentUser = uiState.isCurrentUser)
                }
                "top_music" -> if (ws.showTopMusic && uiState.topMusic.isNotEmpty()) {
                    TopMusicWidget(tracks = uiState.topMusic, isCurrentUser = uiState.isCurrentUser)
                }
                "top_films" -> if (ws.showTopFilms && uiState.topFilms.isNotEmpty()) {
                    TopFilmsWidget(tracks = uiState.topFilms, isCurrentUser = uiState.isCurrentUser)
                }
                "about_me" -> if (ws.showAboutMe) {
                    AboutMeCard(aboutMe = user.aboutMe, heroes = user.heroesSection)
                }
                "interests" -> if (ws.showInterests && user.interests.isNotEmpty()) {
                    InterestsCard(interests = user.interests)
                }
                "top_friends" -> if (ws.showTopFriends) {
                    TopFriendsCard(
                        friends = uiState.topFriends,
                        onFriendClick = { friend -> onNavigateToProfile(friend.friendId) },
                        onViewAllClick = onNavigateToFriends
                    )
                }
                "go_live" -> if (ws.showGoLive) {
                    GoLiveWidget(
                        liveStream = uiState.liveStream,
                        isCurrentUser = uiState.isCurrentUser,
                        onGoLive = { title -> viewModel.goLive(title) },
                        onEndLive = { viewModel.endLive() }
                    )
                }
                "events" -> if (ws.showEvents) {
                    EventsWidget(
                        events = uiState.events,
                        isCurrentUser = uiState.isCurrentUser,
                        onAddEvent = { event -> viewModel.addEvent(event) },
                        onDeleteEvent = { event -> viewModel.deleteEvent(event) },
                        onRsvp = { event -> viewModel.rsvpEvent(event) }
                    )
                }
                "badges" -> if (ws.showBadges && uiState.badges.isNotEmpty()) {
                    BadgesWidget(
                        badges = uiState.badges,
                        isCurrentUser = uiState.isCurrentUser
                    )
                }
                "mood_board" -> if (ws.showMoodBoard) {
                    MoodBoardWidget(
                        items = uiState.moodBoard,
                        isCurrentUser = uiState.isCurrentUser,
                        onUpdateCell = { item -> viewModel.updateMoodBoardCell(item) }
                    )
                }
                "pinned_posts" -> if (ws.showPinnedPosts) {
                    PinnedPostsWidget(
                        pinnedPosts = uiState.pinnedPosts,
                        allPosts = uiState.posts,
                        isCurrentUser = uiState.isCurrentUser,
                        onPin = { post -> viewModel.pinPost(post) },
                        onUnpin = { post -> viewModel.unpinPost(post) }
                    )
                }
                "recently_played" -> if (ws.showRecentlyPlayed && uiState.recentTracks.isNotEmpty()) {
                    RecentlyPlayedWidget(
                        tracks = uiState.recentTracks,
                        isCurrentUser = uiState.isCurrentUser
                    )
                }
                "my_playlist" -> if (ws.showMyPlaylist) {
                    MyPlaylistWidget(
                        items = uiState.playlist,
                        isCurrentUser = uiState.isCurrentUser,
                        onAdd = { item -> viewModel.addPlaylistItem(item) },
                        onDelete = { item -> viewModel.deletePlaylistItem(item) }
                    )
                }
                "currently_reading" -> if (ws.showCurrentlyReading) {
                    CurrentlyReadingWidget(
                        book = uiState.currentlyReading,
                        isCurrentUser = uiState.isCurrentUser,
                        onSave = { book -> viewModel.saveCurrentlyReading(book) },
                        onClear = { viewModel.clearCurrentlyReading() }
                    )
                }
                "currently_watching" -> if (ws.showCurrentlyWatching) {
                    CurrentlyWatchingWidget(
                        show = uiState.currentlyWatching,
                        isCurrentUser = uiState.isCurrentUser,
                        onSave = { show -> viewModel.saveCurrentlyWatching(show) },
                        onClear = { viewModel.clearCurrentlyWatching() }
                    )
                }
                "nft_showcase" -> if (ws.showNftShowcase) {
                    NftShowcaseWidget(
                        nfts = uiState.nfts,
                        isCurrentUser = uiState.isCurrentUser,
                        onAdd = { nft -> viewModel.addNft(nft) },
                        onDelete = { nft -> viewModel.deleteNft(nft) }
                    )
                }
                "polls" -> if (ws.showPolls) {
                    PollsWidget(
                        polls = uiState.polls,
                        isCurrentUser = uiState.isCurrentUser,
                        userId = uiState.user?.id ?: "",
                        onCreatePoll = { poll -> viewModel.createPoll(poll) },
                        onDeletePoll = { poll -> viewModel.deletePoll(poll) },
                        onVote = { pollId, option -> viewModel.voteOnPoll(pollId, option) }
                    )
                }
                "code_snippets" -> if (ws.showCodeSnippets) {
                    CodeSnippetsWidget(
                        snippets = uiState.codeSnippets,
                        isCurrentUser = uiState.isCurrentUser,
                        onAdd = { snippet -> viewModel.addCodeSnippet(snippet) },
                        onDelete = { snippet -> viewModel.deleteCodeSnippet(snippet) }
                    )
                }
                "tech_stack" -> if (ws.showTechStack) {
                    TechStackWidget(
                        items = uiState.techStack,
                        isCurrentUser = uiState.isCurrentUser,
                        onAdd = { item -> viewModel.addTechStackItem(item) },
                        onDelete = { item -> viewModel.deleteTechStackItem(item) }
                    )
                }
                "gif_showcase" -> if (ws.showGifShowcase) {
                    GifShowcaseWidget(
                        gifs = uiState.gifs,
                        isCurrentUser = uiState.isCurrentUser,
                        onAdd = { gif -> viewModel.addGif(gif) },
                        onDelete = { gif -> viewModel.deleteGif(gif) },
                        onToggleRepeat = { gif -> viewModel.toggleGifRepeat(gif) }
                    )
                }
                "spotify_now_playing" -> if (ws.showSpotifyNowPlaying) {
                    SpotifyNowPlayingWidget(
                        track = uiState.spotifyTrack,
                        isCurrentUser = uiState.isCurrentUser,
                        onSetTrack = { track -> viewModel.setSpotifyTrack(track) },
                        onClear = { viewModel.clearSpotifyTrack() }
                    )
                }
                "meme_wall" -> if (ws.showMemeWall) {
                    MemeWallWidget(
                        memes = uiState.memes,
                        isCurrentUser = uiState.isCurrentUser,
                        userId = uiState.user?.id ?: "",
                        onAddMeme = { meme -> viewModel.addMeme(meme) },
                        onDeleteMeme = { meme -> viewModel.deleteMeme(meme) },
                        onReact = { memeId, reaction -> viewModel.reactToMeme(memeId, reaction) }
                    )
                }
                "game_stats" -> if (ws.showGameStats) {
                    GameStatsWidget(
                        stats = uiState.gameStats,
                        isCurrentUser = uiState.isCurrentUser,
                        onAdd = { stats -> viewModel.addGameStats(stats) }
                    )
                }
                "visited_places" -> if (ws.showVisitedPlaces) {
                    VisitedPlacesWidget(
                        places = uiState.visitedPlaces,
                        isCurrentUser = uiState.isCurrentUser,
                        onAdd = { place -> viewModel.addVisitedPlace(place) },
                        onDelete = { place -> viewModel.deleteVisitedPlace(place) }
                    )
                }
                "travel_wishlist" -> if (ws.showTravelWishlist) {
                    TravelWishlistWidget(
                        wishes = uiState.travelWishes,
                        isCurrentUser = uiState.isCurrentUser,
                        onAdd = { wish -> viewModel.addTravelWish(wish) },
                        onDelete = { wish -> viewModel.deleteTravelWish(wish) }
                    )
                }
            }
        }

        // Stats Row
        item { ProfileStatsRow(user = user) }

        // Posts Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.GridView, contentDescription = null, tint = HuabuGold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Posts", style = MaterialTheme.typography.titleMedium, color = HuabuGold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("${uiState.posts.size} posts", style = MaterialTheme.typography.bodySmall, color = HuabuSilver)
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = HuabuHotPink,
                thickness = 1.5.dp
            )
        }

        // Posts
        items(uiState.posts, key = { it.id }) { post ->
            PostCard(
                post = post,
                onAuthorClick = { onNavigateToProfile(post.authorId) }
            )
        }
    }
}

@Composable
private fun ProfileBanner(user: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (user.backgroundImageUrl.isNotEmpty()) {
            AsyncImage(
                model = user.backgroundImageUrl,
                contentDescription = "Profile background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(parseHexColor(user.profileColor1)),
                                Color(parseHexColor(user.profileColor2)),
                                HuabuDarkBg
                            )
                        )
                    )
            )
        }

        GlitterCanvas(
            modifier = Modifier.fillMaxSize(),
            sparkleCount = 25
        )

        // Decorative border strips (MySpace-style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(HuabuHotPink, HuabuGold, HuabuAccentCyan, HuabuHotPink)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(HuabuAccentCyan, HuabuHotPink, HuabuGold, HuabuAccentCyan)
                    )
                )
        )
    }
}

@Composable
private fun ProfileInfoCard(
    user: User,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    onCustomiseClick: () -> Unit = {},
    onEditThemeClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .offset(y = (-36).dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(listOf(HuabuCardBg, HuabuCardBg2))
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            listOf(HuabuHotPink, HuabuGold, HuabuAccentCyan, HuabuNeonGreen, HuabuHotPink)
                        ),
                        shape = CircleShape
                    )
            ) {
                if (user.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = user.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(HuabuDeepPurple, HuabuHotPink)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = HuabuGold
                )
                if (user.isVerified) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = HuabuElectricBlue, modifier = Modifier.size(18.dp))
                }
                if (user.isOnline) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(HuabuNeonGreen, CircleShape)
                    )
                }
            }

            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = HuabuAccentPink
            )

            if (user.mood.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = HuabuAccentCyan, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = user.mood, style = MaterialTheme.typography.bodySmall, color = HuabuAccentCyan)
                }
            }

            if (user.location.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = HuabuSilver, modifier = Modifier.size(14.dp))
                    Text(text = user.location, style = MaterialTheme.typography.bodySmall, color = HuabuSilver)
                }
            }

            if (user.bio.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = HuabuOnSurface
                )
            }

            Spacer(Modifier.height(12.dp))

            if (!isCurrentUser) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onFollowClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) HuabuSurface else HuabuHotPink
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            if (isFollowing) Icons.Filled.PersonRemove else Icons.Filled.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (isFollowing) "Following" else "Follow")
                    }
                    OutlinedButton(
                        onClick = onMessageClick,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Message")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditProfileClick,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Edit", fontSize = 12.sp, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onCustomiseClick,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Widgets", fontSize = 12.sp, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onEditThemeClick,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Palette, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Theme", fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSongWidget(song: String, artist: String) {
    var isPlaying by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .offset(y = (-24).dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg2),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(HuabuDeepPurple.copy(alpha = 0.8f), HuabuSurface.copy(alpha = 0.8f))
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(listOf(HuabuHotPink, HuabuDeepPurple)),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("♪", fontSize = 24.sp, color = HuabuGold)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Profile Song",
                    style = MaterialTheme.typography.labelSmall,
                    color = HuabuAccentCyan
                )
                Text(
                    text = song,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = HuabuGold
                )
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = HuabuSilver
                )
            }

            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = HuabuHotPink,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun AboutMeCard(aboutMe: String, heroes: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .offset(y = (-20).dp)
    ) {
        if (aboutMe.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HuabuCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(listOf(HuabuCardBg, HuabuCardBg2))
                        )
                        .padding(16.dp)
                ) {
                    SectionHeader(title = "★ About Me ★", color = HuabuHotPink)
                    Spacer(Modifier.height(8.dp))
                    Text(text = aboutMe, style = MaterialTheme.typography.bodyMedium, color = HuabuOnSurface)

                    if (heroes.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = HuabuDivider)
                        Spacer(Modifier.height(12.dp))
                        SectionHeader(title = "★ Who I'd Like to Meet ★", color = HuabuAccentCyan)
                        Spacer(Modifier.height(8.dp))
                        Text(text = heroes, style = MaterialTheme.typography.bodyMedium, color = HuabuOnSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun InterestsCard(interests: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .offset(y = (-16).dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(HuabuCardBg, HuabuCardBg2)))
                .padding(16.dp)
        ) {
            SectionHeader(title = "★ Interests ★", color = HuabuNeonGreen)
            Spacer(Modifier.height(8.dp))
            FlowRow(interests = interests)
        }
    }
}

@Composable
private fun FlowRow(interests: String) {
    val items = interests.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val rows = items.chunked(3)
    rows.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            row.forEach { interest ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = HuabuDeepPurple.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = interest,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = HuabuAccentPink
                    )
                }
            }
        }
    }
}

@Composable
private fun TopFriendsCard(
    friends: List<Friend>,
    onFriendClick: (Friend) -> Unit,
    onViewAllClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .offset(y = (-12).dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(HuabuCardBg, HuabuCardBg2)))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionHeader(title = "★ Top Friends ★", color = HuabuGold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onViewAllClick) {
                    Text("View All", color = HuabuAccentCyan, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            val topEight = friends.take(8)
            val rows = topEight.chunked(4)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { friend ->
                        FriendTile(
                            friend = friend,
                            onClick = { onFriendClick(friend) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FriendTile(
    friend: Friend,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    2.dp,
                    brush = Brush.sweepGradient(listOf(HuabuHotPink, HuabuGold, HuabuHotPink)),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    brush = Brush.radialGradient(listOf(HuabuDeepPurple, HuabuSurface))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (friend.friendImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = friend.friendImageUrl,
                    contentDescription = friend.friendName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            } else {
                Text(
                    text = friend.friendName.firstOrNull()?.uppercase() ?: "?",
                    color = HuabuGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = friend.friendName.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            color = HuabuSilver,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileStatsRow(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .offset(y = (-8).dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Posts", count = user.postsCount, color = HuabuAccentCyan)
            VerticalDivider(modifier = Modifier.height(40.dp), color = HuabuDivider)
            StatItem(label = "Followers", count = user.followersCount, color = HuabuHotPink)
            VerticalDivider(modifier = Modifier.height(40.dp), color = HuabuDivider)
            StatItem(label = "Following", count = user.followingCount, color = HuabuGold)
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = HuabuSilver)
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        letterSpacing = 0.5.sp
    )
}

private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> "${count / 1_000_000}M"
    count >= 1_000 -> "${count / 1_000}K"
    else -> count.toString()
}

private fun parseHexColor(hex: String): Long {
    return try {
        val cleaned = hex.trimStart('#')
        ("FF$cleaned").toLong(16)
    } catch (e: Exception) {
        0xFF6A0572L
    }
}
