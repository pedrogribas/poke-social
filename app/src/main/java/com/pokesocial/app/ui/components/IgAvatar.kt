package com.pokesocial.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgStory1
import com.pokesocial.app.ui.theme.IgStory2
import com.pokesocial.app.ui.theme.IgStory3
import com.pokesocial.app.ui.theme.IgWhite

@Composable
fun IgAvatar(
    imageUrl: String,
    size: Dp = 40.dp,
    showStoryRing: Boolean = false,
    seen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ring = if (showStoryRing && !seen) {
        Brush.linearGradient(listOf(IgStory1, IgStory2, IgStory3))
    } else {
        Brush.linearGradient(listOf(IgLightGray, IgLightGray))
    }
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showStoryRing) Modifier.border(2.dp, ring, CircleShape)
                else Modifier
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(if (showStoryRing) Modifier.border(2.5.dp, IgWhite, CircleShape) else Modifier)
                .clip(CircleShape)
        )
    }
}
