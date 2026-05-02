package com.huabu.app.ui.screens.profile

import androidx.compose.runtime.compositionLocalOf
import com.huabu.app.data.model.ProfileTheme
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.huabu.app.data.model.*
import com.huabu.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// ─────────────────────────────────────────────
// Photo Gallery Widget
// ─────────────────────────────────────────────

@Composable
fun PhotoGalleryWidget(
    photos: List<ProfilePhoto>,
    isCurrentUser: Boolean,
    onPhotoClick: (ProfilePhoto) -> Unit,
    onFrameChange: (ProfilePhoto, PhotoFrameStyle) -> Unit,
    onAddPhoto: (Uri, String) -> Unit = { _, _ -> },
    onDeletePhoto: (ProfilePhoto) -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }

    WidgetCard(title = "★ My Photos ★", titleColor = HuabuCoral) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(photos) { photo ->
                PhotoItem(
                    photo = photo,
                    isCurrentUser = isCurrentUser,
                    onClick = { onPhotoClick(photo) },
                    onFrameChange = { style -> onFrameChange(photo, style) },
                    onDelete = { onDeletePhoto(photo) }
                )
            }
            if (isCurrentUser) {
                item { AddPhotoButton(onClick = { showAddDialog = true }) }
            }
        }
    }

    if (showAddDialog) {
        AddPhotoDialog(
            onAdd = { uri, caption ->
                showAddDialog = false
                onAddPhoto(uri, caption)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoItem(
    photo: ProfilePhoto,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    onFrameChange: (PhotoFrameStyle) -> Unit,
    onDelete: () -> Unit = {}
) {
    var showFramePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .then(frameModifier(photo.frameStyle))
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = { if (isCurrentUser) showDeleteConfirm = true }
                )
        ) {
            if (photo.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = photo.imageUrl,
                    contentDescription = photo.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(listOf(HuabuDeepPurple, HuabuCardBg))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = HuabuSilver, modifier = Modifier.size(36.dp))
                }
            }
            if (isCurrentUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .background(HuabuViolet.copy(alpha = 0.85f), CircleShape)
                        .clickable { showFramePicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Change frame", tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
        if (photo.caption.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = photo.caption,
                style = MaterialTheme.typography.labelSmall,
                color = HuabuSilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 100.dp)
            )
        }
        if (isCurrentUser) {
            Text(
                text = photo.frameStyle.label(),
                style = MaterialTheme.typography.labelSmall,
                color = HuabuAccentCyan,
                fontSize = 9.sp
            )
        }

        if (showFramePicker) {
            FramePickerDialog(
                current = photo.frameStyle,
                onSelect = { style ->
                    onFrameChange(style)
                    showFramePicker = false
                },
                onDismiss = { showFramePicker = false }
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                containerColor = HuabuCardBg,
                title = { Text("Remove photo?", color = HuabuOnSurface, fontWeight = FontWeight.Bold) },
                confirmButton = {
                    Button(
                        onClick = { showDeleteConfirm = false; onDelete() },
                        colors = ButtonDefaults.buttonColors(containerColor = HuabuError),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Remove") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = HuabuSilver) }
                }
            )
        }
    }
}

@Composable
private fun AddPhotoButton(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, HuabuViolet, RoundedCornerShape(12.dp))
            .background(HuabuCardBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Add, contentDescription = "Add photo", tint = HuabuViolet, modifier = Modifier.size(28.dp))
            Text("Add", style = MaterialTheme.typography.labelSmall, color = HuabuViolet)
        }
    }
}

@Composable
private fun AddPhotoDialog(
    onAdd: (Uri, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var caption    by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri -> uri?.let { selectedUri = it } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("📷 Add Photo", color = HuabuCoral, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Image preview / pick button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HuabuSurface)
                        .border(1.dp, if (selectedUri != null) HuabuCoral else HuabuDivider, RoundedCornerShape(12.dp))
                        .clickable { galleryLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = HuabuCoral, modifier = Modifier.size(36.dp))
                            Text("Tap to choose from gallery", color = HuabuSilver, fontSize = 12.sp)
                        }
                    }
                }
                OutlinedTextField(
                    value = caption, onValueChange = { caption = it },
                    label = { Text("Caption (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuabuCoral, unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface, unfocusedTextColor = HuabuOnSurface,
                        cursorColor = HuabuCoral
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedUri?.let { onAdd(it, caption.trim()) } },
                enabled = selectedUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = HuabuCoral),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Upload") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun FramePickerDialog(
    current: PhotoFrameStyle,
    onSelect: (PhotoFrameStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("Choose Frame", color = HuabuGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PhotoFrameStyle.entries.forEach { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (style == current) HuabuSurface else Color.Transparent)
                            .clickable { onSelect(style) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .then(frameModifier(style))
                                .clip(RoundedCornerShape(6.dp))
                                .background(HuabuDeepPurple)
                        )
                        Text(style.label(), color = HuabuOnSurface, style = MaterialTheme.typography.bodyMedium)
                        if (style == current) {
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Filled.Check, contentDescription = null, tint = HuabuCoral, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = HuabuSilver) }
        }
    )
}

