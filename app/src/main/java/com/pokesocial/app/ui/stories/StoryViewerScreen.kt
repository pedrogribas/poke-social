package com.pokesocial.app.ui.stories

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Story
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.theme.IgWhite

@Composable
fun StoryViewerScreen(
    stories: List<Story>,
    startId: String,
    repo: SocialRepository,
    onClose: () -> Unit
) {
    if (stories.isEmpty()) {
        onClose()
        return
    }
    var index by remember {
        mutableIntStateOf(stories.indexOfFirst { it.id == startId }.coerceAtLeast(0))
    }
    val story = stories[index]
    val progress = remember(index) { Animatable(0f) }

    LaunchedEffect(index) {
        repo.markStorySeen(story.id)
        progress.snapTo(0f)
        progress.animateTo(1f, tween(5000, easing = LinearEasing))
        if (index < stories.lastIndex) index++ else onClose()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(index) {
                detectTapGestures { offset ->
                    if (offset.x < size.width / 2f) {
                        if (index > 0) index-- else onClose()
                    } else {
                        if (index < stories.lastIndex) index++ else onClose()
                    }
                }
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(story.mediaUrl)
                .crossfade(true)
                .build(),
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
                stories.forEachIndexed { i, _ ->
                    LinearProgressIndicator(
                        progress = {
                            when {
                                i < index -> 1f
                                i == index -> progress.value
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
                IgAvatar(story.author.avatarUrl, size = 32.dp)
                Text(
                    story.author.username,
                    color = IgWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, null, tint = IgWhite, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}
