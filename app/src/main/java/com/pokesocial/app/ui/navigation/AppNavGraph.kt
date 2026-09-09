package com.pokesocial.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pokesocial.app.PokeSocialApp
import com.pokesocial.app.domain.model.Story
import com.pokesocial.app.ui.AppViewModelFactory
import com.pokesocial.app.ui.chat.ChatThreadScreen
import com.pokesocial.app.ui.chat.ChatThreadViewModel
import com.pokesocial.app.ui.chat.InboxScreen
import com.pokesocial.app.ui.chat.InboxViewModel
import com.pokesocial.app.ui.comments.CommentsScreen
import com.pokesocial.app.ui.comments.CommentsViewModel
import com.pokesocial.app.ui.components.BottomNavItem
import com.pokesocial.app.ui.components.IgBottomBar
import com.pokesocial.app.ui.explore.ExploreScreen
import com.pokesocial.app.ui.explore.ExploreViewModel
import com.pokesocial.app.ui.feed.FeedScreen
import com.pokesocial.app.ui.feed.FeedViewModel
import com.pokesocial.app.ui.profile.ProfileScreen
import com.pokesocial.app.ui.profile.ProfileViewModel
import com.pokesocial.app.ui.reels.ReelsScreen
import com.pokesocial.app.ui.reels.ReelsViewModel
import com.pokesocial.app.ui.splash.SplashScreen
import com.pokesocial.app.ui.splash.SplashViewModel
import com.pokesocial.app.ui.stories.StoryViewerScreen

object Routes {
    const val SPLASH = "splash"
    const val FEED = "feed"
    const val EXPLORE = "explore"
    const val REELS = "reels"
    const val INBOX = "inbox"
    const val PROFILE = "profile"
    const val COMMENTS = "comments/{postId}"
    const val CHAT = "chat/{conversationId}"
    const val STORY = "story/{storyId}"

    fun comments(postId: String) = "comments/$postId"
    fun chat(id: String) = "chat/$id"
    fun story(id: String) = "story/$id"
}

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val app = remember { context.applicationContext as PokeSocialApp }
    val baseFactory = remember { AppViewModelFactory(app) }
    val navController = rememberNavController()
    var storyCache by remember { mutableStateOf<List<Story>>(emptyList()) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf(
        Routes.FEED, Routes.EXPLORE, Routes.REELS, Routes.INBOX, Routes.PROFILE
    )

    val bottomItems = listOf(
        BottomNavItem(Routes.FEED, "Home", Icons.Outlined.Home, Icons.Filled.Home),
        BottomNavItem(Routes.EXPLORE, "Search", Icons.Outlined.Search, Icons.Filled.Search),
        BottomNavItem(Routes.REELS, "Reels", Icons.Outlined.PlayArrow, Icons.Filled.PlayArrow),
        BottomNavItem(Routes.INBOX, "Chat", Icons.AutoMirrored.Outlined.Send, Icons.AutoMirrored.Filled.Send),
        BottomNavItem(Routes.PROFILE, "Profile", Icons.Outlined.Person, Icons.Filled.Person)
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                IgBottomBar(
                    items = bottomItems,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.SPLASH) {
                val vm: SplashViewModel = viewModel(factory = baseFactory)
                SplashScreen(vm) {
                    navController.navigate(Routes.FEED) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            }
            composable(Routes.FEED) {
                val vm: FeedViewModel = viewModel(factory = baseFactory)
                val feedUi by vm.ui.collectAsStateWithLifecycle()
                LaunchedEffect(feedUi.stories) { storyCache = feedUi.stories }
                FeedScreen(
                    viewModel = vm,
                    onOpenComments = { navController.navigate(Routes.comments(it)) },
                    onOpenStory = { navController.navigate(Routes.story(it)) },
                    onOpenMessages = {
                        navController.navigate(Routes.INBOX) { launchSingleTop = true }
                    }
                )
            }
            composable(Routes.EXPLORE) {
                val vm: ExploreViewModel = viewModel(factory = baseFactory)
                ExploreScreen(vm) { postId ->
                    navController.navigate(Routes.comments(postId))
                }
            }
            composable(Routes.REELS) {
                val vm: ReelsViewModel = viewModel(factory = baseFactory)
                ReelsScreen(vm) { navController.navigate(Routes.comments(it)) }
            }
            composable(Routes.INBOX) {
                val vm: InboxViewModel = viewModel(factory = baseFactory)
                InboxScreen(vm) { id -> navController.navigate(Routes.chat(id)) }
            }
            composable(Routes.PROFILE) {
                val vm: ProfileViewModel = viewModel(factory = baseFactory)
                ProfileScreen(vm)
            }
            composable(
                Routes.COMMENTS,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { entry ->
                val postId = entry.arguments?.getString("postId").orEmpty()
                val vm: CommentsViewModel = viewModel(
                    key = "comments-$postId",
                    factory = AppViewModelFactory(app, postId = postId)
                )
                CommentsScreen(vm) { navController.popBackStack() }
            }
            composable(
                Routes.CHAT,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("conversationId").orEmpty()
                val inboxVm: InboxViewModel = viewModel(factory = baseFactory)
                val convs by inboxVm.conversations.collectAsStateWithLifecycle()
                val title = convs.find { it.id == id }?.peer?.username ?: "Chat"
                val vm: ChatThreadViewModel = viewModel(
                    key = "chat-$id",
                    factory = AppViewModelFactory(app, conversationId = id)
                )
                ChatThreadScreen(title, vm) { navController.popBackStack() }
            }
            composable(
                Routes.STORY,
                arguments = listOf(navArgument("storyId") { type = NavType.StringType })
            ) { entry ->
                val storyId = entry.arguments?.getString("storyId").orEmpty()
                StoryViewerScreen(
                    stories = storyCache,
                    startId = storyId,
                    repo = app.socialRepository,
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}
