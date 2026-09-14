package com.pokesocial.app.ui.stories

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Story
import com.pokesocial.app.domain.model.StoryGroup
import com.pokesocial.app.domain.model.groupedByAuthor
import com.pokesocial.app.ui.components.AppAsyncImage
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.MusicPill
import com.pokesocial.app.ui.theme.IgHeart
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.timeAgo
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoryViewerScreen(
    stories: List<Story>,
    startId: String,
    repo: SocialRepository,
    onClose: () -> Unit,
    onOpenProfile: (String) -> Unit = {},
    onOpenChat: (String) -> Unit = {}
) {
    val groups = remember(stories) { stories.groupedByAuthor() }
    if (groups.isEmpty()) {
        onClose()
        return
    }

    val startGroup = groups.indexOfFirst { g -> g.stories.any { it.id == startId } }
        .coerceAtLeast(0)
    val startFrame = groups.getOrNull(startGroup)
        ?.stories
        ?.indexOfFirst { it.id == startId }
        ?.coerceAtLeast(0) ?: 0

    val pagerState = rememberPagerState(initialPage = startGroup, pageCount = { groups.size })
    val scope = rememberCoroutineScope()
    // frame index por grupo
    val frameIndices = remember(groups) {
        IntArray(groups.size) { if (it == startGroup) startFrame else 0 }
    }
    var activeFrame by remember { mutableIntStateOf(frameIndices[startGroup]) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                activeFrame = frameIndices.getOrElse(page) { 0 }
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        beyondViewportPageCount = 1
    ) { page ->
        val group = groups[page]
        val isActivePage = pagerState.currentPage == page && !pagerState.isScrollInProgress
        StoryUserPage(
            group = group,
            frameIndex = if (page == pagerState.currentPage) activeFrame else frameIndices[page],
            isActive = isActivePage,
            pagerState = pagerState,
            page = page,
            repo = repo,
            onFrameChange = { frame ->
                frameIndices[page] = frame
                if (page == pagerState.currentPage) activeFrame = frame
            },
            onPrevUser = {
                if (page > 0) {
                    scope.launch { pagerState.animateScrollToPage(page - 1) }
                } else {
                    onClose()
                }
            },
            onNextUser = {
                if (page < groups.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(page + 1) }
                } else {
                    onClose()
                }
            },
            onClose = onClose,
            onOpenProfile = { onOpenProfile(group.author.id) },
            onReplied = { convId ->
                onClose()
                onOpenChat(convId)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StoryUserPage(
    group: StoryGroup,
    frameIndex: Int,
    isActive: Boolean,
    pagerState: PagerState,
    page: Int,
    repo: SocialRepository,
    onFrameChange: (Int) -> Unit,
    onPrevUser: () -> Unit,
    onNextUser: () -> Unit,
    onClose: () -> Unit,
    onOpenProfile: () -> Unit,
    onReplied: (String) -> Unit
) {
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val frames = group.stories
    val safeIndex = frameIndex.coerceIn(0, frames.lastIndex.coerceAtLeast(0))
    val story = frames.getOrNull(safeIndex) ?: return
    val progress = remember(group.author.id, safeIndex, isActive) { Animatable(0f) }
    var draft by remember { mutableStateOf("") }
    var liked by remember(story.id) { mutableStateOf(story.likedByMe) }
    var paused by remember { mutableStateOf(false) }
    var moreMenu by remember { mutableStateOf(false) }
    var muted by remember(story.id) { mutableStateOf(false) }
    var showMusic by remember(story.id) { mutableStateOf(story.showMusicLabel) }
    val messageFocus = remember { FocusRequester() }

    LaunchedEffect(group.author.id, safeIndex, isActive, paused) {
        if (!isActive || frames.isEmpty() || paused) return@LaunchedEffect
        repo.markStorySeen(story.id)
        progress.snapTo(0f)
        progress.animateTo(1f, tween(5_000, easing = LinearEasing))
        if (safeIndex < frames.lastIndex) {
            onFrameChange(safeIndex + 1)
        } else {
            onNextUser()
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                val pageOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val abs = pageOffset.absoluteValue
                rotationY = pageOffset * -90f
                cameraDistance = 16f * density
                transformOrigin = TransformOrigin(
                    pivotFractionX = if (pageOffset > 0f) 0f else 1f,
                    pivotFractionY = 0.5f
                )
                alpha = if (abs >= 1f) 0f else 1f
            }
            .pointerInput(safeIndex, frames.size, isActive, paused) {
                if (!isActive || paused) return@pointerInput
                detectTapGestures { offset ->
                    if (offset.x < size.width / 2f) {
                        if (safeIndex > 0) onFrameChange(safeIndex - 1) else onPrevUser()
                    } else {
                        if (safeIndex < frames.lastIndex) onFrameChange(safeIndex + 1) else onNextUser()
                    }
                }
            }
    ) {
        AppAsyncImage(
            data = story.mediaUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 8.dp, end = 8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                frames.forEachIndexed { i, _ ->
                    LinearProgressIndicator(
                        progress = {
                            when {
                                i < safeIndex -> 1f
                                i == safeIndex && isActive && !paused -> progress.value
                                i == safeIndex -> progress.value
                                else -> 0f
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = IgWhite,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenProfile),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IgAvatar(story.author.avatarUrl, size = 32.dp)
                    Text(
                        story.author.username,
                        color = IgWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        timeAgo(story.createdAt),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, null, tint = IgWhite, modifier = Modifier.size(22.dp))
                }
            }
            if (!story.musicTitle.isNullOrBlank()) {
                MusicPill(
                    title = story.musicTitle.orEmpty(),
                    artist = story.musicArtist,
                    showLabel = showMusic,
                    muted = muted,
                    onToggleMute = { muted = !muted },
                    onToggleLabel = { showMusic = !showMusic },
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        if (!group.author.isMe) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .focusRequester(messageFocus)
                        .onFocusChanged { paused = it.isFocused },
                    placeholder = { Text("Enviar mensagem", color = Color.White.copy(alpha = 0.8f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = IgWhite,
                        unfocusedTextColor = IgWhite,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = IgWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            liked = repo.toggleStoryLike(story.id)
                        }
                    }
                ) {
                    Icon(
                        if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Curtir",
                        tint = if (liked) IgHeart else IgWhite
                    )
                }
                IconButton(onClick = { messageFocus.requestFocus() }) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comentar", tint = IgWhite)
                }
                IconButton(
                    onClick = {
                        val text = draft.trim().ifBlank { if (liked) "❤️" else return@IconButton }
                        scope.launch {
                            val convId = repo.replyToStory(group.author.id, text)
                            draft = ""
                            onReplied(convId)
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Send, "Enviar", tint = IgWhite)
                }
                Box {
                    IconButton(onClick = { moreMenu = true }) {
                        Icon(Icons.Outlined.MoreHoriz, contentDescription = "Mais", tint = IgWhite)
                    }
                    DropdownMenu(
                        expanded = moreMenu,
                        onDismissRequest = { moreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver perfil") },
                            onClick = {
                                moreMenu = false
                                onOpenProfile()
                            }
                        )
                    }
                }
            }
        }
    }
}
