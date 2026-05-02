package com.huabu.app.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.huabu.app.data.model.*
import com.huabu.app.ui.theme.*

// ─────────────────────────────────────────────
// SPOTIFY NOW PLAYING WIDGET
// ─────────────────────────────────────────────

@Composable
fun SpotifyNowPlayingWidget(
    track: SpotifyTrack?,
    isCurrentUser: Boolean,
    userId: String = "",
    onSetTrack: (SpotifyTrack) -> Unit,
    onClear: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }

    WidgetCard(
        title = "🎵 Now Playing",
        titleColor = Color(0xFF1DB954),
        action = if (isCurrentUser) {{
            IconButton(onClick = { if (track != null) showEditor = true else showEditor = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    if (track != null) Icons.Filled.Edit else Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(20.dp)
                )
            }
        }} else null
    ) {
        if (track == null || track.title.isBlank()) {
            Text(
                text = if (isCurrentUser) "Tap + to share what you're listening to!" else "Not listening to anything",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            SpotifyTrackCard(track = track, isCurrentUser = isCurrentUser, onEdit = { showEditor = true }, onClear = onClear)
        }
    }

    if (showEditor) {
        SpotifyEditorDialog(
            existing = track,
            userId = userId,
            onSave = { showEditor = false; onSetTrack(it) },
            onDismiss = { showEditor = false }
        )
    }
}

