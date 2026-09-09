package com.pokesocial.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repo: SocialRepository) : ViewModel() {
    val me: StateFlow<User?> = repo.observeMe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            _posts.value = repo.postsByAuthor(AppConstants.ME_USER_ID)
            _loading.value = false
        }
    }
}
