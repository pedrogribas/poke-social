package com.pokesocial.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pokesocial.app.PokeSocialApp
import com.pokesocial.app.ui.chat.ChatThreadViewModel
import com.pokesocial.app.ui.chat.InboxViewModel
import com.pokesocial.app.ui.comments.CommentsViewModel
import com.pokesocial.app.ui.explore.ExploreViewModel
import com.pokesocial.app.ui.feed.FeedViewModel
import com.pokesocial.app.ui.profile.ProfileViewModel
import com.pokesocial.app.ui.reels.ReelsViewModel
import com.pokesocial.app.ui.splash.SplashViewModel

class AppViewModelFactory(
    private val app: PokeSocialApp,
    private val postId: String? = null,
    private val conversationId: String? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = app.socialRepository
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel(app.seedManager) as T
            modelClass.isAssignableFrom(FeedViewModel::class.java) ->
                FeedViewModel(repo) as T
            modelClass.isAssignableFrom(ExploreViewModel::class.java) ->
                ExploreViewModel(repo) as T
            modelClass.isAssignableFrom(ReelsViewModel::class.java) ->
                ReelsViewModel(repo) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(repo) as T
            modelClass.isAssignableFrom(InboxViewModel::class.java) ->
                InboxViewModel(repo) as T
            modelClass.isAssignableFrom(CommentsViewModel::class.java) ->
                CommentsViewModel(repo, requireNotNull(postId)) as T
            modelClass.isAssignableFrom(ChatThreadViewModel::class.java) ->
                ChatThreadViewModel(repo, requireNotNull(conversationId)) as T
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        }
    }
}
