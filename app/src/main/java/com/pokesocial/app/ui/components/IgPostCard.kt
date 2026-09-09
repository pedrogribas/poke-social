package com.pokesocial.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgHeart
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.formatCount
import com.pokesocial.app.ui.util.timeAgo
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IgPostCard(
    post: Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit = {},
    onAuthorClick: () -> Unit = {}
) {
    var showHeart by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { post.media.size.coerceAtLeast(1) })

    Column(Modifier.fillMaxWidth().background(IgWhite)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onAuthorClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IgAvatar(post.author.avatarUrl, size = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(post.author.username, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = IgBlack)
                Text(post.author.displayName, fontSize = 11.sp, color = IgGray)
            }
            Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = IgBlack)
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(420.dp)
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        if (!post.likedByMe) onLike()
                        showHeart = true
                    }
                )
        ) {
            if (post.mediaType == "VIDEO" && post.media.firstOrNull()?.type == "VIDEO") {
                VideoPlayer(
                    url = post.media.first().url,
                    modifier = Modifier.fillMaxWidth().height(420.dp),
                    playWhenReady = false
                )
            } else {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(420.dp)) { page ->
                    val item = post.media.getOrNull(page)
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item?.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(420.dp)
                    )
                }
            }

            if (post.media.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1}/${post.media.size}",
                    color = IgWhite,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (showHeart) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = IgWhite,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(96.dp)
                )
                LaunchedEffect(showHeart) {
                    delay(650)
                    showHeart = false
                }
            }
        }

        if (post.media.size > 1) {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(post.media.size) { i ->
                    Box(
                        Modifier
                            .padding(horizontal = 2.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (i == pagerState.currentPage) Color(0xFF3897F0) else IgLightGray)
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLike) {
                Icon(
                    if (post.likedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Curtir",
                    tint = if (post.likedByMe) IgHeart else IgBlack
                )
            }
            IconButton(onClick = onComment) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comentar", tint = IgBlack)
            }
            IconButton(onClick = onShare) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Enviar", tint = IgBlack)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Salvar", tint = IgBlack)
            }
        }

        Text(
            "${formatCount(post.likeCount)} curtidas",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = IgBlack,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = IgBlack)) {
                    append(post.author.username)
                }
                append("  ")
                append(post.caption)
            },
            fontSize = 13.sp,
            color = IgBlack,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (post.commentCount > 0) {
            Text(
                "Ver todos os ${post.commentCount} comentários",
                color = IgGray,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onComment)
            )
        }

        Text(
            timeAgo(post.createdAt).uppercase(),
            color = IgGray,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}
