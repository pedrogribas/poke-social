package com.pokesocial.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUi(
    val loading: Boolean = true,
    val posts: List<Post> = emptyList(),
    val query: String = "",
    val users: List<User> = emptyList()
)

class ExploreViewModel(private val repo: SocialRepository) : ViewModel() {
    private val _ui = MutableStateFlow(ExploreUi())
    val ui: StateFlow<ExploreUi> = _ui.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            val posts = repo.randomExplore(60)
            _ui.update { it.copy(loading = false, posts = posts) }
        }
    }

    fun onQuery(q: String) {
        _ui.update { it.copy(query = q) }
        viewModelScope.launch {
            _ui.update { it.copy(users = repo.searchUsers(q)) }
        }
    }
}
