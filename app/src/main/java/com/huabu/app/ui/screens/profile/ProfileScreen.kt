package com.huabu.app.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.huabu.app.data.model.Friend
import com.huabu.app.data.model.ProfilePhoto
import com.huabu.app.data.model.User
import com.huabu.app.ui.components.CommentsBottomSheet
import com.huabu.app.ui.components.GlitterCanvas
import com.huabu.app.ui.components.PostCard
import com.huabu.app.ui.theme.*

@Composable
fun ProfileScreen(
    userId: String,
    onNavigateToFriends: () -> Unit,
    onNavigateToMessages: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToThemeEditor: (String) -> Unit = {},
    onNavigateToEditProfile: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToEditPost: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val commentsState by viewModel.commentsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showWidgetSettings by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    var fullScreenPhoto by remember { mutableStateOf<ProfilePhoto?>(null) }
    
    // Layout edit mode for drag-and-drop
    var isLayoutEditMode by remember { mutableStateOf(false) }
    var draggedWidgetId by remember { mutableStateOf<String?>(null) }
    var widgetPositions by remember(uiState.widgetSettings.widgetPositions) { 
        mutableStateOf(parseWidgetPositions(uiState.widgetSettings.widgetPositions))
    }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    if (showWidgetSettings) {
        WidgetSettingsPanel(
            settings = uiState.widgetSettings,
            onToggle = { viewModel.toggleWidget(it) },
            onDismiss = { showWidgetSettings = false },
            onBackgroundImageSelected = { uri ->
                viewModel.updateProfileBackgroundImage(uri)
            }
        )
    }

    if (commentsState.postId.isNotEmpty()) {
        CommentsBottomSheet(
            postId = commentsState.postId,
            comments = commentsState.comments,
            currentUserName = uiState.user?.displayName ?: "",
            currentUserId = uiState.user?.id ?: "",
            currentUserImageUrl = uiState.user?.profileImageUrl ?: "",
            isLoading = commentsState.isLoading,
            onDismiss = { viewModel.closeComments() },
            onSubmit = { text -> viewModel.commentPost(commentsState.postId, text) }
        )
    }

    fullScreenPhoto?.let { photo ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .clickable { fullScreenPhoto = null },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photo.imageUrl,
                contentDescription = photo.caption.ifEmpty { "Photo" },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
            )
            IconButton(
                onClick = { fullScreenPhoto = null },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            if (photo.caption.isNotEmpty()) {
                Text(
                    text = photo.caption,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text(if (uiState.isBlocked) "Unblock user?" else "Block user?", color = HuabuGold) },
            text = { Text(
                if (uiState.isBlocked) "This user will be able to see your profile and posts again."
                else "This user won't be able to see your profile or contact you.",
                color = HuabuOnSurface
            ) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.blockUser()
                    showBlockDialog = false
                }) { Text(if (uiState.isBlocked) "Unblock" else "Block", color = HuabuError) }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("Cancel", color = HuabuSilver) }
            },
            containerColor = HuabuCardBg
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false; reportReason = "" },
            title = { Text("Report user", color = HuabuGold) },
            text = {
                Column {
                    Text("What's the reason for this report?", color = HuabuOnSurface)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Describe the issue…", color = HuabuSilver) },
                        singleLine = false,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuabuHotPink,
                            unfocusedBorderColor = HuabuDivider,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuHotPink
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (reportReason.isNotBlank()) {
                            viewModel.reportUser(reportReason.trim())
                        }
                        showReportDialog = false
                        reportReason = ""
                    },
                    enabled = reportReason.isNotBlank()
                ) { Text("Report", color = HuabuError) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false; reportReason = "" }) { Text("Cancel", color = HuabuSilver) }
            },
            containerColor = HuabuCardBg
        )
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize().background(HuabuDarkBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HuabuHotPink)
        }
        return
    }

    val user = uiState.user ?: return
    val currentUserId = user.id
    val isMe = uiState.isCurrentUser
    var selectedTab by remember { mutableStateOf(0) }
    LaunchedEffect(selectedTab) { if (selectedTab == 1) viewModel.loadSavedPosts() }

    val themeCardColor = try { Color(parseHexColor(uiState.theme.cardColor)) } catch (_: Exception) { HuabuCardBg }
    val themeBgColor   = try { Color(parseHexColor(uiState.theme.backgroundColor)) } catch (_: Exception) { HuabuDarkBg }

    // Custom background image
    val bgImageUrl = uiState.widgetSettings.backgroundImageUrl

    CompositionLocalProvider(LocalProfileTheme provides uiState.theme) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background layer
        if (bgImageUrl.isNotEmpty()) {
            AsyncImage(
                model = bgImageUrl,
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Dark overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(if (bgImageUrl.isEmpty()) Modifier.background(themeBgColor) else Modifier),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Profile Header Banner
        item { ProfileBanner(user = user, theme = uiState.theme) }

        // Profile Info Card
        item {
            ProfileInfoCard(
                user = user,
                theme = uiState.theme,
                isCurrentUser = uiState.isCurrentUser,
                isFollowing = uiState.isFollowing,
                isBlocked = uiState.isBlocked,
                onFollowClick = { viewModel.toggleFollow() },
                onMessageClick = { onNavigateToMessages(user.id) },
                onCustomiseClick = { showWidgetSettings = true },
                onEditThemeClick = { onNavigateToThemeEditor(userId) },
                onEditProfileClick = { onNavigateToEditProfile(userId) },
                onSettingsClick = onNavigateToSettings,
                onBlockClick = { showBlockDialog = true },
                onReportClick = { showReportDialog = true }
            )
        }

        // Widgets rendered in user-defined order
        val ws = uiState.widgetSettings
        
        // Layout edit mode toggle (only for current user)
        if (uiState.isCurrentUser) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { isLayoutEditMode = !isLayoutEditMode },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLayoutEditMode) Color(0xFFEF4444) else HuabuViolet
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            if (isLayoutEditMode) Icons.Filled.Done else Icons.Filled.DragIndicator,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isLayoutEditMode) "Done" else "Edit Layout")
                    }
                }
            }
        }
        
        items(ws.orderedWidgetIds().filter { ws.isEnabled(it) }) { widgetId ->
            val position = widgetPositions[widgetId] ?: IntOffset(0, 0)
            
            when (widgetId) {
                "profile_song" -> if (ws.showProfileSong) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> 
                            widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }
                        },
                        onDragEnd = {
                            viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions))
                        }
                    ) {
                        ProfileSongWidget(
                            song = user.profileSong,
                            artist = user.profileSongArtist,
                            isCurrentUser = uiState.isCurrentUser,
                            onSave = { s, a ->
                                viewModel.saveUser(user.copy(profileSong = s, profileSongArtist = a))
                            }
                        )
                    }
                }
                "photo_gallery" -> if (ws.showPhotoGallery) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> 
                            widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }
                        },
                        onDragEnd = {
                            viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions))
                        }
                    ) {
                        PhotoGalleryWidget(
                            photos = uiState.photos,
                            isCurrentUser = uiState.isCurrentUser,
                            onPhotoClick = { photo -> fullScreenPhoto = photo },
                            onFrameChange = { photo, frame -> viewModel.updatePhotoFrame(photo, frame) },
                            onAddPhoto = { uri, caption -> viewModel.uploadAndAddPhoto(uri, caption) },
                            onDeletePhoto = { photo -> viewModel.deletePhoto(photo) }
                        )
                    }
                }
                "video_links" -> if (ws.showVideoLinks) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        VideoLinksWidget(
                            videos = uiState.videoLinks,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { link -> viewModel.addVideoLink(link) },
                            onDelete = { link -> viewModel.deleteVideoLink(link) },
                            onReorder = { link, up -> viewModel.reorderVideoLink(link, up) }
                        )
                    }
                }
                "top_music" -> if (ws.showTopMusic) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        TopMusicWidget(
                            tracks = uiState.topMusic,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { track -> viewModel.addMediaTrack(track) },
                            onDelete = { track -> viewModel.deleteMediaTrack(track) },
                            onReorder = { track, up -> viewModel.reorderMediaTrack(track, up) }
                        )
                    }
                }
                "top_films" -> if (ws.showTopFilms) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        TopFilmsWidget(
                            tracks = uiState.topFilms,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { track -> viewModel.addMediaTrack(track) },
                            onDelete = { track -> viewModel.deleteMediaTrack(track) },
                            onReorder = { track, up -> viewModel.reorderMediaTrack(track, up) }
                        )
                    }
                }
                "about_me" -> if (ws.showAboutMe) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        AboutMeCard(aboutMe = user.aboutMe, heroes = user.heroesSection)
                    }
                }
                "interests" -> if (ws.showInterests && user.interests.isNotEmpty()) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        InterestsCard(interests = user.interests)
                    }
                }
                "top_friends" -> if (ws.showTopFriends) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        TopFriendsCard(
                            friends = uiState.topFriends,
                            onFriendClick = { friend -> onNavigateToProfile(friend.friendId) },
                            onViewAllClick = onNavigateToFriends
                        )
                    }
                }
                "go_live" -> if (ws.showGoLive) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        GoLiveWidget(
                            liveStream = uiState.liveStream,
                            isCurrentUser = uiState.isCurrentUser,
                            onGoLive = { title -> viewModel.goLive(title) },
                            onEndLive = { viewModel.endLive() },
                            onWatch = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("huabu://live/${uiState.user?.id ?: ""}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            }
                        )
                    }
                }
                "events" -> if (ws.showEvents) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        EventsWidget(
                            events = uiState.events,
                            isCurrentUser = uiState.isCurrentUser,
                            onAddEvent = { event -> viewModel.addEvent(event) },
                            onDeleteEvent = { event -> viewModel.deleteEvent(event) },
                            onRsvp = { event -> viewModel.rsvpEvent(event) }
                        )
                    }
                }
                "badges" -> if (ws.showBadges && uiState.badges.isNotEmpty()) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        BadgesWidget(badges = uiState.badges, isCurrentUser = uiState.isCurrentUser)
                    }
                }
                "mood_board" -> if (ws.showMoodBoard) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        MoodBoardWidget(
                            items = uiState.moodBoard,
                            isCurrentUser = uiState.isCurrentUser,
                            onUpdateCell = { item -> viewModel.updateMoodBoardCell(item) }
                        )
                    }
                }
                "pinned_posts" -> if (ws.showPinnedPosts) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        PinnedPostsWidget(
                            pinnedPosts = uiState.pinnedPosts,
                            allPosts = uiState.posts,
                            isCurrentUser = uiState.isCurrentUser,
                            onPin = { post -> viewModel.pinPost(post) },
                            onUnpin = { post -> viewModel.unpinPost(post) }
                        )
                    }
                }
                "recently_played" -> if (ws.showRecentlyPlayed && uiState.recentTracks.isNotEmpty()) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        RecentlyPlayedWidget(tracks = uiState.recentTracks, isCurrentUser = uiState.isCurrentUser)
                    }
                }
                "my_playlist" -> if (ws.showMyPlaylist) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        MyPlaylistWidget(
                            items = uiState.playlist,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { item -> viewModel.addPlaylistItem(item) },
                            onDelete = { item -> viewModel.deletePlaylistItem(item) },
                            onReorder = { item, up -> viewModel.reorderPlaylistItem(item, up) }
                        )
                    }
                }
                "currently_reading" -> if (ws.showCurrentlyReading) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        CurrentlyReadingWidget(
                            book = uiState.currentlyReading,
                            isCurrentUser = uiState.isCurrentUser,
                            onSave = { book -> viewModel.saveCurrentlyReading(book) },
                            onClear = { viewModel.clearCurrentlyReading() }
                        )
                    }
                }
                "currently_watching" -> if (ws.showCurrentlyWatching) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        CurrentlyWatchingWidget(
                            show = uiState.currentlyWatching,
                            isCurrentUser = uiState.isCurrentUser,
                            onSave = { show -> viewModel.saveCurrentlyWatching(show) },
                            onClear = { viewModel.clearCurrentlyWatching() }
                        )
                    }
                }
                "nft_showcase" -> if (ws.showNftShowcase) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        NftShowcaseWidget(
                            nfts = uiState.nfts,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { nft -> viewModel.addNft(nft) },
                            onDelete = { nft -> viewModel.deleteNft(nft) }
                        )
                    }
                }
                "polls" -> if (ws.showPolls) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        PollsWidget(
                            polls = uiState.polls,
                            isCurrentUser = uiState.isCurrentUser,
                            userId = uiState.user?.id ?: "",
                            votedPollOptions = uiState.votedPollOptions,
                            onCreatePoll = { poll -> viewModel.createPoll(poll) },
                            onDeletePoll = { poll -> viewModel.deletePoll(poll) },
                            onVote = { pollId, option -> viewModel.voteOnPoll(pollId, option) }
                        )
                    }
                }
                "code_snippets" -> if (ws.showCodeSnippets) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        CodeSnippetsWidget(
                            snippets = uiState.codeSnippets,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { snippet -> viewModel.addCodeSnippet(snippet) },
                            onDelete = { snippet -> viewModel.deleteCodeSnippet(snippet) }
                        )
                    }
                }
                "tech_stack" -> if (ws.showTechStack) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        TechStackWidget(
                            items = uiState.techStack,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { item -> viewModel.addTechStackItem(item) },
                            onDelete = { item -> viewModel.deleteTechStackItem(item) },
                            onReorder = { item, up -> viewModel.reorderTechStackItem(item, up) }
                        )
                    }
                }
                "gif_showcase" -> if (ws.showGifShowcase) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        GifShowcaseWidget(
                            gifs = uiState.gifs,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { gif -> viewModel.addGif(gif) },
                            onDelete = { gif -> viewModel.deleteGif(gif) },
                            onToggleRepeat = { gif -> viewModel.toggleGifRepeat(gif) }
                        )
                    }
                }
                "gif_showcase_1" -> if (ws.showGifShowcase1) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        GifShowcaseWidget(
                            gifs = uiState.gifs,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { gif -> viewModel.addGif(gif) },
                            onDelete = { gif -> viewModel.deleteGif(gif) },
                            onToggleRepeat = { gif -> viewModel.toggleGifRepeat(gif) }
                        )
                    }
                }
                "gif_showcase_2" -> if (ws.showGifShowcase2) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        GifShowcaseWidget(
                            gifs = uiState.gifs,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { gif -> viewModel.addGif(gif) },
                            onDelete = { gif -> viewModel.deleteGif(gif) },
                            onToggleRepeat = { gif -> viewModel.toggleGifRepeat(gif) }
                        )
                    }
                }
                "gif_showcase_3" -> if (ws.showGifShowcase3) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        GifShowcaseWidget(
                            gifs = uiState.gifs,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { gif -> viewModel.addGif(gif) },
                            onDelete = { gif -> viewModel.deleteGif(gif) },
                            onToggleRepeat = { gif -> viewModel.toggleGifRepeat(gif) }
                        )
                    }
                }
                "spotify_now_playing" -> if (ws.showSpotifyNowPlaying) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        SpotifyNowPlayingWidget(
                            track = uiState.spotifyTrack,
                            isCurrentUser = uiState.isCurrentUser,
                            userId = uiState.user?.id ?: "",
                            onSetTrack = { track -> viewModel.setSpotifyTrack(track) },
                            onClear = { viewModel.clearSpotifyTrack() }
                        )
                    }
                }
                "meme_wall" -> if (ws.showMemeWall) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        MemeWallWidget(
                            memes = uiState.memes,
                            isCurrentUser = uiState.isCurrentUser,
                            userId = uiState.user?.id ?: "",
                            onAddMeme = { meme -> viewModel.addMeme(meme) },
                            onDeleteMeme = { meme -> viewModel.deleteMeme(meme) },
                            onReact = { memeId, reaction -> viewModel.reactToMeme(memeId, reaction) }
                        )
                    }
                }
                "game_stats" -> if (ws.showGameStats) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        GameStatsWidget(
                            stats = uiState.gameStats,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { stats -> viewModel.addGameStats(stats) }
                        )
                    }
                }
                "visited_places" -> if (ws.showVisitedPlaces) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        VisitedPlacesWidget(
                            places = uiState.visitedPlaces,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { place -> viewModel.addVisitedPlace(place) },
                            onDelete = { place -> viewModel.deleteVisitedPlace(place) }
                        )
                    }
                }
                "travel_wishlist" -> if (ws.showTravelWishlist) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        TravelWishlistWidget(
                            wishes = uiState.travelWishes,
                            isCurrentUser = uiState.isCurrentUser,
                            onAdd = { wish -> viewModel.addTravelWish(wish) },
                            onDelete = { wish -> viewModel.deleteTravelWish(wish) }
                        )
                    }
                }
                "multiplayer_games" -> if (ws.showMultiplayerGames) {
                    DraggableWidgetWrapper(
                        widgetId = widgetId,
                        isEditMode = isLayoutEditMode,
                        position = position,
                        onPositionChange = { newPos -> widgetPositions = widgetPositions.toMutableMap().apply { put(widgetId, newPos) }},
                        onDragEnd = { viewModel.saveWidgetPositions(widgetPositionsToJson(widgetPositions)) }
                    ) {
                        MultiplayerGamesWidget(
                            ticTacToeGames = uiState.ticTacToeGames,
                            minesweeperGames = uiState.minesweeperGames,
                            pendingInvites = uiState.pendingGameInvites,
                            friends = uiState.topFriends,
                            isCurrentUser = uiState.isCurrentUser,
                            userId = uiState.user?.id ?: "",
                            userName = uiState.user?.displayName ?: "",
                            onCreateTicTacToe = { oppId, oppName -> viewModel.createTicTacToeGame(oppId, oppName) },
                            onCreateMinesweeper = { oppId, oppName -> viewModel.createMinesweeperGame(oppId, oppName) },
                            onMakeMove = { gameId, row, col -> viewModel.makeTicTacToeMove(gameId, row, col) },
                            onAcceptInvite = { invite -> viewModel.acceptGameInvite(invite) },
                            onDeclineInvite = { invite -> viewModel.declineGameInvite(invite) },
                            onPlayGame = { _, _ -> }
                        )
                    }
                }
            }
        }

        // Stats Row
        item { ProfileStatsRow(user = user) }

        // Posts Header with tab switcher
        item {
            Column {
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
                if (isMe) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Posts" to Icons.Filled.GridView, "Saved" to Icons.Filled.Bookmark).forEachIndexed { idx, (label, icon) ->
                            FilterChip(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                label = { Text(label, fontSize = 12.sp) },
                                leadingIcon = { Icon(icon, null, modifier = Modifier.size(14.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HuabuHotPink.copy(alpha = 0.2f),
                                    selectedLabelColor = HuabuHotPink,
                                    selectedLeadingIconColor = HuabuHotPink
                                )
                            )
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = HuabuHotPink,
                    thickness = 1.5.dp
                )
            }
        }

        // Saved Posts (only when Saved tab selected)
        if (isMe && selectedTab == 1) {
            if (uiState.savedPosts.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No saved posts yet — bookmark posts to see them here", color = HuabuSilver, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                items(uiState.savedPosts, key = { "saved_${it.id}" }) { post ->
                    PostCard(
                        post = post,
                        currentUserId = currentUserId,
                        onLike = { viewModel.likePost(post.id) },
                        onComment = { viewModel.openComments(post.id) },
                        onBookmark = { viewModel.bookmarkPost(post.id) },
                        onReact = { emoji -> viewModel.reactToPost(post.id, emoji) },
                        onAuthorClick = { onNavigateToProfile(post.authorId) },
                        onPostClick = { onNavigateToPost(post.id) }
                    )
                }
            }
        }

        // Pinned Posts
        if (uiState.pinnedPosts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PushPin, contentDescription = null, tint = HuabuGold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pinned", style = MaterialTheme.typography.titleSmall, color = HuabuGold, fontWeight = FontWeight.Bold)
                }
            }
            items(uiState.pinnedPosts, key = { "pinned_${it.id}" }) { post ->
                PostCard(
                    post = post,
                    currentUserId = currentUserId,
                    onLike = { viewModel.likePost(post.id) },
                    onComment = { viewModel.openComments(post.id) },
                    onBookmark = { viewModel.bookmarkPost(post.id) },
                    onReact = { emoji -> viewModel.reactToPost(post.id, emoji) },
                    onShare = {
                        viewModel.incrementPostField(post.id, "sharesCount")
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out this post on Huabu: \"${post.content.take(100)}\"")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share post"))
                    },
                    onAuthorClick = { onNavigateToProfile(post.authorId) },
                    onPostClick = { onNavigateToPost(post.id) },
                    isPinned = true,
                    onPin = if (post.authorId == currentUserId) { { viewModel.togglePin(post) } } else null
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = HuabuDivider) }
        }

        // Posts (hidden when Saved tab selected)
        if (!isMe || selectedTab == 0)
        items(uiState.posts, key = { it.id }) { post ->
            PostCard(
                post = post,
                currentUserId = currentUserId,
                onLike = { viewModel.likePost(post.id) },
                onComment = { viewModel.openComments(post.id) },
                onBookmark = { viewModel.bookmarkPost(post.id) },
                onReact = { emoji -> viewModel.reactToPost(post.id, emoji) },
                onShare = {
                    viewModel.incrementPostField(post.id, "sharesCount")
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out this post on Huabu: \"${post.content.take(100)}\"")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share post"))
                },
                onAuthorClick = { onNavigateToProfile(post.authorId) },
                onPostClick = { onNavigateToPost(post.id) },
                onDelete = if (post.authorId == currentUserId) {
                    { viewModel.deletePost(post.id) }
                } else null,
                onEdit = if (post.authorId == currentUserId) {
                    { onNavigateToEditPost(post.id) }
                } else null,
                onPin = if (post.authorId == currentUserId) { { viewModel.togglePin(post) } } else null,
                isPinned = uiState.pinnedPosts.any { it.id == post.id }
            )
        }
    }
    } // end Box (background image wrapper)
    } // end CompositionLocalProvider
}

