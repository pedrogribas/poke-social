package com.pokesocial.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.theme.IgBg
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.formatCount

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val me by viewModel.me.collectAsStateWithLifecycle()
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = me?.username ?: "lucar_10",
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.AddBox, null, tint = IgBlack)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Menu, null, tint = IgBlack)
                    }
                }
            )
        }
    ) { padding ->
        if (loading || me == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IgBlue)
            }
        } else {
            val user = me!!
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(IgWhite),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                item(span = { GridItemSpan(3) }) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IgAvatar(user.avatarUrl, 86.dp)
                            Spacer(Modifier.width(24.dp))
                            Row(
                                Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Stat(posts.size.toString(), "posts")
                                Stat(formatCount(user.followers), "seguidores")
                                Stat(formatCount(user.following), "seguindo")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(user.displayName, fontWeight = FontWeight.SemiBold, color = IgBlack)
                        Text(user.bio, color = IgBlack, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f).height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = IgBg,
                                    contentColor = IgBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("Editar perfil", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f).height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = IgBg,
                                    contentColor = IgBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) { Text("Compartilhar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
                items(posts, key = { it.id }) { post ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.media.firstOrNull()?.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(IgLightGray)
                    )
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = IgBlack)
        Text(label, fontSize = 12.sp, color = IgBlack)
    }
}
