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
import androidx.hilt.navigation.compose.hiltViewModel
import com.huabu.app.data.model.Notification
import com.huabu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Likes", "Comments", "Follows", "System")

    val notifications = uiState.notifications
    val unreadCount = notifications.count { !it.read }

    val filtered = when (selectedFilter) {
        "Likes"    -> notifications.filter { it.type == "like" }
        "Comments" -> notifications.filter { it.type == "comment" }
        "Follows"  -> notifications.filter { it.type == "follow" || it.type == "friend_request" }
        "System"   -> notifications.filter { it.type == "system" }
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
                        TextButton(onClick = { viewModel.markAllRead() }) {
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
            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HuabuElectricBlue)
                    }
                }
            } else if (filtered.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No notifications yet", color = HuabuSilver)
                    }
                }
            }
            items(filtered, key = { it.id }) { notification ->
                NotificationItem(
                    notification = notification,
                    onClick = {
                        viewModel.markRead(notification.id)
                        when (notification.type) {
                            "message" -> {
                                if (notification.targetId.isNotEmpty()) onNavigateToChat(notification.targetId)
                                else if (notification.senderId.isNotEmpty()) onNavigateToProfile(notification.senderId)
                            }
                            "like", "comment" -> {
                                if (notification.targetId.isNotEmpty()) onNavigateToPost(notification.targetId)
                                else if (notification.senderId.isNotEmpty()) onNavigateToProfile(notification.senderId)
                            }
                            "friend_request", "follow" -> {
                                if (notification.senderId.isNotEmpty()) onNavigateToProfile(notification.senderId)
                            }
                            else -> {
                                if (notification.senderId.isNotEmpty()) onNavigateToProfile(notification.senderId)
                            }
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
        "like"           -> Icons.Filled.Favorite to HuabuHotPink
        "comment"        -> Icons.Filled.Comment to HuabuElectricBlue
        "follow",
        "friend_request" -> Icons.Filled.PersonAdd to HuabuNeonGreen
        "mention"        -> Icons.Filled.AlternateEmail to HuabuGold
        "game_invite"    -> Icons.Filled.VideogameAsset to HuabuViolet
        "message"        -> Icons.Filled.Message to HuabuAccentCyan
        else             -> Icons.Filled.Info to HuabuSilver
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (notification.read) Color.Transparent else HuabuCardBg.copy(alpha = 0.5f))
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
                color = if (notification.read) HuabuSilver else HuabuOnSurface,
                fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = notification.message,
                color = if (notification.read) HuabuSilver.copy(0.7f) else HuabuSilver,
                fontSize = 12.sp,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTimestamp(notification.timestamp),
                color = if (notification.read) HuabuSilver.copy(0.5f) else HuabuHotPink,
                fontSize = 11.sp
            )
            if (!notification.read) {
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

