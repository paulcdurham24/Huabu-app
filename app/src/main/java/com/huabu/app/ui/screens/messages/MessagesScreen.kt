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
import com.huabu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class MockConvo(
    val id: String,
    val name: String,
    val username: String,
    val lastMessage: String,
    val time: Long,
    val unread: Int = 0,
    val isOnline: Boolean = false,
    val mood: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val convos = remember {
        listOf(
            MockConvo("c1", "Xena Starfire", "xenastar", "omg did u see my new profile theme???", System.currentTimeMillis() - 300_000, unread = 3, isOnline = true, mood = "😍"),
            MockConvo("c2", "DJ Phantom", "djphantom", "bro you need to hear this drop 🔥", System.currentTimeMillis() - 3_600_000, isOnline = true, mood = "🎵"),
            MockConvo("c3", "Luna Eclipse", "lunaeclipse", "updated my top 8, check if ur still on there lol", System.currentTimeMillis() - 86_400_000, unread = 1, mood = "🌙"),
            MockConvo("c4", "Glitter Queen", "glitterqueen99", "tysm for the comment on my page!! ✨", System.currentTimeMillis() - 7_200_000, mood = "💅"),
            MockConvo("c5", "Retro Kid", "retrokid2k", "my profile song just changed if u wanna check", System.currentTimeMillis() - 172_800_000, mood = "🎧"),
            MockConvo("c6", "Neon Ninja", "neonninja", "you should join the Huabu crew!!!", System.currentTimeMillis() - 259_200_000, mood = "⚡")
        )
    }

    val filtered = if (searchQuery.isEmpty()) convos
    else convos.filter { it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true) }

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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HuabuCardBg)
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
                        focusedBorderColor = HuabuHotPink,
                        unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface,
                        unfocusedTextColor = HuabuOnSurface,
                        cursorColor = HuabuHotPink
                    ),
                    singleLine = true
                )
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
            items(filtered, key = { it.id }) { convo ->
                ConversationItem(
                    convo = convo,
                    onClick = { onNavigateToChat(convo.id) }
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

@Composable
private fun ConversationItem(convo: MockConvo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (convo.unread > 0) HuabuCardBg.copy(alpha = 0.7f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (convo.isOnline) 2.dp else 1.dp,
                        color = if (convo.isOnline) HuabuNeonGreen else HuabuDivider,
                        shape = CircleShape
                    )
                    .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = convo.name.first().uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            if (convo.isOnline) {
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
                    text = convo.name,
                    fontWeight = if (convo.unread > 0) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (convo.unread > 0) HuabuGold else HuabuOnSurface,
                    fontSize = 15.sp
                )
                if (convo.mood.isNotEmpty()) {
                    Spacer(Modifier.width(4.dp))
                    Text(convo.mood, fontSize = 14.sp)
                }
            }
            Text(
                text = convo.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                color = if (convo.unread > 0) HuabuOnSurface else HuabuSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (convo.unread > 0) FontWeight.Medium else FontWeight.Normal
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTime(convo.time),
                style = MaterialTheme.typography.labelSmall,
                color = if (convo.unread > 0) HuabuHotPink else HuabuSilver
            )
            if (convo.unread > 0) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(HuabuHotPink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = convo.unread.toString(),
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
