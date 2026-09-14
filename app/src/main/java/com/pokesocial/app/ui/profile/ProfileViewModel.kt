package com.pokesocial.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Highlight
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repo: SocialRepository,
    private val userId: String
) : ViewModel() {

    val user: StateFlow<User?> = repo.observeUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isFollowing: StateFlow<Boolean> = repo.observeIsFollowing(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val highlights: StateFlow<List<Highlight>> = repo.observeHighlights(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _followedBy = MutableStateFlow<List<User>>(emptyList())
    val followedBy: StateFlow<List<User>> = _followedBy.asStateFlow()

    private val _followedByExtra = MutableStateFlow(0)
    val followedByExtra: StateFlow<Int> = _followedByExtra.asStateFlow()

    init {
        viewModelScope.launch {
            _posts.value = repo.postsByAuthor(userId)
            val preview = repo.followedByPreview(userId)
            _followedBy.value = preview.first
            _followedByExtra.value = preview.second
            _loading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _posts.value = repo.postsByAuthor(userId)
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            repo.toggleFollow(userId)
        }
    }

    fun updateProfile(displayName: String, bio: String) {
        viewModelScope.launch {
            repo.updateMyProfile(displayName, bio)
        }
    }

    fun openChat(onReady: (String) -> Unit) {
        viewModelScope.launch {
            onReady(repo.getOrCreateConversation(userId))
        }
    }
}
