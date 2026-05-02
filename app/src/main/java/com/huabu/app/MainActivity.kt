package com.huabu.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.ui.navigation.HuabuNavGraph
import com.huabu.app.ui.theme.HuabuDarkBg
import com.huabu.app.ui.theme.HuabuTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var firebaseService: FirebaseService
    @Inject lateinit var authService: AuthService

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — no-op, best effort */ }

    override fun onResume() {
        super.onResume()
        val uid = authService.getCurrentUserId() ?: return
        lifecycleScope.launch { firebaseService.setOnlinePresence(uid, true) }
    }

    override fun onStop() {
        super.onStop()
        val uid = authService.getCurrentUserId() ?: return
        lifecycleScope.launch { firebaseService.setOnlinePresence(uid, false) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)
        val notifType = intent.getStringExtra("notification_type") ?: ""
        val notifTargetId = intent.getStringExtra("notification_target_id") ?: ""
        val initialRoute: String? = when {
            notifType == "message" && notifTargetId.isNotEmpty() -> "chat/$notifTargetId"
            notifType == "post" && notifTargetId.isNotEmpty() -> "post/$notifTargetId"
            notifType == "profile" && notifTargetId.isNotEmpty() -> "profile/$notifTargetId"
            notifType == "like" && notifTargetId.isNotEmpty() -> "post/$notifTargetId"
            notifType == "comment" && notifTargetId.isNotEmpty() -> "post/$notifTargetId"
            notifType == "follow" || notifType == "friend_request" -> "notifications"
            else -> null
        }

        setContent {
            HuabuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HuabuDarkBg
                ) {
                    HuabuNavGraph(initialRoute = initialRoute)
                }
            }
        }
    }
}
