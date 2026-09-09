package com.pokesocial.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pokesocial.app.ui.theme.IgLightGray

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            IgLightGray.copy(alpha = 0.6f),
            Color.White.copy(alpha = 0.85f),
            IgLightGray.copy(alpha = 0.6f)
        ),
        start = androidx.compose.ui.geometry.Offset(x - 200f, 0f),
        end = androidx.compose.ui.geometry.Offset(x, 400f)
    )
    Box(modifier = modifier.background(brush))
}

@Composable
fun FeedSkeleton() {
    Column {
        repeat(3) {
            Column(Modifier.padding(bottom = 16.dp)) {
                Box(Modifier.padding(12.dp)) {
                    ShimmerBox(Modifier.size(36.dp).clip(CircleShape))
                }
                ShimmerBox(
                    Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                )
                Spacer(Modifier.height(12.dp))
                ShimmerBox(
                    Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(8.dp))
                ShimmerBox(
                    Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(0.7f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
