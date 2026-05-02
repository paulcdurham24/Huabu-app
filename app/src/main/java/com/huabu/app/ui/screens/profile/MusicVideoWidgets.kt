package com.huabu.app.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huabu.app.data.model.*
import com.huabu.app.ui.theme.*

// ─────────────────────────────────────────────
// PROFILE SONG WIDGET
// ─────────────────────────────────────────────

@Composable
fun ProfileSongWidget(
    song: String,
    artist: String,
    isCurrentUser: Boolean = false,
    onSave: (song: String, artist: String) -> Unit = { _, _ -> }
) {
    var showEditDialog by remember { mutableStateOf(false) }
    if (showEditDialog) {
        var editSong   by remember { mutableStateOf(song) }
        var editArtist by remember { mutableStateOf(artist) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = HuabuCardBg,
            title = { Text("🎵 Set Profile Song", color = HuabuViolet, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editSong,
                        onValueChange = { editSong = it },
                        label = { Text("Song title", color = HuabuSilver) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuabuViolet,
                            unfocusedBorderColor = HuabuDivider,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuViolet
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editArtist,
                        onValueChange = { editArtist = it },
                        label = { Text("Artist", color = HuabuSilver) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuabuViolet,
                            unfocusedBorderColor = HuabuDivider,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuViolet
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onSave(editSong.trim(), editArtist.trim()); showEditDialog = false },
                    enabled = editSong.isNotBlank()
                ) { Text("Save", color = HuabuViolet, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = HuabuSilver) }
            }
        )
    }
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val barHeights = (1..5).map { i ->
        pulse.animateFloat(
            initialValue = 4f, targetValue = (8 + i * 4).toFloat(),
            animationSpec = infiniteRepeatable(
                tween(400 + i * 80, easing = LinearEasing), RepeatMode.Reverse
            ),
            label = "bar$i"
        )
    }
    val context = LocalContext.current
    
    fun playSong() {
        if (song.isNotBlank()) {
            // Try YouTube Music first, fallback to regular YouTube
            val query = Uri.encode("$song $artist")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/search?q=$query"))
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to regular YouTube if YouTube Music not installed
                val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/results?search_query=$query"))
                context.startActivity(ytIntent)
            }
        }
    }
    
    WidgetCard(
        title = "🎵 Profile Song",
        titleColor = HuabuViolet,
        action = if (isCurrentUser) {{
            IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit song", tint = HuabuViolet, modifier = Modifier.size(18.dp))
            }
        }} else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(listOf(HuabuDeepPurple.copy(0.6f), HuabuViolet.copy(0.3f)))
                )
                .clickable { playSong() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(HuabuViolet, HuabuDeepPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.ifBlank { "No song set" },
                    color = HuabuOnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist.ifBlank { "Unknown artist" },
                    color = HuabuSilver,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.height(24.dp)
            ) {
                barHeights.forEach { h ->
                    val height by h
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(height.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(HuabuViolet)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// VIDEO LINKS WIDGET
// ─────────────────────────────────────────────

@Composable
fun VideoLinksWidget(
    videos: List<VideoLink>,
    isCurrentUser: Boolean,
    onAdd: (VideoLink) -> Unit,
    onDelete: (VideoLink) -> Unit,
    onReorder: (VideoLink, Boolean) -> Unit = { _, _ -> }
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    WidgetCard(
        title = "🎬 My Videos",
        titleColor = HuabuHotPink,
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add video", tint = HuabuHotPink, modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (videos.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to add YouTube videos!" else "No videos yet",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                videos.forEachIndexed { index, video ->
                    VideoLinkRow(
                        video = video,
                        isCurrentUser = isCurrentUser,
                        canMoveUp = index > 0,
                        canMoveDown = index < videos.size - 1,
                        onOpen = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.url)))
                            }
                        },
                        onDelete = { onDelete(video) },
                        onMoveUp = { onReorder(video, true) },
                        onMoveDown = { onReorder(video, false) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddVideoLinkDialog(
            onAdd = { link -> showAddDialog = false; onAdd(link) },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun VideoLinkRow(
    video: VideoLink,
    isCurrentUser: Boolean,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HuabuCardBg2)
            .clickable(onClick = onOpen)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.radialGradient(listOf(HuabuHotPink.copy(0.5f), HuabuDeepPurple))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                video.title,
                color = HuabuOnSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (video.description.isNotBlank()) {
                Text(
                    video.description,
                    color = HuabuSilver,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    video.url.take(40),
                    color = HuabuSilver.copy(0.6f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(Icons.Filled.OpenInNew, contentDescription = "Open", tint = HuabuSilver, modifier = Modifier.size(16.dp))

        if (isCurrentUser) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up", tint = if (canMoveUp) HuabuSilver.copy(0.7f) else Color.Transparent, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down", tint = if (canMoveDown) HuabuSilver.copy(0.7f) else Color.Transparent, modifier = Modifier.size(16.dp))
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = HuabuSilver.copy(0.5f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AddVideoLinkDialog(
    onAdd: (VideoLink) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🎬 Add Video", color = HuabuHotPink, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(HuabuHotPink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url, onValueChange = { url = it },
                    label = { Text("YouTube / Video URL", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(HuabuHotPink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(HuabuHotPink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && url.isNotBlank()) {
                        onAdd(VideoLink(
                            id = "vl_${System.currentTimeMillis()}",
                            userId = "",
                            title = title.trim(),
                            url = url.trim(),
                            description = description.trim()
                        ))
                    }
                },
                enabled = title.isNotBlank() && url.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = HuabuHotPink),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

// ─────────────────────────────────────────────
// TOP MUSIC WIDGET
// ─────────────────────────────────────────────

@Composable
fun TopMusicWidget(
    tracks: List<MediaTrack>,
    isCurrentUser: Boolean,
    onAdd: (MediaTrack) -> Unit,
    onDelete: (MediaTrack) -> Unit,
    onReorder: (MediaTrack, Boolean) -> Unit = { _, _ -> }
) {
    MediaRankWidget(
        title = "🎧 Top Music",
        titleColor = HuabuViolet,
        accentColor = HuabuViolet,
        emoji = "🎵",
        type = MediaTrackType.MUSIC,
        tracks = tracks,
        isCurrentUser = isCurrentUser,
        onAdd = onAdd,
        onDelete = onDelete
    )
}

// ─────────────────────────────────────────────
// TOP FILMS WIDGET
// ─────────────────────────────────────────────

@Composable
fun TopFilmsWidget(
    tracks: List<MediaTrack>,
    isCurrentUser: Boolean,
    onAdd: (MediaTrack) -> Unit,
    onDelete: (MediaTrack) -> Unit,
    onReorder: (MediaTrack, Boolean) -> Unit = { _, _ -> }
) {
    MediaRankWidget(
        title = "🎬 Top Films",
        titleColor = HuabuElectricBlue,
        accentColor = HuabuElectricBlue,
        emoji = "🎬",
        type = MediaTrackType.FILM,
        tracks = tracks,
        isCurrentUser = isCurrentUser,
        onAdd = onAdd,
        onDelete = onDelete,
        onReorder = onReorder
    )
}

@Composable
private fun MediaRankWidget(
    title: String,
    titleColor: Color,
    accentColor: Color,
    emoji: String,
    type: MediaTrackType,
    tracks: List<MediaTrack>,
    isCurrentUser: Boolean,
    onAdd: (MediaTrack) -> Unit,
    onDelete: (MediaTrack) -> Unit,
    onReorder: (MediaTrack, Boolean) -> Unit = { _, _ -> }
) {
    var showAddDialog by remember { mutableStateOf(false) }

    WidgetCard(
        title = title,
        titleColor = titleColor,
        action = if (isCurrentUser && tracks.size < 5) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = accentColor, modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (tracks.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to add your top ${if (type == MediaTrackType.MUSIC) "tracks" else "films"}!" else "Nothing listed yet",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val sorted = tracks.sortedBy { it.rank }
                sorted.forEachIndexed { index, track ->
                    MediaTrackRow(
                        track = track,
                        accentColor = accentColor,
                        emoji = emoji,
                        isCurrentUser = isCurrentUser,
                        canMoveUp = index > 0,
                        canMoveDown = index < sorted.size - 1,
                        onDelete = { onDelete(track) },
                        onMoveUp = { onReorder(track, true) },
                        onMoveDown = { onReorder(track, false) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMediaTrackDialog(
            type = type,
            accentColor = accentColor,
            nextRank = tracks.size + 1,
            onAdd = { track -> showAddDialog = false; onAdd(track) },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun MediaTrackRow(
    track: MediaTrack,
    accentColor: Color,
    emoji: String,
    isCurrentUser: Boolean,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {}
) {
    val context = LocalContext.current
    val rankColors = listOf(
        Color(0xFFFFD700), Color(0xFFC0C0C0), Color(0xFFCD7F32),
        HuabuSilver, HuabuSilver
    )
    val rankColor = rankColors.getOrElse(track.rank - 1) { HuabuSilver }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (track.rank == 1)
                    Brush.horizontalGradient(listOf(accentColor.copy(0.15f), HuabuCardBg2))
                else
                    Brush.horizontalGradient(listOf(HuabuCardBg2, HuabuCardBg2))
            )
            .clickable {
                val query = Uri.encode("${track.title} ${track.subtitle}")
                val url = if (track.type == MediaTrackType.FILM)
                    "https://www.imdb.com/find?q=$query"
                else
                    "https://music.youtube.com/search?q=$query"
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "#${track.rank}",
            color = rankColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            modifier = Modifier.width(28.dp)
        )

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.radialGradient(listOf(accentColor.copy(0.5f), HuabuDeepPurple))),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 16.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                color = HuabuOnSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(track.subtitle, color = HuabuSilver, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (track.year.isNotBlank()) {
                    Text("·", color = HuabuSilver, fontSize = 11.sp)
                    Text(track.year, color = HuabuSilver.copy(0.7f), fontSize = 11.sp)
                }
            }
        }

        if (isCurrentUser) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up", tint = if (canMoveUp) HuabuSilver.copy(0.7f) else Color.Transparent, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down", tint = if (canMoveDown) HuabuSilver.copy(0.7f) else Color.Transparent, modifier = Modifier.size(16.dp))
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = HuabuSilver.copy(0.4f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AddMediaTrackDialog(
    type: MediaTrackType,
    accentColor: Color,
    nextRank: Int,
    onAdd: (MediaTrack) -> Unit,
    onDismiss: () -> Unit,
    searchVm: YouTubeSearchViewModel = hiltViewModel()
) {
    val isMusic = type == MediaTrackType.MUSIC
    var title    by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var year     by remember { mutableStateOf("") }
    var query    by remember { mutableStateOf("") }
    val searchState by searchVm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { searchVm.clear() }

    AlertDialog(
        onDismissRequest = { searchVm.clear(); onDismiss() },
        containerColor = HuabuCardBg,
        title = { Text(if (isMusic) "🎵 Add Track" else "� Add Film", color = accentColor, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search box
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        searchVm.search(it, if (isMusic) "10" else "1")
                    },
                    label = { Text(if (isMusic) "🔍 Search YouTube Music" else "🔍 Search YouTube", color = HuabuSilver) },
                    singleLine = true,
                    trailingIcon = {
                        if (searchState.isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = accentColor)
                        else if (query.isNotEmpty()) IconButton(onClick = { query = ""; searchVm.clear() }) {
                            Icon(Icons.Filled.Clear, contentDescription = null, tint = HuabuSilver, modifier = Modifier.size(16.dp))
                        }
                    },
                    colors = outlinedFieldColors(accentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                // Search results
                if (searchState.results.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        searchState.results.forEach { item ->
                            val resultTitle = item.snippet.title
                            val resultChannel = item.snippet.channelTitle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HuabuCardBg2)
                                    .clickable {
                                        title = resultTitle
                                        subtitle = resultChannel
                                        query = resultTitle
                                        searchVm.clear()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(resultTitle, color = HuabuOnSurface, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(resultChannel, color = HuabuSilver, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = HuabuDivider)
                // Manual override fields
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text(if (isMusic) "Song title" else "Film title", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(accentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle, onValueChange = { subtitle = it },
                    label = { Text(if (isMusic) "Artist" else "Director", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(accentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = year, onValueChange = { year = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Year (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(accentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(MediaTrack(
                            id = "${type.name.lowercase()}_${System.currentTimeMillis()}",
                            userId = "",
                            type = type,
                            title = title.trim(),
                            subtitle = subtitle.trim(),
                            year = year.trim(),
                            rank = nextRank
                        ))
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun outlinedFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accent,
    unfocusedBorderColor = HuabuDivider,
    focusedTextColor = HuabuOnSurface,
    unfocusedTextColor = HuabuOnSurface,
    cursorColor = accent
)
