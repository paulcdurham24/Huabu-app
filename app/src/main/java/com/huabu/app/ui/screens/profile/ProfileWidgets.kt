package com.huabu.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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

// ─────────────────────────────────────────────
// Photo Gallery Widget
// ─────────────────────────────────────────────

@Composable
fun PhotoGalleryWidget(
    photos: List<ProfilePhoto>,
    isCurrentUser: Boolean,
    onPhotoClick: (ProfilePhoto) -> Unit,
    onFrameChange: (ProfilePhoto, PhotoFrameStyle) -> Unit
) {
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
                    onFrameChange = { style -> onFrameChange(photo, style) }
                )
            }
            if (isCurrentUser) {
                item { AddPhotoButton() }
            }
        }
    }
}

@Composable
private fun PhotoItem(
    photo: ProfilePhoto,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    onFrameChange: (PhotoFrameStyle) -> Unit
) {
    var showFramePicker by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .then(frameModifier(photo.frameStyle))
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
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
    }
}

@Composable
private fun AddPhotoButton() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, HuabuViolet, RoundedCornerShape(12.dp))
            .background(HuabuCardBg)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Add, contentDescription = "Add photo", tint = HuabuViolet, modifier = Modifier.size(28.dp))
            Text("Add", style = MaterialTheme.typography.labelSmall, color = HuabuViolet)
        }
    }
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
// Video Links Widget
// ─────────────────────────────────────────────

@Composable
fun VideoLinksWidget(
    videos: List<VideoLink>,
    isCurrentUser: Boolean
) {
    WidgetCard(title = "★ My Videos ★", titleColor = HuabuSkyBlue) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            videos.forEach { video ->
                VideoLinkItem(video = video)
            }
            if (isCurrentUser) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HuabuSkyBlue)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Video Link")
                }
            }
        }
    }
}

@Composable
private fun VideoLinkItem(video: VideoLink) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HuabuCardBg2)
            .clickable { }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp, 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.radialGradient(listOf(HuabuDeepPurple, HuabuCardBg))),
            contentAlignment = Alignment.Center
        ) {
            if (video.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HuabuOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (video.description.isNotEmpty()) {
                Text(
                    text = video.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = HuabuSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(Icons.Filled.Launch, contentDescription = "Open", tint = HuabuSkyBlue, modifier = Modifier.size(16.dp))
    }
}

// ─────────────────────────────────────────────
// Top 5 Music / Films Widgets
// ─────────────────────────────────────────────

@Composable
fun TopMusicWidget(tracks: List<MediaTrack>, isCurrentUser: Boolean) {
    WidgetCard(title = "★ Top 5 Tracks ★", titleColor = HuabuLime) {
        MediaTrackList(tracks = tracks, isCurrentUser = isCurrentUser, icon = "🎵")
    }
}

@Composable
fun TopFilmsWidget(tracks: List<MediaTrack>, isCurrentUser: Boolean) {
    WidgetCard(title = "★ Top 5 Films ★", titleColor = HuabuCoral) {
        MediaTrackList(tracks = tracks, isCurrentUser = isCurrentUser, icon = "🎬")
    }
}

@Composable
private fun MediaTrackList(tracks: List<MediaTrack>, isCurrentUser: Boolean, icon: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        tracks.take(5).forEachIndexed { index, track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HuabuCardBg2)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "#${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = HuabuGold,
                    modifier = Modifier.width(28.dp)
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.radialGradient(listOf(HuabuSurface, HuabuCardBg))),
                    contentAlignment = Alignment.Center
                ) {
                    if (track.artworkUrl.isNotEmpty()) {
                        AsyncImage(
                            model = track.artworkUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(icon, fontSize = 20.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = HuabuOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append(track.subtitle)
                            if (track.year.isNotEmpty()) append("  •  ${track.year}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = HuabuSilver,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (isCurrentUser) {
            TextButton(onClick = {}, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = HuabuSilver)
                Spacer(Modifier.width(4.dp))
                Text("Edit", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
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
)

@Composable
fun WidgetSettingsPanel(
    settings: ProfileWidgetSettings,
    onToggle: ((ProfileWidgetSettings) -> ProfileWidgetSettings) -> Unit,
    onDismiss: () -> Unit
) {
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
                modifier = Modifier.heightIn(max = 440.dp),
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

// ─────────────────────────────────────────────
// Shared widget card shell
// ─────────────────────────────────────────────

@Composable
fun WidgetCard(
    title: String,
    titleColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HuabuCardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(HuabuCardBg, HuabuCardBg2)))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = titleColor,
                letterSpacing = 0.5.sp
            )
            content()
        }
    }
}
