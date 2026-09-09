package com.pokesocial.app.ui.reels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.VideoPlayer
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgHeart
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.formatCount

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen(
    viewModel: ReelsViewModel,
    onComment: (String) -> Unit
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { posts.size })

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = IgBlue)
        } else if (posts.isEmpty()) {
            Text("Sem reels ainda", color = IgWhite, modifier = Modifier.align(Alignment.Center))
        } else {
            VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val post = posts[page]
                val url = post.media.firstOrNull()?.url.orEmpty()
                Box(Modifier.fillMaxSize()) {
                    if (url.isNotBlank()) {
                        VideoPlayer(
                            url = url,
                            modifier = Modifier.fillMaxSize(),
                            playWhenReady = pagerState.currentPage == page
                        )
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .padding(end = 72.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IgAvatar(post.author.avatarUrl, 36.dp)
                            Text(
                                post.author.username,
                                color = IgWhite,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Text(
                            post.caption,
                            color = IgWhite,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = { viewModel.toggleLike(post.id) }) {
                            Icon(
                                if (post.likedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                null,
                                tint = if (post.likedByMe) IgHeart else IgWhite
                            )
                        }
                        Text(formatCount(post.likeCount), color = IgWhite, fontSize = 12.sp)
                        IconButton(onClick = { onComment(post.id) }) {
                            Icon(Icons.Outlined.ChatBubbleOutline, null, tint = IgWhite)
                        }
                        Text(formatCount(post.commentCount), color = IgWhite, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
