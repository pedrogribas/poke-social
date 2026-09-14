package com.pokesocial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesocial.app.ui.theme.IgWhite

@Composable
fun MusicPill(
    title: String,
    artist: String?,
    showLabel: Boolean,
    muted: Boolean,
    onToggleMute: () -> Unit,
    onToggleLabel: () -> Unit,
    modifier: Modifier = Modifier,
    light: Boolean = false
) {
    val fg = if (light) Color.Black else IgWhite
    val bg = if (light) Color.White.copy(alpha = 0.92f) else Color.Black.copy(alpha = 0.45f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLabel) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable(onClick = onToggleLabel)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    buildString {
                        append(title)
                        if (!artist.isNullOrBlank()) {
                            append(" · ")
                            append(artist)
                        }
                    },
                    color = fg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        } else {
            Text(
                "Música",
                color = fg.copy(alpha = 0.8f),
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable(onClick = onToggleLabel)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        IconButton(onClick = onToggleMute, modifier = Modifier.size(36.dp)) {
            Icon(
                if (muted) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
                contentDescription = if (muted) "Ativar áudio" else "Silenciar",
                tint = fg
            )
        }
    }
}
