package com.pokesocial.app.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val pokemonId: Int,
    val avatarUrl: String,
    val bio: String,
    val followers: Int,
    val following: Int,
    val isMe: Boolean = false
)

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("authorId"), Index("createdAt")]
)
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val caption: String,
    val mediaType: String, // IMAGE | CAROUSEL | VIDEO
    val createdAt: Long,
    val likeCount: Int,
    val commentCount: Int
)

@Entity(
    tableName = "post_media",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("postId")]
)
data class PostMediaEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val url: String,
    val position: Int,
    val type: String // IMAGE | VIDEO
)

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("postId"), Index("authorId")]
)
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorId: String,
    val text: String,
    val createdAt: Long
)

@Entity(
    tableName = "likes",
    primaryKeys = ["postId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class LikeEntity(
    val postId: String,
    val userId: String
)

@Entity(
    tableName = "stories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("authorId")]
)
data class StoryEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val mediaUrl: String,
    val createdAt: Long,
    val seenByMe: Boolean = false
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val peerUserId: String,
    val updatedAt: Long,
    val unreadCount: Int = 0
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val createdAt: Long
)
