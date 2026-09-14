package com.pokesocial.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.data.local.ContentThemes
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.ui.components.AppAsyncImage
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.formatCount
import kotlin.math.abs

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onOpenPost: (String) -> Unit,
    onOpenProfile: (String) -> Unit = {}
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IgWhite)
        ) {
            TextField(
                value = ui.query,
                onValueChange = viewModel::onQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = IgGray) },
                placeholder = { Text("Pesquisar", color = IgGray) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = IgLightGray.copy(alpha = 0.45f),
                    unfocusedContainerColor = IgLightGray.copy(alpha = 0.45f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = IgBlack
                )
            )

            when {
                ui.query.isNotBlank() -> {
                    val empty = ui.users.isEmpty() && ui.hashtagPosts.isEmpty()
                    if (empty) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum resultado", color = IgGray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(1.dp),
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(ui.users, key = { "u-${it.id}" }, span = { GridItemSpan(3) }) { user ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenProfile(user.id) }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IgAvatar(user.avatarUrl, 44.dp)
                                    Column(Modifier.padding(start = 12.dp)) {
                                        Text(user.username, color = IgBlack, fontSize = 14.sp)
                                        Text(user.displayName, color = IgGray, fontSize = 12.sp)
                                    }
                                }
                            }
                            if (ui.hashtagPosts.isNotEmpty()) {
                                item(span = { GridItemSpan(3) }) {
                                    Text(
                                        "Publicações",
                                        color = IgBlack,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                items(ui.hashtagPosts, key = { it.id }) { post ->
                                    ExploreThumb(post = post, featured = false) {
                                        onOpenPost(post.id)
                                    }
                                }
                            }
                        }
                    }
                }
                ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IgBlue)
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = ui.posts.size,
                        key = { ui.posts[it].id },
                        span = {
                            val post = ui.posts[it]
                            val featured = post.mediaType == "VIDEO" && it % 5 == 0
                            GridItemSpan(if (featured) 2 else 1)
                        }
                    ) { index ->
                        val post = ui.posts[index]
                        val featured = post.mediaType == "VIDEO" && index % 5 == 0
                        ExploreThumb(post = post, featured = featured) {
                            onOpenPost(post.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreThumb(
    post: Post,
    featured: Boolean,
    onClick: () -> Unit
) {
    val isVideo = post.mediaType == "VIDEO"
    val mediaUrl = post.media.firstOrNull()?.url
    val thumbUrl = when {
        isVideo -> ContentThemes.assetUri(
            ContentThemes.extraImages[abs(post.id.hashCode()) % ContentThemes.extraImages.size]
        )
        else -> mediaUrl
    }

    Box(
        Modifier
            .aspectRatio(if (featured) 0.5f else 1f)
            .clickable(onClick = onClick)
    ) {
        if (thumbUrl != null) {
            AppAsyncImage(
                data = thumbUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize().background(IgBlack))
        }
        if (isVideo) {
            Text(
                "▶  ${formatCount((post.likeCount * 17).coerceAtLeast(120))}",
                color = IgWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}
