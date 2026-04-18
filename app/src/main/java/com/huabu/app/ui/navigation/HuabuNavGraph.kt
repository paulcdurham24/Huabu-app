package com.huabu.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.huabu.app.ui.screens.feed.FeedScreen
import com.huabu.app.ui.screens.friends.FriendsScreen
import com.huabu.app.ui.screens.messages.MessagesScreen
import com.huabu.app.ui.screens.profile.ProfileScreen
import com.huabu.app.ui.screens.search.SearchScreen
import com.huabu.app.ui.screens.splash.SplashScreen
import com.huabu.app.ui.screens.compose.ComposePostScreen
import com.huabu.app.ui.components.HuabuBottomNav

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Feed : Screen("feed")
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object Friends : Screen("friends")
    object Messages : Screen("messages")
    object Search : Screen("search")
    object ComposePost : Screen("compose_post")
}

val bottomNavScreens = listOf(Screen.Feed, Screen.Friends, Screen.Messages, Screen.Search)

@Composable
fun HuabuNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomRoutes = setOf(Screen.Feed.route, Screen.Friends.route, Screen.Messages.route, Screen.Search.route)
    val showBottomBar = currentRoute in bottomRoutes || currentRoute?.startsWith("profile/") == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                HuabuBottomNav(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onNavigateToFeed = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Feed.route) {
                FeedScreen(
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    },
                    onNavigateToCompose = {
                        navController.navigate(Screen.ComposePost.route)
                    }
                )
            }
            composable(
                route = Screen.Profile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "me"
                ProfileScreen(
                    userId = userId,
                    onNavigateToFriends = { navController.navigate(Screen.Friends.route) },
                    onNavigateToMessages = { navController.navigate(Screen.Messages.route) },
                    onNavigateToProfile = { uid ->
                        navController.navigate(Screen.Profile.createRoute(uid))
                    }
                )
            }
            composable(Screen.Friends.route) {
                FriendsScreen(
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            }
            composable(Screen.Messages.route) {
                MessagesScreen(
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            }
            composable(Screen.ComposePost.route) {
                ComposePostScreen(
                    onPostSubmitted = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
