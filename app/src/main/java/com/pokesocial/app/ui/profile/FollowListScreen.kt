package com.pokesocial.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.User
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class FollowListMode { FOLLOWERS, FOLLOWING }

@Composable
fun FollowListScreen(
    userId: String,
    mode: FollowListMode,
    repo: SocialRepository,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    LaunchedEffect(userId, mode) {
        users = withContext(Dispatchers.IO) {
            when (mode) {
                FollowListMode.FOLLOWERS -> repo.followersOf(userId)
                FollowListMode.FOLLOWING -> repo.followingOf(userId)
            }
        }
    }

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = if (mode == FollowListMode.FOLLOWERS) "Seguidores" else "Seguindo",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = IgBlack)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IgWhite)
        ) {
            items(users, key = { it.id }) { user ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenProfile(user.id) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IgAvatar(user.avatarUrl, 48.dp)
                    androidx.compose.foundation.layout.Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Text(user.username, fontWeight = FontWeight.SemiBold, color = IgBlack)
                        Text(user.displayName, color = IgGray, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
