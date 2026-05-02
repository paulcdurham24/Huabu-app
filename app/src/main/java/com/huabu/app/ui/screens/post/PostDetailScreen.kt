package com.huabu.app.ui.screens.post

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.huabu.app.data.model.Comment
import com.huabu.app.data.model.Post
import com.huabu.app.ui.components.PostCard
import com.huabu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) { viewModel.loadPost(postId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", color = HuabuGold, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HuabuSilver)

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HuabuCardBg)
            )
        },
        bottomBar = {
            Column {
                if (uiState.replyingTo != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HuabuCardBg2)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Reply, null, tint = HuabuElectricBlue, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Replying to ${uiState.replyingTo!!.authorName}",
                            color = HuabuElectricBlue, fontSize = 12.sp, modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.setReplyingTo(null) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, null, tint = HuabuSilver, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                CommentInputBar(
                    value = commentText,
                    currentUserImageUrl = uiState.currentUserImageUrl,
                    replyingTo = uiState.replyingTo?.authorName,
                    onValueChange = { commentText = it },
                    onSend = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(postId, commentText.trim())
                            commentText = ""
                        }
                    }
                )
            }
        },
        containerColor = HuabuDarkBg
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HuabuHotPink)
            }
            return@Scaffold
        }
        val post = uiState.post
        if (post == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Post not found", color = HuabuSilver)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                PostCard(
                    post = post,
                    currentUserId = uiState.currentUserId,
                    onLike = { viewModel.likePost(postId) },
                    onReact = { emoji -> viewModel.reactToPost(postId, emoji) },
                    onAuthorClick = { onNavigateToProfile(post.authorId) },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out this post on Huabu: \"${post.content.take(100)}\"")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share post"))
                    }
                )
            }

            item {
                HorizontalDivider(color = HuabuDivider, thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HuabuCardBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Comment, contentDescription = null, tint = HuabuElectricBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${uiState.comments.size} Comment${if (uiState.comments.size != 1) "s" else ""}",
                        color = HuabuElectricBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                HorizontalDivider(color = HuabuDivider, thickness = 0.5.dp)
            }

            if (uiState.comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No comments yet — be the first!", color = HuabuSilver, fontSize = 13.sp)
                    }
                }
            }

            items(uiState.comments, key = { it.id }) { comment ->
                val isReply = comment.parentId.isNotEmpty()
                CommentRow(
                    comment = comment,
                    currentUserId = uiState.currentUserId,
                    isReply = isReply,
                    onAuthorClick = { onNavigateToProfile(comment.authorId) },
                    onLike = { viewModel.likeComment(comment) },
                    onReply = { viewModel.setReplyingTo(comment) },
                    onDelete = if (comment.authorId == uiState.currentUserId) {
                        { viewModel.deleteComment(comment) }
                    } else null
                )
                HorizontalDivider(modifier = Modifier.padding(start = if (isReply) 96.dp else 64.dp), color = HuabuDivider, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun CommentRow(
    comment: Comment,
    currentUserId: String = "",
    isReply: Boolean = false,
    onAuthorClick: () -> Unit,
    onLike: () -> Unit = {},
    onReply: () -> Unit = {},
    onDelete: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val isLiked = currentUserId.isNotEmpty() && currentUserId in comment.likedBy
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isReply) HuabuCardBg2.copy(alpha = 0.4f) else HuabuDarkBg)
            .padding(start = if (isReply) 40.dp else 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink)))
                .border(1.dp, HuabuHotPink.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (comment.authorImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = comment.authorImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = comment.authorName.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onAuthorClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = comment.authorName,
                        fontWeight = FontWeight.Bold,
                        color = HuabuGold,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatCommentTime(comment.timestamp),
                    color = HuabuSilver,
                    fontSize = 11.sp
                )
            }
            if (comment.replyToName.isNotEmpty()) {
                Text(
                    text = "↩ ${comment.replyToName}",
                    color = HuabuElectricBlue,
                    fontSize = 11.sp
                )
            }
            Text(
                text = comment.content,
                color = HuabuOnSurface,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            TextButton(
                onClick = onReply,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Reply", color = HuabuElectricBlue, fontSize = 11.sp)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onLike,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like comment",
                    tint = if (isLiked) HuabuHotPink else HuabuSilver,
                    modifier = Modifier.size(15.dp)
                )
            }
            if (comment.likesCount > 0) {
                Text(
                    text = comment.likesCount.toString(),
                    color = if (isLiked) HuabuHotPink else HuabuSilver,
                    fontSize = 10.sp
                )
            }
        }

        if (onDelete != null) {
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = HuabuSilver,
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete", color = HuabuError) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = HuabuError)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    currentUserImageUrl: String,
    replyingTo: String? = null,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        color = HuabuCardBg,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))),
                contentAlignment = Alignment.Center
            ) {
                if (currentUserImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = currentUserImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (replyingTo != null) "Reply to $replyingTo…" else "Add a comment…", color = HuabuSilver, fontSize = 13.sp) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HuabuHotPink,
                    unfocusedBorderColor = HuabuDivider,
                    focusedTextColor = HuabuOnSurface,
                    unfocusedTextColor = HuabuOnSurface,
                    cursorColor = HuabuHotPink
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (value.isNotBlank()) HuabuHotPink else HuabuSilver,
                )
            }
        }
    }
}

private fun formatCommentTime(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000L -> "now"
        diff < 3_600_000L -> "${diff / 60_000}m"
        diff < 86_400_000L -> "${diff / 3_600_000}h"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
    }
}
