package com.pokesocial.app.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GifBox
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.domain.model.InboxFolder
import com.pokesocial.app.ui.battle.BattleDialog
import com.pokesocial.app.ui.components.AppAsyncImage
import com.pokesocial.app.ui.components.GifPicker
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.theme.IgBg
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.timeAgo

private enum class InboxPane { MESSAGES, REQUESTS, SPAM, ARCHIVED }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    myUsername: String,
    onBack: (() -> Unit)? = null,
    onOpenChat: (String) -> Unit,
    onOpenProfile: (String) -> Unit = {}
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val allConversations by viewModel.allConversations.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf(false) }
    var noteDraft by remember { mutableStateOf("") }
    var pane by remember { mutableStateOf(InboxPane.MESSAGES) }
    var accountMenu by remember { mutableStateOf(false) }
    var newMessage by remember { mutableStateOf(false) }

    val filtered = remember(conversations, query) {
        if (query.isBlank()) conversations
        else conversations.filter {
            it.peer.username.contains(query, ignoreCase = true) ||
                it.peer.displayName.contains(query, ignoreCase = true) ||
                it.lastMessage.contains(query, ignoreCase = true)
        }
    }
    val orderedNotes = remember(notes) {
        notes.sortedWith(
            compareByDescending<com.pokesocial.app.domain.model.Note> { it.user.isMe }
                .thenByDescending { it.updatedAt }
        )
    }

    val title = when (pane) {
        InboxPane.MESSAGES -> myUsername
        InboxPane.REQUESTS -> "Pedidos de contato"
        InboxPane.SPAM -> "Spam"
        InboxPane.ARCHIVED -> "Arquivadas"
    }

    LaunchedEffect(pane) {
        viewModel.setFolder(
            when (pane) {
                InboxPane.MESSAGES -> InboxFolder.PRIMARY
                InboxPane.REQUESTS, InboxPane.SPAM -> InboxFolder.REQUESTS
                InboxPane.ARCHIVED -> InboxFolder.ARCHIVED
            }
        )
    }

    Scaffold(
        topBar = {
            Column(Modifier.background(IgWhite)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            when (pane) {
                                InboxPane.MESSAGES -> onBack?.invoke()
                                InboxPane.REQUESTS, InboxPane.ARCHIVED -> pane = InboxPane.MESSAGES
                                InboxPane.SPAM -> pane = InboxPane.REQUESTS
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = IgBlack)
                    }
                    if (pane == InboxPane.MESSAGES) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { accountMenu = true }
                            ) {
                                Text(
                                    title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = IgBlack
                                )
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = IgBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = accountMenu,
                                onDismissRequest = { accountMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Arquivadas") },
                                    onClick = {
                                        accountMenu = false
                                        pane = InboxPane.ARCHIVED
                                    }
                                )
                            }
                        }
                        IconButton(onClick = { newMessage = true }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Nova mensagem", tint = IgBlack)
                        }
                    } else {
                        Text(
                            title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = IgBlack,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(48.dp))
                    }
                }
                if (pane == InboxPane.MESSAGES) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = IgGray) },
                        placeholder = { Text("Pesquise", color = IgGray) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = IgBg,
                            unfocusedContainerColor = IgBg
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (pane) {
            InboxPane.MESSAGES -> {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(IgWhite)
                ) {
                    item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item(key = "my-note") {
                                    val mine = orderedNotes.firstOrNull { it.user.isMe }
                                    NoteBubble(
                                        avatarUrl = mine?.user?.avatarUrl
                                            ?: AppConstants.artworkUrl(AppConstants.ME_POKEMON_ID),
                                        text = mine?.text.orEmpty(),
                                        label = "Sua nota",
                                        empty = mine == null || mine.text.isBlank(),
                                        onClick = {
                                            noteDraft = mine?.text.orEmpty()
                                            editingNote = true
                                        }
                                    )
                                }
                                items(
                                    orderedNotes.filter { !it.user.isMe },
                                    key = { it.userId }
                                ) { note ->
                                    NoteBubble(
                                        avatarUrl = note.user.avatarUrl,
                                        text = note.text,
                                        label = note.user.displayName.ifBlank { note.user.username },
                                        onClick = {
                                            viewModel.openChatWith(note.userId, onOpenChat)
                                        }
                                    )
                                }
                            }
                        }
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Mensagens",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = IgBlack,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "Pedidos",
                                color = IgBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    pane = InboxPane.REQUESTS
                                }
                            )
                        }
                    }
                    items(filtered, key = { it.id }) { conv ->
                        ConversationRow(
                            conv = conv,
                            onOpenChat = { onOpenChat(conv.id) },
                            onOpenProfile = { onOpenProfile(conv.peer.id) },
                            onLongPress = { viewModel.archive(conv.id) }
                        )
                    }
                }
            }
            InboxPane.REQUESTS -> RequestsPane(
                modifier = Modifier.padding(padding),
                conversations = filtered,
                onOpenSpam = { pane = InboxPane.SPAM },
                onOpenChat = onOpenChat,
                onOpenProfile = onOpenProfile,
                onAccept = { viewModel.moveTo(it, InboxFolder.PRIMARY) }
            )
            InboxPane.SPAM -> EmptyInboxPane(
                modifier = Modifier.padding(padding),
                title = "Isso é tudo",
                body = "Você não tem nenhum spam.\nAs mensagens que talvez sejam ofensivas ou indesejadas aparecerão aqui.\n\nPedidos de spam feitos há mais de 30 dias não são exibidos."
            )
            InboxPane.ARCHIVED -> {
                if (filtered.isEmpty()) {
                    EmptyInboxPane(
                        modifier = Modifier.padding(padding),
                        title = "Nenhuma conversa arquivada",
                        body = "As conversas que você arquivar ficarão aqui. Toque e segure uma conversa nas Mensagens para arquivar."
                    )
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .background(IgWhite)
                    ) {
                        items(filtered, key = { it.id }) { conv ->
                            ConversationRow(
                                conv = conv,
                                onOpenChat = { onOpenChat(conv.id) },
                                onOpenProfile = { onOpenProfile(conv.peer.id) },
                                onLongPress = { viewModel.moveTo(conv.id, InboxFolder.PRIMARY) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (newMessage) {
        AlertDialog(
            onDismissRequest = { newMessage = false },
            title = { Text("Nova mensagem") },
            text = {
                LazyColumn(Modifier.height(320.dp)) {
                    items(allConversations, key = { it.id }) { conv ->
                        ConversationRow(
                            conv = conv,
                            onOpenChat = {
                                newMessage = false
                                onOpenChat(conv.id)
                            },
                            onOpenProfile = {
                                newMessage = false
                                onOpenProfile(conv.peer.id)
                            },
                            onLongPress = {}
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { newMessage = false }) { Text("Fechar") }
            }
        )
    }

    if (editingNote) {
        AlertDialog(
            onDismissRequest = { editingNote = false },
            title = { Text("Sua nota") },
            text = {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { if (it.length <= 60) noteDraft = it },
                    placeholder = { Text("Deixe uma nota…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.upsertMyNote(noteDraft)
                        editingNote = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editingNote = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun RequestsPane(
    modifier: Modifier,
    conversations: List<com.pokesocial.app.domain.model.Conversation>,
    onOpenSpam: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onAccept: (String) -> Unit
) {
    Column(
        modifier
            .fillMaxSize()
            .background(IgWhite)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IgBg)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "As solicitações ocultas agora são chamadas de Spam",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = IgBlack
                )
                Text(
                    "Ao excluir um pedido de contato, você pode marcá-lo como spam para que não apareça novamente.",
                    fontSize = 13.sp,
                    color = IgGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenSpam)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .border(1.dp, IgLightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.VisibilityOff, null, tint = IgBlack)
            }
            Text(
                "Spam",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = IgBlack,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = IgGray)
        }
        HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
        if (conversations.isEmpty()) {
            EmptyInboxPane(
                modifier = Modifier.fillMaxSize(),
                title = "Nenhum pedido de contato ainda",
            body = "Você pode controlar quem pode enviar pedidos de contato para você nas configurações.\n\nPedidos feitos há mais de 30 dias não são exibidos."
            )
        } else {
            LazyColumn {
                items(conversations, key = { it.id }) { conv ->
                    ConversationRow(
                        conv = conv,
                        onOpenChat = { onOpenChat(conv.id) },
                        onOpenProfile = { onOpenProfile(conv.peer.id) },
                        onLongPress = { onAccept(conv.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyInboxPane(
    modifier: Modifier,
    title: String,
    body: String
) {
    Column(
        modifier
            .fillMaxSize()
            .background(IgWhite)
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(88.dp)
                .border(2.dp, IgBlack, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = IgBlack,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = IgBlack,
            modifier = Modifier.padding(top = 20.dp),
            textAlign = TextAlign.Center
        )
        Text(
            body,
            color = IgGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
            lineHeight = 20.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationRow(
    conv: com.pokesocial.app.domain.model.Conversation,
    onOpenChat: () -> Unit,
    onOpenProfile: () -> Unit,
    onLongPress: () -> Unit
) {
    val unread = conv.unreadCount > 0
    val online = conv.unreadCount == 0 && (System.currentTimeMillis() - conv.updatedAt) < 3_600_000L
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onOpenChat, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.clickable(onClick = onOpenProfile)) {
            IgAvatar(conv.peer.avatarUrl, 56.dp, online = online)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                conv.peer.displayName.ifBlank { conv.peer.username },
                fontWeight = if (unread) FontWeight.Bold else FontWeight.Normal,
                color = IgBlack,
                fontSize = 15.sp
            )
            val subtitle = when {
                unread -> "${conv.unreadCount} ${if (conv.unreadCount == 1) "nova mensagem" else "novas mensagens"} · ${timeAgo(conv.updatedAt)}"
                online -> "Online agora"
                conv.lastMediaUrl != null && conv.lastMessage.isBlank() -> "GIF · ${timeAgo(conv.updatedAt)}"
                conv.lastMessage.isNotBlank() -> "${conv.lastMessage} · ${timeAgo(conv.updatedAt)}"
                else -> "Enviado há ${timeAgo(conv.updatedAt)}"
            }
            Text(
                subtitle,
                color = if (unread) IgBlack else IgGray,
                fontWeight = if (unread) FontWeight.Medium else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (unread) {
            Box(
                Modifier
                    .padding(start = 8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(IgBlue)
            )
        }
    }
}

@Composable
private fun NoteBubble(
    avatarUrl: String,
    text: String,
    label: String,
    empty: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            IgAvatar(avatarUrl, 64.dp, modifier = Modifier.padding(top = 22.dp))
            Box(
                Modifier
                    .widthIn(max = 76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(IgWhite)
                    .border(0.5.dp, IgLightGray, RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    if (empty && text.isBlank()) "Sua nota" else text,
                    fontSize = 11.sp,
                    color = if (empty && text.isBlank()) IgGray else IgBlack,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }
        }
        Spacer(Modifier.size(4.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = IgBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatThreadScreen(
    title: String,
    viewModel: ChatThreadViewModel,
    onBack: () -> Unit,
    onOpenProfile: (() -> Unit)? = null
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val peer by viewModel.peer.collectAsStateWithLifecycle()
    var showGifs by remember { mutableStateOf(false) }
    var showBattle by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = title,
                titleBold = true,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = IgBlack)
                    }
                },
                onTitleClick = onOpenProfile,
                actions = {
                    if (peer != null) {
                        IconButton(onClick = { showBattle = true }) {
                            Icon(
                                Icons.Outlined.Bolt,
                                contentDescription = "Batalha",
                                tint = IgBlack
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column(Modifier.background(IgWhite)) {
                if (showGifs) {
                    GifPicker(onPick = {
                        viewModel.sendGif(it)
                        showGifs = false
                    })
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showGifs = !showGifs }) {
                        Icon(Icons.Outlined.GifBox, "GIF", tint = IgBlue)
                    }
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
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IgWhite)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start
                ) {
                    Box(contentAlignment = if (msg.isMine) Alignment.BottomEnd else Alignment.BottomStart) {
                        Column(
                            horizontalAlignment = if (msg.isMine) Alignment.End else Alignment.Start,
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (msg.isMine) Color(0xFF3797F0) else IgBg)
                                .combinedClickable(
                                    onClick = {},
                                    onDoubleClick = { viewModel.toggleReaction(msg.id) },
                                    onLongClick = { viewModel.toggleReaction(msg.id) }
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            msg.mediaUrl?.let { url ->
                                AppAsyncImage(
                                    data = url,
                                    contentDescription = "GIF",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                            if (msg.text.isNotBlank()) {
                                Text(
                                    msg.text,
                                    color = if (msg.isMine) IgWhite else IgBlack,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (!msg.reaction.isNullOrBlank()) {
                            Text(
                                msg.reaction,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .align(if (msg.isMine) Alignment.BottomStart else Alignment.BottomEnd)
                                    .offset(y = 6.dp)
                                    .padding(
                                        start = if (msg.isMine) 0.dp else 8.dp,
                                        end = if (msg.isMine) 8.dp else 0.dp
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IgWhite)
                                    .border(0.5.dp, IgLightGray, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    peer?.let { opponent ->
        if (showBattle) {
            BattleDialog(
                peer = opponent,
                onDismiss = { showBattle = false },
                onFinished = { won, fled -> viewModel.sendBattleAftermath(won, fled) }
            )
        }
    }
}
