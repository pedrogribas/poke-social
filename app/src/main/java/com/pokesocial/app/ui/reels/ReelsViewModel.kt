package com.pokesocial.app.ui.reels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Conversation
import com.pokesocial.app.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReelsViewModel(private val repo: SocialRepository) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _following = MutableStateFlow<Set<String>>(emptySet())
    val following: StateFlow<Set<String>> = _following.asStateFlow()

    val conversations: StateFlow<List<Conversation>> =
        repo.observeAllConversations().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _following.value = repo.followingOf(AppConstants.ME_USER_ID).map { it.id }.toSet()
            _posts.value = repo.videoPosts()
            _loading.value = false
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val updated = repo.toggleLike(postId) ?: return@launch
            _posts.value = _posts.value.map { if (it.id == postId) updated else it }
        }
    }

    fun toggleBookmark(postId: String) {
        viewModelScope.launch {
            val updated = repo.toggleBookmark(postId) ?: return@launch
            _posts.value = _posts.value.map { if (it.id == postId) updated else it }
        }
    }

    fun toggleFollow(userId: String) {
        viewModelScope.launch {
            val nowFollowing = repo.toggleFollow(userId)
            _following.value = if (nowFollowing) {
                _following.value + userId
            } else {
                _following.value - userId
            }
        }
    }

    fun repost(postId: String) {
        viewModelScope.launch {
            val updated = repo.toggleRepost(postId) ?: return@launch
            _posts.value = _posts.value.map { if (it.id == postId) updated else it }
        }
    }

    fun sharePost(conversationId: String, post: Post) {
        viewModelScope.launch {
            repo.sharePostToConversation(conversationId, post)
        }
    }
}