@Composable
private fun SpotifyTrackCard(
    track: SpotifyTrack,
    isCurrentUser: Boolean,
    onEdit: () -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val query = Uri.encode("${track.title} ${track.artist}")
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/search?q=$query"))) }
            },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art placeholder
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1DB954).copy(0.3f), Color(0xFF1DB954).copy(0.1f))
                    )
                )
                .border(1.dp, Color(0xFF1DB954).copy(0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (track.albumArtUrl.isNotBlank()) {
                AsyncImage(
                    model = track.albumArtUrl,
                    contentDescription = track.album,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color(0xFF1DB954), modifier = Modifier.size(28.dp))
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(track.title, color = HuabuOnSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, color = HuabuSilver, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.album, color = Color(0xFF1DB954).copy(0.8f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

            // Progress bar
            if (track.durationMs > 0) {
                val progress = track.progressMs.toFloat() / track.durationMs
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF1DB954),
                    trackColor = HuabuDivider
                )
            }
        }

        // Playing indicator
        if (track.isPlaying) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1DB954).copy(0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("PLAYING", color = Color(0xFF1DB954), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SpotifyEditorDialog(
    existing: SpotifyTrack?,
    userId: String = "",
    onSave: (SpotifyTrack) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var artist by remember { mutableStateOf(existing?.artist ?: "") }
    var album by remember { mutableStateOf(existing?.album ?: "") }
    var isPlaying by remember { mutableStateOf(existing?.isPlaying ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🎵 What's Playing?", color = Color(0xFF1DB954), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Track Title", color = HuabuSilver) },
                    singleLine = true,
                    colors = spotifyFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist, onValueChange = { artist = it },
                    label = { Text("Artist", color = HuabuSilver) },
                    singleLine = true,
                    colors = spotifyFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = album, onValueChange = { album = it },
                    label = { Text("Album", color = HuabuSilver) },
                    singleLine = true,
                    colors = spotifyFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show as playing", color = HuabuSilver)
                    Switch(
                        checked = isPlaying,
                        onCheckedChange = { isPlaying = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1DB954),
                            checkedTrackColor = Color(0xFF1DB954).copy(0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && artist.isNotBlank()) {
                        onSave(SpotifyTrack(
                            id = existing?.id ?: "spotify_$userId",
                            userId = userId,
                            title = title.trim(),
                            artist = artist.trim(),
                            album = album.trim().ifEmpty { "Unknown Album" },
                            isPlaying = isPlaying
                        ))
                    }
                },
                enabled = title.isNotBlank() && artist.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Save", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun spotifyFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF1DB954),
    unfocusedBorderColor = HuabuDivider,
    focusedTextColor = HuabuOnSurface,
    unfocusedTextColor = HuabuOnSurface,
    cursorColor = Color(0xFF1DB954)
)

// ─────────────────────────────────────────────
// MEME WALL WIDGET
// ─────────────────────────────────────────────

@Composable
fun MemeWallWidget(
    memes: List<MemeItem>,
    isCurrentUser: Boolean,
    userId: String,
    onAddMeme: (MemeItem) -> Unit,
    onDeleteMeme: (MemeItem) -> Unit,
    onReact: (memeId: String, reaction: String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    WidgetCard(
        title = "😂 Meme Wall",
        titleColor = Color(0xFFFF9500),
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add meme", tint = Color(0xFFFF9500), modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (memes.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to add your favourite memes!" else "No memes yet",
                color = HuabuSilver,
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                memes.take(3).forEach { meme ->
                    MemeCard(
                        meme = meme,
                        isCurrentUser = isCurrentUser,
                        onReact = onReact,
                        onDelete = { onDeleteMeme(meme) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemeDialog(
            onAdd = { meme ->
                showAddDialog = false
                onAddMeme(meme)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun MemeCard(
    meme: MemeItem,
    isCurrentUser: Boolean,
    onReact: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HuabuCardBg2)
    ) {
        // Meme image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Color(0xFF2A2A3E)),
            contentAlignment = Alignment.Center
        ) {
            if (meme.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = meme.imageUrl,
                    contentDescription = meme.caption,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("🖼️", fontSize = 48.sp)
            }
        }

        // Caption and reactions
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (meme.caption.isNotBlank()) {
                Text(meme.caption, color = HuabuOnSurface, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemeReactionButton("❤️", meme.likes, Color(0xFFFF4757)) { onReact(meme.id, "like") }
                MemeReactionButton("🔥", meme.fire, Color(0xFFFF6348)) { onReact(meme.id, "fire") }
                MemeReactionButton("😂", meme.laugh, Color(0xFFFFEaa7)) { onReact(meme.id, "laugh") }
                MemeReactionButton("🤯", meme.mindblown, Color(0xFF8B5CF6)) { onReact(meme.id, "mindblown") }

                if (isCurrentUser) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = HuabuSilver.copy(0.5f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MemeReactionButton(emoji: String, count: Int, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Text(count.toString(), color = if (count > 0) color else HuabuSilver, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AddMemeDialog(
    onAdd: (MemeItem) -> Unit,
    onDismiss: () -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("😂 Add Meme", color = Color(0xFFFF9500), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = imageUrl, onValueChange = { imageUrl = it },
                    label = { Text("Image URL", color = HuabuSilver) },
                    singleLine = true,
                    colors = memeFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caption, onValueChange = { caption = it },
                    label = { Text("Caption (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = memeFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (imageUrl.isNotBlank()) {
                        onAdd(MemeItem(
                            id = "meme_${System.currentTimeMillis()}",
                            userId = "",
                            caption = caption.trim(),
                            imageUrl = imageUrl.trim()
                        ))
                    }
                },
                enabled = imageUrl.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add", color = Color.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun memeFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFFFF9500),
    unfocusedBorderColor = HuabuDivider,
    focusedTextColor = HuabuOnSurface,
    unfocusedTextColor = HuabuOnSurface,
    cursorColor = Color(0xFFFF9500)
)

// ─────────────────────────────────────────────
// GAME STATS WIDGET
// ─────────────────────────────────────────────

@Composable
fun GameStatsWidget(
    stats: List<GameStats>,
    isCurrentUser: Boolean,
    onAdd: (GameStats) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    WidgetCard(
        title = "🎮 Game Stats",
        titleColor = Color(0xFF7B68EE),
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add stats", tint = Color(0xFF7B68EE), modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (stats.isEmpty()) {
            val mockStats = getMockGameStats()
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                mockStats.forEach { GameStatCard(it) }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                stats.forEach { GameStatCard(it) }
            }
        }
    }

    if (showAddDialog) {
        AddGameStatsDialog(
            onAdd = { gameStats ->
                showAddDialog = false
                onAdd(gameStats)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun GameStatCard(stats: GameStats) {
    val gameColors = mapOf(
        GameType.WORDLE to Color(0xFF6AAA64),
        GameType.CONNECTIONS to Color(0xFFFFA500),
        GameType.STRANDS to Color(0xFFB5A7FF),
        GameType.MINI_CROSSWORD to Color(0xFF87CEEB),
        GameType.GEOGuessr to Color(0xFFDC143C),
        GameType.OTHER to HuabuSilver
    )
    val color = gameColors[stats.gameType] ?: HuabuSilver

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HuabuCardBg2)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (stats.gameType) {
                        GameType.WORDLE -> "W"
                        GameType.CONNECTIONS -> "C"
                        GameType.STRANDS -> "S"
                        GameType.MINI_CROSSWORD -> "X"
                        GameType.GEOGuessr -> "G"
                        GameType.OTHER -> "?"
                    },
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column {
                Text(stats.gameType.name, color = HuabuOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${stats.gamesPlayed} played • ${(stats.winRate * 100).toInt()}% win", color = HuabuSilver, fontSize = 11.sp)
            }
        }

        // Streak badge
        if (stats.streak > 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔥", fontSize = 16.sp)
                Text("${stats.streak}", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AddGameStatsDialog(
    onAdd: (GameStats) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGame by remember { mutableStateOf(GameType.WORDLE) }
    var gamesPlayed by remember { mutableStateOf("10") }
    var winRate by remember { mutableStateOf("50") }
    var currentStreak by remember { mutableStateOf("0") }
    var maxStreak by remember { mutableStateOf("0") }

    val gameColors = mapOf(
        GameType.WORDLE to Color(0xFF6AAA64),
        GameType.CONNECTIONS to Color(0xFFFFA500),
        GameType.STRANDS to Color(0xFFB5A7FF),
        GameType.MINI_CROSSWORD to Color(0xFF87CEEB),
        GameType.GEOGuessr to Color(0xFFDC143C),
        GameType.OTHER to HuabuSilver
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🎮 Add Game Stats", color = Color(0xFF7B68EE), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Game selector
                Text("Game", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                val rows = GameType.entries.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rows.forEach { rowGames ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowGames.forEach { game ->
                                val color = gameColors[game] ?: HuabuSilver
                                FilterChip(
                                    selected = selectedGame == game,
                                    onClick = { selectedGame = game },
                                    label = { Text(game.name, fontSize = 9.sp, maxLines = 1) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = color.copy(0.25f),
                                        selectedLabelColor = color
                                    )
                                )
                            }
                            repeat(3 - rowGames.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = gamesPlayed, onValueChange = { gamesPlayed = it.filter { c -> c.isDigit() } },
                    label = { Text("Games Played", color = HuabuSilver) },
                    singleLine = true,
                    colors = gameFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = winRate, onValueChange = { winRate = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Win Rate % (0-100)", color = HuabuSilver) },
                    singleLine = true,
                    colors = gameFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentStreak, onValueChange = { currentStreak = it.filter { c -> c.isDigit() } },
                        label = { Text("Current Streak", color = HuabuSilver, fontSize = 11.sp) },
                        singleLine = true,
                        colors = gameFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxStreak, onValueChange = { maxStreak = it.filter { c -> c.isDigit() } },
                        label = { Text("Max Streak", color = HuabuSilver, fontSize = 11.sp) },
                        singleLine = true,
                        colors = gameFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val played = gamesPlayed.toIntOrNull() ?: 0
                    val rate = (winRate.toIntOrNull() ?: 0) / 100f
                    val currStreak = currentStreak.toIntOrNull() ?: 0
                    val max = maxStreak.toIntOrNull() ?: 0
                    if (played > 0) {
                        onAdd(GameStats(
                            id = "game_${System.currentTimeMillis()}",
                            userId = "",
                            gameType = selectedGame,
                            gamesPlayed = played,
                            winRate = rate.coerceIn(0f, 1f),
                            streak = currStreak,
                            maxStreak = max
                        ))
                    }
                },
                enabled = (gamesPlayed.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B68EE)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

@Composable
private fun gameFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF7B68EE),
    unfocusedBorderColor = HuabuDivider,
    focusedTextColor = HuabuOnSurface,
    unfocusedTextColor = HuabuOnSurface,
    cursorColor = Color(0xFF7B68EE)
)

private fun getMockGameStats(): List<GameStats> = listOf(
    GameStats("gs1", "", GameType.WORDLE, score = 450, streak = 12, maxStreak = 45, gamesPlayed = 156, winRate = 0.94f),
    GameStats("gs2", "", GameType.CONNECTIONS, score = 8, streak = 5, maxStreak = 18, gamesPlayed = 89, winRate = 0.87f),
    GameStats("gs3", "", GameType.STRANDS, score = 7, streak = 3, maxStreak = 12, gamesPlayed = 45, winRate = 0.82f)
)

// ─────────────────────────────────────────────
// VISITED PLACES WIDGET
// ─────────────────────────────────────────────

@Composable
fun VisitedPlacesWidget(
    places: List<VisitedPlace>,
    isCurrentUser: Boolean,
    onAdd: (VisitedPlace) -> Unit,
    onDelete: (VisitedPlace) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    WidgetCard(
        title = "✈️ Visited Places",
        titleColor = Color(0xFF00B4D8),
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add place", tint = Color(0xFF00B4D8), modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (places.isEmpty()) {
            val mockPlaces = getMockVisitedPlaces()
            PlacesGrid(places = mockPlaces, isCurrentUser = false, onDelete = {})
        } else {
            PlacesGrid(places = places, isCurrentUser = isCurrentUser, onDelete = onDelete)
        }
    }

    if (showAddDialog) {
        AddPlaceDialog(
            onAdd = { place ->
                showAddDialog = false
                onAdd(place)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun PlacesGrid(
    places: List<VisitedPlace>,
    isCurrentUser: Boolean,
    onDelete: (VisitedPlace) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        places.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { place ->
                    PlaceChip(place = place, isCurrentUser = isCurrentUser, onDelete = { onDelete(place) }, modifier = Modifier.weight(1f))
                }
                repeat(3 - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PlaceChip(
    place: VisitedPlace,
    isCurrentUser: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF00B4D8).copy(0.15f))
            .border(1.dp, Color(0xFF00B4D8).copy(0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(place.countryCode.ifEmpty { "🌍" }, fontSize = 24.sp)
            Text(
                place.name,
                color = HuabuOnSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(place.country, color = HuabuSilver, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

            if (isCurrentUser) {
                IconButton(onClick = onDelete, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = HuabuSilver.copy(0.5f), modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AddPlaceDialog(
    onAdd: (VisitedPlace) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("✈️ Add Visited Place", color = Color(0xFF00B4D8), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("City/Place", color = HuabuSilver) },
                    singleLine = true,
                    colors = travelFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = country, onValueChange = { country = it },
                    label = { Text("Country", color = HuabuSilver) },
                    singleLine = true,
                    colors = travelFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = countryCode, onValueChange = { countryCode = it },
                    label = { Text("Flag emoji (e.g. 🇯🇵)", color = HuabuSilver) },
                    singleLine = true,
                    colors = travelFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && country.isNotBlank()) {
                        onAdd(VisitedPlace(
                            id = "place_${System.currentTimeMillis()}",
                            userId = "",
                            name = name.trim(),
                            country = country.trim(),
                            countryCode = countryCode.trim().ifEmpty { "🌍" }
                        ))
                    }
                },
                enabled = name.isNotBlank() && country.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

private fun getMockVisitedPlaces(): List<VisitedPlace> = listOf(
    VisitedPlace("p1", "", "Tokyo", "Japan", "🇯🇵"),
    VisitedPlace("p2", "", "Paris", "France", "🇫🇷"),
    VisitedPlace("p3", "", "New York", "USA", "🇺🇸"),
    VisitedPlace("p4", "", "London", "UK", "🇬🇧"),
    VisitedPlace("p5", "", "Barcelona", "Spain", "🇪🇸"),
    VisitedPlace("p6", "", "Rome", "Italy", "🇮🇹")
)

// ─────────────────────────────────────────────
// TRAVEL WISHLIST WIDGET
// ─────────────────────────────────────────────

@Composable
fun TravelWishlistWidget(
    wishes: List<TravelWish>,
    isCurrentUser: Boolean,
    onAdd: (TravelWish) -> Unit,
    onDelete: (TravelWish) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    WidgetCard(
        title = "🌎 Travel Wishlist",
        titleColor = Color(0xFFFF6B9D),
        action = if (isCurrentUser) {{
            IconButton(onClick = { showAddDialog = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Add destination", tint = Color(0xFFFF6B9D), modifier = Modifier.size(20.dp))
            }
        }} else null
    ) {
        if (wishes.isEmpty()) {
            val mockWishes = getMockTravelWishes()
            TravelWishesList(wishes = mockWishes, isCurrentUser = false, onDelete = {})
        } else {
            TravelWishesList(wishes = wishes, isCurrentUser = isCurrentUser, onDelete = onDelete)
        }
    }

    if (showAddDialog) {
        AddWishDialog(
            onAdd = { wish ->
                showAddDialog = false
                onAdd(wish)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun TravelWishesList(
    wishes: List<TravelWish>,
    isCurrentUser: Boolean,
    onDelete: (TravelWish) -> Unit
) {
    // Group by priority
    val grouped = wishes.groupBy { it.priority }.toSortedMap()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        grouped.forEach { (priority, items) ->
            val priorityLabel = when (priority) {
                1 -> "🔥 MUST VISIT"
                2 -> "⭐ HIGH PRIORITY"
                else -> "💭 SOMEDAY"
            }
            val priorityColor = when (priority) {
                1 -> Color(0xFFFF6B6B)
                2 -> Color(0xFFFFD93D)
                else -> HuabuSilver
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    priorityLabel,
                    color = priorityColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                items.forEach { wish ->
                    WishItem(wish = wish, isCurrentUser = isCurrentUser, onDelete = { onDelete(wish) })
                }
            }
        }
    }
}

@Composable
private fun WishItem(
    wish: TravelWish,
    isCurrentUser: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HuabuCardBg2)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(wish.countryCode.ifEmpty { "🌍" }, fontSize = 20.sp)

            Column {
                Text(wish.destination, color = HuabuOnSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text(wish.country, color = HuabuSilver, fontSize = 11.sp)
                if (wish.notes.isNotBlank()) {
                    Text(wish.notes, color = HuabuSilver.copy(0.7f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        if (isCurrentUser) {
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = HuabuSilver.copy(0.5f), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun AddWishDialog(
    onAdd: (TravelWish) -> Unit,
    onDismiss: () -> Unit
) {
    var destination by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("🌎 Add to Wishlist", color = Color(0xFFFF6B9D), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = destination, onValueChange = { destination = it },
                    label = { Text("Destination (city/region)", color = HuabuSilver) },
                    singleLine = true,
                    colors = travelFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = country, onValueChange = { country = it },
                    label = { Text("Country", color = HuabuSilver) },
                    singleLine = true,
                    colors = travelFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = countryCode, onValueChange = { countryCode = it },
                    label = { Text("Flag emoji", color = HuabuSilver) },
                    singleLine = true,
                    colors = travelFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority selector
                Text("Priority", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1 to "🔥 Must", 2 to "⭐ High", 3 to "💭 Someday").forEach { (p, label) ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(label, fontSize = 10.sp, maxLines = 1) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF6B9D).copy(0.25f),
                                selectedLabelColor = Color(0xFFFF6B9D)
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes (optional)", color = HuabuSilver) },
                    singleLine = true,
                    colors = travelFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (destination.isNotBlank() && country.isNotBlank()) {
                        onAdd(TravelWish(
                            id = "wish_${System.currentTimeMillis()}",
                            userId = "",
                            destination = destination.trim(),
                            country = country.trim(),
                            countryCode = countryCode.trim().ifEmpty { "🌍" },
                            priority = priority,
                            notes = notes.trim()
                        ))
                    }
                },
                enabled = destination.isNotBlank() && country.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B9D)),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) } }
    )
}

private fun getMockTravelWishes(): List<TravelWish> = listOf(
    TravelWish("w1", "", "Kyoto", "Japan", "🇯🇵", priority = 1, notes = "Cherry blossom season"),
    TravelWish("w2", "", "Iceland", "Iceland", "🇮🇸", priority = 1, notes = "Northern lights"),
    TravelWish("w3", "", "New Zealand", "New Zealand", "🇳🇿", priority = 2, notes = "Lord of the Rings tour"),
    TravelWish("w4", "", "Morocco", "Morocco", "🇲🇦", priority = 3, notes = "Sahara desert"),
    TravelWish("w5", "", "Peru", "Peru", "🇵🇪", priority = 2, notes = "Machu Picchu")
)

@Composable
private fun travelFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00B4D8),
    unfocusedBorderColor = HuabuDivider,
    focusedTextColor = HuabuOnSurface,
    unfocusedTextColor = HuabuOnSurface,
    cursorColor = Color(0xFF00B4D8)
)
