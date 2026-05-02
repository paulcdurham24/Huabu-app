package com.huabu.app.ui.screens.compose

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostScreen(
    onPostSubmitted: () -> Unit,
    onBack: () -> Unit,
    editPostId: String? = null,
    viewModel: ComposePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = editPostId != null
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var content by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var showMoodPicker by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf("public") }

    LaunchedEffect(editPostId) {
        if (editPostId != null) viewModel.loadPostForEdit(editPostId)
    }

    LaunchedEffect(uiState.initialContent) {
        if (uiState.initialContent.isNotEmpty() && content.isEmpty()) {
            content = uiState.initialContent
            mood = uiState.initialMood
            tags = uiState.initialTags
            if (isEditMode) visibility = uiState.initialVisibility
        }
    }

    // Auto-save draft as user types (non-edit mode only)
    LaunchedEffect(content, mood, tags) {
        if (!isEditMode) viewModel.onDraftChanged(content, mood, tags)
    }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            viewModel.onSubmitHandled()
            onPostSubmitted()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val moods = listOf("😍", "🎵", "🌙", "💅", "🎮", "⚡", "🌟", "✨", "🔥", "💔", "🎉", "😎", "🌊", "💫", "🥺", "😤")
    val charLimit = 500
    val isPostEnabled = content.isNotBlank() && content.length <= charLimit && !uiState.isSubmitting

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Post" else "New Post",
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
                        onClick = {
                            if (isPostEnabled) {
                                if (isEditMode && editPostId != null) {
                                    viewModel.editPost(editPostId, content, mood, tags, visibility)
                                } else {
                                    viewModel.submitPost(content, mood, tags, visibility, selectedImageUri)
                                }
                            }
                        },
                        enabled = isPostEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HuabuHotPink,
                            disabledContainerColor = HuabuDivider
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(if (isEditMode) "Save ✦" else "Post ✦", fontWeight = FontWeight.Bold)
                        }
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
            // Draft restored banner
            if (uiState.hasDraft && !isEditMode && content.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HuabuCardBg2)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Edit, null, tint = HuabuGold, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Draft restored", color = HuabuGold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.clearDraft(); content = ""; mood = ""; tags = "" },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("Discard", color = HuabuSilver, fontSize = 11.sp)
                    }
                }
            }
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
                    if (uiState.authorImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = uiState.authorImageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val initial = uiState.authorName.firstOrNull()?.uppercaseChar() ?: 'Y'
                        Text(initial.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(uiState.authorName.ifBlank { "Your Name" }, color = HuabuGold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            onClick = { visibility = if (visibility == "public") "friends" else "public" },
                            shape = RoundedCornerShape(20.dp),
                            color = if (visibility == "public") HuabuElectricBlue.copy(alpha = 0.2f) else HuabuHotPink.copy(alpha = 0.2f),
                            modifier = Modifier.border(
                                1.dp,
                                if (visibility == "public") HuabuElectricBlue else HuabuHotPink,
                                RoundedCornerShape(20.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (visibility == "public") Icons.Filled.Public else Icons.Filled.Group,
                                    contentDescription = null,
                                    tint = if (visibility == "public") HuabuElectricBlue else HuabuHotPink,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    if (visibility == "public") "Public" else "Friends",
                                    color = if (visibility == "public") HuabuElectricBlue else HuabuHotPink,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Text("@${uiState.authorUsername.ifBlank { "you" }}", color = HuabuSilver, fontSize = 12.sp)

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

            // Selected image preview
            if (selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove image", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = HuabuDivider)

            // Upload progress
            if (uiState.isSubmitting && selectedImageUri != null) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = HuabuHotPink,
                    trackColor = HuabuDivider
                )
            }

            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HuabuCardBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComposeAction(icon = Icons.Filled.Image, tint = HuabuElectricBlue, label = "Photo") {
                    imagePickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                }
                ComposeAction(icon = Icons.Filled.VideoCall, tint = HuabuNeonGreen, label = "Video") {
                    Toast.makeText(context, "Video coming soon", Toast.LENGTH_SHORT).show()
                }
                ComposeAction(
                    icon = Icons.Filled.EmojiEmotions,
                    tint = HuabuGold,
                    label = "Mood"
                ) { showMoodPicker = !showMoodPicker }
                ComposeAction(icon = Icons.Filled.MusicNote, tint = HuabuHotPink, label = "Song") {
                    Toast.makeText(context, "Song coming soon", Toast.LENGTH_SHORT).show()
                }
                ComposeAction(icon = Icons.Filled.Palette, tint = HuabuAccentPink, label = "Theme") {
                    Toast.makeText(context, "Theme coming soon", Toast.LENGTH_SHORT).show()
                }

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