private fun frameModifier(style: PhotoFrameStyle): Modifier = when (style) {
    PhotoFrameStyle.NONE -> Modifier
    PhotoFrameStyle.RAINBOW_GLOW -> Modifier
        .border(3.dp, Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)), RoundedCornerShape(12.dp))
        .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = Color.Magenta)
    PhotoFrameStyle.GOLD_ORNATE -> Modifier
        .border(4.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))), RoundedCornerShape(12.dp))
        .shadow(6.dp, RoundedCornerShape(12.dp), ambientColor = Color(0xFFFFD700))
    PhotoFrameStyle.NEON_PINK -> Modifier
        .border(3.dp, HuabuCoral, RoundedCornerShape(12.dp))
        .shadow(10.dp, RoundedCornerShape(12.dp), ambientColor = HuabuCoral)
    PhotoFrameStyle.COSMIC_PURPLE -> Modifier
        .border(3.dp, Brush.linearGradient(listOf(HuabuViolet, HuabuDeepPurple, HuabuCyan)), RoundedCornerShape(12.dp))
        .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = HuabuViolet)
    PhotoFrameStyle.RETRO_POLAROID -> Modifier
        .border(6.dp, Color.White, RoundedCornerShape(4.dp))
        .shadow(4.dp, RoundedCornerShape(4.dp))
    PhotoFrameStyle.GLITTER_SILVER -> Modifier
        .border(3.dp, Brush.linearGradient(listOf(Color(0xFFC0C0C0), Color.White, Color(0xFFC0C0C0))), RoundedCornerShape(12.dp))
        .shadow(6.dp, RoundedCornerShape(12.dp), ambientColor = Color.White)
}

private fun PhotoFrameStyle.label(): String = when (this) {
    PhotoFrameStyle.NONE -> "No Frame"
    PhotoFrameStyle.RAINBOW_GLOW -> "Rainbow Glow"
    PhotoFrameStyle.GOLD_ORNATE -> "Gold Ornate"
    PhotoFrameStyle.NEON_PINK -> "Neon Coral"
    PhotoFrameStyle.COSMIC_PURPLE -> "Cosmic Purple"
    PhotoFrameStyle.RETRO_POLAROID -> "Retro Polaroid"
    PhotoFrameStyle.GLITTER_SILVER -> "Glitter Silver"
}


// ─────────────────────────────────────────────
// Widget Toggle + Drag-to-Reorder Settings Panel
// ─────────────────────────────────────────────

private data class WidgetDef(val id: String, val label: String, val emoji: String)

private val ALL_WIDGETS = listOf(
    WidgetDef("photo_gallery", "Photo Gallery",  "🖼️"),
    WidgetDef("video_links",   "Video Links",    "🎬"),
    WidgetDef("top_music",     "Top 5 Tracks",   "🎵"),
    WidgetDef("top_films",     "Top 5 Films",    "🎥"),
    WidgetDef("profile_song",  "Profile Song",   "♪"),
    WidgetDef("about_me",      "About Me",       "✏️"),
    WidgetDef("interests",     "Interests",      "⭐"),
    WidgetDef("top_friends",   "Top Friends",    "👥"),
    WidgetDef("go_live",        "Go Live",        "🔴"),
    WidgetDef("events",         "Events",         "📅"),
    WidgetDef("badges",         "Badges",         "🏆"),
    WidgetDef("mood_board",     "Mood Board",     "🎨"),
    WidgetDef("pinned_posts",       "Pinned Posts",      "📌"),
    WidgetDef("recently_played",    "Recently Played",   "🎧"),
    WidgetDef("my_playlist",        "My Playlist",       "🎶"),
    WidgetDef("currently_reading",  "Currently Reading", "📖"),
    WidgetDef("currently_watching", "Currently Watching","🎬"),
    WidgetDef("nft_showcase",       "NFT Showcase",      "🖼️"),
    WidgetDef("polls",              "Polls",             "📊"),
    WidgetDef("code_snippets",      "Code Snippets",     "💻"),
    WidgetDef("tech_stack",         "Tech Stack",        "🛠️"),
    WidgetDef("gif_showcase",       "GIF Showcase",      "🎬"),
    WidgetDef("gif_showcase_1",     "GIF Box 1",         "🎬"),
    WidgetDef("gif_showcase_2",     "GIF Box 2",         "🎬"),
    WidgetDef("gif_showcase_3",     "GIF Box 3",         "🎬"),
    WidgetDef("spotify_now_playing","Spotify Now Playing","🎵"),
    WidgetDef("meme_wall",          "Meme Wall",         "😂"),
    WidgetDef("game_stats",         "Game Stats",        "🎮"),
    WidgetDef("visited_places",     "Visited Places",    "✈️"),
    WidgetDef("travel_wishlist",    "Travel Wishlist",   "🌎"),
    WidgetDef("multiplayer_games",  "Multiplayer Games", "🎮"),
)

