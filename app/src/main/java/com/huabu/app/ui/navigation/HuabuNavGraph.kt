package com.huabu.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import javax.inject.Inject
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
import com.huabu.app.ui.screens.auth.AuthViewModel
import com.huabu.app.ui.screens.privacy.PrivacyScreen
import com.huabu.app.ui.screens.post.PostDetailScreen
import androidx.compose.runtime.CompositionLocalProvider
import com.huabu.app.ui.screens.profile.LocalProfileTheme
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.local.dao.ProfileThemeDao
import com.huabu.app.data.model.ProfileTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

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
    object DirectChat : Screen("direct_chat/{userId}") {
        fun createRoute(userId: String) = "direct_chat/$userId"
    }
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Privacy : Screen("privacy")
    object PostDetail : Screen("post/{postId}") {
        fun createRoute(postId: String) = "post/$postId"
    }
    object EditPost : Screen("edit_post/{postId}") {
        fun createRoute(postId: String) = "edit_post/$postId"
    }
}

val bottomNavScreens = listOf(Screen.Feed, Screen.Friends, Screen.Messages, Screen.Search)

@HiltViewModel
class NavViewModel @Inject constructor(
    val firebaseService: FirebaseService,
    val authService: AuthService,
    private val profileThemeDao: ProfileThemeDao
) : ViewModel() {
    fun themeFlow(uid: String) = profileThemeDao.getThemeForUser(uid).catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

@Composable
fun HuabuNavGraph(initialRoute: String? = null) {
    val vm: NavViewModel = hiltViewModel()
    val firebaseService = vm.firebaseService
    val authService = vm.authService
    val currentUserId = authService.getCurrentUserId() ?: ""
    var unreadNotifCount by remember { mutableIntStateOf(0) }
    var unreadMsgCount  by remember { mutableIntStateOf(0) }
    val userTheme by (if (currentUserId.isNotEmpty()) vm.themeFlow(currentUserId) else kotlinx.coroutines.flow.MutableStateFlow(null))
        .collectAsStateWithLifecycle()

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            firebaseService.getUnreadNotificationsCountFlow(currentUserId)
                .catch { }
                .collect { unreadNotifCount = it }
        }
    }
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            firebaseService.getUnreadMessagesCountFlow(currentUserId)
                .catch { }
                .collect { unreadMsgCount = it }
        }
    }
    val navController = rememberNavController()
    LaunchedEffect(initialRoute) {
        if (!initialRoute.isNullOrEmpty()) {
            navController.navigate(initialRoute) { launchSingleTop = true }
        }
    }
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
                    currentRoute = currentRoute,
                    unreadNotifCount = unreadNotifCount,
                    unreadMsgCount = unreadMsgCount,
                    theme = userTheme
                )
            }
        }
    ) { innerPadding ->
        CompositionLocalProvider(LocalProfileTheme provides (userTheme ?: com.huabu.app.data.model.ProfileTheme(""))) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToFeed = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Feed.route) {
                FeedScreen(
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    },
                    onNavigateToCompose = {
                        navController.navigate(Screen.ComposePost.route)
                    },
                    onNavigateToPost = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    },
                    onNavigateToEditPost = { postId ->
                        navController.navigate(Screen.EditPost.createRoute(postId))
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
                    onNavigateToMessages = { otherUserId ->
                        navController.navigate(Screen.DirectChat.createRoute(otherUserId))
                    },
                    onNavigateToProfile = { uid ->
                        navController.navigate(Screen.Profile.createRoute(uid))
                    },
                    onNavigateToThemeEditor = { uid ->
                        navController.navigate(Screen.ThemeEditor.createRoute(uid))
                    },
                    onNavigateToEditProfile = { uid ->
                        navController.navigate(Screen.EditProfile.createRoute(uid))
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToPost = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    },
                    onNavigateToEditPost = { postId ->
                        navController.navigate(Screen.EditPost.createRoute(postId))
                    }
                )
            }
            composable(Screen.Friends.route) {
                FriendsScreen(
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    },
                    onNavigateToChat = { conversationId ->
                        navController.navigate(Screen.Chat.createRoute(conversationId))
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
                    },
                    onNavigateToChat = { conversationId ->
                        navController.navigate(Screen.Chat.createRoute(conversationId))
                    },
                    onNavigateToPost = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
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
                val resolvedUserId = uiState.user?.id ?: userId
                androidx.compose.runtime.LaunchedEffect(userId) { viewModel.loadProfile(userId) }
                ProfileThemeEditor(
                    initialTheme = uiState.theme,
                    displayName = uiState.user?.displayName ?: "Your Name",
                    username = uiState.user?.username ?: "you",
                    onSave = { theme ->
                        viewModel.saveTheme(theme.copy(userId = resolvedUserId))
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
                val authViewModel: AuthViewModel = hiltViewModel()
                val currentUserId = authViewModel.authState.collectAsStateWithLifecycle().value
                    .let { it as? com.huabu.app.data.firebase.AuthState.Authenticated }?.userId ?: ""
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Splash.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToPrivacy = { navController.navigate(Screen.Privacy.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToEditProfile = {
                        if (currentUserId.isNotEmpty())
                            navController.navigate(Screen.EditProfile.createRoute(currentUserId))
                    },
                    onNavigateToTheme = {
                        if (currentUserId.isNotEmpty())
                            navController.navigate(Screen.ThemeEditor.createRoute(currentUserId))
                    }
                )
            }
            composable(Screen.Privacy.route) {
                PrivacyScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    },
                    onNavigateToPost = { postId ->
                        navController.navigate(Screen.PostDetail.createRoute(postId))
                    },
                    onNavigateToChat = { conversationId ->
                        navController.navigate(Screen.Chat.createRoute(conversationId))
                    }
                )
            }
            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                PostDetailScreen(
                    postId = postId,
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
            composable(
                route = Screen.DirectChat.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val otherUserId = backStackEntry.arguments?.getString("userId") ?: ""
                val currentUid = authService.getCurrentUserId() ?: ""
                var convoId by remember { mutableStateOf<String?>(null) }
                var error by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(otherUserId) {
                    if (currentUid.isNotEmpty() && otherUserId.isNotEmpty()) {
                        val result = firebaseService.getOrCreateConversation(currentUid, otherUserId)
                        result.onSuccess { convoId = it }
                            .onFailure { error = it.message }
                    }
                }
                when {
                    convoId != null -> ChatScreen(
                        conversationId = convoId!!,
                        onBack = { navController.popBackStack() },
                        onNavigateToProfile = { uid -> navController.navigate(Screen.Profile.createRoute(uid)) }
                    )
                    error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    }
                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
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
            composable(
                route = Screen.EditPost.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                ComposePostScreen(
                    editPostId = postId,
                    onPostSubmitted = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        } // end CompositionLocalProvider
    }
}
