package com.pokesocial.app.ui.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GifBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.domain.model.Comment
import com.pokesocial.app.ui.components.AppAsyncImage
import com.pokesocial.app.ui.components.GifPicker
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.timeAgo

private val QUICK_EMOJIS = listOf("❤️", "🙌", "🔥", "👏", "😢", "😍", "😮", "😂")

@Composable
fun CommentsScreen(
    viewModel: CommentsViewModel,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit = {},
    embedded: Boolean = false
) {
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    var showGifs by remember { mutableStateOf(false) }

    if (embedded) {
        Column(Modifier.fillMaxWidth().background(IgWhite)) {
            comments.forEach { c -> CommentRow(c, onOpenProfile) { viewModel.onDraft("@${c.author.username} ") } }
            Composer(draft, showGifs, viewModel, onToggleGifs = { showGifs = !showGifs }) {
                showGifs = false
            }
        }
    } else {
        Scaffold(
            topBar = {
                IgSimpleTopBar(
                    title = "Comentários",
                    titleBold = true,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = IgBlack)
                        }
                    }
                )
            },
            bottomBar = {
                Composer(draft, showGifs, viewModel, onToggleGifs = { showGifs = !showGifs }) {
                    showGifs = false
                }
            }
        ) { padding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(IgWhite)
            ) {
                items(comments, key = { it.id }) { c ->
                    CommentRow(c, onOpenProfile) { viewModel.onDraft("@${c.author.username} ") }
                }
            }
        }
    }
}

@Composable
private fun CommentRow(c: Comment, onOpenProfile: (String) -> Unit, onReply: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        IgAvatar(
            c.author.avatarUrl,
            size = 36.dp,
            modifier = Modifier.clickable { onOpenProfile(c.author.id) }
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = IgBlack)) {
                        append(c.author.username)
                    }
                    if (c.text.isNotBlank()) {
                        append("  ")
                        append(c.text)
                    }
                },
                fontSize = 14.sp,
                color = IgBlack,
                lineHeight = 18.sp
            )
            c.mediaUrl?.let { url ->
                AppAsyncImage(
                    data = url,
                    contentDescription = "GIF",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(140.dp)
                )
            }
            Row(
                Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(timeAgo(c.createdAt), fontSize = 12.sp, color = IgGray)
                Text(
                    "Responder",
                    fontSize = 12.sp,
                    color = IgGray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onReply)
                )
            }
        }
        Icon(
            Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = IgGray,
            modifier = Modifier
                .padding(top = 4.dp)
                .size(16.dp)
        )
    }
}

@Composable
private fun Composer(
    draft: String,
    showGifs: Boolean,
    viewModel: CommentsViewModel,
    onToggleGifs: () -> Unit,
    onGifSent: () -> Unit
) {
    Column(Modifier.background(IgWhite)) {
        if (showGifs) {
            GifPicker(onPick = {
                viewModel.sendGif(it)
                onGifSent()
            })
        }
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(QUICK_EMOJIS) { emoji ->
                Text(
                    emoji,
                    fontSize = 24.sp,
                    modifier = Modifier.clickable { viewModel.onDraft(draft + emoji) }
                )
            }
        }
        HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IgAvatar(AppConstants.artworkUrl(AppConstants.ME_POKEMON_ID), 32.dp)
            TextField(
                value = draft,
                onValueChange = viewModel::onDraft,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Participe da conversa...", color = IgGray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = IgWhite,
                    unfocusedContainerColor = IgWhite,
                    focusedIndicatorColor = IgWhite,
                    unfocusedIndicatorColor = IgWhite
                ),
                singleLine = true
            )
            IconButton(onClick = onToggleGifs) {
                Icon(Icons.Outlined.GifBox, "GIF", tint = IgBlack)
            }
            if (draft.isNotBlank()) {
                Text(
                    "Publicar",
                    color = IgBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable(onClick = viewModel::send)
                        .padding(end = 8.dp)
                )
            }
        }
    }
}
