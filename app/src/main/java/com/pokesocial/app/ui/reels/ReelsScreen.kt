package com.pokesocial.app.ui.reels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.MusicPill
import com.pokesocial.app.ui.components.SharePostSheet
import com.pokesocial.app.ui.components.VideoPlayer
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgHeart
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.formatCount

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen(
    viewModel: ReelsViewModel,
    onComment: (String) -> Unit,
    onOpenProfile: (String) -> Unit = {},
    onOpenLikes: (String) -> Unit = {}
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val following by viewModel.following.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { posts.size })
    var sharePostId by remember { mutableStateOf<String?>(null) }

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
                val isFollowing = following.contains(post.author.id) || post.author.isMe
                var speed by remember(post.id) { mutableFloatStateOf(1f) }
                var muted by remember(post.id) { mutableStateOf(false) }
                var showMusic by remember(post.id) { mutableStateOf(post.showMusicLabel) }
                val active = pagerState.currentPage == page

                Box(Modifier.fillMaxSize()) {
                    if (url.isNotBlank()) {
                        VideoPlayer(
                            url = url,
                            modifier = Modifier.fillMaxSize(),
                            playWhenReady = active,
                            muted = muted,
                            playbackSpeed = if (active) speed else 1f
                        )
                    }

                    // Cantos: segurar = 2x (estilo Instagram)
                    Row(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(post.id) {
                                    detectTapGestures(
                                        onPress = {
                                            speed = 2f
                                            tryAwaitRelease()
                                            speed = 1f
                                        }
                                    )
                                }
                        )
                        Box(Modifier.weight(1.2f).fillMaxHeight())
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(post.id) {
                                    detectTapGestures(
                                        onPress = {
                                            speed = 2f
                                            tryAwaitRelease()
                                            speed = 1f
                                        }
                                    )
                                }
                        )
                    }

                    if (speed > 1.5f) {
                        Text(
                            "2x",
                            color = IgWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 56.dp)
                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        "Reels",
                        color = IgWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp, top = 48.dp)
                    )

                    Column(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .padding(end = 80.dp, bottom = 24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onOpenProfile(post.author.id) }
                        ) {
                            IgAvatar(post.author.avatarUrl, 36.dp)
                            Text(
                                post.author.username,
                                color = IgWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            if (!post.author.isMe && !isFollowing) {
                                Text(
                                    "Seguir",
                                    color = IgWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .padding(start = 10.dp)
                                        .border(1.dp, IgWhite, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.toggleFollow(post.author.id) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            post.caption,
                            color = IgWhite,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        if (!post.musicTitle.isNullOrBlank()) {
                            MusicPill(
                                title = post.musicTitle.orEmpty(),
                                artist = post.musicArtist,
                                showLabel = showMusic,
                                muted = muted,
                                onToggleMute = { muted = !muted },
                                onToggleLabel = { showMusic = !showMusic },
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }

                    Column(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp, bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ReelAction(
                            icon = if (post.likedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            tint = if (post.likedByMe) IgHeart else IgWhite,
                            label = formatCount(post.likeCount),
                            onClick = { viewModel.toggleLike(post.id) },
                            onLabelClick = { onOpenLikes(post.id) }
                        )
                        ReelAction(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            label = formatCount(post.commentCount),
                            onClick = { onComment(post.id) }
                        )
                        ReelAction(
                            icon = Icons.AutoMirrored.Outlined.Send,
                            label = "",
                            onClick = { sharePostId = post.id }
                        )
                        ReelAction(
                            icon = Icons.Outlined.Repeat,
                            tint = if (post.repostedByMe) IgBlue else IgWhite,
                            label = if (post.repostedByMe) "Feito" else "",
                            onClick = { viewModel.repost(post.id) }
                        )
                        ReelAction(
                            icon = if (post.bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            label = "",
                            onClick = { viewModel.toggleBookmark(post.id) }
                        )
                        IconButton(onClick = { onOpenProfile(post.author.id) }) {
                            Icon(Icons.Outlined.MoreHoriz, contentDescription = "Mais", tint = IgWhite)
                        }
                        Spacer(Modifier.height(8.dp))
                        IgAvatar(post.author.avatarUrl, 28.dp)
                    }
                }
            }
        }
    }

    val shareId = sharePostId
    val sharePost = posts.firstOrNull { it.id == shareId }
    if (sharePost != null) {
        SharePostSheet(
            conversations = conversations,
            onSelect = { conv ->
                viewModel.sharePost(conv.id, sharePost)
                sharePostId = null
            },
            onDismiss = { sharePostId = null }
        )
    }
}

@Composable
private fun ReelAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = IgWhite,
    onLabelClick: (() -> Unit)? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        }
        if (label.isNotBlank()) {
            Text(
                label,
                color = IgWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = if (onLabelClick != null) Modifier.clickable(onClick = onLabelClick) else Modifier
            )
        }
    }
}
