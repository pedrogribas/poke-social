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
    val isMe: Boolean = false
)

data class MediaItem(
    val id: String,
    val url: String,
    val type: String, // IMAGE | VIDEO
    val position: Int
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
    val bookmarked: Boolean = false
)

data class Comment(
    val id: String,
    val postId: String,
    val author: User,
    val text: String,
    val createdAt: Long
)

data class Story(
    val id: String,
    val author: User,
    val mediaUrl: String,
    val createdAt: Long,
    val seenByMe: Boolean
)

data class Conversation(
    val id: String,
    val peer: User,
    val lastMessage: String,
    val updatedAt: Long,
    val unreadCount: Int
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val createdAt: Long,
    val isMine: Boolean
)
