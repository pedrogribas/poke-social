package com.pokesocial.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesocial.app.domain.model.Highlight
import com.pokesocial.app.ui.components.AppAsyncImage
import com.pokesocial.app.ui.theme.IgWhite
import kotlinx.coroutines.delay

@Composable
fun HighlightViewerScreen(
    highlight: Highlight,
    onClose: () -> Unit
) {
    val urls = highlight.mediaUrls
    if (urls.isEmpty()) {
        onClose()
        return
    }
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(index, highlight.id) {
        delay(4_000)
        if (index < urls.lastIndex) index++ else onClose()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(index, urls.size) {
                detectTapGestures { offset ->
                    val mid = size.width / 2f
                    if (offset.x < mid) {
                        if (index > 0) index-- else onClose()
                    } else {
                        if (index < urls.lastIndex) index++ else onClose()
                    }
                }
            }
    ) {
        AppAsyncImage(
            data = urls[index],
            contentDescription = highlight.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 10.dp, end = 10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                urls.forEachIndexed { i, _ ->
                    LinearProgressIndicator(
                        progress = {
                            when {
                                i < index -> 1f
                                i == index -> 0.55f
                                else -> 0f
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = IgWhite,
                        trackColor = IgWhite.copy(alpha = 0.3f)
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(IgWhite.copy(alpha = 0.2f))
                ) {
                    AppAsyncImage(
                        data = highlight.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    highlight.title,
                    color = IgWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, null, tint = IgWhite)
                }
            }
        }
    }
}
