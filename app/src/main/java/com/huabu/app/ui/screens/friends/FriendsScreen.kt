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
import com.huabu.app.data.model.User
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(onNavigateToProfile: (String) -> Unit) {
    val mockFriends = remember { generateMockFriends() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Friends", "Requests", "Find Friends")

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
                    containerColor = HuabuCardBg
                )
            )
        },
        containerColor = HuabuDarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = HuabuCardBg,
                contentColor = HuabuHotPink,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = HuabuHotPink
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
                0 -> FriendsGrid(friends = mockFriends, onNavigateToProfile = onNavigateToProfile)
                1 -> FriendRequestsTab(onNavigateToProfile = onNavigateToProfile)
                2 -> FindFriendsTab(onNavigateToProfile = onNavigateToProfile)
            }
        }
    }
}

@Composable
private fun FriendsGrid(friends: List<User>, onNavigateToProfile: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(friends, key = { it.id }) { user ->
            FriendGridCard(user = user, onClick = { onNavigateToProfile(user.id) })
        }
    }
}

@Composable
private fun FriendGridCard(user: User, onClick: () -> Unit) {
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
                Text(
                    text = user.displayName.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
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
        }
    }
}

@Composable
private fun FriendRequestsTab(onNavigateToProfile: (String) -> Unit) {
    val requests = remember { generateMockFriends().take(3) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            "${requests.size} friend requests",
            style = MaterialTheme.typography.titleSmall,
            color = HuabuSilver,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        requests.forEach { user ->
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
                        Text(user.displayName.first().uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName, color = HuabuGold, fontWeight = FontWeight.Bold)
                        Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = HuabuSilver)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalButton(
                            onClick = {},
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = HuabuHotPink),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("Accept", fontSize = 12.sp) }
                        OutlinedButton(
                            onClick = {},
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("Decline", fontSize = 12.sp, color = HuabuSilver) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FindFriendsTab(onNavigateToProfile: (String) -> Unit) {
    val suggestions = remember { generateMockFriends().drop(3) }

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
                        Text(user.displayName.first().uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName, color = HuabuGold, fontWeight = FontWeight.Bold)
                        Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = HuabuSilver)
                        Text("${user.followersCount} followers", style = MaterialTheme.typography.labelSmall, color = HuabuAccentCyan)
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = HuabuHotPink),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun generateMockFriends(): List<User> = listOf(
    User("u1", "xenastar", "Xena Starfire", mood = "😍", isOnline = true, followersCount = 428),
    User("u2", "djphantom", "DJ Phantom", mood = "🎵", isOnline = true, followersCount = 1203),
    User("u3", "lunaeclipse", "Luna Eclipse", mood = "🌙", followersCount = 334),
    User("u4", "retrokid2k", "Retro Kid", mood = "🎮", isOnline = false, followersCount = 89),
    User("u5", "glitterqueen99", "Glitter Queen", mood = "💅", isOnline = true, followersCount = 777),
    User("u6", "neonninja", "Neon Ninja", mood = "⚡", followersCount = 256),
    User("u7", "starchaser", "Star Chaser", mood = "🌟", isOnline = true, followersCount = 512),
    User("u8", "pixelprince", "Pixel Prince", mood = "🎮", followersCount = 198),
    User("u9", "cosmicwave", "Cosmic Wave", mood = "🌊", isOnline = true, followersCount = 631),
    User("u10", "velvetdream", "Velvet Dream", mood = "✨", followersCount = 445)
)
