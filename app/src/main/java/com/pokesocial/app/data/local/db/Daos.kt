package com.pokesocial.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaDao {
    @Query("SELECT value FROM meta WHERE key = :key LIMIT 1")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(meta: MetaEntity)
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("SELECT * FROM users WHERE isMe = 1 LIMIT 1")
    suspend fun getMe(): UserEntity?

    @Query("SELECT * FROM users WHERE isMe = 1 LIMIT 1")
    fun observeMe(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE username LIKE '%' || :q || '%' OR displayName LIKE '%' || :q || '%' LIMIT 50")
    suspend fun search(q: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE isMe = 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun randomOthers(limit: Int): List<UserEntity>

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<UserEntity>

    @Query("UPDATE users SET followers = :followers, following = :following WHERE id = :id")
    suspend fun updateFollowCounts(id: String, followers: Int, following: Int)

    @Query("UPDATE users SET displayName = :displayName, bio = :bio WHERE id = :id")
    suspend fun updateProfile(id: String, displayName: String, bio: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}

@Dao
interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<FollowEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun follow(item: FollowEntity): Long

    @Query("DELETE FROM follows WHERE followerId = :followerId AND followeeId = :followeeId")
    suspend fun unfollow(followerId: String, followeeId: String)

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM follows
            WHERE followerId = :followerId AND followeeId = :followeeId
        )
        """
    )
    suspend fun isFollowing(followerId: String, followeeId: String): Boolean

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM follows
            WHERE followerId = :followerId AND followeeId = :followeeId
        )
        """
    )
    fun observeIsFollowing(followerId: String, followeeId: String): Flow<Boolean>

    @Query(
        """
        SELECT u.* FROM users u
        INNER JOIN follows f ON f.followerId = u.id
        WHERE f.followeeId = :userId
        ORDER BY u.username ASC
        """
    )
    suspend fun followersOf(userId: String): List<UserEntity>

    @Query(
        """
        SELECT u.* FROM users u
        INNER JOIN follows f ON f.followeeId = u.id
        WHERE f.followerId = :userId
        ORDER BY u.username ASC
        """
    )
    suspend fun followingOf(userId: String): List<UserEntity>

    @Query("SELECT COUNT(*) FROM follows WHERE followeeId = :userId")
    suspend fun followersCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    suspend fun followingCount(userId: String): Int
}

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: List<PostMediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(media: PostMediaEntity)

    @Query(
        """
        SELECT * FROM posts
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun feedPage(limit: Int, offset: Int): List<PostEntity>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getById(id: String): PostEntity?

    @Query("SELECT * FROM post_media WHERE postId = :postId ORDER BY position ASC")
    suspend fun mediaFor(postId: String): List<PostMediaEntity>

    @Query("SELECT * FROM posts WHERE mediaType = 'VIDEO' ORDER BY RANDOM()")
    suspend fun videoPosts(): List<PostEntity>

    @Query("SELECT * FROM posts ORDER BY RANDOM() LIMIT :limit")
    suspend fun randomPosts(limit: Int): List<PostEntity>

    @Query(
        """
        SELECT * FROM posts
        WHERE caption LIKE '%' || :q || '%'
        ORDER BY createdAt DESC
        LIMIT :limit
        """
    )
    suspend fun searchByCaption(q: String, limit: Int = 60): List<PostEntity>

    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun observeByAuthor(authorId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    suspend fun listByAuthor(authorId: String): List<PostEntity>

    @Query("SELECT COUNT(*) FROM posts WHERE authorId = :authorId")
    suspend fun countByAuthor(authorId: String): Int

    @Query("UPDATE posts SET likeCount = :count WHERE id = :postId")
    suspend fun updateLikeCount(postId: String, count: Int)

    @Query("UPDATE posts SET commentCount = :count WHERE id = :postId")
    suspend fun updateCommentCount(postId: String, count: Int)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int
}

@Dao
interface LikeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(like: LikeEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(likes: List<LikeEntity>)

    @Query("DELETE FROM likes WHERE postId = :postId AND userId = :userId")
    suspend fun delete(postId: String, userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE postId = :postId AND userId = :userId)")
    suspend fun isLiked(postId: String, userId: String): Boolean

    @Query("SELECT postId FROM likes WHERE userId = :userId")
    suspend fun likedPostIds(userId: String): List<String>

    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId")
    suspend fun countForPost(postId: String): Int

    @Query(
        """
        SELECT u.* FROM users u
        INNER JOIN likes l ON l.userId = u.id
        WHERE l.postId = :postId
        ORDER BY u.username ASC
        LIMIT :limit
        """
    )
    suspend fun likersFor(postId: String, limit: Int = 200): List<UserEntity>

    @Query(
        """
        SELECT u.* FROM users u
        INNER JOIN likes l ON l.userId = u.id
        WHERE l.postId = :postId
        ORDER BY CASE WHEN u.isMe = 1 THEN 0 ELSE 1 END, u.username ASC
        LIMIT :limit
        """
    )
    suspend fun likersPreview(postId: String, limit: Int = 3): List<UserEntity>
}

@Dao
interface MediaTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<MediaTagEntity>)

    @Query("SELECT * FROM media_tags WHERE mediaId = :mediaId")
    suspend fun forMedia(mediaId: String): List<MediaTagEntity>

    @Query("SELECT * FROM media_tags WHERE mediaId IN (:mediaIds)")
    suspend fun forMediaIds(mediaIds: List<String>): List<MediaTagEntity>
}

@Dao
interface RepostDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(repost: RepostEntity): Long

    @Query("DELETE FROM reposts WHERE postId = :postId AND userId = :userId")
    suspend fun delete(postId: String, userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM reposts WHERE postId = :postId AND userId = :userId)")
    suspend fun isReposted(postId: String, userId: String): Boolean
}

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CommentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity)

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun observeForPost(postId: String): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    suspend fun forPost(postId: String): List<CommentEntity>

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun countForPost(postId: String): Int
}

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stories: List<StoryEntity>)

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    suspend fun all(): List<StoryEntity>

    @Query("UPDATE stories SET seenByMe = 1 WHERE id = :id")
    suspend fun markSeen(id: String)

    @Query("UPDATE stories SET seenByMe = 1 WHERE authorId = :authorId")
    suspend fun markAuthorSeen(authorId: String)

    @Query("UPDATE stories SET likedByMe = :liked WHERE id = :id")
    suspend fun setLiked(id: String, liked: Boolean)
}

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(items: List<ConversationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(items: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE folder = :folder ORDER BY isPinned DESC, updatedAt DESC")
    fun observeByFolder(folder: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversation(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE peerUserId = :peerUserId LIMIT 1")
    suspend fun getByPeer(peerUserId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET folder = :folder WHERE id = :id")
    suspend fun setFolder(id: String, folder: String)

    @Query("UPDATE conversations SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    @Query(
        """
        SELECT * FROM messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt DESC LIMIT 1
        """
    )
    suspend fun lastMessage(conversationId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessage(id: String): MessageEntity?

    @Query("UPDATE messages SET reaction = :reaction WHERE id = :id")
    suspend fun setReaction(id: String, reaction: String?)

    @Transaction
    suspend fun sendAndTouch(message: MessageEntity, conversation: ConversationEntity) {
        insertMessage(message)
        updateConversation(conversation)
    }
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NotificationEntity>)

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    suspend fun all(): List<NotificationEntity>

    @Query("UPDATE notifications SET seen = 1")
    suspend fun markAllSeen()

    @Query("UPDATE notifications SET seen = 1 WHERE id = :id")
    suspend fun markSeen(id: String)
}

@Dao
interface HighlightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HighlightEntity>)

    @Query("SELECT * FROM highlights WHERE userId = :userId ORDER BY createdAt ASC")
    fun observeByUser(userId: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE userId = :userId ORDER BY createdAt ASC")
    suspend fun byUser(userId: String): List<HighlightEntity>

    @Query("SELECT * FROM highlights WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): HighlightEntity?
}

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId LIMIT 1")
    suspend fun getByUser(userId: String): NoteEntity?

    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun delete(userId: String)
}

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE postId = :postId AND userId = :userId")
    suspend fun delete(postId: String, userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE postId = :postId AND userId = :userId)")
    suspend fun isBookmarked(postId: String, userId: String): Boolean

    @Query("SELECT postId FROM bookmarks WHERE userId = :userId")
    suspend fun bookmarkedPostIds(userId: String): List<String>
}
