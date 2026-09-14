package com.pokesocial.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.domain.model.Highlight
import com.pokesocial.app.domain.model.Story
import com.pokesocial.app.domain.model.User
import com.pokesocial.app.ui.AppViewModelFactory
import com.pokesocial.app.ui.activity.ActivityScreen
import com.pokesocial.app.ui.activity.ActivityViewModel
import com.pokesocial.app.ui.chat.ChatThreadScreen
import com.pokesocial.app.ui.chat.ChatThreadViewModel
import com.pokesocial.app.ui.comments.CommentsScreen
import com.pokesocial.app.ui.comments.CommentsViewModel
import com.pokesocial.app.ui.components.BottomNavItem
import com.pokesocial.app.ui.components.IgBottomBar
import com.pokesocial.app.ui.explore.ExploreScreen
import com.pokesocial.app.ui.explore.ExploreViewModel
import com.pokesocial.app.ui.feed.FeedScreen
import com.pokesocial.app.ui.feed.FeedViewModel
import com.pokesocial.app.ui.post.CreatePostScreen
import com.pokesocial.app.ui.post.CreatePostViewModel
import com.pokesocial.app.ui.post.PostDetailScreen
import com.pokesocial.app.ui.profile.FollowListMode
import com.pokesocial.app.ui.profile.FollowListScreen
import com.pokesocial.app.ui.profile.HighlightViewerScreen
import com.pokesocial.app.ui.profile.ProfileScreen
import com.pokesocial.app.ui.profile.ProfileViewModel
import com.pokesocial.app.ui.reels.ReelsScreen
import com.pokesocial.app.ui.reels.ReelsViewModel
import com.pokesocial.app.ui.splash.SplashScreen
import com.pokesocial.app.ui.splash.SplashViewModel
import com.pokesocial.app.ui.stories.StoryViewerScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Routes {
    const val SPLASH = "splash"
    const val FEED = "feed"
    const val EXPLORE = "explore"
    const val REELS = "reels"
    const val PROFILE = "profile/{userId}"
    const val POST = "post/{postId}"
    const val FOLLOWS = "follows/{userId}/{mode}"
    const val COMMENTS = "comments/{postId}"
    const val CHAT = "chat/{conversationId}"
    const val STORY = "story/{storyId}"
    const val ACTIVITY = "activity"
    const val CREATE_POST = "create_post"
    const val HIGHLIGHT = "highlight/{highlightId}"
    const val LIKES = "likes/{postId}"

    fun profile(userId: String) = "profile/$userId"
    fun post(postId: String) = "post/$postId"
    fun follows(userId: String, mode: String) = "follows/$userId/$mode"
    fun comments(postId: String) = "comments/$postId"
    fun chat(id: String) = "chat/$id"
    fun story(id: String) = "story/$id"
    fun highlight(id: String) = "highlight/$id"
    fun likes(postId: String) = "likes/$postId"
}

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val app = remember { context.applicationContext as PokeSocialApp }
    val baseFactory = remember { AppViewModelFactory(app) }
    val navController = rememberNavController()
    var storyCache by remember { mutableStateOf<List<Story>>(emptyList()) }
    var highlightCache by remember { mutableStateOf<Highlight?>(null) }
    var inboxOpen by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUserId = navBackStackEntry?.arguments?.getString("userId")
    val showBottomBar = currentRoute in setOf(
        Routes.FEED, Routes.EXPLORE, Routes.REELS, Routes.PROFILE
    ) && !(currentRoute == Routes.FEED && inboxOpen)

    fun openProfile(userId: String) {
        navController.navigate(Routes.profile(userId)) {
            launchSingleTop = true
        }
    }

    val myProfileRoute = Routes.profile(AppConstants.ME_USER_ID)
    val bottomItems = listOf(
        BottomNavItem(Routes.FEED, "Home", Icons.Outlined.Home, Icons.Filled.Home),
        BottomNavItem(Routes.EXPLORE, "Search", Icons.Outlined.Search, Icons.Filled.Search),
        BottomNavItem(Routes.REELS, "Reels", Icons.Outlined.PlayArrow, Icons.Filled.PlayArrow),
        BottomNavItem(myProfileRoute, "Profile", Icons.Outlined.Person, Icons.Filled.Person)
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                IgBottomBar(
                    items = bottomItems,
                    currentRoute = when {
                        currentRoute == Routes.PROFILE &&
                            currentUserId == AppConstants.ME_USER_ID -> myProfileRoute
                        currentRoute == Routes.PROFILE -> null
                        else -> currentRoute
                    },
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
                // Refresh when returning from create post
                val refreshFlag by navController.currentBackStackEntry!!
                    .savedStateHandle
                    .getStateFlow("refresh_feed", false)
                    .collectAsStateWithLifecycle()
                LaunchedEffect(refreshFlag) {
                    if (refreshFlag) {
                        vm.refresh()
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("refresh_feed", false)
                    }
                }
                FeedScreen(
                    viewModel = vm,
                    inboxFactory = baseFactory,
                    onOpenComments = { navController.navigate(Routes.comments(it)) },
                    onOpenStory = { navController.navigate(Routes.story(it)) },
                    onOpenProfile = ::openProfile,
                    onOpenChat = { navController.navigate(Routes.chat(it)) },
                    onHashtagClick = { tag ->
                        navController.navigate(Routes.EXPLORE) {
                            launchSingleTop = true
                            restoreState = true
                        }
                        navController.getBackStackEntry(Routes.EXPLORE)
                            .savedStateHandle["hashtag"] = tag
                    },
                    onOpenActivity = { navController.navigate(Routes.ACTIVITY) },
                    onCreatePost = { navController.navigate(Routes.CREATE_POST) },
                    onOpenLikes = { navController.navigate(Routes.likes(it)) },
                    onInboxVisibilityChange = { inboxOpen = it }
                )
            }
            composable(Routes.ACTIVITY) {
                val vm: ActivityViewModel = viewModel(factory = baseFactory)
                ActivityScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onOpenProfile = ::openProfile,
                    onOpenPost = { navController.navigate(Routes.post(it)) }
                )
            }
            composable(Routes.CREATE_POST) {
                val vm: CreatePostViewModel = viewModel(factory = baseFactory)
                CreatePostScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onPublished = {
                        runCatching {
                            navController.getBackStackEntry(Routes.FEED)
                                .savedStateHandle["refresh_feed"] = true
                        }
                        navController.popBackStack(Routes.FEED, inclusive = false)
                    }
                )
            }
            composable(Routes.EXPLORE) { entry ->
                val vm: ExploreViewModel = viewModel(factory = baseFactory)
                val pendingTag by entry.savedStateHandle
                    .getStateFlow("hashtag", null as String?)
                    .collectAsStateWithLifecycle()
                LaunchedEffect(pendingTag) {
                    val tag = pendingTag
                    if (!tag.isNullOrBlank()) {
                        vm.openHashtag(tag)
                        entry.savedStateHandle["hashtag"] = null
                    }
                }
                ExploreScreen(
                    viewModel = vm,
                    onOpenPost = { postId -> navController.navigate(Routes.post(postId)) },
                    onOpenProfile = ::openProfile
                )
            }
            composable(Routes.REELS) {
                val vm: ReelsViewModel = viewModel(factory = baseFactory)
                ReelsScreen(
                    viewModel = vm,
                    onComment = { navController.navigate(Routes.comments(it)) },
                    onOpenProfile = ::openProfile,
                    onOpenLikes = { navController.navigate(Routes.likes(it)) }
                )
            }
            composable(
                Routes.POST,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { entry ->
                val postId = entry.arguments?.getString("postId").orEmpty()
                PostDetailScreen(
                    postId = postId,
                    repo = app.socialRepository,
                    commentsFactory = AppViewModelFactory(app, postId = postId),
                    onBack = { navController.popBackStack() },
                    onOpenProfile = ::openProfile,
                    onOpenLikes = { navController.navigate(Routes.likes(it)) },
                    onHashtagClick = { tag ->
                        navController.navigate(Routes.EXPLORE) {
                            launchSingleTop = true
                            restoreState = true
                        }
                        navController.getBackStackEntry(Routes.EXPLORE)
                            .savedStateHandle["hashtag"] = tag
                    }
                )
            }
            composable(
                Routes.LIKES,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { entry ->
                val postId = entry.arguments?.getString("postId").orEmpty()
                com.pokesocial.app.ui.likes.LikersScreen(
                    postId = postId,
                    repo = app.socialRepository,
                    onBack = { navController.popBackStack() },
                    onOpenProfile = ::openProfile
                )
            }
            composable(
                Routes.FOLLOWS,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("mode") { type = NavType.StringType }
                )
            ) { entry ->
                val userId = entry.arguments?.getString("userId").orEmpty()
                val mode = entry.arguments?.getString("mode").orEmpty()
                FollowListScreen(
                    userId = userId,
                    mode = if (mode == "following") FollowListMode.FOLLOWING else FollowListMode.FOLLOWERS,
                    repo = app.socialRepository,
                    onBack = { navController.popBackStack() },
                    onOpenProfile = ::openProfile
                )
            }
            composable(
                Routes.PROFILE,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { entry ->
                val userId = entry.arguments?.getString("userId").orEmpty()
                val vm: ProfileViewModel = viewModel(
                    key = "profile-$userId",
                    factory = AppViewModelFactory(app, userId = userId)
                )
                val canPop = navController.previousBackStackEntry != null
                ProfileScreen(
                    viewModel = vm,
                    onBack = if (userId != AppConstants.ME_USER_ID && canPop) {
                        { navController.popBackStack() }
                    } else null,
                    onMessage = if (userId != AppConstants.ME_USER_ID) {
                        {
                            vm.openChat { convId ->
                                navController.navigate(Routes.chat(convId))
                            }
                        }
                    } else null,
                    onOpenPost = { navController.navigate(Routes.post(it)) },
                    onOpenFollowers = {
                        navController.navigate(Routes.follows(userId, "followers"))
                    },
                    onOpenFollowing = {
                        navController.navigate(Routes.follows(userId, "following"))
                    },
                    onOpenHighlight = { hl ->
                        highlightCache = hl
                        navController.navigate(Routes.highlight(hl.id))
                    },
                    onCreatePost = if (userId == AppConstants.ME_USER_ID) {
                        { navController.navigate(Routes.CREATE_POST) }
                    } else null
                )
            }
            composable(
                Routes.HIGHLIGHT,
                arguments = listOf(navArgument("highlightId") { type = NavType.StringType })
            ) { entry ->
                val highlightId = entry.arguments?.getString("highlightId").orEmpty()
                var highlight by remember { mutableStateOf(highlightCache?.takeIf { it.id == highlightId }) }
                LaunchedEffect(highlightId) {
                    if (highlight == null) {
                        highlight = withContext(Dispatchers.IO) {
                            app.socialRepository.getHighlight(highlightId)
                        }
                    }
                }
                val hl = highlight
                if (hl != null) {
                    HighlightViewerScreen(
                        highlight = hl,
                        onClose = { navController.popBackStack() }
                    )
                }
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
                CommentsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onOpenProfile = ::openProfile
                )
            }
            composable(
                Routes.CHAT,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("conversationId").orEmpty()
                var peer by remember { mutableStateOf<User?>(null) }
                LaunchedEffect(id) {
                    peer = withContext(Dispatchers.IO) {
                        app.socialRepository.conversationPeer(id)
                    }
                }
                val title = peer?.username ?: "Chat"
                val vm: ChatThreadViewModel = viewModel(
                    key = "chat-$id",
                    factory = AppViewModelFactory(app, conversationId = id)
                )
                ChatThreadScreen(
                    title = title,
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onOpenProfile = peer?.let { p -> { openProfile(p.id) } }
                )
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
                    onClose = { navController.popBackStack() },
                    onOpenProfile = { userId ->
                        navController.popBackStack()
                        openProfile(userId)
                    },
                    onOpenChat = { convId ->
                        navController.navigate(Routes.chat(convId))
                    }
                )
            }
        }
    }
}
