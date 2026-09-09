package com.pokesocial.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.ChatMessage
import com.pokesocial.app.domain.model.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InboxViewModel(repo: SocialRepository) : ViewModel() {
    val conversations: StateFlow<List<Conversation>> = repo.observeConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class ChatThreadViewModel(
    private val repo: SocialRepository,
    private val conversationId: String
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = repo.observeMessages(conversationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft

    init {
        viewModelScope.launch { repo.clearUnread(conversationId) }
    }

    fun onDraft(v: String) {
        _draft.value = v
    }

    fun send() {
        val text = _draft.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            repo.sendMessage(conversationId, text)
            _draft.value = ""
            repo.autoReply(conversationId)
        }
    }
}