@Composable
fun WidgetSettingsPanel(
    settings: ProfileWidgetSettings,
    onToggle: ((ProfileWidgetSettings) -> ProfileWidgetSettings) -> Unit,
    onDismiss: () -> Unit,
    onBackgroundImageSelected: ((android.net.Uri) -> Unit)? = null
) {
    val context = LocalContext.current
    var showBgImagePicker by remember { mutableStateOf(false) }
    val orderedIds = remember(settings.widgetOrder) {
        val saved = settings.orderedWidgetIds()
        val all = ALL_WIDGETS.map { it.id }
        val ordered = saved.filter { it in all }
        val missing = all.filter { it !in ordered }
        mutableStateListOf<String>().apply { addAll(ordered + missing) }
    }

    fun saveOrder() {
        onToggle { it.copy(widgetOrder = orderedIds.joinToString(",")) }
    }

    fun moveUp(index: Int) {
        if (index > 0) {
            orderedIds.add(index - 1, orderedIds.removeAt(index))
            saveOrder()
        }
    }

    fun moveDown(index: Int) {
        if (index < orderedIds.size - 1) {
            orderedIds.add(index + 1, orderedIds.removeAt(index))
            saveOrder()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = {
            Column {
                Text("★ Customise My Page ★", color = HuabuGold, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Use ▲ ▼ to reorder  •  toggle switch to show/hide",
                    style = MaterialTheme.typography.labelSmall,
                    color = HuabuSilver
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                orderedIds.forEachIndexed { index, widgetId ->
                    val def = ALL_WIDGETS.find { it.id == widgetId } ?: return@forEachIndexed
                    val enabled = settings.isEnabled(widgetId)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HuabuCardBg2)
                            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(def.emoji, fontSize = 16.sp)

                        Text(
                            text = def.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (enabled) HuabuOnSurface else HuabuSilver
                        )

                        // Up / Down arrow buttons
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            IconButton(
                                onClick = { moveUp(index) },
                                modifier = Modifier.size(28.dp),
                                enabled = index > 0
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Move up",
                                    tint = if (index > 0) HuabuGold else HuabuDivider,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { moveDown(index) },
                                modifier = Modifier.size(28.dp),
                                enabled = index < orderedIds.size - 1
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Move down",
                                    tint = if (index < orderedIds.size - 1) HuabuGold else HuabuDivider,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                onToggle { s ->
                                    when (widgetId) {
                                        "photo_gallery" -> s.copy(showPhotoGallery = !s.showPhotoGallery)
                                        "video_links"   -> s.copy(showVideoLinks   = !s.showVideoLinks)
                                        "top_music"     -> s.copy(showTopMusic     = !s.showTopMusic)
                                        "top_films"     -> s.copy(showTopFilms     = !s.showTopFilms)
                                        "profile_song"  -> s.copy(showProfileSong  = !s.showProfileSong)
                                        "about_me"      -> s.copy(showAboutMe      = !s.showAboutMe)
                                        "interests"     -> s.copy(showInterests    = !s.showInterests)
                                        "top_friends"   -> s.copy(showTopFriends   = !s.showTopFriends)
                                        "go_live"       -> s.copy(showGoLive       = !s.showGoLive)
                                        "events"        -> s.copy(showEvents        = !s.showEvents)
                                        "badges"        -> s.copy(showBadges        = !s.showBadges)
                                        "mood_board"    -> s.copy(showMoodBoard     = !s.showMoodBoard)
                                        "pinned_posts"       -> s.copy(showPinnedPosts        = !s.showPinnedPosts)
                                        "recently_played"    -> s.copy(showRecentlyPlayed    = !s.showRecentlyPlayed)
                                        "my_playlist"        -> s.copy(showMyPlaylist        = !s.showMyPlaylist)
                                        "currently_reading"  -> s.copy(showCurrentlyReading  = !s.showCurrentlyReading)
                                        "currently_watching" -> s.copy(showCurrentlyWatching = !s.showCurrentlyWatching)
                                        "nft_showcase"       -> s.copy(showNftShowcase       = !s.showNftShowcase)
                                        "polls"              -> s.copy(showPolls            = !s.showPolls)
                                        "code_snippets"      -> s.copy(showCodeSnippets     = !s.showCodeSnippets)
                                        "tech_stack"         -> s.copy(showTechStack        = !s.showTechStack)
                                        "gif_showcase"       -> s.copy(showGifShowcase      = !s.showGifShowcase)
                                        "gif_showcase_1"     -> s.copy(showGifShowcase1     = !s.showGifShowcase1)
                                        "gif_showcase_2"     -> s.copy(showGifShowcase2     = !s.showGifShowcase2)
                                        "gif_showcase_3"     -> s.copy(showGifShowcase3     = !s.showGifShowcase3)
                                        "spotify_now_playing"-> s.copy(showSpotifyNowPlaying= !s.showSpotifyNowPlaying)
                                        "meme_wall"          -> s.copy(showMemeWall         = !s.showMemeWall)
                                        "game_stats"         -> s.copy(showGameStats        = !s.showGameStats)
                                        "visited_places"     -> s.copy(showVisitedPlaces    = !s.showVisitedPlaces)
                                        "travel_wishlist"    -> s.copy(showTravelWishlist   = !s.showTravelWishlist)
                                        "multiplayer_games"  -> s.copy(showMultiplayerGames = !s.showMultiplayerGames)
                                        else -> s
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HuabuViolet,
                                uncheckedThumbColor = HuabuSilver,
                                uncheckedTrackColor = HuabuDivider
                            )
                        )
                    }
                }

                // Background Image Section
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = HuabuDivider)
                Spacer(modifier = Modifier.height(8.dp))

                Text("🖼️ Background Image", color = HuabuGold, fontWeight = FontWeight.Bold)

                if (settings.backgroundImageUrl.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = settings.backgroundImageUrl,
                            contentDescription = "Background preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Remove button
                        IconButton(
                            onClick = { onToggle { it.copy(backgroundImageUrl = "") } },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Button(
                    onClick = { showBgImagePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HuabuCardBg2),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = HuabuViolet)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (settings.backgroundImageUrl.isEmpty()) "Add Background Image" else "Change Background", color = HuabuOnSurface)
                }

                if (showBgImagePicker && onBackgroundImageSelected != null) {
                    BackgroundImagePickerDialog(
                        onImageSelected = { uri ->
                            showBgImagePicker = false
                            onBackgroundImageSelected(uri)
                        },
                        onDismiss = { showBgImagePicker = false }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = HuabuViolet)
            ) { Text("Done") }
        }
    )
}

// Background Image Picker Dialog
@Composable
private fun BackgroundImagePickerDialog(
    onImageSelected: (android.net.Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun copyUriToCache(uri: android.net.Uri): android.net.Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val cacheDir = File(context.cacheDir, "background_picker")
            cacheDir.mkdirs()
            val file = File(cacheDir, "bg_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            android.net.Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { originalUri ->
                scope.launch {
                    val cachedUri = withContext(Dispatchers.IO) {
                        copyUriToCache(originalUri)
                    }
                    cachedUri?.let { onImageSelected(it) }
                }
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🖼️ Select Background", color = HuabuGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Choose an image for your profile background.", color = HuabuSilver)

                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HuabuViolet),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick from Gallery")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) }
        }
    )
}

// ─────────────────────────────────────────────
// Shared widget card shell
// ─────────────────────────────────────────────

val LocalProfileTheme = compositionLocalOf { ProfileTheme("") }

@Composable
fun WidgetCard(
    title: String,
    titleColor: Color,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalProfileTheme.current
    val cardColor = try {
        Color(android.graphics.Color.parseColor(theme.cardColor))
    } catch (_: Exception) { HuabuCardBg }
    val cardColor2 = try {
        Color(android.graphics.Color.parseColor(theme.cardColor)).copy(alpha = 0.85f)
    } catch (_: Exception) { HuabuCardBg2 }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(cardColor, cardColor2)))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
                if (action != null) action()
            }
            content()
        }
    }
}
