package com.pokesocial.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesocial.app.domain.model.MediaTag
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgHeart
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.formatCount
import com.pokesocial.app.ui.util.timeAgo
import kotlinx.coroutines.delay

private val HASHTAG_REGEX = Regex("""#[\p{L}\p{N}_]+""")
private val MENTION_REGEX = Regex("""@[\p{L}\p{N}_.]+""")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IgPostCard(
    post: Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onAuthorClick: () -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onBookmark: () -> Unit = {},
    onShare: () -> Unit = {},
    onLikesClick: () -> Unit = {},
    onTaggedUserClick: (String) -> Unit = {},
    onToggleMusicLabel: () -> Unit = {}
) {
    var showHeart by remember { mutableStateOf(false) }
    var muted by remember(post.id) { mutableStateOf(false) }
    var showTags by remember(post.id) { mutableStateOf(false) }
    var showMusic by remember(post.id, post.showMusicLabel) { mutableStateOf(post.showMusicLabel) }
    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }
    val view = LocalView.current
    val pagerState = rememberPagerState(pageCount = { post.media.size.coerceAtLeast(1) })
    val currentMedia = post.media.getOrNull(pagerState.currentPage)
    val hasMusic = !post.musicTitle.isNullOrBlank()

    val caption = remember(post.id, post.caption, post.author.username) {
        buildAnnotatedString {
            pushStringAnnotation("author", post.author.id)
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = IgBlack)) {
                append(post.author.username)
            }
            pop()
            append("  ")
            var last = 0
            val matches = (HASHTAG_REGEX.findAll(post.caption) + MENTION_REGEX.findAll(post.caption))
                .sortedBy { it.range.first }
            for (match in matches) {
                if (match.range.first < last) continue
                append(post.caption.substring(last, match.range.first))
                val value = match.value
                if (value.startsWith("#")) {
                    pushStringAnnotation("hashtag", value)
                    withStyle(SpanStyle(color = IgBlue, fontWeight = FontWeight.Medium)) { append(value) }
                    pop()
                } else {
                    pushStringAnnotation("mention", value)
                    withStyle(SpanStyle(color = IgBlue, fontWeight = FontWeight.Medium)) { append(value) }
                    pop()
                }
                last = match.range.last + 1
            }
            if (last < post.caption.length) append(post.caption.substring(last))
        }
    }

    Column(Modifier.fillMaxWidth().background(IgWhite)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onAuthorClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IgAvatar(post.author.avatarUrl, size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(post.author.username, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = IgBlack)
                if (post.originalPostId != null) {
                    Text("Repost", fontSize = 11.sp, color = IgGray)
                } else if (hasMusic && showMusic) {
                    Text(
                        listOfNotNull(post.musicTitle, post.musicArtist).joinToString(" · "),
                        fontSize = 11.sp,
                        color = IgGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(Icons.Outlined.MoreHoriz, contentDescription = null, tint = IgBlack)
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(420.dp)
                .combinedClickable(
                    onClick = {
                        if (currentMedia?.tags?.isNotEmpty() == true) showTags = !showTags
                    },
                    onDoubleClick = {
                        if (!post.likedByMe) onLike()
                        showHeart = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                )
        ) {
            if (post.mediaType == "VIDEO" && post.media.firstOrNull()?.type == "VIDEO") {
                VideoPlayer(
                    url = post.media.first().url,
                    modifier = Modifier.fillMaxSize(),
                    playWhenReady = false,
                    muted = muted
                )
            } else {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val item = post.media.getOrNull(page)
                    ZoomableAsyncImage(
                        data = item?.url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (showTags) {
                currentMedia?.tags?.forEach { tag ->
                    MentionBalloon(
                        tag = tag,
                        onClick = { onTaggedUserClick(tag.userId) },
                        modifier = Modifier.fillMaxSize()
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

            if (currentMedia?.tags?.isNotEmpty() == true) {
                IconButton(
                    onClick = { showTags = !showTags },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Marcações", tint = IgWhite, modifier = Modifier.size(18.dp))
                }
            }

            if (hasMusic) {
                MusicPill(
                    title = post.musicTitle.orEmpty(),
                    artist = post.musicArtist,
                    showLabel = showMusic,
                    muted = muted,
                    onToggleMute = { muted = !muted },
                    onToggleLabel = {
                        showMusic = !showMusic
                        onToggleMusicLabel()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            } else if (post.mediaType == "VIDEO") {
                IconButton(
                    onClick = { muted = !muted },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        if (muted) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = IgWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (showHeart) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = IgWhite.copy(alpha = heartAlpha.value),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(96.dp)
                        .scale(heartScale.value)
                )
                LaunchedEffect(showHeart) {
                    heartScale.snapTo(0.2f)
                    heartAlpha.snapTo(1f)
                    heartScale.animateTo(
                        1.15f,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    )
                    heartScale.animateTo(1f, tween(120))
                    delay(350)
                    heartAlpha.animateTo(0f, tween(220))
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
            IconButton(onClick = onBookmark) {
                Icon(
                    if (post.bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Salvar",
                    tint = IgBlack
                )
            }
        }

        LikesRow(post = post, onClick = onLikesClick)

        ClickableText(
            text = caption,
            style = TextStyle(fontSize = 13.sp, color = IgBlack),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            onClick = { offset ->
                caption.getStringAnnotations("author", offset, offset).firstOrNull()?.let {
                    onAuthorClick()
                    return@ClickableText
                }
                caption.getStringAnnotations("hashtag", offset, offset).firstOrNull()?.let {
                    onHashtagClick(it.item)
                    return@ClickableText
                }
                caption.getStringAnnotations("mention", offset, offset).firstOrNull()?.let { ann ->
                    val username = ann.item.removePrefix("@")
                    post.media.flatMap { it.tags }.firstOrNull { it.username.equals(username, true) }
                        ?.let { onTaggedUserClick(it.userId) }
                }
            }
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
            timeAgo(post.createdAt),
            color = IgGray,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun LikesRow(post: Post, onClick: () -> Unit) {
    val preview = post.likedPreview
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (preview.isNotEmpty()) {
            Box(Modifier.width((16 + (preview.size - 1).coerceAtMost(2) * 12).dp).height(20.dp)) {
                preview.take(3).forEachIndexed { i, user ->
                    IgAvatar(
                        user.avatarUrl,
                        size = 18.dp,
                        modifier = Modifier.offset(x = (i * 12).dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }
        val text = when {
            post.likedByMe && post.likeCount <= 1 -> "Curtido por você"
            post.likedByMe && preview.any { !it.isMe } -> {
                val other = preview.firstOrNull { !it.isMe }?.username ?: "outros"
                "Curtido por $other e outras ${formatCount((post.likeCount - 1).coerceAtLeast(0))} pessoas"
            }
            post.likedByMe -> "Curtido por você e outras ${formatCount((post.likeCount - 1).coerceAtLeast(0))} pessoas"
            preview.isNotEmpty() -> {
                val first = preview.first().username
                if (post.likeCount <= 1) "Curtido por $first"
                else "Curtido por $first e outras ${formatCount(post.likeCount - 1)} pessoas"
            }
            else -> "${formatCount(post.likeCount)} curtidas"
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = IgBlack)
    }
}

@Composable
private fun MentionBalloon(
    tag: MediaTag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val x = with(density) { (maxWidth.toPx() * tag.x).toDp() }
        val y = with(density) { (maxHeight.toPx() * tag.y).toDp() }
        Text(
            "@${tag.username}",
            color = IgWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .offset(x = x - 40.dp, y = y - 12.dp)
                .widthIn(max = 140.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
