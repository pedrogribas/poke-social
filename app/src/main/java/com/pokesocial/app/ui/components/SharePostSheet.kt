package com.pokesocial.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesocial.app.domain.model.Conversation
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePostSheet(
    conversations: List<Conversation>,
    onSelect: (Conversation) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                "Enviar para",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = IgBlack,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (conversations.isEmpty()) {
                Text(
                    "Nenhuma conversa ainda",
                    color = IgGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(Modifier.height(360.dp)) {
                    items(conversations, key = { it.id }) { conv ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(conv) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IgAvatar(conv.peer.avatarUrl, 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    conv.peer.username,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IgBlack,
                                    fontSize = 14.sp
                                )
                                Text(
                                    conv.peer.displayName,
                                    color = IgGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
