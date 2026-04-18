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
import com.huabu.app.data.model.User
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onNavigateToProfile: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("People") }

    val categories = listOf("People", "Posts", "Tags", "Music")

    val mockUsers = remember { generateSearchUsers() }
    val filtered = if (query.isEmpty()) mockUsers
    else mockUsers.filter {
        it.displayName.contains(query, ignoreCase = true) ||
            it.username.contains(query, ignoreCase = true) ||
            it.interests.contains(query, ignoreCase = true)
    }

    val trendingTags = listOf("#huabu", "#mypage", "#top8", "#profilesong", "#aesthetic", "#vibes", "#glitter", "#neonlife")

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
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    placeholder = { Text("Search Huabu...", color = HuabuSilver) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = HuabuSilver) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
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
                            onClick = { selectedCategory = cat },
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
            if (query.isEmpty()) {
                item {
                    TrendingTagsSection(tags = trendingTags, onTagClick = { query = it.trimStart('#') })
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    SectionLabel(title = "✦ Trending Users ✦")
                }
            } else {
                item {
                    SectionLabel(title = "${filtered.size} results for \"$query\"")
                }
            }

            items(filtered, key = { it.id }) { user ->
                SearchUserRow(user = user, onClick = { onNavigateToProfile(user.id) })
                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp),
                    color = HuabuDivider,
                    thickness = 0.5.dp
                )
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
private fun SearchUserRow(user: User, onClick: () -> Unit) {
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
        }
    }
}

private fun formatFollowers(count: Int): String = when {
    count >= 1_000 -> "${count / 1_000}K"
    else -> count.toString()
}

private fun generateSearchUsers(): List<User> = listOf(
    User("u1", "xenastar", "Xena Starfire", mood = "😍", interests = "Music, Art, Fashion", isOnline = true, followersCount = 4280, isVerified = true),
    User("u2", "djphantom", "DJ Phantom", mood = "🎵", interests = "Music, Mixing, Nightlife", isOnline = true, followersCount = 12030),
    User("u3", "lunaeclipse", "Luna Eclipse", mood = "🌙", interests = "Astrology, Photography, Writing", followersCount = 3340),
    User("u4", "retrokid2k", "Retro Kid", mood = "🎮", interests = "Gaming, Anime, Retrowave", followersCount = 890),
    User("u5", "glitterqueen99", "Glitter Queen", mood = "💅", interests = "Fashion, Beauty, Art", isOnline = true, followersCount = 7770),
    User("u6", "neonninja", "Neon Ninja", mood = "⚡", interests = "Skateboarding, Music, Street Art", followersCount = 2560),
    User("u7", "starchaser", "Star Chaser", mood = "🌟", interests = "Astronomy, Sci-Fi, Gaming", isOnline = true, followersCount = 5120),
    User("u8", "pixelprince", "Pixel Prince", mood = "🎮", interests = "Game Dev, Pixel Art, Anime", followersCount = 1980),
    User("u9", "cosmicwave", "Cosmic Wave", mood = "🌊", interests = "Music, Surfing, Travel", isOnline = true, followersCount = 6310),
    User("u10", "velvetdream", "Velvet Dream", mood = "✨", interests = "Poetry, Music, Photography", followersCount = 4450)
)
