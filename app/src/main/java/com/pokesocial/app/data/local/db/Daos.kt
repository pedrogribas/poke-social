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

    @Query("SELECT * FROM users WHERE username LIKE '%' || :q || '%' OR displayName LIKE '%' || :q || '%' LIMIT 50")
    suspend fun search(q: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE isMe = 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun randomOthers(limit: Int): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: List<PostMediaEntity>)

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

    @Query("DELETE FROM likes WHERE postId = :postId AND userId = :userId")
    suspend fun delete(postId: String, userId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE postId = :postId AND userId = :userId)")
    suspend fun isLiked(postId: String, userId: String): Boolean

    @Query("SELECT postId FROM likes WHERE userId = :userId")
    suspend fun likedPostIds(userId: String): List<String>
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

    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversation(id: String): ConversationEntity?

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

    @Transaction
    suspend fun sendAndTouch(message: MessageEntity, conversation: ConversationEntity) {
        insertMessage(message)
        updateConversation(conversation)
    }
}
