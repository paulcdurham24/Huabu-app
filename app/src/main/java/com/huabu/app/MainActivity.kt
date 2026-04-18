package com.huabu.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.huabu.app.ui.navigation.HuabuNavGraph
import com.huabu.app.ui.theme.HuabuDarkBg
import com.huabu.app.ui.theme.HuabuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)
        setContent {
            HuabuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HuabuDarkBg
                ) {
                    HuabuNavGraph()
                }
            }
        }
    }
}
