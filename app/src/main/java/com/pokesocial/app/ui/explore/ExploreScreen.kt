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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onOpenPost: (String) -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IgWhite)
        ) {
            OutlinedTextField(
                value = ui.query,
                onValueChange = viewModel::onQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = IgGray) },
                placeholder = { Text("Pesquisar", color = IgGray) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IgLightGray,
                    unfocusedBorderColor = IgLightGray,
                    focusedContainerColor = IgLightGray.copy(alpha = 0.35f),
                    unfocusedContainerColor = IgLightGray.copy(alpha = 0.35f)
                )
            )

            if (ui.query.isNotBlank()) {
                ui.users.forEach { user ->
                    Row(
                        Modifier
                            .fillMaxWidth()
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
            } else if (ui.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IgBlue)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ui.posts, key = { it.id }) { post ->
                        Box(
                            Modifier
                                .aspectRatio(1f)
                                .clickable { onOpenPost(post.id) }
                        ) {
                            val thumb = post.media.firstOrNull()?.url
                            if (post.mediaType == "VIDEO") {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(IgBlack)
                                )
                                Icon(
                                    Icons.Filled.PlayCircleFilled,
                                    null,
                                    tint = IgWhite,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(20.dp)
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(thumb)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
