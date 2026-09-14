package com.pokesocial.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.ChatMessage
import com.pokesocial.app.domain.model.Conversation
import com.pokesocial.app.domain.model.InboxFolder
import com.pokesocial.app.domain.model.Note
import com.pokesocial.app.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModel(private val repo: SocialRepository) : ViewModel() {
    private val _folder = MutableStateFlow(InboxFolder.PRIMARY)
    val folder: StateFlow<InboxFolder> = _folder

    val conversations: StateFlow<List<Conversation>> = _folder
        .flatMapLatest { repo.observeConversations(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allConversations: StateFlow<List<Conversation>> = repo.observeAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notes: StateFlow<List<Note>> = repo.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFolder(folder: InboxFolder) {
        _folder.value = folder
    }

    fun archive(id: String) {
        viewModelScope.launch { repo.setConversationFolder(id, InboxFolder.ARCHIVED) }
    }

    fun moveTo(id: String, folder: InboxFolder) {
        viewModelScope.launch { repo.setConversationFolder(id, folder) }
    }

    fun togglePin(id: String) {
        viewModelScope.launch { repo.togglePin(id) }
    }

    fun upsertMyNote(text: String) {
        viewModelScope.launch { repo.upsertMyNote(text) }
    }

    fun openChatWith(userId: String, onReady: (String) -> Unit) {
        viewModelScope.launch {
            onReady(repo.getOrCreateConversation(userId))
        }
    }
}

class ChatThreadViewModel(
    private val repo: SocialRepository,
    private val conversationId: String
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = repo.observeMessages(conversationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _peer = MutableStateFlow<User?>(null)
    val peer: StateFlow<User?> = _peer

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft

    init {
        viewModelScope.launch {
            _peer.value = repo.conversationPeer(conversationId)
            repo.clearUnread(conversationId)
        }
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

    fun sendGif(url: String) {
        viewModelScope.launch {
            repo.sendMessage(conversationId, "", mediaUrl = url)
            repo.autoReply(conversationId)
        }
    }

    fun toggleReaction(messageId: String) {
        viewModelScope.launch {
            repo.toggleHeartReaction(messageId)
        }
    }

    fun sendBattleAftermath(won: Boolean, fled: Boolean) {
        viewModelScope.launch {
            repo.sendBattleAftermath(conversationId, won, fled)
        }
    }
}
