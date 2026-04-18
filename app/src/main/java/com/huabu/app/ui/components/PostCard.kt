package com.huabu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.huabu.app.data.model.Post
import com.huabu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostCard(
    post: Post,
    onLike: () -> Unit = {},
    onComment: () -> Unit = {},
    onShare: () -> Unit = {},
    onAuthorClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likesCount by remember { mutableIntStateOf(post.likesCount) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(HuabuCardBg, HuabuCardBg2)
                        )
                    )
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    listOf(HuabuHotPink, HuabuElectricBlue, HuabuGold, HuabuHotPink)
                                ),
                                shape = CircleShape
                            )
                            .clickable { onAuthorClick() }
                    ) {
                        if (post.authorImageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = post.authorImageUrl,
                                contentDescription = post.authorName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            listOf(HuabuDeepPurple, HuabuHotPink)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = post.authorName.firstOrNull()?.uppercase() ?: "?",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HuabuGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "@${post.authorUsername}",
                            style = MaterialTheme.typography.bodySmall,
                            color = HuabuSilver
                        )
                    }

                    if (post.mood.isNotEmpty()) {
                        Text(
                            text = post.mood,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.width(4.dp))
                    }

                    Text(
                        text = formatTimestamp(post.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = HuabuSilver
                    )
                }

                HorizontalDivider(color = HuabuDivider, thickness = 0.5.dp)

                // Content
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = HuabuOnSurface
                )

                if (post.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Post image",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp))
                    )
                }

                // Tags
                if (post.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        post.tags.split(",").take(4).forEach { tag ->
                            Text(
                                text = "#${tag.trim()}",
                                color = HuabuAccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider(color = HuabuDivider, thickness = 0.5.dp)

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PostAction(
                        icon = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        count = likesCount,
                        tint = if (isLiked) HuabuHotPink else HuabuSilver,
                        label = "Like"
                    ) {
                        isLiked = !isLiked
                        likesCount = if (isLiked) likesCount + 1 else likesCount - 1
                        onLike()
                    }
                    PostAction(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        count = post.commentsCount,
                        tint = HuabuElectricBlue,
                        label = "Comment"
                    ) { onComment() }
                    PostAction(
                        icon = Icons.Filled.Share,
                        count = post.sharesCount,
                        tint = HuabuNeonGreen,
                        label = "Share"
                    ) { onShare() }
                }
            }
        }
    }
}

@Composable
private fun PostAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    tint: Color,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Text(text = count.toString(), color = tint, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
