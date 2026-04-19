package com.huabu.app.ui.screens.profile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.huabu.app.data.model.User
import com.huabu.app.ui.components.ProfileImagePicker
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    onSave: (User) -> Unit,
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var displayName     by remember { mutableStateOf(user.displayName) }
    var username        by remember { mutableStateOf(user.username) }
    var bio             by remember { mutableStateOf(user.bio) }
    var location        by remember { mutableStateOf(user.location) }
    var website         by remember { mutableStateOf(user.website) }
    var mood            by remember { mutableStateOf(user.mood) }
    var aboutMe         by remember { mutableStateOf(user.aboutMe) }
    var heroesSection   by remember { mutableStateOf(user.heroesSection) }
    var interests       by remember { mutableStateOf(user.interests) }
    var profileSong     by remember { mutableStateOf(user.profileSong) }
    var profileSongArtist by remember { mutableStateOf(user.profileSongArtist) }

    var nameError by remember { mutableStateOf(false) }

    // Track avatar URL from ViewModel updates
    var currentAvatarUrl by remember { mutableStateOf(user.profileImageUrl) }

    // Update avatar URL when ViewModel updates
    LaunchedEffect(uiState.user) {
        uiState.user?.let {
            currentAvatarUrl = it.profileImageUrl
        }
    }

    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.onSaveComplete()
            onSave(uiState.user ?: user)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = HuabuGold, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = HuabuSilver)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (displayName.isBlank()) { nameError = true; return@Button }
                            viewModel.saveProfile(
                                user.id,
                                mapOf(
                                    "displayName" to displayName.trim(),
                                    "username" to username.trim().lowercase().replace(" ", "_"),
                                    "bio" to bio.trim(),
                                    "location" to location.trim(),
                                    "website" to website.trim(),
                                    "mood" to mood.trim(),
                                    "aboutMe" to aboutMe.trim(),
                                    "heroesSection" to heroesSection.trim(),
                                    "interests" to interests.trim(),
                                    "profileSong" to profileSong.trim(),
                                    "profileSongArtist" to profileSongArtist.trim()
                                )
                            )
                        },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = HuabuViolet),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HuabuDarkBg)
            )
        },
        containerColor = HuabuDarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar with Firebase Storage upload
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileImagePicker(
                        currentImageUrl = currentAvatarUrl.takeIf { it.isNotEmpty() },
                        onImageSelected = { uri ->
                            viewModel.updateAvatar(user.id, uri)
                        },
                        size = 120
                    )
                }

                // Upload progress indicator
                if (uiState.avatarUploadProgress in 0.01f..0.99f) {
                    LinearProgressIndicator(
                        progress = { uiState.avatarUploadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 64.dp),
                        color = HuabuHotPink
                    )
                }
            }

            // Basic Info
            item { EditSection("👤 Basic Info") }
            item {
                EditField(
                    label = "Display Name",
                    value = displayName,
                    onValueChange = { displayName = it; nameError = false },
                    isError = nameError,
                    errorMsg = "Name can't be empty",
                    icon = Icons.Filled.Person
                )
            }
            item {
                EditField(
                    label = "Username",
                    value = username,
                    onValueChange = { username = it },
                    icon = Icons.Filled.AlternateEmail,
                    prefix = "@"
                )
            }
            item {
                EditField(
                    label = "Bio",
                    value = bio,
                    onValueChange = { bio = it },
                    icon = Icons.Filled.Edit,
                    singleLine = false,
                    maxLines = 3,
                    maxChars = 150,
                    imeAction = ImeAction.Default
                )
            }

            // Location & Web
            item { EditSection("📍 Location & Web") }
            item {
                EditField(
                    label = "Location",
                    value = location,
                    onValueChange = { location = it },
                    icon = Icons.Filled.LocationOn
                )
            }
            item {
                EditField(
                    label = "Website",
                    value = website,
                    onValueChange = { website = it },
                    icon = Icons.Filled.Link,
                    keyboardType = KeyboardType.Uri
                )
            }

            // Status & Mood
            item { EditSection("😎 Status & Mood") }
            item {
                EditField(
                    label = "Current Mood / Status",
                    value = mood,
                    onValueChange = { mood = it },
                    icon = Icons.Filled.MusicNote,
                    hint = "e.g. 😎 chillin, 🎵 listening to music"
                )
            }

            // Profile Song
            item { EditSection("♪ Profile Song") }
            item {
                EditField(
                    label = "Song Title",
                    value = profileSong,
                    onValueChange = { profileSong = it },
                    icon = Icons.Filled.MusicNote
                )
            }
            item {
                EditField(
                    label = "Artist",
                    value = profileSongArtist,
                    onValueChange = { profileSongArtist = it },
                    icon = Icons.Filled.Person
                )
            }

            // About Me
            item { EditSection("✏️ About Me") }
            item {
                EditField(
                    label = "About Me",
                    value = aboutMe,
                    onValueChange = { aboutMe = it },
                    icon = Icons.Filled.Info,
                    singleLine = false,
                    maxLines = 6,
                    maxChars = 500,
                    imeAction = ImeAction.Default
                )
            }
            item {
                EditField(
                    label = "Who I'd Like to Meet",
                    value = heroesSection,
                    onValueChange = { heroesSection = it },
                    icon = Icons.Filled.Star,
                    singleLine = false,
                    maxLines = 4,
                    maxChars = 300,
                    imeAction = ImeAction.Default
                )
            }

            // Interests
            item { EditSection("⭐ Interests") }
            item {
                EditField(
                    label = "Interests (comma-separated)",
                    value = interests,
                    onValueChange = { interests = it },
                    icon = Icons.Filled.Favorite,
                    hint = "e.g. Music, Gaming, Art, Fashion",
                    singleLine = false,
                    maxLines = 3,
                    imeAction = ImeAction.Default
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun EditSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = HuabuGold,
        modifier = Modifier.padding(top = 4.dp)
    )
    HorizontalDivider(color = HuabuDivider, thickness = 1.dp)
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hint: String = "",
    prefix: String = "",
    isError: Boolean = false,
    errorMsg: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    maxChars: Int = 0,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (maxChars == 0 || it.length <= maxChars) onValueChange(it) },
            label = { Text(label, color = HuabuSilver) },
            placeholder = if (hint.isNotEmpty()) {{ Text(hint, color = HuabuDivider, fontSize = 13.sp) }} else null,
            leadingIcon = { Icon(icon, contentDescription = null, tint = HuabuViolet, modifier = Modifier.size(20.dp)) },
            prefix = if (prefix.isNotEmpty()) {{ Text(prefix, color = HuabuSilver) }} else null,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HuabuViolet,
                unfocusedBorderColor = HuabuDivider,
                focusedLabelColor = HuabuViolet,
                cursorColor = HuabuViolet,
                focusedTextColor = HuabuOnSurface,
                unfocusedTextColor = HuabuOnSurface,
                errorBorderColor = Color.Red,
                errorLabelColor = Color.Red
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            if (isError) {
                Text(errorMsg, color = Color.Red, style = MaterialTheme.typography.labelSmall)
            }
            if (maxChars > 0) {
                Spacer(Modifier.weight(1f))
                Text("${value.length}/$maxChars", color = HuabuSilver, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
