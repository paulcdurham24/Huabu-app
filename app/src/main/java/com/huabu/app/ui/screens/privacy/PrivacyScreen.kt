package com.huabu.app.ui.screens.privacy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    viewModel: PrivacyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "★ Privacy & Safety ★",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.linearGradient(listOf(HuabuAccentCyan, HuabuElectricBlue)),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = HuabuSilver)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Account Privacy
            PrivacySection(title = "Account Privacy") {
                PrivacyToggle(
                    icon = Icons.Filled.Lock,
                    title = "Private Account",
                    subtitle = "Only approved followers can see your posts",
                    checked = uiState.privateAccount,
                    onCheckedChange = { viewModel.setPrivateAccount(it) }
                )
                PrivacyToggle(
                    icon = Icons.Filled.Visibility,
                    title = "Show Online Status",
                    subtitle = "Let others see when you're active",
                    checked = uiState.showOnlineStatus,
                    onCheckedChange = { viewModel.setShowOnlineStatus(it) }
                )
                PrivacyToggle(
                    icon = Icons.Filled.Search,
                    title = "Appear in Search",
                    subtitle = "Allow others to find you by name or username",
                    checked = uiState.appearInSearch,
                    onCheckedChange = { viewModel.setAppearInSearch(it) }
                )
            }

            // Interactions
            PrivacySection(title = "Interactions") {
                PrivacyToggle(
                    icon = Icons.Filled.Message,
                    title = "Allow Messages from Anyone",
                    subtitle = "Receive DMs from people you don't follow",
                    checked = uiState.allowMessagesFromAnyone,
                    onCheckedChange = { viewModel.setAllowMessages(it) }
                )
                PrivacyToggle(
                    icon = Icons.Filled.PersonAdd,
                    title = "Allow Friend Requests",
                    subtitle = "Let anyone send you a friend request",
                    checked = uiState.allowFriendRequests,
                    onCheckedChange = { viewModel.setAllowFriendRequests(it) }
                )
                PrivacyToggle(
                    icon = Icons.Filled.Comment,
                    title = "Allow Comments",
                    subtitle = "Let others comment on your posts",
                    checked = uiState.allowComments,
                    onCheckedChange = { viewModel.setAllowComments(it) }
                )
            }

            // Content visibility
            PrivacySection(title = "Default Post Visibility") {
                val options = listOf("public" to "Public", "friends" to "Friends Only")
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (value == "public") Icons.Filled.Public else Icons.Filled.Group,
                            contentDescription = null,
                            tint = HuabuHotPink,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = label,
                            color = HuabuOnSurface,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        RadioButton(
                            selected = uiState.defaultVisibility == value,
                            onClick = { viewModel.setDefaultVisibility(value) },
                            colors = RadioButtonDefaults.colors(selectedColor = HuabuHotPink)
                        )
                    }
                }
            }

            // Data
            PrivacySection(title = "Data & Security") {
                PrivacyToggle(
                    icon = Icons.Filled.Analytics,
                    title = "Share Usage Data",
                    subtitle = "Help improve Huabu with anonymous analytics",
                    checked = uiState.shareUsageData,
                    onCheckedChange = { viewModel.setShareUsageData(it) }
                )
                PrivacyToggle(
                    icon = Icons.Filled.Notifications,
                    title = "Personalised Notifications",
                    subtitle = "Receive recommendations based on activity",
                    checked = uiState.personalisedNotifications,
                    onCheckedChange = { viewModel.setPersonalisedNotifications(it) }
                )
            }

            if (uiState.isSaving) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HuabuHotPink, modifier = Modifier.size(24.dp))
                }
            }

            if (uiState.savedMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HuabuNeonGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = uiState.savedMessage!!,
                        color = HuabuNeonGreen,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Privacy Policy link
            val context = LocalContext.current
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://phantasyfireapps.blogspot.com/2026/04/privacy-policy-for-huabu.html"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Policy, contentDescription = null, tint = HuabuSilver, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("View Privacy Policy", color = HuabuSilver, fontSize = 13.sp)
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = HuabuGold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HuabuCardBg)
                .padding(vertical = 8.dp)
        ) { content() }
    }
}

@Composable
private fun PrivacyToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = HuabuHotPink, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = HuabuOnSurface, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(subtitle, color = HuabuSilver, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = HuabuHotPink,
                checkedTrackColor = HuabuHotPink.copy(alpha = 0.4f)
            )
        )
    }
}
