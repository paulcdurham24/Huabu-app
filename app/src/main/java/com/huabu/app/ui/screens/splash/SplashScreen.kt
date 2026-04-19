package com.huabu.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.huabu.app.data.firebase.AuthState
import com.huabu.app.ui.components.GlitterCanvas
import com.huabu.app.ui.screens.auth.AuthViewModel
import com.huabu.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToFeed: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "alpha"
    )

    // Wait for splash animation then check auth state
    LaunchedEffect(Unit) {
        delay(1500)
        // Auth state will determine navigation
    }

    // Navigate based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onNavigateToFeed()
            is AuthState.Unauthenticated -> onNavigateToLogin()
            else -> { /* Loading - stay on splash */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(HuabuDeepPurple, HuabuDarkBg, HuabuCardBg),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        GlitterCanvas(modifier = Modifier.fillMaxSize(), sparkleCount = 15)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha)
        ) {
            Text(
                text = "✦",
                fontSize = 48.sp,
                color = HuabuGold,
                modifier = Modifier.scale(scale)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Huabu",
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(HuabuHotPink, HuabuGold, HuabuAccentCyan)
                    )
                ),
                modifier = Modifier.scale(scale)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "✦ Your World. Your Page. ✦",
                fontSize = 16.sp,
                color = HuabuAccentPink,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            Text(
                text = "be yourself. be loud. be Huabu.",
                fontSize = 13.sp,
                color = HuabuSilver,
                textAlign = TextAlign.Center
            )
        }
    }
}
