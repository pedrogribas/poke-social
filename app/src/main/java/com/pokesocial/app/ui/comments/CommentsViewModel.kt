package com.pokesocial.app.ui.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CommentsViewModel(
    private val repo: SocialRepository,
    private val postId: String
) : ViewModel() {

    val comments: StateFlow<List<Comment>> = repo.observeComments(postId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft

    fun onDraft(value: String) {
        _draft.value = value
    }

    fun send() {
        val text = _draft.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            repo.addComment(postId, text)
            _draft.value = ""
        }
    }

    fun sendGif(url: String) {
        viewModelScope.launch {
            repo.addComment(postId, "", mediaUrl = url)
            _draft.value = ""
        }
    }
}
