package com.pokesocial.app.ui.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokesocial.app.ui.components.IgAvatar
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgLightGray
import com.pokesocial.app.ui.theme.IgWhite
import com.pokesocial.app.ui.util.timeAgo

@Composable
fun CommentsScreen(
    viewModel: CommentsViewModel,
    onBack: () -> Unit
) {
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = "Comentários",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = IgBlack)
                    }
                }
            )
        },
        bottomBar = {
            Column(Modifier.background(IgWhite)) {
                HorizontalDivider(color = IgLightGray, thickness = 0.5.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = draft,
                        onValueChange = viewModel::onDraft,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Adicione um comentário…", color = IgGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = IgWhite,
                            unfocusedContainerColor = IgWhite,
                            focusedIndicatorColor = IgWhite,
                            unfocusedIndicatorColor = IgWhite
                        ),
                        singleLine = true
                    )
                    TextButton(onClick = viewModel::send, enabled = draft.isNotBlank()) {
                        Text("Publicar", color = IgBlue, fontWeight = FontWeight.SemiBold)
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
        ) {
            items(comments, key = { it.id }) { c ->
                Row(Modifier.padding(16.dp)) {
                    IgAvatar(c.author.avatarUrl, size = 32.dp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            buildString {
                                append(c.author.username)
                                append("  ")
                                append(c.text)
                            },
                            fontSize = 13.sp,
                            color = IgBlack
                        )
                        Text(timeAgo(c.createdAt), fontSize = 11.sp, color = IgGray)
                    }
                }
            }
        }
    }
}
