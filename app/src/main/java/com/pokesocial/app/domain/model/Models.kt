package com.pokesocial.app.domain.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val pokemonId: Int,
    val avatarUrl: String,
    val bio: String,
    val followers: Int,
    val following: Int,
    val isMe: Boolean = false,
    val types: List<String> = emptyList()
)

data class Note(
    val userId: String,
    val text: String,
    val updatedAt: Long,
    val user: User
)

data class MediaItem(
    val id: String,
    val url: String,
    val type: String,
    val position: Int,
    val tags: List<MediaTag> = emptyList()
)

data class MediaTag(
    val userId: String,
    val username: String,
    val displayName: String,
    val x: Float,
    val y: Float
)

data class Post(
    val id: String,
    val author: User,
    val caption: String,
    val mediaType: String,
    val media: List<MediaItem>,
    val createdAt: Long,
    val likeCount: Int,
    val commentCount: Int,
    val likedByMe: Boolean,
    val bookmarked: Boolean = false,
    val musicTitle: String? = null,
    val musicArtist: String? = null,
    val showMusicLabel: Boolean = true,
    val originalPostId: String? = null,
    val repostedByMe: Boolean = false,
    val likedPreview: List<User> = emptyList()
)

data class Comment(
    val id: String,
    val postId: String,
    val author: User,
    val text: String,
    val createdAt: Long,
    val mediaUrl: String? = null
)

data class Story(
    val id: String,
    val author: User,
    val mediaUrl: String,
    val createdAt: Long,
    val seenByMe: Boolean,
    val likedByMe: Boolean = false,
    val musicTitle: String? = null,
    val musicArtist: String? = null,
    val showMusicLabel: Boolean = true
)

data class StoryGroup(
    val author: User,
    val stories: List<Story>
) {
    val allSeen: Boolean get() = stories.isNotEmpty() && stories.all { it.seenByMe }
    val previewUrl: String get() = stories.lastOrNull()?.mediaUrl.orEmpty()
}

fun List<Story>.groupedByAuthor(): List<StoryGroup> {
    if (isEmpty()) return emptyList()
    val groups = groupBy { it.author.id }.map { (_, items) ->
        val ordered = items.sortedBy { it.createdAt }
        StoryGroup(author = ordered.first().author, stories = ordered)
    }
    return groups.sortedWith(
        compareByDescending<StoryGroup> { it.author.isMe }
            .thenByDescending { it.stories.maxOf { s -> s.createdAt } }
    )
}

enum class InboxFolder {
    PRIMARY, GENERAL, REQUESTS, ARCHIVED
}

data class Conversation(
    val id: String,
    val peer: User,
    val lastMessage: String,
    val lastMediaUrl: String? = null,
    val updatedAt: Long,
    val unreadCount: Int,
    val folder: InboxFolder = InboxFolder.PRIMARY,
    val isPinned: Boolean = false
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val createdAt: Long,
    val isMine: Boolean,
    val mediaUrl: String? = null,
    val reaction: String? = null
)

data class NotificationItem(
    val id: String,
    val type: String,
    val actor: User,
    val postId: String?,
    val text: String,
    val createdAt: Long,
    val seen: Boolean
)

data class Highlight(
    val id: String,
    val userId: String,
    val title: String,
    val coverUrl: String,
    val mediaUrls: List<String>,
    val createdAt: Long
)
