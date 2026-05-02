package com.huabu.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.huabu.app.ui.screens.auth.AuthViewModel
import com.huabu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToTheme: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var passwordSnackbar by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(passwordSnackbar) {
        passwordSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            passwordSnackbar = null
        }
    }

    // Handle logout
    fun handleLogout() {
        viewModel.logout()
        onLogout()
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { newPw ->
                viewModel.changePassword(newPw) { result ->
                    showChangePasswordDialog = false
                    passwordSnackbar = if (result.isSuccess) "Password updated successfully"
                    else "Failed: ${result.exceptionOrNull()?.message}"
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = HuabuCardBg,
            title = { Text("Delete Account", color = Color(0xFFE74C3C), fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete your account and all your data. This cannot be undone.", color = HuabuSilver) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount { result ->
                            showDeleteAccountDialog = false
                            if (result.isSuccess) onLogout()
                            else passwordSnackbar = "Failed: ${result.exceptionOrNull()?.message}"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                ) { Text("Delete Forever") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = HuabuSilver)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "★ Settings ★",
                        style = androidx.compose.ui.text.TextStyle(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(HuabuHotPink, HuabuGold)
                            ),
                            fontSize = 22.sp,
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
            // Account Section
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Filled.Person,
                    title = "Edit Profile",
                    subtitle = "Change your display name, bio, and more",
                    onClick = onNavigateToEditProfile
                )
                SettingsItem(
                    icon = Icons.Filled.Palette,
                    title = "Theme & Appearance",
                    subtitle = "Customize your profile colors",
                    onClick = onNavigateToTheme
                )
                SettingsItem(
                    icon = Icons.Filled.Lock,
                    title = "Privacy & Safety",
                    subtitle = "Control who can see your content",
                    onClick = onNavigateToPrivacy
                )
            }

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notification Preferences",
                    subtitle = "Manage push and in-app notifications",
                    onClick = onNavigateToNotifications
                )
                SettingsToggleItem(
                    icon = Icons.Filled.Email,
                    title = "Email Notifications",
                    checked = true,
                    onCheckedChange = { }
                )
            }

            // Content Section
            SettingsSection(title = "Content & Display") {
                SettingsToggleItem(
                    icon = Icons.Filled.DarkMode,
                    title = "Dark Mode",
                    checked = true,
                    onCheckedChange = { }
                )
                SettingsToggleItem(
                    icon = Icons.Filled.VolumeUp,
                    title = "Auto-play Sounds",
                    checked = false,
                    onCheckedChange = { }
                )
            }

            // Security Section
            SettingsSection(title = "Security") {
                SettingsItem(
                    icon = Icons.Filled.Password,
                    title = "Change Password",
                    subtitle = "Update your account password",
                    onClick = { showChangePasswordDialog = true }
                )
                SettingsItem(
                    icon = Icons.Filled.DeleteForever,
                    title = "Delete Account",
                    subtitle = "Permanently remove your account and data",
                    onClick = { showDeleteAccountDialog = true },
                    tint = Color(0xFFE74C3C)
                )
            }

            // Support Section
            SettingsSection(title = "Support") {
                SettingsItem(
                    icon = Icons.Filled.Help,
                    title = "Help Center",
                    subtitle = "FAQs and troubleshooting",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Filled.Report,
                    title = "Report a Problem",
                    subtitle = "Let us know if something's wrong",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About Huabu",
                    subtitle = "Version 1.0.0 (Debug)",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE74C3C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = HuabuCardBg,
            title = { Text("Log Out?", color = HuabuOnSurface, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out?", color = HuabuSilver) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        handleLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE74C3C),
                        contentColor = Color.White
                    )
                ) { Text("Log Out", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = HuabuSilver)
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
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
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    tint: Color = HuabuHotPink,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = HuabuOnSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = HuabuSilver,
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = HuabuSilver,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HuabuCardBg,
        title = { Text("Change Password", color = HuabuOnSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = { Text("New password", color = HuabuSilver) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuabuHotPink,
                        unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface,
                        unfocusedTextColor = HuabuOnSurface
                    )
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Confirm password", color = HuabuSilver) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HuabuHotPink,
                        unfocusedBorderColor = HuabuDivider,
                        focusedTextColor = HuabuOnSurface,
                        unfocusedTextColor = HuabuOnSurface
                    )
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        newPassword.length < 6 -> error = "Password must be at least 6 characters"
                        newPassword != confirmPassword -> error = "Passwords do not match"
                        else -> onConfirm(newPassword)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HuabuHotPink)
            ) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = HuabuSilver) }
        }
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HuabuHotPink,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = HuabuOnSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = HuabuHotPink,
                checkedTrackColor = HuabuHotPink.copy(alpha = 0.5f)
            )
        )
    }
}
