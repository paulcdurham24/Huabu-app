package com.huabu.app.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.huabu.app.data.model.User
import com.huabu.app.ui.screens.profile.LocalProfileTheme
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Friends", "Requests", "Find Friends")

    val appTheme = LocalProfileTheme.current
    val themeBg = remember(appTheme.backgroundColor) {
        runCatching { Color(android.graphics.Color.parseColor(appTheme.backgroundColor)) }.getOrElse { HuabuDarkBg }
    }
    val themeCard = remember(appTheme.cardColor) {
        runCatching { Color(android.graphics.Color.parseColor(appTheme.cardColor)) }.getOrElse { HuabuCardBg }
    }
    val themeAccent = remember(appTheme.primaryColor) {
        runCatching { Color(android.graphics.Color.parseColor(appTheme.primaryColor)) }.getOrElse { HuabuHotPink }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "★ Friends ★",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.linearGradient(listOf(HuabuHotPink, HuabuGold)),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeCard
                )
            )
        },
        containerColor = themeBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = themeCard,
                contentColor = themeAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = themeAccent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) HuabuGold else HuabuSilver,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> FriendsGrid(
                    friends = uiState.friends,
                    isLoading = uiState.isLoading,
                    onNavigateToProfile = onNavigateToProfile,
                    onMessage = { userId ->
                        viewModel.getConversationForUser(userId) { onNavigateToChat(it) }
                    },
                    onUnfriend = { userId -> viewModel.unfriend(userId) }
                )
                1 -> FriendRequestsTab(
                    requests = uiState.friendRequests,
                    onNavigateToProfile = onNavigateToProfile,
                    onAccept = { docId -> viewModel.respondToRequest(docId, true) },
                    onDecline = { docId -> viewModel.respondToRequest(docId, false) }
                )
                2 -> FindFriendsTab(
                    suggestions = uiState.suggestedUsers,
                    pendingSentIds = uiState.pendingSentIds,
                    friendIds = uiState.friendIds,
                    onNavigateToProfile = onNavigateToProfile,
                    onAddFriend = { userId -> viewModel.sendFriendRequest(userId) }
                )
            }
        }
    }
}

@Composable
private fun FriendsGrid(
    friends: List<User>,
    isLoading: Boolean,
    onNavigateToProfile: (String) -> Unit,
    onMessage: (String) -> Unit,
    onUnfriend: (String) -> Unit
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HuabuHotPink)
        }
        return
    }
    if (friends.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No friends yet — find some in Find Friends!", color = HuabuSilver, textAlign = TextAlign.Center)
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(friends, key = { it.id }) { user ->
            FriendGridCard(
                user = user,
                onClick = { onNavigateToProfile(user.id) },
                onMessage = { onMessage(user.id) },
                onUnfriend = { onUnfriend(user.id) }
            )
        }
    }
}

@Composable
private fun FriendGridCard(user: User, onClick: () -> Unit, onMessage: () -> Unit, onUnfriend: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },

        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(HuabuCardBg, HuabuCardBg2)))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        brush = Brush.sweepGradient(listOf(HuabuHotPink, HuabuGold, HuabuHotPink)),
                        shape = CircleShape
                    )
                    .background(
                        brush = Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        text = user.displayName.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                }
                if (user.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .background(HuabuNeonGreen, CircleShape)
                            .border(2.dp, HuabuCardBg, CircleShape)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = user.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = HuabuGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.labelSmall,
                color = HuabuSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (user.mood.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(text = user.mood, fontSize = 14.sp)
            }

            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalButton(
                    onClick = onMessage,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = HuabuElectricBlue.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Message, contentDescription = null, tint = HuabuElectricBlue, modifier = Modifier.size(12.dp))
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = HuabuSilver, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Unfriend", color = HuabuHotPink) },
                            onClick = { showMenu = false; onUnfriend() },
                            leadingIcon = { Icon(Icons.Filled.PersonRemove, contentDescription = null, tint = HuabuHotPink) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRequestsTab(
    requests: List<FriendRequest>,
    onNavigateToProfile: (String) -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            if (requests.isEmpty()) "No pending requests" else "${requests.size} friend request${if (requests.size != 1) "s" else ""}",
            style = MaterialTheme.typography.titleSmall,
            color = HuabuSilver,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        requests.forEach { req ->
            val user = req.user
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = HuabuCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuElectricBlue)))
                            .clickable { onNavigateToProfile(user.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profileImageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = user.profileImageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(user.displayName.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName, color = HuabuGold, fontWeight = FontWeight.Bold)
                        Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = HuabuSilver)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalButton(
                            onClick = { onAccept(req.docId) },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = HuabuHotPink),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("Accept", fontSize = 12.sp) }
                        OutlinedButton(
                            onClick = { onDecline(req.docId) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("Decline", fontSize = 12.sp, color = HuabuSilver) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FindFriendsTab(
    suggestions: List<User>,
    pendingSentIds: Set<String>,
    friendIds: Set<String>,
    onNavigateToProfile: (String) -> Unit,
    onAddFriend: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            "People you might know",
            style = MaterialTheme.typography.titleSmall,
            color = HuabuSilver,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        suggestions.forEach { user ->
            val isFriend = user.id in friendIds
            val isPending = user.id in pendingSentIds
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onNavigateToProfile(user.id) },
                colors = CardDefaults.cardColors(containerColor = HuabuCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(HuabuHotPink, HuabuDeepPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.displayName.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName, color = HuabuGold, fontWeight = FontWeight.Bold)
                        Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = HuabuSilver)
                        Text("${user.followersCount} followers", style = MaterialTheme.typography.labelSmall, color = HuabuAccentCyan)
                    }
                    Button(
                        onClick = { if (!isFriend && !isPending) onAddFriend(user.id) },
                        enabled = !isFriend && !isPending,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isFriend -> HuabuNeonGreen
                                isPending -> HuabuDivider
                                else -> HuabuHotPink
                            }
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        when {
                            isFriend -> { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Friends", fontSize = 12.sp) }
                            isPending -> Text("Pending", fontSize = 12.sp)
                            else -> { Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Add", fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}