@Composable
private fun ProfileBanner(user: User, theme: com.huabu.app.data.model.ProfileTheme = com.huabu.app.data.model.ProfileTheme("")) {
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
            val c1 = try { Color(parseHexColor(theme.primaryColor)) } catch (_: Exception) { Color(parseHexColor(user.profileColor1)) }
            val c2 = try { Color(parseHexColor(theme.secondaryColor)) } catch (_: Exception) { Color(parseHexColor(user.profileColor2)) }
            val bg = try { Color(parseHexColor(theme.backgroundColor)) } catch (_: Exception) { HuabuDarkBg }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(c1, c2, bg)
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
    theme: com.huabu.app.data.model.ProfileTheme = com.huabu.app.data.model.ProfileTheme(""),
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    isBlocked: Boolean = false,
    onFollowClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onCustomiseClick: () -> Unit = {},
    onEditThemeClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBlockClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    val cardColor = try { Color(parseHexColor(theme.cardColor)) } catch (_: Exception) { HuabuCardBg }
    val nameColor = try { Color(parseHexColor(theme.nameColor)) } catch (_: Exception) { HuabuGold }
    val usernameColor = try { Color(parseHexColor(theme.usernameColor)) } catch (_: Exception) { HuabuAccentPink }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .offset(y = (-36).dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(listOf(cardColor, cardColor.copy(alpha = 0.85f)))
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isCurrentUser) {
                Box(modifier = Modifier.align(Alignment.End)) {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = HuabuSilver)
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isBlocked) "Unblock" else "Block", color = HuabuError) },
                            onClick = { showMoreMenu = false; onBlockClick() },
                            leadingIcon = {
                                Icon(Icons.Filled.Block, contentDescription = null, tint = HuabuError)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Report", color = HuabuError) },
                            onClick = { showMoreMenu = false; onReportClick() },
                            leadingIcon = {
                                Icon(Icons.Filled.Flag, contentDescription = null, tint = HuabuError)
                            }
                        )
                    }
                }
            }

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
                    color = nameColor
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
                color = usernameColor
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
                        onClick = { onMessageClick(user.id) },
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
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Palette, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("Theme", fontSize = 11.sp, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onSettingsClick,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("Settings", fontSize = 11.sp, maxLines = 1)
                    }
                }
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
            VerticalDivider(modifier = Modifier.height(40.dp), color = HuabuDivider)
            StatItem(label = "Views", count = user.profileViewsCount, color = HuabuNeonGreen)
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

