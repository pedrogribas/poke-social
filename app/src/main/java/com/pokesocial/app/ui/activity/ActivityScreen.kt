package com.pokesocial.app.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.NotificationItem
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.theme.IgBg
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgHeart
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.timeAgo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityViewModel(private val repo: SocialRepository) : ViewModel() {
    val notifications: StateFlow<List<NotificationItem>> = repo.observeNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repo.markNotificationsSeen() }
    }
}

@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenPost: (String) -> Unit = {}
) {
    val items by viewModel.notifications.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = "Atividade",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = IgBlack)
                    }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(IgWhite),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma atividade ainda", color = IgGray)
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(IgWhite)
            ) {
                items(items, key = { it.id }) { n ->
                    NotificationRow(
                        item = n,
                        onClick = {
                            when {
                                !n.postId.isNullOrBlank() -> onOpenPost(n.postId)
                                else -> onOpenProfile(n.actor.id)
                            }
                        },
                        onAvatarClick = { onOpenProfile(n.actor.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (item.seen) IgWhite else IgBg.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.clickable(onClick = onAvatarClick)) {
            IgAvatar(item.actor.avatarUrl, 44.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = IgBlack)) {
                        append(item.actor.username)
                    }
                    append(" ")
                    withStyle(SpanStyle(color = IgBlack)) {
                        append(item.text)
                    }
                },
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
            Text(
                timeAgo(item.createdAt),
                color = IgGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        TypeIcon(item.type)
    }
}

@Composable
private fun TypeIcon(type: String) {
    val (icon, tint) = when (type) {
        "LIKE" -> Icons.Filled.Favorite to IgHeart
        "FOLLOW" -> Icons.Filled.PersonAdd to IgBlue
        "COMMENT" -> Icons.Outlined.ChatBubbleOutline to IgBlack
        else -> Icons.Outlined.AutoAwesome to IgBlue
    }
    Box(
        Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(IgBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
    }
}
