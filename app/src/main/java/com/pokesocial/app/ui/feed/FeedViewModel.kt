package com.pokesocial.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.domain.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val stories: List<Story> = emptyList(),
    val posts: List<Post> = emptyList(),
    val initialLoading: Boolean = true,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null
)

class FeedViewModel(
    private val repo: SocialRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(FeedUiState())
    val ui: StateFlow<FeedUiState> = _ui.asStateFlow()

    private var offset = 0

    init {
        viewModelScope.launch {
            repo.observeStories().collect { stories ->
                _ui.update { it.copy(stories = stories) }
            }
        }
        refresh(initial = true)
    }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            _ui.update {
                it.copy(
                    refreshing = !initial,
                    initialLoading = initial,
                    error = null,
                    endReached = false
                )
            }
            offset = 0
            runCatching {
                val page = repo.feedPage(0)
                offset = page.size
                _ui.update {
                    it.copy(
                        posts = page,
                        initialLoading = false,
                        refreshing = false,
                        endReached = page.size < AppConstants.FEED_PAGE_SIZE
                    )
                }
            }.onFailure { e ->
                _ui.update {
                    it.copy(
                        initialLoading = false,
                        refreshing = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun loadMore() {
        val state = _ui.value
        if (state.loadingMore || state.endReached || state.initialLoading) return
        viewModelScope.launch {
            _ui.update { it.copy(loadingMore = true) }
            runCatching {
                val page = repo.feedPage(offset)
                offset += page.size
                _ui.update {
                    it.copy(
                        posts = it.posts + page,
                        loadingMore = false,
                        endReached = page.size < AppConstants.FEED_PAGE_SIZE
                    )
                }
            }.onFailure {
                _ui.update { it.copy(loadingMore = false) }
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val updated = repo.toggleLike(postId) ?: return@launch
            _ui.update { st ->
                st.copy(posts = st.posts.map { if (it.id == postId) updated else it })
            }
        }
    }
}
