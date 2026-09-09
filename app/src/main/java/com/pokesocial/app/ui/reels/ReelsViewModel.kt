package com.pokesocial.app.ui.reels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReelsViewModel(private val repo: SocialRepository) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
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
}
