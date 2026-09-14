package com.pokesocial.app.ui.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.domain.model.Conversation
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.domain.model.groupedByAuthor
import com.pokesocial.app.ui.AppViewModelFactory
import com.pokesocial.app.ui.chat.InboxScreen
import com.pokesocial.app.ui.chat.InboxViewModel
import com.pokesocial.app.ui.components.FeedSkeleton
import com.pokesocial.app.ui.components.IgHomeTopBar
import com.pokesocial.app.ui.components.IgPostCard
import com.pokesocial.app.ui.components.SharePostSheet
import com.pokesocial.app.ui.components.StoryRail
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgLightGray
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    inboxFactory: AppViewModelFactory,
    onOpenComments: (String) -> Unit,
    onOpenStory: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onOpenActivity: () -> Unit = {},
    onCreatePost: () -> Unit = {},
    onOpenLikes: (String) -> Unit = {},
    onInboxVisibilityChange: (Boolean) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val inboxVm: InboxViewModel = viewModel(factory = inboxFactory)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { page ->
            onInboxVisibilityChange(page == 1)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 0
    ) { page ->
        when (page) {
            0 -> FeedPage(
                viewModel = viewModel,
                inboxViewModel = inboxVm,
                onOpenComments = onOpenComments,
                onOpenStory = onOpenStory,
                onOpenProfile = onOpenProfile,
                onHashtagClick = onHashtagClick,
                onOpenActivity = onOpenActivity,
                onCreatePost = onCreatePost,
                onOpenMessages = { scope.launch { pagerState.animateScrollToPage(1) } },
                onOpenLikes = onOpenLikes
            )
            else -> InboxScreen(
                viewModel = inboxVm,
                myUsername = AppConstants.ME_USERNAME,
                onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                onOpenChat = onOpenChat,
                onOpenProfile = onOpenProfile
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedPage(
    viewModel: FeedViewModel,
    inboxViewModel: InboxViewModel,
    onOpenComments: (String) -> Unit,
    onOpenStory: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onOpenActivity: () -> Unit,
    onCreatePost: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenLikes: (String) -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val conversations by inboxViewModel.allConversations.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val storyGroups = remember(ui.stories) { ui.stories.groupedByAuthor() }
    var sharePost by remember { mutableStateOf<Post?>(null) }
    val unread = remember(conversations) { conversations.sumOf { it.unreadCount } }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            last >= total - 3
        }.distinctUntilChanged().collect { nearEnd ->
            if (nearEnd) viewModel.loadMore()
        }
    }

    Scaffold(
        topBar = {
            IgHomeTopBar(
                onActivityClick = onOpenActivity,
                onCreateClick = onCreatePost,
                onMessagesClick = onOpenMessages,
                unreadMessages = unread
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = ui.refreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                ui.initialLoading -> FeedSkeleton()
                ui.error != null && ui.posts.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ui.error ?: "Erro", color = IgBlack)
                }
                else -> {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        item {
                            StoryRail(
                                groups = storyGroups,
                                onStoryClick = { group ->
                                    val firstUnseen = group.stories.firstOrNull { !it.seenByMe }
                                    onOpenStory((firstUnseen ?: group.stories.first()).id)
                                }
                            )
                            HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
                        }
                        items(ui.posts, key = { it.id }) { post ->
                            IgPostCard(
                                post = post,
                                onLike = { viewModel.toggleLike(post.id) },
                                onComment = { onOpenComments(post.id) },
                                onAuthorClick = { onOpenProfile(post.author.id) },
                                onHashtagClick = onHashtagClick,
                                onBookmark = { viewModel.toggleBookmark(post.id) },
                                onShare = { sharePost = post },
                                onLikesClick = { onOpenLikes(post.id) },
                                onTaggedUserClick = onOpenProfile
                            )
                            HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
                        }
                        if (ui.loadingMore) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = IgBlue, strokeWidth = 2.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val pending = sharePost
    if (pending != null) {
        SharePostSheet(
            conversations = conversations,
            onSelect = { conv: Conversation ->
                viewModel.sharePost(conv.id, pending)
                sharePost = null
            },
            onDismiss = { sharePost = null }
        )
    }
}
