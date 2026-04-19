package com.huabu.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huabu.app.data.model.*
import com.huabu.app.ui.theme.*

// ─────────────────────────────────────────────
// BADGES WIDGET
// ─────────────────────────────────────────────

@Composable
fun BadgesWidget(
    badges: List<Badge>,
    isCurrentUser: Boolean
) {
    WidgetCard(title = "🏆 Badges", titleColor = HuabuGold) {
        if (badges.isEmpty()) {
            Text(
                text = "No badges yet — keep using Huabu to earn them!",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(badges) { badge ->
                    BadgeItem(badge = badge)
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(badge: Badge) {
    val (glowColor, bgGradient) = when (badge.rarity) {
        BadgeRarity.LEGENDARY -> Color(0xFFFFD700) to listOf(Color(0xFF3D2800), Color(0xFF1A1000))
        BadgeRarity.EPIC      -> Color(0xFFA855F7) to listOf(Color(0xFF2D0045), Color(0xFF150020))
        BadgeRarity.RARE      -> Color(0xFF06B6D4) to listOf(Color(0xFF002833), Color(0xFF001018))
        BadgeRarity.COMMON    -> HuabuSilver       to listOf(Color(0xFF1A1A2E), Color(0xFF0D0D1A))
    }

    var showTooltip by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { showTooltip = true }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(if (badge.rarity != BadgeRarity.COMMON) 8.dp else 2.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.radialGradient(bgGradient))
                .border(
                    width = if (badge.rarity == BadgeRarity.LEGENDARY) 2.dp else 1.dp,
                    brush = Brush.sweepGradient(listOf(glowColor, glowColor.copy(0.3f), glowColor)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(badge.emoji, fontSize = 24.sp)
        }
        Text(
            text = badge.name,
            color = glowColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(54.dp)
        )
    }

    if (showTooltip) {
        AlertDialog(
            onDismissRequest = { showTooltip = false },
            containerColor = HuabuCardBg,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(badge.emoji, fontSize = 28.sp)
                    Column {
                        Text(badge.name, color = glowColor, fontWeight = FontWeight.ExtraBold)
                        Text(
                            badge.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = glowColor.copy(0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            },
            text = {
                Text(badge.description, color = HuabuSilver, fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = { showTooltip = false }) {
                    Text("Nice!", color = HuabuViolet)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────
// MOOD BOARD WIDGET
// ─────────────────────────────────────────────

private val MOOD_COLORS = listOf(
    "#8B5CF6","#EC4899","#EF4444","#F97316","#EAB308",
    "#22C55E","#06B6D4","#3B82F6","#6366F1","#1A1A2E",
    "#0D0D1A","#2D0045","#FFD700","#FF006E","#00FFFF"
)

private val MOOD_EMOJIS = listOf(
    "✨","🌙","🔥","💜","🎵",
    "🌊","🌸","💫","🦋","👑",
    "🎭","💎","🌈","🎨",""
)

@Composable
fun MoodBoardWidget(
    items: List<MoodBoardItem>,
    isCurrentUser: Boolean,
    onUpdateCell: (MoodBoardItem) -> Unit
) {
    WidgetCard(title = "🎨 Mood Board", titleColor = HuabuHotPink) {
        val grid = (0..8).map { pos -> items.find { it.gridPosition == pos } }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = false
        ) {
            items(9) { pos ->
                val cell = grid[pos]
                MoodBoardCell(
                    item = cell,
                    position = pos,
                    isCurrentUser = isCurrentUser,
                    onUpdate = { updated -> onUpdateCell(updated) }
                )
            }
        }

        if (isCurrentUser) {
            Text(
                "Tap any cell to customise",
                color = HuabuSilver,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MoodBoardCell(
    item: MoodBoardItem?,
    position: Int,
    isCurrentUser: Boolean,
    onUpdate: (MoodBoardItem) -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }

    val bgColor = if (item?.color?.isNotEmpty() == true)
        hexToColor(item.color) else HuabuSurface

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (item?.color?.isNotEmpty() == true)
                    Brush.radialGradient(listOf(bgColor, bgColor.copy(0.6f)))
                else
                    Brush.linearGradient(listOf(HuabuSurface, HuabuCardBg2))
            )
            .then(
                if (isCurrentUser) Modifier.clickable { showEditor = true } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (item?.emoji?.isNotEmpty() == true) {
            Text(item.emoji, fontSize = 28.sp)
        } else if (item == null && isCurrentUser) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add",
                tint = HuabuDivider,
                modifier = Modifier.size(20.dp)
            )
        }
        if (item?.caption?.isNotEmpty() == true) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.45f))
                    .padding(2.dp)
            ) {
                Text(
                    item.caption,
                    color = Color.White,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showEditor) {
        MoodCellEditor(
            existing = item,
            position = position,
            onSave = { updated ->
                showEditor = false
                onUpdate(updated)
            },
            onDismiss = { showEditor = false }
        )
    }
}

@Composable
private fun MoodCellEditor(
    existing: MoodBoardItem?,
    position: Int,
    onSave: (MoodBoardItem) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(existing?.color ?: MOOD_COLORS[0]) }
    var selectedEmoji by remember { mutableStateOf(existing?.emoji ?: "") }
    var caption       by remember { mutableStateOf(existing?.caption ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = {
            Text("✏️ Edit Cell ${position + 1}", color = HuabuGold, fontWeight = FontWeight.ExtraBold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Colour picker
                Text("Colour", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    userScrollEnabled = false
                ) {
                    items(MOOD_COLORS.size) { i ->
                        val hex = MOOD_COLORS[i]
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(hexToColor(hex))
                                .then(
                                    if (selectedColor == hex)
                                        Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                // Emoji picker
                Text("Emoji", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.height(132.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    userScrollEnabled = false
                ) {
                    items(MOOD_EMOJIS.size) { i ->
                        val e = MOOD_EMOJIS[i]
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedEmoji == e && e.isNotEmpty()) HuabuViolet.copy(0.3f)
                                    else HuabuSurface
                                )
                                .clickable { selectedEmoji = if (selectedEmoji == e) "" else e },
                            contentAlignment = Alignment.Center
                        ) {
                            if (e.isNotEmpty()) Text(e, fontSize = 18.sp)
                            else Icon(Icons.Filled.Clear, contentDescription = "None", tint = HuabuSilver, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Caption
                OutlinedTextField(
                    value = caption,
                    onValueChange = { if (it.length <= 20) caption = it },
                    label = { Text("Caption (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuabuViolet,
                        unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface,
                        unfocusedTextColor = HuabuOnSurface,
                        cursorColor = HuabuViolet
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        MoodBoardItem(
                            id = existing?.id ?: "mb_${System.currentTimeMillis()}_$position",
                            userId = existing?.userId ?: "",
                            gridPosition = position,
                            color = selectedColor,
                            emoji = selectedEmoji,
                            caption = caption.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = HuabuViolet),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) }
        }
    )
}

// ─────────────────────────────────────────────
// PINNED POSTS WIDGET
// ─────────────────────────────────────────────

@Composable
fun PinnedPostsWidget(
    pinnedPosts: List<Post>,
    allPosts: List<Post>,
    isCurrentUser: Boolean,
    onPin: (Post) -> Unit,
    onUnpin: (Post) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    WidgetCard(
        title = "📌 Pinned Posts",
        titleColor = HuabuGold,
        action = if (isCurrentUser && pinnedPosts.size < 3) {{
            IconButton(
                onClick = { showPicker = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Pin a post", tint = HuabuGold, modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (pinnedPosts.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to pin your best posts here!" else "No pinned posts",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pinnedPosts.forEach { post ->
                    PinnedPostCard(
                        post = post,
                        isCurrentUser = isCurrentUser,
                        onUnpin = { onUnpin(post) }
                    )
                }
            }
        }
    }

    if (showPicker) {
        PinPostPicker(
            posts = allPosts.filter { post -> pinnedPosts.none { it.id == post.id } },
            onPick = { post ->
                showPicker = false
                onPin(post)
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun PinnedPostCard(
    post: Post,
    isCurrentUser: Boolean,
    onUnpin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HuabuCardBg2)
            .border(1.dp, HuabuGold.copy(0.25f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.PushPin, contentDescription = null, tint = HuabuGold, modifier = Modifier.size(14.dp))
                    Text(
                        text = post.authorName,
                        color = HuabuGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "@${post.authorUsername}",
                        color = HuabuSilver,
                        fontSize = 11.sp
                    )
                }
                if (isCurrentUser) {
                    IconButton(onClick = onUnpin, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.PushPin, contentDescription = "Unpin", tint = HuabuSilver, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Text(
                text = post.content,
                color = HuabuOnSurface,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = HuabuHotPink, modifier = Modifier.size(12.dp))
                    Text("${post.likesCount}", color = HuabuSilver, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.ChatBubble, contentDescription = null, tint = HuabuAccentCyan, modifier = Modifier.size(12.dp))
                    Text("${post.commentsCount}", color = HuabuSilver, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun PinPostPicker(
    posts: List<Post>,
    onPick: (Post) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = {
            Text("📌 Pin a Post", color = HuabuGold, fontWeight = FontWeight.ExtraBold)
        },
        text = {
            if (posts.isEmpty()) {
                Text("No posts to pin yet.", color = HuabuSilver)
            } else {
                Column(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    posts.forEach { post ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(HuabuCardBg2)
                                .clickable { onPick(post) }
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    post.content,
                                    color = HuabuOnSurface,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Favorite, null, tint = HuabuHotPink, modifier = Modifier.size(11.dp))
                                    Text("${post.likesCount}", color = HuabuSilver, fontSize = 11.sp)
                                    Icon(Icons.Filled.ChatBubble, null, tint = HuabuAccentCyan, modifier = Modifier.size(11.dp))
                                    Text("${post.commentsCount}", color = HuabuSilver, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) }
        }
    )
}
