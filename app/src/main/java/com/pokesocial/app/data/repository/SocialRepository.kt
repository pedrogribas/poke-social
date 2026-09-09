package com.pokesocial.app.data.repository

import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.data.local.db.AppDatabase
import com.pokesocial.app.data.local.db.CommentEntity
import com.pokesocial.app.data.local.db.LikeEntity
import com.pokesocial.app.data.local.db.MessageEntity
import com.pokesocial.app.data.local.db.PostEntity
import com.pokesocial.app.data.local.db.UserEntity
import com.pokesocial.app.domain.model.ChatMessage
import com.pokesocial.app.domain.model.Comment
import com.pokesocial.app.domain.model.Conversation
import com.pokesocial.app.domain.model.MediaItem
import com.pokesocial.app.domain.model.Post
import com.pokesocial.app.domain.model.Story
import com.pokesocial.app.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class SocialRepository(private val db: AppDatabase) {

    fun observeMe(): Flow<User?> = db.userDao().observeMe().map { it?.toDomain() }

    suspend fun getMe(): User? = withContext(Dispatchers.IO) {
        db.userDao().getMe()?.toDomain()
    }

    suspend fun feedPage(offset: Int, limit: Int = AppConstants.FEED_PAGE_SIZE): List<Post> =
        withContext(Dispatchers.IO) {
            db.postDao().feedPage(limit, offset).map { it.toDomain() }
        }

    suspend fun randomExplore(limit: Int = 60): List<Post> = withContext(Dispatchers.IO) {
        db.postDao().randomPosts(limit).map { it.toDomain() }
    }

    suspend fun videoPosts(): List<Post> = withContext(Dispatchers.IO) {
        db.postDao().videoPosts().map { it.toDomain() }
    }

    suspend fun postsByAuthor(authorId: String): List<Post> = withContext(Dispatchers.IO) {
        db.postDao().listByAuthor(authorId).map { it.toDomain() }
    }

    suspend fun getPost(id: String): Post? = withContext(Dispatchers.IO) {
        db.postDao().getById(id)?.toDomain()
    }

    fun observeStories(): Flow<List<Story>> = flow {
        db.storyDao().observeAll().collect { list ->
            emit(list.mapNotNull { s ->
                val author = db.userDao().getById(s.authorId)?.toDomain() ?: return@mapNotNull null
                Story(s.id, author, s.mediaUrl, s.createdAt, s.seenByMe)
            })
        }
    }.flowOn(Dispatchers.IO)

    suspend fun storiesOnce(): List<Story> = withContext(Dispatchers.IO) {
        db.storyDao().all().mapNotNull { s ->
            val author = db.userDao().getById(s.authorId)?.toDomain() ?: return@mapNotNull null
            Story(s.id, author, s.mediaUrl, s.createdAt, s.seenByMe)
        }
    }

    suspend fun markStorySeen(id: String) = withContext(Dispatchers.IO) {
        db.storyDao().markSeen(id)
    }

    suspend fun toggleLike(postId: String): Post? = withContext(Dispatchers.IO) {
        val me = AppConstants.ME_USER_ID
        val post = db.postDao().getById(postId) ?: return@withContext null
        val liked = db.likeDao().isLiked(postId, me)
        if (liked) {
            db.likeDao().delete(postId, me)
            db.postDao().updateLikeCount(postId, (post.likeCount - 1).coerceAtLeast(0))
        } else {
            db.likeDao().insert(LikeEntity(postId, me))
            db.postDao().updateLikeCount(postId, post.likeCount + 1)
        }
        db.postDao().getById(postId)?.toDomain()
    }

    fun observeComments(postId: String): Flow<List<Comment>> = flow {
        db.commentDao().observeForPost(postId).collect { list ->
            emit(list.mapNotNull { c ->
                val author = db.userDao().getById(c.authorId)?.toDomain() ?: return@mapNotNull null
                Comment(c.id, c.postId, author, c.text, c.createdAt)
            })
        }
    }.flowOn(Dispatchers.IO)

    suspend fun addComment(postId: String, text: String): Comment? = withContext(Dispatchers.IO) {
        val me = db.userDao().getMe() ?: return@withContext null
        val entity = CommentEntity(
            id = "c-${UUID.randomUUID()}",
            postId = postId,
            authorId = me.id,
            text = text.trim(),
            createdAt = System.currentTimeMillis()
        )
        db.commentDao().insert(entity)
        val count = db.commentDao().countForPost(postId)
        db.postDao().updateCommentCount(postId, count)
        Comment(entity.id, postId, me.toDomain(), entity.text, entity.createdAt)
    }

    fun observeConversations(): Flow<List<Conversation>> = flow {
        db.chatDao().observeConversations().collect { list ->
            emit(list.mapNotNull { conv ->
                val peer = db.userDao().getById(conv.peerUserId)?.toDomain() ?: return@mapNotNull null
                val last = db.chatDao().lastMessage(conv.id)
                Conversation(
                    id = conv.id,
                    peer = peer,
                    lastMessage = last?.text.orEmpty(),
                    updatedAt = conv.updatedAt,
                    unreadCount = conv.unreadCount
                )
            })
        }
    }.flowOn(Dispatchers.IO)

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> =
        db.chatDao().observeMessages(conversationId).map { list ->
            list.map {
                ChatMessage(
                    id = it.id,
                    conversationId = it.conversationId,
                    senderId = it.senderId,
                    text = it.text,
                    createdAt = it.createdAt,
                    isMine = it.senderId == AppConstants.ME_USER_ID
                )
            }
        }

    suspend fun sendMessage(conversationId: String, text: String) = withContext(Dispatchers.IO) {
        val conv = db.chatDao().getConversation(conversationId) ?: return@withContext
        val now = System.currentTimeMillis()
        db.chatDao().sendAndTouch(
            MessageEntity(
                id = "m-${UUID.randomUUID()}",
                conversationId = conversationId,
                senderId = AppConstants.ME_USER_ID,
                text = text.trim(),
                createdAt = now
            ),
            conv.copy(updatedAt = now, unreadCount = 0)
        )
    }

    suspend fun autoReply(conversationId: String) = withContext(Dispatchers.IO) {
        delay(900)
        val conv = db.chatDao().getConversation(conversationId) ?: return@withContext
        val replies = listOf(
            "Haha verdade 😂",
            "Partiu batalha então!",
            "Aura detectada.",
            "Te vejo no ginásio.",
            "gg lucar_10"
        )
        val now = System.currentTimeMillis()
        db.chatDao().sendAndTouch(
            MessageEntity(
                id = "m-${UUID.randomUUID()}",
                conversationId = conversationId,
                senderId = conv.peerUserId,
                text = replies.random(),
                createdAt = now
            ),
            conv.copy(updatedAt = now)
        )
    }

    suspend fun searchUsers(q: String): List<User> = withContext(Dispatchers.IO) {
        if (q.isBlank()) emptyList() else db.userDao().search(q).map { it.toDomain() }
    }

    suspend fun clearUnread(conversationId: String) = withContext(Dispatchers.IO) {
        val conv = db.chatDao().getConversation(conversationId) ?: return@withContext
        db.chatDao().updateConversation(conv.copy(unreadCount = 0))
    }

    private suspend fun PostEntity.toDomain(): Post {
        val author = db.userDao().getById(authorId)?.toDomain()
            ?: User(authorId, "unknown", "Unknown", 0, "", "", 0, 0)
        val media = db.postDao().mediaFor(id).map {
            MediaItem(it.id, it.url, it.type, it.position)
        }
        val liked = db.likeDao().isLiked(id, AppConstants.ME_USER_ID)
        return Post(
            id = id,
            author = author,
            caption = caption,
            mediaType = mediaType,
            media = media,
            createdAt = createdAt,
            likeCount = likeCount,
            commentCount = commentCount,
            likedByMe = liked
        )
    }
}

fun UserEntity.toDomain() = User(
    id = id,
    username = username,
    displayName = displayName,
    pokemonId = pokemonId,
    avatarUrl = avatarUrl,
    bio = bio,
    followers = followers,
    following = following,
    isMe = isMe
)
