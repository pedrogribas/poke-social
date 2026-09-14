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
    val isMe: Boolean = false,
    /** Comma-separated Pokémon types, e.g. "fighting,steel" */
    val types: String = ""
)

@Entity(
    tableName = "follows",
    primaryKeys = ["followerId", "followeeId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["followerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["followeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("followeeId"), Index("followerId")]
)
data class FollowEntity(
    val followerId: String,
    val followeeId: String
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
    val mediaType: String,
    val createdAt: Long,
    val likeCount: Int,
    val commentCount: Int,
    val musicTitle: String? = null,
    val musicArtist: String? = null,
    val showMusicLabel: Boolean = true,
    /** Original post id when this is a repost */
    val originalPostId: String? = null
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
    val type: String
)

@Entity(
    tableName = "media_tags",
    primaryKeys = ["mediaId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = PostMediaEntity::class,
            parentColumns = ["id"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mediaId"), Index("userId")]
)
data class MediaTagEntity(
    val mediaId: String,
    val userId: String,
    /** Normalized 0..1 position on the media */
    val x: Float,
    val y: Float
)

@Entity(
    tableName = "reposts",
    primaryKeys = ["postId", "userId"],
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
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class RepostEntity(
    val postId: String,
    val userId: String,
    val createdAt: Long
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
    val createdAt: Long,
    val mediaUrl: String? = null
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
    val seenByMe: Boolean = false,
    val likedByMe: Boolean = false,
    val musicTitle: String? = null,
    val musicArtist: String? = null,
    val showMusicLabel: Boolean = true
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val peerUserId: String,
    val updatedAt: Long,
    val unreadCount: Int = 0,
    /** PRIMARY | GENERAL | REQUESTS | ARCHIVED */
    val folder: String = "PRIMARY",
    val isPinned: Boolean = false
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
    val createdAt: Long,
    val mediaUrl: String? = null,
    val reaction: String? = null
)

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["actorUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("actorUserId"), Index("createdAt")]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    /** LIKE | FOLLOW | COMMENT | STORY_REPLY */
    val type: String,
    val actorUserId: String,
    val postId: String? = null,
    val text: String,
    val createdAt: Long,
    val seen: Boolean = false
)

@Entity(
    tableName = "highlights",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class HighlightEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val coverUrl: String,
    /** Comma-separated media URLs */
    val mediaUrls: String,
    val createdAt: Long
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteEntity(
    @PrimaryKey val userId: String,
    val text: String,
    val updatedAt: Long
)

@Entity(
    tableName = "bookmarks",
    primaryKeys = ["postId", "userId"],
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
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class BookmarkEntity(
    val postId: String,
    val userId: String
)
