package com.huabu.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.huabu.app.data.model.MediaTrack
import com.huabu.app.data.model.Post
import com.huabu.app.data.model.User
import com.huabu.app.ui.components.PostCard
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("People") }

    val categories = listOf("People", "Posts", "Tags", "Music")
    val displayUsers = if (uiState.hasQuery) uiState.results else uiState.trendingUsers

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HuabuCardBg)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "★ Discover ★",
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.linearGradient(listOf(HuabuNeonGreen, HuabuAccentCyan)),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HuabuCardBg)
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; viewModel.onQueryChanged(it, selectedCategory) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    placeholder = { Text("Search Huabu...", color = HuabuSilver) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = HuabuSilver) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = ""; viewModel.onQueryChanged("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = HuabuSilver)
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuabuNeonGreen,
                        unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface,
                        unfocusedTextColor = HuabuOnSurface,
                        cursorColor = HuabuNeonGreen
                    ),
                    singleLine = true
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            onClick = { selectedCategory = cat; if (query.isNotEmpty()) viewModel.onQueryChanged(query, cat) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) HuabuNeonGreen else HuabuCardBg2,
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = if (isSelected) HuabuNeonGreen else HuabuDivider,
                                shape = RoundedCornerShape(20.dp)
                            )
                        ) {
                            Text(
                                text = cat,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                color = if (isSelected) HuabuDarkBg else HuabuSilver,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = HuabuDarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (!uiState.hasQuery && selectedCategory == "Music") {
                item { SectionLabel(title = "✦ Trending Music ✦") }
                items(uiState.trendingTracks, key = { it.id }) { track ->
                    TrackRow(track = track)
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = HuabuDivider, thickness = 0.5.dp)
                }
            } else if (!uiState.hasQuery) {
                item {
                    val tagLabels = if (uiState.trendingTags.isNotEmpty())
                        uiState.trendingTags.map { (tag, _) -> "#$tag" }
                    else listOf("#huabu", "#mypage", "#top8", "#profilesong", "#aesthetic", "#vibes", "#glitter", "#neonlife")
                    TrendingTagsSection(tags = tagLabels, onTagClick = { tag ->
                        query = tag.trimStart('#')
                        selectedCategory = "Tags"
                        viewModel.onQueryChanged(query, "Tags")
                    })
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    SectionLabel(title = "✦ Trending Users ✦")
                }
                items(uiState.trendingUsers, key = { it.id }) { user ->
                    SearchUserRow(
                        user = user,
                        isPending = user.id in uiState.pendingSentIds,
                        isFollowing = user.id in uiState.followingIds,
                        onClick = { onNavigateToProfile(user.id) },
                        onMessage = { viewModel.getConversationForUser(user.id) { onNavigateToChat(it) } },
                        onAddFriend = { viewModel.sendFriendRequest(user.id) },
                        onFollow = { viewModel.toggleFollow(user.id) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = HuabuDivider, thickness = 0.5.dp)
                }
            } else if (uiState.isSearching) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HuabuNeonGreen)
                    }
                }
            } else when (selectedCategory) {
                "Posts" -> {
                    item { SectionLabel(title = "${uiState.postResults.size} posts for \"$query\"") }
                    items(uiState.postResults, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            currentUserId = uiState.currentUserId,
                            onLike = { viewModel.likePost(post.id) },
                            onReact = { emoji -> viewModel.reactToPost(post.id, emoji) },
                            onAuthorClick = { onNavigateToProfile(post.authorId) },
                            onPostClick = { onNavigateToPost(post.id) }
                        )
                    }
                }
                "Tags" -> {
                    item { SectionLabel(title = "${uiState.tagResults.size} posts tagged \"#$query\"") }
                    items(uiState.tagResults, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            currentUserId = uiState.currentUserId,
                            onLike = { viewModel.likePost(post.id) },
                            onReact = { emoji -> viewModel.reactToPost(post.id, emoji) },
                            onAuthorClick = { onNavigateToProfile(post.authorId) },
                            onPostClick = { onNavigateToPost(post.id) }
                        )
                    }
                }
                "Music" -> {
                    item { SectionLabel(title = "${uiState.trackResults.size} tracks for \"$query\"") }
                    items(uiState.trackResults, key = { it.id }) { track ->
                        TrackRow(track = track)
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = HuabuDivider, thickness = 0.5.dp)
                    }
                }
                else -> {
                    item { SectionLabel(title = "${uiState.results.size} results for \"$query\"") }
                    items(uiState.results, key = { it.id }) { user ->
                        SearchUserRow(
                            user = user,
                            isPending = user.id in uiState.pendingSentIds,
                            isFollowing = user.id in uiState.followingIds,
                            onClick = { onNavigateToProfile(user.id) },
                            onMessage = { viewModel.getConversationForUser(user.id) { onNavigateToChat(it) } },
                            onAddFriend = { viewModel.sendFriendRequest(user.id) },
                            onFollow = { viewModel.toggleFollow(user.id) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = HuabuDivider, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingTagsSection(tags: List<String>, onTagClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        SectionLabel(title = "✦ Trending Tags ✦")
        Spacer(Modifier.height(8.dp))

        val rows = tags.chunked(4)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { tag ->
                    Surface(
                        onClick = { onTagClick(tag) },
                        shape = RoundedCornerShape(20.dp),
                        color = HuabuDeepPurple.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(vertical = 3.dp)
                            .border(1.dp, HuabuHotPink.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = HuabuAccentPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = HuabuGold,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun SearchUserRow(
    user: User,
    onClick: () -> Unit,
    onMessage: () -> Unit = {},
    onAddFriend: () -> Unit = {},
    onFollow: () -> Unit = {},
    isPending: Boolean = false,
    isFollowing: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    brush = Brush.sweepGradient(listOf(HuabuHotPink, HuabuGold, HuabuHotPink)),
                    shape = CircleShape
                )
                .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.displayName.first().uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (user.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .background(HuabuNeonGreen, CircleShape)
                        .border(2.dp, HuabuDarkBg, CircleShape)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.displayName,
                    fontWeight = FontWeight.Bold,
                    color = HuabuGold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.isVerified) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = HuabuElectricBlue, modifier = Modifier.size(14.dp))
                }
                if (user.mood.isNotEmpty()) {
                    Spacer(Modifier.width(4.dp))
                    Text(user.mood, fontSize = 14.sp)
                }
            }
            Text(
                text = "@${user.username}",
                color = HuabuSilver,
                fontSize = 12.sp
            )
            if (user.interests.isNotEmpty()) {
                Text(
                    text = user.interests,
                    color = HuabuAccentCyan,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatFollowers(user.followersCount),
                color = HuabuHotPink,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(text = "followers", color = HuabuSilver, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalButton(
                    onClick = onFollow,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isFollowing) HuabuNeonGreen.copy(alpha = 0.15f) else HuabuHotPink.copy(alpha = 0.15f)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        if (isFollowing) Icons.Filled.PersonRemove else Icons.Filled.PersonAdd,
                        contentDescription = null,
                        tint = if (isFollowing) HuabuNeonGreen else HuabuHotPink,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        if (isFollowing) "Following" else "Follow",
                        fontSize = 11.sp,
                        color = if (isFollowing) HuabuNeonGreen else HuabuHotPink
                    )
                }
                FilledTonalButton(
                    onClick = onMessage,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = HuabuElectricBlue.copy(alpha = 0.15f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.Message, contentDescription = null, tint = HuabuElectricBlue, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Chat", fontSize = 11.sp, color = HuabuElectricBlue)
                }
            }
        }
    }
}

private fun formatFollowers(count: Int): String = when {
    count >= 1_000 -> "${count / 1_000}K"
    else -> count.toString()
}

@Composable
private fun TrackRow(track: MediaTrack) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (track.artworkUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = HuabuGold, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontWeight = FontWeight.Bold,
                color = HuabuGold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (track.subtitle.isNotEmpty()) {
                Text(
                    text = track.subtitle,
                    color = HuabuSilver,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (track.year.isNotEmpty()) {
            Text(text = track.year, color = HuabuSilver, fontSize = 12.sp)
        }
    }
}

