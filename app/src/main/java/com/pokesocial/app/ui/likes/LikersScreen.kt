package com.pokesocial.app.ui.likes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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

@Composable
fun LikersScreen(
    postId: String,
    repo: SocialRepository,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    LaunchedEffect(postId) {
        users = withContext(Dispatchers.IO) { repo.likersOf(postId) }
    }

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = "Curtidas",
                titleBold = true,
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
                    IgAvatar(user.avatarUrl, 44.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Text(user.username, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = IgBlack)
                        Text(user.displayName, fontSize = 13.sp, color = IgGray)
                    }
                }
            }
        }
    }
}
