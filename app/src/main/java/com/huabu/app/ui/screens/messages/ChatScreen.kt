package com.huabu.app.ui.screens.messages

import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.huabu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var showChatMenu by remember { mutableStateOf(false) }
    val imageLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri: Uri? ->
        uri?.let { viewModel.sendImageMessage(conversationId, it) }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.loadChat(conversationId)
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Load more messages when scrolled to top
    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisibleIndex) {
        if (firstVisibleIndex == 0 && !uiState.isLoading) {
            viewModel.loadMoreMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val otherUser = uiState.otherUser
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { otherUser?.let { onNavigateToProfile(it.id) } }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                otherUser?.displayName?.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            if (otherUser?.isOnline == true) {
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
                                    otherUser?.displayName ?: "...",
                                    color = HuabuGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (otherUser?.mood?.isNotEmpty() == true) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(otherUser.mood, fontSize = 14.sp)
                                }
                            }
                            Text(
                                when {
                                    uiState.otherUserTyping -> "typing…"
                                    otherUser?.isOnline == true -> "Active now"
                                    else -> "Offline"
                                },
                                color = when {
                                    uiState.otherUserTyping -> HuabuElectricBlue
                                    otherUser?.isOnline == true -> HuabuNeonGreen
                                    else -> HuabuSilver
                                },
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
                    IconButton(onClick = { uiState.otherUser?.let { onNavigateToProfile(it.id) } }) {
                        Icon(Icons.Filled.Person, contentDescription = "View Profile", tint = HuabuSilver)
                    }
                    Box {
                        IconButton(onClick = { showChatMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = HuabuSilver)
                        }
                        DropdownMenu(
                            expanded = showChatMenu,
                            onDismissRequest = { showChatMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (uiState.isMuted) "Unmute notifications" else "Mute notifications") },
                                onClick = { showChatMenu = false; viewModel.toggleMute() },
                                leadingIcon = {
                                    Icon(
                                        if (uiState.isMuted) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                                        contentDescription = null,
                                        tint = if (uiState.isMuted) HuabuNeonGreen else HuabuSilver
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("View profile") },
                                onClick = { showChatMenu = false; uiState.otherUser?.let { onNavigateToProfile(it.id) } },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = HuabuSilver) }
                            )
                        }
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
                if (uiState.isRecording) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Mic, null, tint = HuabuHotPink, modifier = Modifier.size(20.dp))
                        val secs = uiState.recordingSeconds
                        Text(
                            "%d:%02d".format(secs / 60, secs % 60),
                            color = HuabuHotPink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.cancelRecording() }) {
                            Icon(Icons.Filled.Delete, null, tint = HuabuSilver)
                        }
                        IconButton(onClick = { viewModel.stopAndSendVoice(conversationId) }) {
                            Icon(Icons.Filled.Send, null, tint = HuabuNeonGreen)
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { imageLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }) {
                            Icon(Icons.Filled.Image, contentDescription = "Add Image", tint = HuabuHotPink)
                        }
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it; if (it.isNotEmpty()) viewModel.onTyping() },
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
                        if (messageText.isBlank()) {
                            IconButton(onClick = { viewModel.startRecording() }) {
                                Icon(Icons.Filled.Mic, contentDescription = "Voice", tint = HuabuHotPink)
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    viewModel.stopTyping()
                                    viewModel.sendMessage(conversationId, messageText)
                                    messageText = ""
                                },
                                enabled = !uiState.isSending
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = "Send", tint = HuabuHotPink)
                            }
                        }
                    }
                }
            }
        },
        containerColor = HuabuDarkBg
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HuabuHotPink)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (uiState.isLoadingMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = HuabuHotPink, strokeWidth = 2.dp)
                        }
                    }
                }
                val lastReadSentId = uiState.messages
                        .filter { it.senderId == uiState.currentUserId && it.isRead }
                        .maxByOrNull { it.timestamp }?.id
                items(uiState.messages, key = { it.id.ifBlank { it.timestamp.toString() } }) { message ->
                    val isMe = message.senderId == uiState.currentUserId
                    ChatBubble(
                        message = message,
                        isMe = isMe,
                        otherUserName = uiState.otherUser?.displayName ?: "",
                        isLastReadSent = isMe && message.id.isNotEmpty() && message.id == lastReadSentId,
                        onDelete = if (isMe && message.id.isNotEmpty()) {
                            { viewModel.deleteMessage(conversationId, message.id) }
                        } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubble(
    message: com.huabu.app.data.model.Message,
    isMe: Boolean,
    otherUserName: String,
    isLastReadSent: Boolean = false,
    onDelete: (() -> Unit)? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete message") },
            text = { Text("Remove this message for everyone?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete?.invoke() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

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
                val bubbleShape = RoundedCornerShape(
                    topStart = if (isMe) 16.dp else 4.dp,
                    topEnd = if (isMe) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                )
                val isVoice = message.voiceUrl.isNotEmpty()
                val isImage = !isVoice && message.content.startsWith("https://") &&
                    (message.content.contains(".jpg") || message.content.contains(".png") ||
                     message.content.contains(".jpeg") || message.content.contains("firebasestorage"))

                Box(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(
                            if (isMe) Brush.linearGradient(listOf(HuabuHotPink, HuabuElectricBlue))
                            else Brush.linearGradient(listOf(HuabuCardBg, HuabuCardBg2))
                        )
                        .border(1.dp, if (isMe) Color.Transparent else HuabuDivider, bubbleShape)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { if (onDelete != null) showDeleteDialog = true }
                        )
                        .padding(if (isImage) 4.dp else 0.dp)
                ) {
                    when {
                        isVoice -> {
                            VoiceBubble(
                                voiceUrl = message.voiceUrl,
                                durationMs = message.voiceDurationMs,
                                isMe = isMe
                            )
                        }
                        isImage -> {
                            AsyncImage(
                                model = message.content,
                                contentDescription = "Image message",
                                modifier = Modifier
                                    .widthIn(max = 220.dp)
                                    .clip(bubbleShape),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                        else -> {
                            Text(
                                text = message.content,
                                color = if (isMe) Color.White else HuabuOnSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
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
                if (isLastReadSent) {
                    Text(
                        text = "Seen",
                        color = HuabuNeonGreen,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceBubble(
    voiceUrl: String,
    durationMs: Long,
    isMe: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(voiceUrl) {
        onDispose {
            mediaPlayer.runCatching {
                if (isPlaying) stop()
                release()
            }
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val startTime = System.currentTimeMillis() - elapsedMs
            while (isPlaying && mediaPlayer.isPlaying) {
                val now = System.currentTimeMillis() - startTime
                elapsedMs = now
                progress = if (durationMs > 0) now.toFloat() / durationMs else 0f
                if (progress >= 1f) {
                    isPlaying = false
                    progress = 0f
                    elapsedMs = 0L
                    break
                }
                kotlinx.coroutines.delay(100)
            }
        }
    }

    fun togglePlay() {
        if (isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        } else {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.runCatching {
                    if (elapsedMs == 0L) {
                        reset()
                        setDataSource(voiceUrl)
                        setOnPreparedListener { mp ->
                            mp.start()
                            isPlaying = true
                        }
                        prepareAsync()
                    } else {
                        start()
                        isPlaying = true
                    }
                }
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "voice_progress"
    )

    val displayMs = if (isPlaying || elapsedMs > 0) elapsedMs else durationMs
    val dispSec = (displayMs / 1000).toInt()
    val iconTint = if (isMe) Color.White else HuabuHotPink
    val textColor = if (isMe) Color.White else HuabuOnSurface
    val trackColor = if (isMe) Color.White.copy(alpha = 0.3f) else HuabuDivider
    val barColor = if (isMe) Color.White else HuabuHotPink

    Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { togglePlay() },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.widthIn(min = 100.dp, max = 160.dp)) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
                color = barColor,
                trackColor = trackColor
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "%d:%02d".format(dispSec / 60, dispSec % 60),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(
            Icons.Filled.GraphicEq,
            contentDescription = null,
            tint = iconTint.copy(alpha = if (isPlaying) 1f else 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun formatTime(ts: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(ts))
}

