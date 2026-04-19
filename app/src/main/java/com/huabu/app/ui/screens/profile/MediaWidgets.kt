package com.huabu.app.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.huabu.app.data.model.*
import com.huabu.app.ui.theme.*

// ─────────────────────────────────────────────
// RECENTLY PLAYED WIDGET
// ─────────────────────────────────────────────

@Composable
fun RecentlyPlayedWidget(
    tracks: List<RecentTrack>,
    isCurrentUser: Boolean
) {
    var nowPlayingId by remember { mutableStateOf<String?>(null) }

    WidgetCard(title = "🎧 Recently Played", titleColor = HuabuViolet) {
        if (tracks.isEmpty()) {
            Text(
                text = "Nothing played recently",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tracks.forEach { track ->
                    val isPlaying = nowPlayingId == track.id
                    RecentTrackRow(
                        track = track,
                        isPlaying = isPlaying,
                        onPlayPause = {
                            nowPlayingId = if (isPlaying) null else track.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTrackRow(
    track: RecentTrack,
    isPlaying: Boolean,
    onPlayPause: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HuabuCardBg2)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Album art placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.radialGradient(listOf(hexToColor(track.albumArtColor), hexToColor(track.albumArtColor).copy(0.4f)))),
            contentAlignment = Alignment.Center
        ) {
            Text("🎵", fontSize = 18.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                color = if (isPlaying) HuabuViolet else HuabuOnSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                track.artist,
                color = HuabuSilver,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Play/pause
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                contentDescription = null,
                tint = if (isPlaying) HuabuViolet else HuabuSilver,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// MY PLAYLIST WIDGET
// ─────────────────────────────────────────────

@Composable
fun MyPlaylistWidget(
    items: List<PlaylistItem>,
    isCurrentUser: Boolean,
    onAdd: (PlaylistItem) -> Unit,
    onDelete: (PlaylistItem) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var nowPlayingId  by remember { mutableStateOf<String?>(null) }

    WidgetCard(
        title = "🎶 My Playlist",
        titleColor = HuabuAccentCyan,
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add track", tint = HuabuAccentCyan, modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (items.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to build your playlist!" else "No playlist yet",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEachIndexed { index, item ->
                    val isPlaying = nowPlayingId == item.id
                    PlaylistRow(
                        item = item,
                        index = index + 1,
                        isPlaying = isPlaying,
                        isCurrentUser = isCurrentUser,
                        onPlayPause = { nowPlayingId = if (isPlaying) null else item.id },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddPlaylistItemDialog(
            existingCount = items.size,
            onAdd = { item ->
                showAddDialog = false
                onAdd(item)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun PlaylistRow(
    item: PlaylistItem,
    index: Int,
    isPlaying: Boolean,
    isCurrentUser: Boolean,
    onPlayPause: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isPlaying)
                    Brush.horizontalGradient(listOf(HuabuAccentCyan.copy(0.15f), HuabuCardBg2))
                else
                    Brush.horizontalGradient(listOf(HuabuCardBg2, HuabuCardBg2))
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "$index",
            color = if (isPlaying) HuabuAccentCyan else HuabuSilver,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(18.dp)
        )

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.radialGradient(listOf(hexToColor(item.albumArtColor), hexToColor(item.albumArtColor).copy(0.4f)))),
            contentAlignment = Alignment.Center
        ) {
            Text("🎵", fontSize = 16.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                color = if (isPlaying) HuabuAccentCyan else HuabuOnSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.artist,
                color = HuabuSilver,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onPlayPause, modifier = Modifier.size(28.dp)) {
            Icon(
                if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                contentDescription = null,
                tint = if (isPlaying) HuabuAccentCyan else HuabuSilver,
                modifier = Modifier.size(24.dp)
            )
        }

        if (isCurrentUser) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = HuabuSilver.copy(0.5f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AddPlaylistItemDialog(
    existingCount: Int,
    onAdd: (PlaylistItem) -> Unit,
    onDismiss: () -> Unit
) {
    var title  by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    val colors = listOf("#8B5CF6","#EC4899","#06B6D4","#EAB308","#22C55E","#EF4444","#3B82F6","#F97316")
    var selectedColor by remember { mutableStateOf(colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🎵 Add to Playlist", color = HuabuAccentCyan, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song title", color = HuabuSilver) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuabuAccentCyan,
                        unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface,
                        unfocusedTextColor = HuabuOnSurface,
                        cursorColor = HuabuAccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist", color = HuabuSilver) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuabuAccentCyan,
                        unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface,
                        unfocusedTextColor = HuabuOnSurface,
                        cursorColor = HuabuAccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Album colour", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(hexToColor(hex))
                                .then(if (selectedColor == hex) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && artist.isNotBlank()) {
                        onAdd(PlaylistItem(
                            id = "pl_${System.currentTimeMillis()}",
                            userId = "",
                            title = title.trim(),
                            artist = artist.trim(),
                            albumArtColor = selectedColor,
                            sortOrder = existingCount
                        ))
                    }
                },
                enabled = title.isNotBlank() && artist.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = HuabuAccentCyan),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add", color = Color.Black) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) }
        }
    )
}

// ─────────────────────────────────────────────
// CURRENTLY READING WIDGET
// ─────────────────────────────────────────────

private val BOOK_COLORS = listOf(
    "#8B5CF6","#EC4899","#EF4444","#F97316","#EAB308","#22C55E","#06B6D4","#3B82F6"
)

@Composable
fun CurrentlyReadingWidget(
    book: CurrentlyReading?,
    isCurrentUser: Boolean,
    onSave: (CurrentlyReading) -> Unit,
    onClear: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }

    WidgetCard(
        title = "📖 Currently Reading",
        titleColor = Color(0xFFEAB308),
        action = if (isCurrentUser) {{
            IconButton(
                onClick = { showEditor = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    if (book != null) Icons.Filled.Edit else Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color(0xFFEAB308),
                    modifier = Modifier.size(20.dp)
                )
            }
        }} else null
    ) {
        if (book == null || book.title.isBlank()) {
            Text(
                text = if (isCurrentUser) "Tap + to share what you're reading!" else "Nothing listed",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            BookCard(book = book, isCurrentUser = isCurrentUser, onEdit = { showEditor = true }, onClear = onClear)
        }
    }

    if (showEditor) {
        BookEditor(existing = book, onSave = { showEditor = false; onSave(it) }, onDismiss = { showEditor = false })
    }
}

@Composable
private fun BookCard(
    book: CurrentlyReading,
    isCurrentUser: Boolean,
    onEdit: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Book cover
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(78.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.verticalGradient(listOf(hexToColor(book.coverColor), hexToColor(book.coverColor).copy(0.5f))))
                .border(1.dp, hexToColor(book.coverColor).copy(0.6f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("📖", fontSize = 22.sp)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(book.title, color = HuabuOnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(book.author, color = HuabuSilver, fontSize = 12.sp)

            if (book.totalPages > 0) {
                Text("Page ${book.currentPage} of ${book.totalPages}", color = HuabuSilver, fontSize = 11.sp)
            }

            // Progress bar
            val progress = book.progressPercent / 100f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFEAB308),
                trackColor = HuabuDivider
            )
            Text("${book.progressPercent}% complete", color = Color(0xFFEAB308), fontSize = 11.sp)

            if (book.rating > 0f) {
                StarRating(rating = book.rating, color = Color(0xFFEAB308))
            }
        }
    }
}

@Composable
private fun BookEditor(
    existing: CurrentlyReading?,
    onSave: (CurrentlyReading) -> Unit,
    onDismiss: () -> Unit
) {
    var title        by remember { mutableStateOf(existing?.title ?: "") }
    var author       by remember { mutableStateOf(existing?.author ?: "") }
    var totalPages   by remember { mutableStateOf(existing?.totalPages?.takeIf { it > 0 }?.toString() ?: "") }
    var currentPage  by remember { mutableStateOf(existing?.currentPage?.takeIf { it > 0 }?.toString() ?: "") }
    var selectedColor by remember { mutableStateOf(existing?.coverColor ?: BOOK_COLORS[0]) }
    var rating       by remember { mutableStateOf(existing?.rating ?: 0f) }

    val progress = if (totalPages.toIntOrNull() != null && totalPages.toInt() > 0 && currentPage.toIntOrNull() != null)
        ((currentPage.toInt().coerceAtMost(totalPages.toInt()) * 100) / totalPages.toInt())
    else existing?.progressPercent ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("📖 What are you reading?", color = Color(0xFFEAB308), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Book title", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(Color(0xFFEAB308)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = author, onValueChange = { author = it },
                    label = { Text("Author", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(Color(0xFFEAB308)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPage, onValueChange = { currentPage = it.filter { c -> c.isDigit() } },
                        label = { Text("Current page", color = HuabuSilver) },
                        singleLine = true,
                        colors = outlinedFieldColors(Color(0xFFEAB308)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalPages, onValueChange = { totalPages = it.filter { c -> c.isDigit() } },
                        label = { Text("Total pages", color = HuabuSilver) },
                        singleLine = true,
                        colors = outlinedFieldColors(Color(0xFFEAB308)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Cover colour", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BOOK_COLORS.forEach { hex ->
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(hexToColor(hex))
                                .then(if (selectedColor == hex) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                                .clickable { selectedColor = hex }
                        )
                    }
                }
                Text("Your rating", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                InteractiveStarRating(rating = rating, onRatingChange = { rating = it }, color = Color(0xFFEAB308))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(CurrentlyReading(
                            userId = existing?.userId ?: "",
                            title = title.trim(),
                            author = author.trim(),
                            coverColor = selectedColor,
                            currentPage = currentPage.toIntOrNull() ?: 0,
                            totalPages = totalPages.toIntOrNull() ?: 0,
                            progressPercent = progress,
                            rating = rating
                        ))
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Save", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

// ─────────────────────────────────────────────
// CURRENTLY WATCHING WIDGET
// ─────────────────────────────────────────────

private val WATCH_COLORS = listOf(
    "#0D2137","#1A0030","#001A1A","#2D0000","#1A1A00","#002D00","#1A000D","#0D0D2D"
)

@Composable
fun CurrentlyWatchingWidget(
    show: CurrentlyWatching?,
    isCurrentUser: Boolean,
    onSave: (CurrentlyWatching) -> Unit,
    onClear: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }

    WidgetCard(
        title = "🎬 Currently Watching",
        titleColor = HuabuHotPink,
        action = if (isCurrentUser) {{
            IconButton(onClick = { showEditor = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    if (show != null && show.title.isNotBlank()) Icons.Filled.Edit else Icons.Filled.Add,
                    contentDescription = null, tint = HuabuHotPink, modifier = Modifier.size(20.dp)
                )
            }
        }} else null
    ) {
        if (show == null || show.title.isBlank()) {
            Text(
                text = if (isCurrentUser) "Tap + to share what you're watching!" else "Nothing listed",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            WatchCard(show = show, isCurrentUser = isCurrentUser, onEdit = { showEditor = true }, onClear = onClear)
        }
    }

    if (showEditor) {
        WatchEditor(existing = show, onSave = { showEditor = false; onSave(it) }, onDismiss = { showEditor = false })
    }
}

@Composable
private fun WatchCard(
    show: CurrentlyWatching,
    isCurrentUser: Boolean,
    onEdit: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Poster placeholder
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(78.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(hexToColor(show.coverColor), hexToColor(show.coverColor).copy(0.4f))))
                .border(1.dp, HuabuHotPink.copy(0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when (show.type) {
                    WatchType.ANIME        -> "⛩️"
                    WatchType.DOCUMENTARY  -> "🎙️"
                    WatchType.FILM         -> "🎬"
                    WatchType.SERIES       -> "📺"
                },
                fontSize = 20.sp
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(show.title, color = HuabuOnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HuabuHotPink.copy(0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(show.type.name, color = HuabuHotPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (show.platform.isNotBlank()) {
                Text(show.platform, color = HuabuSilver, fontSize = 11.sp)
            }

            if (show.type == WatchType.SERIES || show.type == WatchType.ANIME) {
                Text("S${show.season} · E${show.episode}${if (show.totalEpisodes > 0) "/${show.totalEpisodes}" else ""}", color = HuabuSilver, fontSize = 11.sp)
            }

            val progress = show.progressPercent / 100f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
                color = HuabuHotPink,
                trackColor = HuabuDivider
            )
            Text("${show.progressPercent}% watched", color = HuabuHotPink, fontSize = 11.sp)

            if (show.rating > 0f) {
                StarRating(rating = show.rating, color = HuabuHotPink)
            }
        }
    }
}

@Composable
private fun WatchEditor(
    existing: CurrentlyWatching?,
    onSave: (CurrentlyWatching) -> Unit,
    onDismiss: () -> Unit
) {
    var title    by remember { mutableStateOf(existing?.title ?: "") }
    var platform by remember { mutableStateOf(existing?.platform ?: "") }
    var type     by remember { mutableStateOf(existing?.type ?: WatchType.SERIES) }
    var season   by remember { mutableStateOf(existing?.season?.toString() ?: "1") }
    var episode  by remember { mutableStateOf(existing?.episode?.toString() ?: "1") }
    var total    by remember { mutableStateOf(existing?.totalEpisodes?.takeIf { it > 0 }?.toString() ?: "") }
    var progress by remember { mutableStateOf(existing?.progressPercent?.toString() ?: "0") }
    var rating   by remember { mutableStateOf(existing?.rating ?: 0f) }
    var selectedColor by remember { mutableStateOf(existing?.coverColor ?: WATCH_COLORS[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🎬 What are you watching?", color = HuabuHotPink, fontWeight = FontWeight.ExtraBold) },
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
                    value = platform, onValueChange = { platform = it },
                    label = { Text("Platform (e.g. Netflix)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(HuabuHotPink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Type selector
                Text("Type", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WatchType.entries.forEach { t ->
                        val label = when(t) {
                            WatchType.DOCUMENTARY -> "DOC"
                            else -> t.name
                        }
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(label, fontSize = 10.sp, maxLines = 1) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HuabuHotPink.copy(0.25f),
                                selectedLabelColor = HuabuHotPink
                            )
                        )
                    }
                }

                if (type == WatchType.SERIES || type == WatchType.ANIME) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = season, onValueChange = { season = it.filter { c -> c.isDigit() } },
                            label = { Text("Season", color = HuabuSilver, fontSize = 11.sp) },
                            singleLine = true,
                            colors = outlinedFieldColors(HuabuHotPink),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )
                        OutlinedTextField(
                            value = episode, onValueChange = { episode = it.filter { c -> c.isDigit() } },
                            label = { Text("Episode", color = HuabuSilver, fontSize = 11.sp) },
                            singleLine = true,
                            colors = outlinedFieldColors(HuabuHotPink),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )
                        OutlinedTextField(
                            value = total, onValueChange = { total = it.filter { c -> c.isDigit() } },
                            label = { Text("Total eps", color = HuabuSilver, fontSize = 11.sp) },
                            singleLine = true,
                            colors = outlinedFieldColors(HuabuHotPink),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )
                    }
                }

                OutlinedTextField(
                    value = progress, onValueChange = { v -> progress = v.filter { c -> c.isDigit() }.take(3).let { if (it.toIntOrNull() ?: 0 > 100) "100" else it } },
                    label = { Text("% watched (0–100)", color = HuabuSilver) },
                    singleLine = true,
                    colors = outlinedFieldColors(HuabuHotPink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Poster colour", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WATCH_COLORS.forEach { hex ->
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                                .background(hexToColor(hex))
                                .border(1.dp, HuabuDivider, CircleShape)
                                .then(if (selectedColor == hex) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                                .clickable { selectedColor = hex }
                        )
                    }
                }
                Text("Your rating", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                InteractiveStarRating(rating = rating, onRatingChange = { rating = it }, color = HuabuHotPink)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(CurrentlyWatching(
                            userId = existing?.userId ?: "",
                            title = title.trim(),
                            type = type,
                            coverColor = selectedColor,
                            platform = platform.trim(),
                            season = season.toIntOrNull() ?: 1,
                            episode = episode.toIntOrNull() ?: 1,
                            totalEpisodes = total.toIntOrNull() ?: 0,
                            progressPercent = progress.toIntOrNull()?.coerceIn(0, 100) ?: 0,
                            rating = rating
                        ))
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = HuabuHotPink),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

// ─────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────

@Composable
private fun StarRating(rating: Float, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        (1..5).forEach { i ->
            Icon(
                if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun InteractiveStarRating(rating: Float, onRatingChange: (Float) -> Unit, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { i ->
            Icon(
                if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "$i stars",
                tint = color,
                modifier = Modifier.size(28.dp).clickable { onRatingChange(i.toFloat()) }
            )
        }
    }
}

@Composable
private fun outlinedFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accent,
    unfocusedBorderColor = HuabuDivider,
    focusedTextColor = HuabuOnSurface,
    unfocusedTextColor = HuabuOnSurface,
    cursorColor = accent
)
