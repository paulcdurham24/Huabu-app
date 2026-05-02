package com.huabu.app.ui.screens.feed

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.huabu.app.data.model.Story
import com.huabu.app.ui.components.CommentsBottomSheet
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.huabu.app.ui.components.GlitterCanvas
import com.huabu.app.ui.components.PostCard
import com.huabu.app.ui.screens.profile.LocalProfileTheme
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToCompose: () -> Unit,
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToEditPost: (String) -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel(),
    authViewModel: com.huabu.app.ui.screens.auth.AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val commentsState by viewModel.commentsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val storyImageLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri: Uri? ->
        uri?.let { viewModel.postStory(it, "") }
    }
    // Story viewer state: list of per-user story groups ordered for swipe
    val storyGroups = remember(uiState.stories) {
        uiState.stories.groupBy { it.authorId }.values.map { it.sortedBy { s -> s.createdAt } }
    }
    var viewingGroupIdx by remember { mutableStateOf(-1) }
    var viewingStoryIdx by remember { mutableStateOf(0) }
    val viewingGroup = if (viewingGroupIdx >= 0 && viewingGroupIdx < storyGroups.size) storyGroups[viewingGroupIdx] else null
    val viewingStory = viewingGroup?.getOrNull(viewingStoryIdx)

    if (viewingStory != null && viewingGroup != null) {
        StoryViewerDialog(
            stories = viewingGroup,
            storyIndex = viewingStoryIdx,
            allGroups = storyGroups,
            groupIndex = viewingGroupIdx,
            currentUserId = uiState.currentUserId,
            onDismiss = { viewingGroupIdx = -1 },
            onView = { viewModel.viewStory(viewingStory.id) },
            onDelete = { viewModel.deleteStory(viewingStory.id); viewingGroupIdx = -1 },
            onAdvance = { newGroup, newStory -> viewingGroupIdx = newGroup; viewingStoryIdx = newStory }
        )
    }

    if (commentsState.postId.isNotEmpty()) {
        CommentsBottomSheet(
            postId = commentsState.postId,
            comments = commentsState.comments,
            currentUserName = uiState.currentUserName,
            currentUserId = uiState.currentUserId,
            currentUserImageUrl = uiState.currentUserImageUrl,
            isLoading = commentsState.isLoading,
            onDismiss = { viewModel.closeComments() },
            onSubmit = { text -> viewModel.commentPost(commentsState.postId, text) }
        )
    }

    val appTheme = LocalProfileTheme.current
    val themeBg = remember(appTheme.backgroundColor) {
        runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(appTheme.backgroundColor)) }.getOrElse { HuabuDarkBg }
    }
    val themeCard = remember(appTheme.cardColor) {
        runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(appTheme.cardColor)) }.getOrElse { HuabuCardBg2 }
    }
    val themeAccent = remember(appTheme.primaryColor) {
        runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(appTheme.primaryColor)) }.getOrElse { HuabuHotPink }
    }

    Scaffold(
        topBar = { HuabuFeedTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCompose,
                containerColor = themeAccent,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Post")
            }
        },
        containerColor = themeBg
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HuabuHotPink)
                }
            } else {
                val listState = rememberLazyListState()
                val lastIndex by remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 } }
                val totalItems by remember { derivedStateOf { listState.layoutInfo.totalItemsCount } }
                LaunchedEffect(lastIndex, totalItems) {
                    if (totalItems > 0 && lastIndex >= totalItems - 3) {
                        viewModel.loadMorePosts()
                    }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (!authViewModel.isEmailVerified()) {
                        item {
                            var cooldown by remember { mutableIntStateOf(0) }
                            LaunchedEffect(cooldown) {
                                if (cooldown > 0) {
                                    kotlinx.coroutines.delay(1000)
                                    cooldown--
                                }
                            }
                            EmailVerificationBanner(
                                cooldownSeconds = cooldown,
                                onResend = {
                                    if (cooldown == 0) {
                                        cooldown = 30
                                        authViewModel.resendVerificationEmail { result ->
                                            result.onSuccess {
                                                android.widget.Toast.makeText(context, "Verification email sent! Check your inbox.", android.widget.Toast.LENGTH_LONG).show()
                                            }.onFailure { e ->
                                                android.widget.Toast.makeText(context, "Failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                                cooldown = 0
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    item { WhatHappeningStrip() }
                    item {
                        StoryRow(
                            stories = uiState.stories,
                            currentUserId = uiState.currentUserId,
                            onAddStory = { storyImageLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                            onViewStory = { story ->
                                val gIdx = storyGroups.indexOfFirst { g -> g.any { it.id == story.id } }
                                if (gIdx >= 0) { viewingGroupIdx = gIdx; viewingStoryIdx = 0 }
                            }
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filters = listOf(
                                FeedFilter.ALL to "✦ All",
                                FeedFilter.FRIENDS to "Friends",
                                FeedFilter.MINE to "My Posts"
                            )
                            items(filters) { (filter, label) ->
                                val selected = uiState.selectedFilter == filter
                                Surface(
                                    onClick = { viewModel.setFilter(filter) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selected) themeAccent else themeCard
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        color = if (selected) Color.White else HuabuSilver,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                    items(uiState.posts, key = { it.id }) { post ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            PostCard(
                                post = post,
                                currentUserId = uiState.currentUserId,
                                isAuthorOnline = post.authorId in uiState.onlineUserIds,
                                onLike = { viewModel.likePost(post.id) },
                                onComment = { viewModel.openComments(post.id) },
                                onBookmark = { viewModel.bookmarkPost(post.id) },
                                onReact = { emoji -> viewModel.reactToPost(post.id, emoji) },
                                onShare = {
                                    viewModel.sharePost(post.id)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Check out this post on Huabu: \"${post.content.take(100)}\"")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share post"))
                                },
                                onAuthorClick = { onNavigateToProfile(post.authorId) },
                                onPostClick = { onNavigateToPost(post.id) },
                                onDelete = if (post.authorId == uiState.currentUserId) {
                                    { viewModel.deletePost(post.id) }
                                } else null,
                                onEdit = if (post.authorId == uiState.currentUserId) {
                                    { onNavigateToEditPost(post.id) }
                                } else null
                            )
                        }
                    }
                    if (uiState.isLoadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = HuabuHotPink, strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryRow(
    stories: List<Story>,
    currentUserId: String,
    onAddStory: () -> Unit,
    onViewStory: (Story) -> Unit
) {
    val grouped = stories.groupBy { it.authorId }
    val myStory = grouped[currentUserId]?.firstOrNull()
    val othersFirst = grouped.entries
        .filter { it.key != currentUserId }
        .map { it.value.first() }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Your story bubble
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(64.dp)
                    .clickable { if (myStory != null) onViewStory(myStory) else onAddStory() }
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                if (myStory != null) Brush.sweepGradient(listOf(HuabuHotPink, HuabuElectricBlue, HuabuGold))
                                else Brush.linearGradient(listOf(HuabuDivider, HuabuDivider)),
                                CircleShape
                            )
                            .background(HuabuCardBg2),
                        contentAlignment = Alignment.Center
                    ) {
                        if (myStory != null) {
                            AsyncImage(
                                model = myStory.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("☆", fontSize = 22.sp)
                        }
                    }
                    if (myStory == null) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(HuabuHotPink, CircleShape)
                                .border(2.dp, HuabuDarkBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
                Text(
                    text = "Your story",
                    color = HuabuSilver,
                    fontSize = 10.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        // Others' story bubbles
        items(othersFirst, key = { it.authorId }) { story ->
            val seen = currentUserId in story.viewerIds
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(64.dp)
                    .clickable { onViewStory(story) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            if (!seen) Brush.sweepGradient(listOf(HuabuHotPink, HuabuElectricBlue, HuabuGold))
                            else Brush.linearGradient(listOf(HuabuDivider, HuabuDivider)),
                            CircleShape
                        )
                        .background(HuabuCardBg2),
                    contentAlignment = Alignment.Center
                ) {
                    if (story.authorImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = story.authorImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            story.authorName.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                Text(
                    text = story.authorName.split(" ").firstOrNull() ?: story.authorName,
                    color = if (!seen) HuabuOnSurface else HuabuSilver,
                    fontSize = 10.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StoryViewerDialog(
    stories: List<Story>,
    storyIndex: Int,
    allGroups: List<List<Story>>,
    groupIndex: Int,
    currentUserId: String,
    onDismiss: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit,
    onAdvance: (newGroup: Int, newStory: Int) -> Unit
) {
    val story = stories.getOrNull(storyIndex) ?: return
    val isOwn = story.authorId == currentUserId
    val storyDurationMs = 5000
    var progress by remember(story.id) { mutableFloatStateOf(0f) }
    LaunchedEffect(story.id) {
        onView()
        progress = 0f
        val steps = 100
        val stepMs = storyDurationMs / steps
        repeat(steps) {
            kotlinx.coroutines.delay(stepMs.toLong())
            progress = (it + 1) / steps.toFloat()
        }
        // auto-advance
        if (storyIndex + 1 < stories.size) {
            onAdvance(groupIndex, storyIndex + 1)
        } else if (groupIndex + 1 < allGroups.size) {
            onAdvance(groupIndex + 1, 0)
        } else {
            onDismiss()
        }
    }

    fun goNext() {
        if (storyIndex + 1 < stories.size) onAdvance(groupIndex, storyIndex + 1)
        else if (groupIndex + 1 < allGroups.size) onAdvance(groupIndex + 1, 0)
        else onDismiss()
    }
    fun goPrev() {
        if (storyIndex - 1 >= 0) onAdvance(groupIndex, storyIndex - 1)
        else if (groupIndex - 1 >= 0) onAdvance(groupIndex - 1, allGroups[groupIndex - 1].lastIndex)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(story.id) {
                    detectTapGestures { offset ->
                        if (offset.x < size.width / 2f) goPrev() else goNext()
                    }
                }
                .pointerInput(story.id) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount < -80) goNext()
                        else if (dragAmount > 80) goPrev()
                    }
                }
        ) {
            AsyncImage(
                model = story.imageUrl,
                contentDescription = "Story",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            // Progress bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                stories.forEachIndexed { idx, _ ->
                    val segProgress = when {
                        idx < storyIndex -> 1f
                        idx == storyIndex -> progress
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segProgress },
                        modifier = Modifier.weight(1f).height(2.dp).clip(RoundedCornerShape(1.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(top = 18.dp)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(HuabuDeepPurple),
                    contentAlignment = Alignment.Center
                ) {
                    if (story.authorImageUrl.isNotEmpty()) {
                        AsyncImage(model = story.authorImageUrl, contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Text(story.authorName.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(story.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (story.caption.isNotEmpty()) Text(story.caption, color = Color.White.copy(0.7f), fontSize = 12.sp)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, null, tint = Color.White) }
            }
            if (isOwn) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
                ) { Text("Delete story", color = HuabuHotPink, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HuabuFeedTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(HuabuDeepPurple, HuabuSurface, HuabuDeepPurple)
                )
            )
    ) {
        GlitterCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            sparkleCount = 15
        )
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = HuabuGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Huabu",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.linearGradient(
                                listOf(HuabuHotPink, HuabuGold, HuabuAccentCyan)
                            ),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
    }
}

@Composable
private fun EmailVerificationBanner(cooldownSeconds: Int, onResend: () -> Unit) {
    var dismissed by remember { mutableStateOf(false) }
    if (dismissed) return
    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .background(Color(0xFF7C3AED))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = androidx.compose.ui.Modifier.size(16.dp))
        Text(
            "Verify your email to unlock all features.",
            color = Color.White,
            fontSize = 12.sp,
            modifier = androidx.compose.ui.Modifier.weight(1f)
        )
        TextButton(
            onClick = onResend,
            enabled = cooldownSeconds == 0,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                if (cooldownSeconds > 0) "Resend (${cooldownSeconds}s)" else "Resend",
                color = if (cooldownSeconds > 0) Color.White.copy(alpha = 0.5f) else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = { dismissed = true }, modifier = androidx.compose.ui.Modifier.size(24.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = Color.White, modifier = androidx.compose.ui.Modifier.size(16.dp))
        }
    }
}

@Composable
private fun WhatHappeningStrip() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.radialGradient(listOf(HuabuHotPink, HuabuDeepPurple)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "What's happening on your page?",
                style = MaterialTheme.typography.bodyMedium,
                color = HuabuSilver,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
