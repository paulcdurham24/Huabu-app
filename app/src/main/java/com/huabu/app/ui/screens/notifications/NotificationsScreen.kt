package com.huabu.app.ui.screens.notifications

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huabu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class NotificationType { LIKE, COMMENT, FOLLOW, MENTION, SYSTEM, GAME_INVITE }

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val fromUserId: String? = null,
    val fromUserName: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Likes", "Comments", "Follows", "System")

    val notifications = remember { generateMockNotifications() }
    val unreadCount = notifications.count { !it.isRead }

    val filtered = when (selectedFilter) {
        "Likes" -> notifications.filter { it.type == NotificationType.LIKE }
        "Comments" -> notifications.filter { it.type == NotificationType.COMMENT }
        "Follows" -> notifications.filter { it.type == NotificationType.FOLLOW }
        "System" -> notifications.filter { it.type == NotificationType.SYSTEM }
        else -> notifications
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "★ Notifications ★",
                                style = androidx.compose.ui.text.TextStyle(
                                    brush = Brush.linearGradient(listOf(HuabuElectricBlue, HuabuAccentCyan)),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Badge(
                                    containerColor = HuabuHotPink,
                                    contentColor = Color.White
                                ) {
                                    Text(unreadCount.toString())
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = HuabuSilver)
                        }
                    },
                    actions = {
                        TextButton(onClick = { /* Mark all read */ }) {
                            Text("Mark all read", color = HuabuHotPink, fontSize = 12.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HuabuCardBg)
                )

                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HuabuCardBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        val selected = filter == selectedFilter
                        FilterChip(
                            selected = selected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HuabuElectricBlue.copy(0.3f),
                                selectedLabelColor = HuabuElectricBlue
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = if (selected) HuabuElectricBlue else HuabuDivider
                            )
                        )
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
            items(filtered, key = { it.id }) { notification ->
                NotificationItem(
                    notification = notification,
                    onClick = {
                        if (notification.fromUserId != null) {
                            onNavigateToProfile(notification.fromUserId)
                        }
                    }
                )
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
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val icon = when (notification.type) {
        NotificationType.LIKE -> Icons.Filled.Favorite to HuabuHotPink
        NotificationType.COMMENT -> Icons.Filled.Comment to HuabuElectricBlue
        NotificationType.FOLLOW -> Icons.Filled.PersonAdd to HuabuNeonGreen
        NotificationType.MENTION -> Icons.Filled.AlternateEmail to HuabuGold
        NotificationType.GAME_INVITE -> Icons.Filled.VideogameAsset to HuabuViolet
        NotificationType.SYSTEM -> Icons.Filled.Info to HuabuSilver
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (notification.isRead) Color.Transparent else HuabuCardBg.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(icon.second.copy(alpha = 0.2f))
                .border(1.dp, icon.second.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon.first,
                contentDescription = null,
                tint = icon.second,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                color = if (notification.isRead) HuabuSilver else HuabuOnSurface,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = notification.message,
                color = if (notification.isRead) HuabuSilver.copy(0.7f) else HuabuSilver,
                fontSize = 12.sp,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTimestamp(notification.timestamp),
                color = if (notification.isRead) HuabuSilver.copy(0.5f) else HuabuHotPink,
                fontSize = 11.sp
            )
            if (!notification.isRead) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(HuabuHotPink, CircleShape)
                )
            }
        }
    }
}

private fun formatTimestamp(ts: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - ts
    return when {
        diff < 60_000 -> "now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
    }
}

private fun generateMockNotifications(): List<Notification> = listOf(
    Notification(
        id = "n1",
        type = NotificationType.LIKE,
        title = "Xena Starfire liked your post",
        message = "They liked: \"Just customized my Huabu page...\"",
        fromUserId = "u1",
        fromUserName = "Xena Starfire",
        timestamp = System.currentTimeMillis() - 300_000,
        isRead = false
    ),
    Notification(
        id = "n2",
        type = NotificationType.COMMENT,
        title = "DJ Phantom commented",
        message = "\"This is fire! 🔥 Check out my new mix too\"",
        fromUserId = "u2",
        fromUserName = "DJ Phantom",
        timestamp = System.currentTimeMillis() - 3_600_000,
        isRead = false
    ),
    Notification(
        id = "n3",
        type = NotificationType.FOLLOW,
        title = "Luna Eclipse followed you",
        message = "They added you to their friends!",
        fromUserId = "u3",
        fromUserName = "Luna Eclipse",
        timestamp = System.currentTimeMillis() - 7_200_000,
        isRead = true
    ),
    Notification(
        id = "n4",
        type = NotificationType.GAME_INVITE,
        title = "Game invitation",
        message = "Retro Kid challenged you to Tic Tac Toe!",
        fromUserId = "u4",
        fromUserName = "Retro Kid",
        timestamp = System.currentTimeMillis() - 14_400_000,
        isRead = false
    ),
    Notification(
        id = "n5",
        type = NotificationType.MENTION,
        title = "Glitter Queen mentioned you",
        message = "\"Thanks @you for the comment on my page!! ✨\"",
        fromUserId = "u5",
        fromUserName = "Glitter Queen",
        timestamp = System.currentTimeMillis() - 86_400_000,
        isRead = true
    ),
    Notification(
        id = "n6",
        type = NotificationType.SYSTEM,
        title = "Welcome to Huabu!",
        message = "Complete your profile to get started. Add a photo, bio, and profile song!",
        timestamp = System.currentTimeMillis() - 259_200_000,
        isRead = true
    )
)
