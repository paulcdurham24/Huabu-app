package com.huabu.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.huabu.app.ui.screens.profile.ProfileThemeEditor
import com.huabu.app.ui.screens.profile.EditProfileScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.huabu.app.ui.screens.profile.ProfileViewModel
import com.huabu.app.ui.screens.search.SearchScreen
import com.huabu.app.ui.screens.splash.SplashScreen
import com.huabu.app.ui.screens.compose.ComposePostScreen
import com.huabu.app.ui.screens.settings.SettingsScreen
import com.huabu.app.ui.screens.notifications.NotificationsScreen
import com.huabu.app.ui.screens.messages.ChatScreen
import com.huabu.app.ui.screens.auth.LoginScreen
import com.huabu.app.ui.screens.auth.SignupScreen
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
    object ThemeEditor : Screen("theme_editor/{userId}") {
        fun createRoute(userId: String) = "theme_editor/$userId"
    }
    object EditProfile : Screen("edit_profile/{userId}") {
        fun createRoute(userId: String) = "edit_profile/$userId"
    }
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    object Login : Screen("login")
    object Signup : Screen("signup")
}

val bottomNavScreens = listOf(Screen.Feed, Screen.Friends, Screen.Messages, Screen.Search)

@Composable
fun HuabuNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomRoutes = setOf(Screen.Feed.route, Screen.Friends.route, Screen.Messages.route, Screen.Search.route)
    val showBottomBar = currentRoute in bottomRoutes || currentRoute?.startsWith("profile/") == true
    val hideBottomBar = currentRoute?.startsWith("theme_editor/") == true
        || currentRoute?.startsWith("edit_profile/") == true
        || currentRoute?.startsWith("chat/") == true
        || currentRoute == Screen.Login.route
        || currentRoute == Screen.Signup.route
        || currentRoute == Screen.Splash.route

    Scaffold(
        bottomBar = {
            if (showBottomBar && !hideBottomBar) {
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
                SplashScreen(onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
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
                    },
                    onNavigateToThemeEditor = { uid ->
                        navController.navigate(Screen.ThemeEditor.createRoute(uid))
                    },
                    onNavigateToEditProfile = { uid ->
                        navController.navigate(Screen.EditProfile.createRoute(uid))
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
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
                    },
                    onNavigateToChat = { conversationId ->
                        navController.navigate(Screen.Chat.createRoute(conversationId))
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
            composable(
                route = Screen.EditProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "me"
                val viewModel: ProfileViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                androidx.compose.runtime.LaunchedEffect(userId) { viewModel.loadProfile(userId) }
                val user = uiState.user
                if (user != null) {
                    EditProfileScreen(
                        user = user,
                        onSave = { updated ->
                            viewModel.saveUser(updated)
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(
                route = Screen.ThemeEditor.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "me"
                val viewModel: ProfileViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                androidx.compose.runtime.LaunchedEffect(userId) { viewModel.loadProfile(userId) }
                ProfileThemeEditor(
                    initialTheme = uiState.theme,
                    displayName = uiState.user?.displayName ?: "Your Name",
                    username = uiState.user?.username ?: "you",
                    onSave = { theme ->
                        viewModel.saveTheme(theme)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ComposePost.route) {
                ComposePostScreen(
                    onPostSubmitted = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        // Navigate to splash/login after logout
                        navController.navigate(Screen.Splash.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToPrivacy = { /* TODO: Privacy screen */ },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
                )
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            }
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                ChatScreen(
                    conversationId = conversationId,
                    onBack = { navController.popBackStack() },
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignup = {
                        navController.navigate(Screen.Signup.route)
                    }
                )
            }
            composable(Screen.Signup.route) {
                SignupScreen(
                    onSignupSuccess = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
