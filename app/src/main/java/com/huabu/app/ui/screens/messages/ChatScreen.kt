package com.huabu.app.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

data class Conversation(
    val id: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserMood: String,
    val isOnline: Boolean,
    val messages: List<ChatMessage>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    val conversation = remember { generateMockConversation(conversationId) }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom on launch
    LaunchedEffect(Unit) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(conversation.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToProfile(conversation.otherUserId) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                conversation.otherUserName.first().uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            if (conversation.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.BottomEnd)
                                        .background(HuabuNeonGreen, CircleShape)
                                        .border(2.dp, HuabuDarkBg, CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    conversation.otherUserName,
                                    color = HuabuGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (conversation.otherUserMood.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(conversation.otherUserMood, fontSize = 14.sp)
                                }
                            }
                            Text(
                                if (conversation.isOnline) "Active now" else "Offline",
                                color = if (conversation.isOnline) HuabuNeonGreen else HuabuSilver,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = HuabuSilver)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToProfile(conversation.otherUserId) }) {
                        Icon(Icons.Filled.Person, contentDescription = "View Profile", tint = HuabuSilver)
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = HuabuSilver)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HuabuCardBg)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HuabuCardBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { /* Add image */ }) {
                        Icon(Icons.Filled.Image, contentDescription = "Add Image", tint = HuabuHotPink)
                    }
                    IconButton(onClick = { /* Add emoji */ }) {
                        Icon(Icons.Filled.EmojiEmotions, contentDescription = "Add Emoji", tint = HuabuHotPink)
                    }
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type a message...", color = HuabuSilver, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuabuHotPink,
                            unfocusedBorderColor = HuabuDivider,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuHotPink
                        ),
                        singleLine = false,
                        maxLines = 4
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                // Send message
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank()) HuabuHotPink else HuabuSilver
                        )
                    }
                }
            }
        },
        containerColor = HuabuDarkBg
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(conversation.messages, key = { it.id }) { message ->
                val isMe = message.senderId == "me"
                ChatBubble(
                    message = message,
                    isMe = isMe,
                    otherUserName = conversation.otherUserName
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isMe: Boolean,
    otherUserName: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!isMe) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        otherUserName.first().uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = if (isMe) 16.dp else 4.dp,
                                topEnd = if (isMe) 4.dp else 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                        .background(
                            if (isMe) {
                                Brush.linearGradient(listOf(HuabuHotPink, HuabuElectricBlue))
                            } else {
                                Brush.linearGradient(listOf(HuabuCardBg, HuabuCardBg2))
                            }
                        )
                        .border(
                            1.dp,
                            if (isMe) Color.Transparent else HuabuDivider,
                            RoundedCornerShape(
                                topStart = if (isMe) 16.dp else 4.dp,
                                topEnd = if (isMe) 4.dp else 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        color = if (isMe) Color.White else HuabuOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        color = HuabuSilver,
                        fontSize = 10.sp
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isRead) Icons.Filled.DoneAll else Icons.Filled.Done,
                            contentDescription = null,
                            tint = if (message.isRead) HuabuNeonGreen else HuabuSilver,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ts: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(ts))
}

private fun generateMockConversation(conversationId: String): Conversation {
    return Conversation(
        id = conversationId,
        otherUserId = "u1",
        otherUserName = "Xena Starfire",
        otherUserMood = "😍",
        isOnline = true,
        messages = listOf(
            ChatMessage(
                id = "m1",
                senderId = "u1",
                text = "Hey! Did you see my new profile theme??",
                timestamp = System.currentTimeMillis() - 3_600_000,
                isRead = true
            ),
            ChatMessage(
                id = "m2",
                senderId = "me",
                text = "Yeah it looks amazing! ✨ Love the neon colors",
                timestamp = System.currentTimeMillis() - 3_500_000,
                isRead = true
            ),
            ChatMessage(
                id = "m3",
                senderId = "u1",
                text = "Thanks!! I spent all night customizing it lol",
                timestamp = System.currentTimeMillis() - 3_400_000,
                isRead = true
            ),
            ChatMessage(
                id = "m4",
                senderId = "u1",
                text = "You should update yours too! The new widgets are so cool",
                timestamp = System.currentTimeMillis() - 3_300_000,
                isRead = true
            ),
            ChatMessage(
                id = "m5",
                senderId = "me",
                text = "I added that music widget - now playing thing",
                timestamp = System.currentTimeMillis() - 3_200_000,
                isRead = true
            ),
            ChatMessage(
                id = "m6",
                senderId = "u1",
                text = "omg did u see my new profile theme???",
                timestamp = System.currentTimeMillis() - 300_000,
                isRead = false
            )
        )
    )
}
