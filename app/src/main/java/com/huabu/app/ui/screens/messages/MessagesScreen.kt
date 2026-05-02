package com.huabu.app.ui.screens.messages

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.huabu.app.data.model.ConversationUI
import com.huabu.app.ui.screens.profile.LocalProfileTheme
import com.huabu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filtered = if (searchQuery.isEmpty()) uiState.conversations
    else uiState.conversations.filter {
        it.otherUser.displayName.contains(searchQuery, ignoreCase = true) ||
        it.otherUser.username.contains(searchQuery, ignoreCase = true)
    }

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
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "★ Messages ★",
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.linearGradient(listOf(HuabuElectricBlue, HuabuAccentCyan)),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Edit, contentDescription = "New Message", tint = HuabuHotPink)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = themeCard)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    placeholder = { Text("Search messages...", color = HuabuSilver) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = HuabuSilver) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeAccent,
                        unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface,
                        unfocusedTextColor = HuabuOnSurface,
                        cursorColor = themeAccent
                    ),
                    singleLine = true
                )
            }
        },
        containerColor = themeBg
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HuabuHotPink)
            }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No messages yet", color = HuabuSilver)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filtered, key = { it.conversationId }) { convo ->
                    ConversationItem(
                        convo = convo,
                        onClick = { onNavigateToChat(convo.conversationId) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp),
                        color = HuabuDivider,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(convo: ConversationUI, onClick: () -> Unit) {
    val hasUnread = convo.unreadCount > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (hasUnread) HuabuCardBg.copy(alpha = 0.7f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(width = 1.dp, color = HuabuDivider, shape = CircleShape)
                    .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = convo.otherUser.displayName.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            if (convo.otherUser.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
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
                    text = convo.otherUser.displayName,
                    fontWeight = if (hasUnread) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (hasUnread) HuabuGold else HuabuOnSurface,
                    fontSize = 15.sp
                )
                if (convo.otherUser.mood.isNotEmpty()) {
                    Spacer(Modifier.width(4.dp))
                    Text(convo.otherUser.mood, fontSize = 14.sp)
                }
            }
            Text(
                text = convo.lastMessage.ifBlank { "Start a conversation" },
                style = MaterialTheme.typography.bodySmall,
                color = if (hasUnread) HuabuOnSurface else HuabuSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTime(convo.lastMessageTimestamp),
                style = MaterialTheme.typography.labelSmall,
                color = if (hasUnread) HuabuHotPink else HuabuSilver
            )
            if (hasUnread) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(HuabuHotPink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = convo.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatTime(ts: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - ts
    return when {
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
    }
}
