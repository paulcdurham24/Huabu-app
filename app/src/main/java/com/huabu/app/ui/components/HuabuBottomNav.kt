package com.huabu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.huabu.app.data.model.ProfileTheme
import com.huabu.app.ui.navigation.Screen
import com.huabu.app.ui.theme.*

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0
)

@Composable
fun HuabuBottomNav(
    navController: NavHostController,
    currentRoute: String?,
    unreadNotifCount: Int = 0,
    unreadMsgCount: Int = 0,
    theme: ProfileTheme? = null
) {
    val accentColor = remember(theme?.primaryColor) {
        theme?.primaryColor?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        } ?: HuabuHotPink
    }
    val navBgColor1 = remember(theme?.cardColor) {
        theme?.cardColor?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        } ?: HuabuDeepPurple
    }
    val navBgColor2 = remember(theme?.cardColor) {
        theme?.cardColor?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()?.copy(alpha = 0.85f)
        } ?: HuabuSurface
    }
    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home, Screen.Feed.route),
        NavItem("Friends", Icons.Filled.People, Screen.Friends.route),
        NavItem("Messages", Icons.Filled.Email, Screen.Messages.route, badgeCount = unreadMsgCount),
        NavItem("Search", Icons.Filled.Search, Screen.Search.route),
        NavItem("Me", Icons.Filled.AccountCircle, Screen.Profile.createRoute("me"), badgeCount = unreadNotifCount)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(navBgColor1, navBgColor2, navBgColor1)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(accentColor, accentColor.copy(alpha = 0.5f), accentColor)
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route ||
                    (item.route == "profile/me" && currentRoute == "profile/me")
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) accentColor else HuabuSilver,
                    animationSpec = tween(300),
                    label = "nav_color"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Feed.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (item.badgeCount > 0) {
                        BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = iconColor
                    )
                    if (isSelected) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(accentColor, accentColor.copy(alpha = 0.5f))
                                    ),
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}
