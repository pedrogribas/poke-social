package com.pokesocial.app.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokesocial.app.data.repository.SocialRepository
import com.pokesocial.app.domain.model.Conversation
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.ui.comments.CommentsScreen
import com.pokesocial.app.ui.comments.CommentsViewModel
import com.pokesocial.app.ui.components.IgPostCard
import com.pokesocial.app.ui.components.IgSimpleTopBar
import com.pokesocial.app.ui.components.SharePostSheet
import com.pokesocial.app.ui.theme.IgBlue
import com.pokesocial.app.ui.theme.IgWhite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val repo: SocialRepository,
    private val postId: String
) : ViewModel() {
    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    val conversations: StateFlow<List<Conversation>> = repo.observeAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { _post.value = repo.getPost(postId) }
    }

    fun toggleLike() {
        viewModelScope.launch {
            _post.value = repo.toggleLike(postId) ?: _post.value
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            _post.value = repo.toggleBookmark(postId) ?: _post.value
        }
    }

    fun shareTo(conversationId: String) {
        viewModelScope.launch {
            val p = _post.value ?: return@launch
            repo.sharePostToConversation(conversationId, p)
        }
    }
}

class PostDetailViewModelFactory(
    private val repo: SocialRepository,
    private val postId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PostDetailViewModel(repo, postId) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    repo: SocialRepository,
    commentsFactory: ViewModelProvider.Factory,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onOpenLikes: (String) -> Unit = {}
) {
    val vm: PostDetailViewModel = viewModel(
        key = "post-$postId",
        factory = PostDetailViewModelFactory(repo, postId)
    )
    val commentsVm: CommentsViewModel = viewModel(
        key = "comments-$postId",
        factory = commentsFactory
    )
    val post by vm.post.collectAsStateWithLifecycle()
    val conversations by vm.conversations.collectAsStateWithLifecycle()
    var showShare by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            IgSimpleTopBar(
                title = "Publicação",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        val p = post
        if (p == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(IgWhite),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IgBlue)
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(IgWhite)
                    .verticalScroll(rememberScrollState())
            ) {
                IgPostCard(
                    post = p,
                    onLike = vm::toggleLike,
                    onComment = {},
                    onAuthorClick = { onOpenProfile(p.author.id) },
                    onHashtagClick = onHashtagClick,
                    onBookmark = vm::toggleBookmark,
                    onShare = { showShare = true },
                    onLikesClick = { onOpenLikes(p.id) },
                    onTaggedUserClick = onOpenProfile
                )
                CommentsScreen(
                    viewModel = commentsVm,
                    onBack = onBack,
                    onOpenProfile = onOpenProfile,
                    embedded = true
                )
            }
        }
    }

    if (showShare) {
        SharePostSheet(
            conversations = conversations,
            onSelect = { conv ->
                vm.shareTo(conv.id)
                showShare = false
            },
            onDismiss = { showShare = false }
        )
    }
}
