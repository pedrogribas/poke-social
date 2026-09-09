package com.pokesocial.app.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.ui.components.FeedSkeleton
import com.pokesocial.app.ui.components.IgHomeTopBar
import com.pokesocial.app.ui.components.IgPostCard
import com.pokesocial.app.ui.components.StoryRail
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgLightGray
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onOpenComments: (String) -> Unit,
    onOpenStory: (String) -> Unit,
    onOpenMessages: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

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
                onMessages = onOpenMessages,
                activityIcon = Icons.Outlined.FavoriteBorder,
                messagesIcon = Icons.AutoMirrored.Outlined.Send
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
                    val myStory = ui.stories.firstOrNull { it.author.isMe }
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        item {
                            StoryRail(
                                stories = ui.stories,
                                myStoryFirst = myStory,
                                onStoryClick = { onOpenStory(it.id) }
                            )
                            HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
                        }
                        items(ui.posts, key = { it.id }) { post ->
                            IgPostCard(
                                post = post,
                                onLike = { viewModel.toggleLike(post.id) },
                                onComment = { onOpenComments(post.id) }
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
}
