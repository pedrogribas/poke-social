package com.pokesocial.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.theme.IgBg
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgHeart
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.timeAgo

@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    onBack: (() -> Unit)? = null,
    onOpenChat: (String) -> Unit
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = "Mensagens",
                navigationIcon = onBack?.let {
                    {
                        IconButton(onClick = it) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = IgBlack)
                        }
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
            items(conversations, key = { it.id }) { conv ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChat(conv.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IgAvatar(conv.peer.avatarUrl, 56.dp)
                        if (conv.unreadCount > 0) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(IgHeart)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(conv.peer.username, fontWeight = FontWeight.SemiBold, color = IgBlack)
                        Text(
                            conv.lastMessage,
                            color = IgGray,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                    Text(timeAgo(conv.updatedAt), color = IgGray, fontSize = 11.sp)
                }
                HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun ChatThreadScreen(
    title: String,
    viewModel: ChatThreadViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = IgBlack)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(IgWhite)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = draft,
                    onValueChange = viewModel::onDraft,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Mensagem…", color = IgGray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = IgBg,
                        unfocusedContainerColor = IgBg,
                        focusedIndicatorColor = IgWhite,
                        unfocusedIndicatorColor = IgWhite
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )
                IconButton(onClick = viewModel::send, enabled = draft.isNotBlank()) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = IgBlue)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IgWhite)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start
                ) {
                    Text(
                        msg.text,
                        color = if (msg.isMine) IgWhite else IgBlack,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (msg.isMine) IgBlue else IgBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
