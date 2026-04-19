package com.huabu.app.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huabu.app.data.model.LiveStream
import com.huabu.app.data.model.ProfileEvent
import com.huabu.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────
// GO LIVE WIDGET
// ─────────────────────────────────────────────

@Composable
fun GoLiveWidget(
    liveStream: LiveStream,
    isCurrentUser: Boolean,
    onGoLive: (String) -> Unit,
    onEndLive: () -> Unit
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulse by pulseAnim.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    WidgetCard(title = "🔴 Live", titleColor = Color(0xFFEF4444)) {
        if (liveStream.isLive) {
            // Currently live — show live banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEF4444).copy(0.25f), Color(0xFFFF6B6B).copy(0.15f))
                        )
                    )
                    .border(1.dp, Color(0xFFEF4444).copy(0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pulsing live dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .scale(pulse)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (liveStream.title.isNotEmpty()) liveStream.title else "Live now",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${liveStream.viewerCount} watching",
                            color = HuabuSilver,
                            fontSize = 12.sp
                        )
                    }
                    if (isCurrentUser) {
                        OutlinedButton(
                            onClick = onEndLive,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFEF4444)))
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("End", fontSize = 12.sp, color = Color(0xFFEF4444))
                        }
                    } else {
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Watch", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else if (isCurrentUser) {
            // Not live — show go live button for owner
            var showDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Go Live", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (showDialog) {
                GoLiveDialog(
                    onStart = { title ->
                        showDialog = false
                        onGoLive(title)
                    },
                    onDismiss = { showDialog = false }
                )
            }
        } else {
            // Not live, not owner — show offline state
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(HuabuDivider)
                )
                Text("Not live right now", color = HuabuSilver, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun GoLiveDialog(onStart: (String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = {
            Text("🔴 Start Live Stream", color = HuabuGold, fontWeight = FontWeight.ExtraBold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Give your stream a title so viewers know what it's about.", color = HuabuSilver, fontSize = 13.sp)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Stream title", color = HuabuSilver) },
                    placeholder = { Text("e.g. Chilling & chatting 🎵", color = HuabuDivider, fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface,
                        unfocusedTextColor = HuabuOnSurface,
                        cursorColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onStart(title) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Go Live")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) }
        }
    )
}

// ─────────────────────────────────────────────
// EVENTS WIDGET
// ─────────────────────────────────────────────

@Composable
fun EventsWidget(
    events: List<ProfileEvent>,
    isCurrentUser: Boolean,
    onAddEvent: (ProfileEvent) -> Unit,
    onDeleteEvent: (ProfileEvent) -> Unit,
    onRsvp: (ProfileEvent) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    WidgetCard(title = "📅 Events", titleColor = HuabuAccentCyan) {
        if (isCurrentUser) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add event", tint = HuabuAccentCyan, modifier = Modifier.size(20.dp))
                }
            }
        }
        if (events.isEmpty()) {
            Text(
                text = if (isCurrentUser) "Tap + to add your first event!" else "No upcoming events",
                color = HuabuSilver,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                events.forEach { event ->
                    EventCard(
                        event = event,
                        isCurrentUser = isCurrentUser,
                        onDelete = { onDeleteEvent(event) },
                        onRsvp = { onRsvp(event) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onAdd = { event ->
                showAddDialog = false
                onAddEvent(event)
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun EventCard(
    event: ProfileEvent,
    isCurrentUser: Boolean,
    onDelete: () -> Unit,
    onRsvp: () -> Unit
) {
    val dateStr = remember(event.eventDate) {
        SimpleDateFormat("EEE, d MMM 'at' HH:mm", Locale.getDefault()).format(Date(event.eventDate))
    }
    val isPast = event.eventDate < System.currentTimeMillis()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HuabuCardBg2)
            .border(
                1.dp,
                if (isPast) HuabuDivider else HuabuAccentCyan.copy(0.35f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isPast) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HuabuAccentCyan.copy(0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("UPCOMING", color = HuabuAccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HuabuDivider.copy(0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PAST", color = HuabuSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (event.isOnline) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HuabuViolet.copy(0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ONLINE", color = HuabuViolet, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(event.title, color = HuabuOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                if (isCurrentUser) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = HuabuSilver, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (event.description.isNotEmpty()) {
                Text(event.description, color = HuabuSilver, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = HuabuSilver, modifier = Modifier.size(13.dp))
                    Text(dateStr, color = HuabuSilver, fontSize = 11.sp)
                }
                if (event.location.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = HuabuSilver, modifier = Modifier.size(13.dp))
                        Text(event.location, color = HuabuSilver, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            if (!isPast) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.People, contentDescription = null, tint = HuabuSilver, modifier = Modifier.size(13.dp))
                        Text("${event.rsvpCount} going", color = HuabuSilver, fontSize = 11.sp)
                    }
                    if (!isCurrentUser) {
                        Button(
                            onClick = onRsvp,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (event.hasRsvped) HuabuSurface else HuabuAccentCyan
                            ),
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (event.hasRsvped) "✓ Going" else "RSVP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (event.hasRsvped) HuabuSilver else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEventDialog(
    onAdd: (ProfileEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location    by remember { mutableStateOf("") }
    var isOnline    by remember { mutableStateOf(false) }
    var eventUrl    by remember { mutableStateOf("") }
    var titleError  by remember { mutableStateOf(false) }
    var dateError   by remember { mutableStateOf(false) }

    // Date/time state
    var day   by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year  by remember { mutableStateOf("") }
    var hour  by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = {
            Text("📅 Create Event", color = HuabuGold, fontWeight = FontWeight.ExtraBold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; titleError = false },
                        label = { Text("Event title *", color = HuabuSilver) },
                        isError = titleError,
                        singleLine = true,
                        colors = dialogFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (titleError) Text("Title is required", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description", color = HuabuSilver) },
                        singleLine = false,
                        maxLines = 3,
                        colors = dialogFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Date & Time *", color = HuabuSilver, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = day, onValueChange = { if (it.length <= 2) day = it },
                            label = { Text("DD", color = HuabuSilver, fontSize = 11.sp) },
                            isError = dateError, singleLine = true,
                            colors = dialogFieldColors(), shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = month, onValueChange = { if (it.length <= 2) month = it },
                            label = { Text("MM", color = HuabuSilver, fontSize = 11.sp) },
                            isError = dateError, singleLine = true,
                            colors = dialogFieldColors(), shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = year, onValueChange = { if (it.length <= 4) year = it },
                            label = { Text("YYYY", color = HuabuSilver, fontSize = 11.sp) },
                            isError = dateError, singleLine = true,
                            colors = dialogFieldColors(), shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(
                            value = hour, onValueChange = { if (it.length <= 2) hour = it },
                            label = { Text("HH", color = HuabuSilver, fontSize = 11.sp) },
                            isError = dateError, singleLine = true,
                            colors = dialogFieldColors(), shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minute, onValueChange = { if (it.length <= 2) minute = it },
                            label = { Text("MM", color = HuabuSilver, fontSize = 11.sp) },
                            isError = dateError, singleLine = true,
                            colors = dialogFieldColors(), shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (dateError) Text("Enter a valid date/time", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Online event?", color = HuabuSilver, fontSize = 13.sp)
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { isOnline = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = HuabuViolet, checkedThumbColor = Color.White)
                        )
                    }
                }
                if (!isOnline) {
                    item {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location", color = HuabuSilver) },
                            leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = HuabuViolet, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            colors = dialogFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = eventUrl,
                            onValueChange = { eventUrl = it },
                            label = { Text("Event link", color = HuabuSilver) },
                            leadingIcon = { Icon(Icons.Filled.Link, null, tint = HuabuViolet, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            colors = dialogFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    val eventDate = try {
                        val d = day.toInt(); val m = month.toInt() - 1; val y = year.toInt()
                        val h = if (hour.isBlank()) 0 else hour.toInt()
                        val min = if (minute.isBlank()) 0 else minute.toInt()
                        val cal = Calendar.getInstance()
                        cal.set(y, m, d, h, min, 0)
                        cal.timeInMillis
                    } catch (e: Exception) { -1L }
                    if (eventDate < 0L) { dateError = true; return@Button }
                    onAdd(
                        ProfileEvent(
                            id = UUID.randomUUID().toString(),
                            userId = "",
                            title = title.trim(),
                            description = description.trim(),
                            location = if (isOnline) "" else location.trim(),
                            eventDate = eventDate,
                            isOnline = isOnline,
                            eventUrl = if (isOnline) eventUrl.trim() else ""
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = HuabuAccentCyan),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Create Event", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) }
        }
    )
}

@Composable
private fun dialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = HuabuViolet,
    unfocusedBorderColor = HuabuDivider,
    focusedTextColor = HuabuOnSurface,
    unfocusedTextColor = HuabuOnSurface,
    cursorColor = HuabuViolet
)
