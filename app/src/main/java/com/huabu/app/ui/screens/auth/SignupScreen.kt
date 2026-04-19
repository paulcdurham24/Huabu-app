package com.huabu.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huabu.app.ui.theme.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HuabuDarkBg)
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            HuabuDeepPurple.copy(alpha = 0.3f),
                            HuabuDarkBg,
                            HuabuDeepPurple.copy(alpha = 0.1f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Title
            Text(
                text = "★ Huabu ★",
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(
                        listOf(HuabuHotPink, HuabuGold, HuabuElectricBlue)
                    ),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create your space on Huabu",
                color = HuabuSilver,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Signup Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = HuabuCardBg),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        color = HuabuGold,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Join the community",
                        color = HuabuSilver,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Display Name field
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it; errorMessage = null },
                        label = { Text("Display Name", color = HuabuSilver) },
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = HuabuHotPink)
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuabuHotPink,
                            unfocusedBorderColor = HuabuDivider,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuHotPink
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Username field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        label = { Text("Username", color = HuabuSilver) },
                        leadingIcon = {
                            Icon(Icons.Filled.AlternateEmail, contentDescription = null, tint = HuabuHotPink)
                        },
                        prefix = { Text("@", color = HuabuSilver) },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuabuHotPink,
                            unfocusedBorderColor = HuabuDivider,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuHotPink
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email", color = HuabuSilver) },
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = HuabuHotPink)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuabuHotPink,
                            unfocusedBorderColor = HuabuDivider,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuHotPink
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password", color = HuabuSilver) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = HuabuHotPink)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = HuabuSilver
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuabuHotPink,
                            unfocusedBorderColor = HuabuDivider,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuHotPink
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = { Text("Confirm Password", color = HuabuSilver) },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = HuabuHotPink)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HuabuHotPink,
                            unfocusedBorderColor = HuabuDivider,
                            focusedTextColor = HuabuOnSurface,
                            unfocusedTextColor = HuabuOnSurface,
                            cursorColor = HuabuHotPink
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Signup Button
                    Button(
                        onClick = {
                            when {
                                displayName.isBlank() -> errorMessage = "Please enter your display name"
                                username.isBlank() -> errorMessage = "Please enter a username"
                                email.isBlank() -> errorMessage = "Please enter your email"
                                password.isBlank() -> errorMessage = "Please enter a password"
                                password != confirmPassword -> errorMessage = "Passwords do not match"
                                password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                                else -> {
                                    // DEMO MODE: Simulate signup
                                    isLoading = true
                                    MainScope().launch {
                                        delay(1500)
                                        isLoading = false
                                        onSignupSuccess()
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HuabuNeonGreen)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = HuabuDivider)
                        Text(
                            "  OR  ",
                            color = HuabuSilver,
                            fontSize = 12.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = HuabuDivider)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login link
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already have an account? ", color = HuabuSilver)
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Sign In", color = HuabuElectricBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
