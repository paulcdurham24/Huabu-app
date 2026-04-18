package com.huabu.app.ui.screens.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostScreen(
    onPostSubmitted: () -> Unit,
    onBack: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var showMoodPicker by remember { mutableStateOf(false) }

    val moods = listOf("😍", "🎵", "🌙", "💅", "🎮", "⚡", "🌟", "✨", "🔥", "💔", "🎉", "😎", "🌊", "💫", "🥺", "😤")
    val charLimit = 500
    val isPostEnabled = content.isNotBlank() && content.length <= charLimit

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Post",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.linearGradient(listOf(HuabuHotPink, HuabuGold)),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = HuabuSilver)
                    }
                },
                actions = {
                    Button(
                        onClick = { if (isPostEnabled) onPostSubmitted() },
                        enabled = isPostEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HuabuHotPink,
                            disabledContainerColor = HuabuDivider
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Post ✦", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HuabuCardBg)
            )
        },
        containerColor = HuabuDarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Author row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuHotPink))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Y", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Your Name", color = HuabuGold, fontWeight = FontWeight.Bold)
                    Text("@you", color = HuabuSilver, fontSize = 12.sp)

                    Spacer(Modifier.height(8.dp))

                    TextField(
                        value = content,
                        onValueChange = { if (it.length <= charLimit) content = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "What's happening on your page? ✨",
                                color = HuabuSilver.copy(alpha = 0.6f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuHotPink,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        minLines = 4,
                        maxLines = 10,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                }
            }

            HorizontalDivider(color = HuabuDivider, thickness = 0.5.dp)

            // Mood display
            if (mood.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mood: ", color = HuabuSilver, fontSize = 13.sp)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = HuabuDeepPurple.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mood, fontSize = 18.sp)
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remove mood",
                                tint = HuabuSilver,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { mood = "" }
                            )
                        }
                    }
                }
            }

            // Tags field
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Tags: music, aesthetic, vibes...", color = HuabuSilver.copy(alpha = 0.5f), fontSize = 13.sp) },
                leadingIcon = { Text("#", color = HuabuAccentCyan, fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HuabuAccentCyan,
                    unfocusedBorderColor = HuabuDivider,
                    focusedTextColor = HuabuOnSurface,
                    unfocusedTextColor = HuabuOnSurface,
                    cursorColor = HuabuAccentCyan
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            // Mood picker toggle row
            AnimatedVisibility(visible = showMoodPicker) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(moods) { m ->
                        Surface(
                            onClick = { mood = m; showMoodPicker = false },
                            shape = CircleShape,
                            color = if (mood == m) HuabuDeepPurple else HuabuCardBg2,
                            modifier = Modifier
                                .size(44.dp)
                                .border(
                                    1.dp,
                                    if (mood == m) HuabuHotPink else HuabuDivider,
                                    CircleShape
                                )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(m, fontSize = 22.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = HuabuDivider)

            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HuabuCardBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComposeAction(icon = Icons.Filled.Image, tint = HuabuElectricBlue, label = "Photo") {}
                ComposeAction(icon = Icons.Filled.VideoCall, tint = HuabuNeonGreen, label = "Video") {}
                ComposeAction(
                    icon = Icons.Filled.EmojiEmotions,
                    tint = HuabuGold,
                    label = "Mood"
                ) { showMoodPicker = !showMoodPicker }
                ComposeAction(icon = Icons.Filled.MusicNote, tint = HuabuHotPink, label = "Song") {}
                ComposeAction(icon = Icons.Filled.Palette, tint = HuabuAccentPink, label = "Theme") {}

                Spacer(Modifier.weight(1f))

                val remaining = charLimit - content.length
                Text(
                    text = remaining.toString(),
                    color = when {
                        remaining < 20 -> HuabuError
                        remaining < 100 -> HuabuGold
                        else -> HuabuSilver
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ComposeAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    label: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
    }
}