// Widget position parsing for drag-and-drop
private fun parseWidgetPositions(json: String): MutableMap<String, androidx.compose.ui.unit.IntOffset> {
    val positions = mutableMapOf<String, androidx.compose.ui.unit.IntOffset>()
    if (json.isEmpty()) return positions
    
    return try {
        val obj = org.json.JSONObject(json)
        obj.keys().forEach { key ->
            val pos = obj.getJSONObject(key)
            positions[key] = androidx.compose.ui.unit.IntOffset(
                pos.optInt("x", 0),
                pos.optInt("y", 0)
            )
        }
        positions
    } catch (e: Exception) {
        positions
    }
}

private fun widgetPositionsToJson(positions: Map<String, androidx.compose.ui.unit.IntOffset>): String {
    val obj = org.json.JSONObject()
    positions.forEach { (key, pos) ->
        val posObj = org.json.JSONObject()
        posObj.put("x", pos.x)
        posObj.put("y", pos.y)
        obj.put(key, posObj)
    }
    return obj.toString()
}

// Draggable widget wrapper for layout edit mode
@Composable
private fun DraggableWidgetWrapper(
    widgetId: String,
    isEditMode: Boolean,
    position: IntOffset,
    onPositionChange: (IntOffset) -> Unit,
    onDragEnd: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!isEditMode) {
        // Normal mode - just show content
        content()
        return
    }
    
    // Edit mode - wrap with draggable and resizable
    var offset by remember(position) { mutableStateOf(position) }
    var widgetSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize(0, 0)) }
    var isResizing by remember { mutableStateOf(false) }
    val gridSize = 20 // Grid snap size in pixels
    
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .offset { offset }
            .onSizeChanged { widgetSize = it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(widgetId) {
                    detectDragGestures(
                        onDragEnd = { 
                            // Snap to grid on drag end
                            val snappedX = (offset.x.toFloat() / gridSize).roundToInt() * gridSize
                            val snappedY = (offset.y.toFloat() / gridSize).roundToInt() * gridSize
                            offset = IntOffset(snappedX, snappedY)
                            onPositionChange(offset)
                            onDragEnd()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offset = IntOffset(
                                offset.x + dragAmount.x.roundToInt(),
                                offset.y + dragAmount.y.roundToInt()
                            )
                            onPositionChange(offset)
                        }
                    )
                }
        ) {
            Column {
                // Drag handle indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFFEF4444).copy(alpha = 0.8f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.DragHandle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("DRAG TO MOVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.DragHandle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                
                // Actual widget content with border and resize handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                ) {
                    content()
                    
                    // Resize handle at bottom-right corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .pointerInput(widgetId) {
                                detectDragGestures(
                                    onDragStart = { isResizing = true },
                                    onDragEnd = { 
                                        isResizing = false
                                        onDragEnd()
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        // For now, we just indicate resizing is happening
                                        // Full resize implementation would require more complex state management
                                    }
                                )
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInFull,
                            contentDescription = "Resize",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
