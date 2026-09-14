package com.pokesocial.app.data.repository

import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.core.LocalGifs
import com.pokesocial.app.core.PokemonChatBrain
import com.pokesocial.app.data.local.db.AppDatabase
import com.pokesocial.app.data.local.db.BookmarkEntity
import com.pokesocial.app.data.local.db.CommentEntity
import com.pokesocial.app.data.local.db.ConversationEntity
import com.pokesocial.app.data.local.db.FollowEntity
import com.pokesocial.app.data.local.db.HighlightEntity
import com.pokesocial.app.data.local.db.LikeEntity
import com.pokesocial.app.data.local.db.MessageEntity
import com.pokesocial.app.data.local.db.NoteEntity
import com.pokesocial.app.data.local.db.PostEntity
import com.pokesocial.app.data.local.db.PostMediaEntity
import com.pokesocial.app.data.local.db.RepostEntity
import com.pokesocial.app.data.local.db.UserEntity
import com.pokesocial.app.domain.model.ChatMessage
import com.pokesocial.app.domain.model.Comment
import com.pokesocial.app.domain.model.Conversation
import com.pokesocial.app.domain.model.Highlight
import com.pokesocial.app.domain.model.InboxFolder
import com.pokesocial.app.domain.model.MediaItem
import com.pokesocial.app.domain.model.MediaTag
import com.pokesocial.app.domain.model.Note
import com.pokesocial.app.domain.model.NotificationItem
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

    fun observeUser(userId: String): Flow<User?> =
        db.userDao().observeById(userId).map { it?.toDomain() }

    suspend fun getUser(userId: String): User? = withContext(Dispatchers.IO) {
        db.userDao().getById(userId)?.toDomain()
    }

    suspend fun getMe(): User? = withContext(Dispatchers.IO) {
        db.userDao().getMe()?.toDomain()
    }

    suspend fun updateMyProfile(displayName: String, bio: String) = withContext(Dispatchers.IO) {
        db.userDao().updateProfile(AppConstants.ME_USER_ID, displayName.trim(), bio.trim())
    }

    fun observeIsFollowing(userId: String): Flow<Boolean> =
        db.followDao().observeIsFollowing(AppConstants.ME_USER_ID, userId)

    suspend fun isFollowing(userId: String): Boolean = withContext(Dispatchers.IO) {
        db.followDao().isFollowing(AppConstants.ME_USER_ID, userId)
    }

    suspend fun toggleFollow(userId: String): Boolean = withContext(Dispatchers.IO) {
        val me = AppConstants.ME_USER_ID
        if (userId == me) return@withContext false
        val following = db.followDao().isFollowing(me, userId)
        if (following) {
            db.followDao().unfollow(me, userId)
        } else {
            db.followDao().follow(FollowEntity(me, userId))
        }
        recountFollows(me)
        recountFollows(userId)
        !following
    }

    private suspend fun recountFollows(userId: String) {
        db.userDao().updateFollowCounts(
            userId,
            db.followDao().followersCount(userId),
            db.followDao().followingCount(userId)
        )
    }

    suspend fun followersOf(userId: String): List<User> = withContext(Dispatchers.IO) {
        db.followDao().followersOf(userId).map { it.toDomain() }
    }

    suspend fun followingOf(userId: String): List<User> = withContext(Dispatchers.IO) {
        db.followDao().followingOf(userId).map { it.toDomain() }
    }

    /** Pessoas que eu sigo e que também seguem este perfil (estilo “Seguido(a) por”). */
    suspend fun followedByPreview(userId: String, limit: Int = 2): Pair<List<User>, Int> =
        withContext(Dispatchers.IO) {
            val me = AppConstants.ME_USER_ID
            val followers = db.followDao().followersOf(userId).filter { it.id != me }
            val myFollowingIds = db.followDao().followingOf(me).map { it.id }.toSet()
            val mutual = followers.filter { it.id in myFollowingIds }.map { it.toDomain() }
            val preview = (mutual.ifEmpty { followers.map { it.toDomain() } }).take(limit)
            preview to (followers.size - preview.size).coerceAtLeast(0)
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
                Story(
                    s.id, author, s.mediaUrl, s.createdAt, s.seenByMe, s.likedByMe,
                    s.musicTitle, s.musicArtist, s.showMusicLabel
                )
            })
        }
    }.flowOn(Dispatchers.IO)

    suspend fun storiesOnce(): List<Story> = withContext(Dispatchers.IO) {
        db.storyDao().all().mapNotNull { s ->
            val author = db.userDao().getById(s.authorId)?.toDomain() ?: return@mapNotNull null
            Story(
                s.id, author, s.mediaUrl, s.createdAt, s.seenByMe, s.likedByMe,
                s.musicTitle, s.musicArtist, s.showMusicLabel
            )
        }
    }

    suspend fun markStorySeen(id: String) = withContext(Dispatchers.IO) {
        db.storyDao().markSeen(id)
    }

    suspend fun toggleStoryLike(id: String): Boolean = withContext(Dispatchers.IO) {
        val story = db.storyDao().all().find { it.id == id } ?: return@withContext false
        val next = !story.likedByMe
        db.storyDao().setLiked(id, next)
        next
    }

    suspend fun replyToStory(authorId: String, text: String, gifUrl: String? = null) =
        withContext(Dispatchers.IO) {
            val convId = getOrCreateConversation(authorId)
            sendMessage(convId, text.ifBlank { "❤️" }, gifUrl)
            convId
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

    suspend fun likersOf(postId: String): List<User> = withContext(Dispatchers.IO) {
        db.likeDao().likersFor(postId).map { it.toDomain() }
    }

    suspend fun toggleRepost(postId: String): Post? = withContext(Dispatchers.IO) {
        val me = AppConstants.ME_USER_ID
        val original = db.postDao().getById(postId) ?: return@withContext null
        val already = db.repostDao().isReposted(postId, me)
        if (already) {
            db.repostDao().delete(postId, me)
        } else {
            db.repostDao().insert(RepostEntity(postId, me, System.currentTimeMillis()))
            // Also create a feed entry pointing to original
            val newId = "repost-${UUID.randomUUID()}"
            val media = db.postDao().mediaFor(postId)
            db.postDao().insert(
                original.copy(
                    id = newId,
                    authorId = me,
                    caption = "Repost de @${db.userDao().getById(original.authorId)?.username.orEmpty()}",
                    createdAt = System.currentTimeMillis(),
                    likeCount = 0,
                    commentCount = 0,
                    originalPostId = postId
                )
            )
            db.postDao().insertMedia(
                media.mapIndexed { idx, m ->
                    m.copy(id = "$newId-m$idx", postId = newId)
                }
            )
        }
        db.postDao().getById(postId)?.toDomain()
    }

    suspend fun setPostMusicLabelVisible(postId: String, visible: Boolean): Post? =
        withContext(Dispatchers.IO) {
            val post = db.postDao().getById(postId) ?: return@withContext null
            db.postDao().insert(post.copy(showMusicLabel = visible))
            db.postDao().getById(postId)?.toDomain()
        }

    suspend fun toggleBookmark(postId: String): Post? = withContext(Dispatchers.IO) {
        val me = AppConstants.ME_USER_ID
        if (db.postDao().getById(postId) == null) return@withContext null
        val bookmarked = db.bookmarkDao().isBookmarked(postId, me)
        if (bookmarked) {
            db.bookmarkDao().delete(postId, me)
        } else {
            db.bookmarkDao().insert(BookmarkEntity(postId, me))
        }
        db.postDao().getById(postId)?.toDomain()
    }

    suspend fun isBookmarked(postId: String): Boolean = withContext(Dispatchers.IO) {
        db.bookmarkDao().isBookmarked(postId, AppConstants.ME_USER_ID)
    }

    fun observeComments(postId: String): Flow<List<Comment>> = flow {
        db.commentDao().observeForPost(postId).collect { list ->
            emit(list.mapNotNull { c ->
                val author = db.userDao().getById(c.authorId)?.toDomain() ?: return@mapNotNull null
                Comment(c.id, c.postId, author, c.text, c.createdAt, c.mediaUrl)
            })
        }
    }.flowOn(Dispatchers.IO)

    suspend fun addComment(
        postId: String,
        text: String,
        mediaUrl: String? = null
    ): Comment? = withContext(Dispatchers.IO) {
        val me = db.userDao().getMe() ?: return@withContext null
        val entity = CommentEntity(
            id = "c-${UUID.randomUUID()}",
            postId = postId,
            authorId = me.id,
            text = text.trim(),
            createdAt = System.currentTimeMillis(),
            mediaUrl = mediaUrl
        )
        db.commentDao().insert(entity)
        val count = db.commentDao().countForPost(postId)
        db.postDao().updateCommentCount(postId, count)
        Comment(entity.id, postId, me.toDomain(), entity.text, entity.createdAt, entity.mediaUrl)
    }

    fun observeNotes(): Flow<List<Note>> = flow {
        db.noteDao().observeAll().collect { list ->
            emit(list.mapNotNull { n ->
                val user = db.userDao().getById(n.userId)?.toDomain() ?: return@mapNotNull null
                Note(n.userId, n.text, n.updatedAt, user)
            })
        }
    }.flowOn(Dispatchers.IO)

    suspend fun upsertMyNote(text: String) = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            db.noteDao().delete(AppConstants.ME_USER_ID)
        } else {
            db.noteDao().upsert(
                NoteEntity(
                    userId = AppConstants.ME_USER_ID,
                    text = trimmed.take(60),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun observeConversations(folder: InboxFolder = InboxFolder.PRIMARY): Flow<List<Conversation>> =
        flow {
            db.chatDao().observeByFolder(folder.name).collect { list ->
                emit(list.mapNotNull { it.toDomainConv() })
            }
        }.flowOn(Dispatchers.IO)

    /** All conversations across folders — used by share sheet. */
    fun observeAllConversations(): Flow<List<Conversation>> = flow {
        db.chatDao().observeConversations().collect { list ->
            emit(list.mapNotNull { it.toDomainConv() })
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
                    isMine = it.senderId == AppConstants.ME_USER_ID,
                    mediaUrl = it.mediaUrl,
                    reaction = it.reaction
                )
            }
        }

    suspend fun setMessageReaction(messageId: String, reaction: String?): String? =
        withContext(Dispatchers.IO) {
            val msg = db.chatDao().getMessage(messageId) ?: return@withContext null
            val next = when {
                reaction == null -> null
                msg.reaction == reaction -> null
                else -> reaction
            }
            db.chatDao().setReaction(messageId, next)
            next
        }

    suspend fun toggleHeartReaction(messageId: String): String? =
        setMessageReaction(messageId, "❤️")

    suspend fun createPost(
        caption: String,
        mediaUrl: String,
        mediaType: String
    ): Post = withContext(Dispatchers.IO) {
        val id = "post-${UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        val type = if (mediaType.equals("VIDEO", ignoreCase = true)) "VIDEO" else "IMAGE"
        db.postDao().insert(
            PostEntity(
                id = id,
                authorId = AppConstants.ME_USER_ID,
                caption = caption.trim(),
                mediaType = type,
                createdAt = now,
                likeCount = 0,
                commentCount = 0
            )
        )
        db.postDao().insertMediaItem(
            PostMediaEntity(
                id = "$id-m0",
                postId = id,
                url = mediaUrl,
                position = 0,
                type = type
            )
        )
        db.postDao().getById(id)!!.toDomain()
    }

    fun observeNotifications(): Flow<List<NotificationItem>> = flow {
        db.notificationDao().observeAll().collect { list ->
            emit(list.mapNotNull { n ->
                val actor = db.userDao().getById(n.actorUserId)?.toDomain() ?: return@mapNotNull null
                NotificationItem(
                    id = n.id,
                    type = n.type,
                    actor = actor,
                    postId = n.postId,
                    text = n.text,
                    createdAt = n.createdAt,
                    seen = n.seen
                )
            })
        }
    }.flowOn(Dispatchers.IO)

    suspend fun markNotificationsSeen() = withContext(Dispatchers.IO) {
        db.notificationDao().markAllSeen()
    }

    fun observeHighlights(userId: String): Flow<List<Highlight>> =
        db.highlightDao().observeByUser(userId).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun highlightsFor(userId: String): List<Highlight> = withContext(Dispatchers.IO) {
        db.highlightDao().byUser(userId).map { it.toDomain() }
    }

    suspend fun getHighlight(id: String): Highlight? = withContext(Dispatchers.IO) {
        db.highlightDao().getById(id)?.toDomain()
    }

    private fun HighlightEntity.toDomain() = Highlight(
        id = id,
        userId = userId,
        title = title,
        coverUrl = coverUrl,
        mediaUrls = mediaUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        createdAt = createdAt
    )

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        mediaUrl: String? = null
    ) = withContext(Dispatchers.IO) {
        val conv = db.chatDao().getConversation(conversationId) ?: return@withContext
        val now = System.currentTimeMillis()
        db.chatDao().sendAndTouch(
            MessageEntity(
                id = "m-${UUID.randomUUID()}",
                conversationId = conversationId,
                senderId = AppConstants.ME_USER_ID,
                text = text.trim(),
                createdAt = now,
                mediaUrl = mediaUrl
            ),
            conv.copy(updatedAt = now, unreadCount = 0)
        )
    }

    suspend fun sharePostToConversation(conversationId: String, post: Post) =
        withContext(Dispatchers.IO) {
            val snippet = post.caption.take(80).let { if (post.caption.length > 80) "$it…" else it }
            val text = buildString {
                append("Olha esse post 👀")
                if (snippet.isNotBlank()) {
                    append("\n")
                    append(snippet)
                }
            }
            sendMessage(conversationId, text)
        }

    suspend fun autoReply(conversationId: String) = withContext(Dispatchers.IO) {
        delay((700L..1400L).random())
        val conv = db.chatDao().getConversation(conversationId) ?: return@withContext
        val peer = db.userDao().getById(conv.peerUserId)?.toDomain() ?: return@withContext
        val last = db.chatDao().lastMessage(conversationId)
        val fromMe = last?.senderId == AppConstants.ME_USER_ID
        val incoming = if (fromMe) last?.text.orEmpty() else ""
        val isGif = fromMe && !last?.mediaUrl.isNullOrBlank() && incoming.isBlank()
        val reply = PokemonChatBrain.reply(peer, incoming, isGif)
        val now = System.currentTimeMillis()
        db.chatDao().sendAndTouch(
            MessageEntity(
                id = "m-${UUID.randomUUID()}",
                conversationId = conversationId,
                senderId = conv.peerUserId,
                text = reply.text,
                createdAt = now,
                mediaUrl = if (reply.withGif) LocalGifs.pick(now.toInt()) else null
            ),
            conv.copy(updatedAt = now)
        )
    }

    suspend fun sendBattleAftermath(conversationId: String, won: Boolean, fled: Boolean) =
        withContext(Dispatchers.IO) {
            delay(650)
            val conv = db.chatDao().getConversation(conversationId) ?: return@withContext
            val peer = db.userDao().getById(conv.peerUserId)?.toDomain() ?: return@withContext
            val now = System.currentTimeMillis()
            db.chatDao().sendAndTouch(
                MessageEntity(
                    id = "m-${UUID.randomUUID()}",
                    conversationId = conversationId,
                    senderId = conv.peerUserId,
                    text = PokemonChatBrain.battleAftermath(peer, won, fled),
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

    suspend fun setConversationFolder(id: String, folder: InboxFolder) = withContext(Dispatchers.IO) {
        db.chatDao().setFolder(id, folder.name)
    }

    suspend fun togglePin(id: String) = withContext(Dispatchers.IO) {
        val conv = db.chatDao().getConversation(id) ?: return@withContext
        db.chatDao().setPinned(id, !conv.isPinned)
    }

    suspend fun getOrCreateConversation(peerUserId: String): String = withContext(Dispatchers.IO) {
        db.chatDao().getByPeer(peerUserId)?.id ?: run {
            val id = "conv-$peerUserId"
            db.chatDao().insertConversation(
                ConversationEntity(
                    id = id,
                    peerUserId = peerUserId,
                    updatedAt = System.currentTimeMillis(),
                    unreadCount = 0,
                    folder = InboxFolder.PRIMARY.name
                )
            )
            id
        }
    }

    suspend fun conversationPeer(conversationId: String): User? = withContext(Dispatchers.IO) {
        val conv = db.chatDao().getConversation(conversationId) ?: return@withContext null
        db.userDao().getById(conv.peerUserId)?.toDomain()
    }

    suspend fun searchPostsByCaption(q: String): List<Post> = withContext(Dispatchers.IO) {
        if (q.isBlank()) return@withContext emptyList()
        db.postDao().searchByCaption(q.trim()).map { it.toDomain() }
    }

    private suspend fun ConversationEntity.toDomainConv(): Conversation? {
        val peer = db.userDao().getById(peerUserId)?.toDomain() ?: return null
        val last = db.chatDao().lastMessage(id)
        return Conversation(
            id = id,
            peer = peer,
            lastMessage = last?.text.orEmpty().ifBlank {
                if (last?.mediaUrl != null) "GIF" else ""
            },
            lastMediaUrl = last?.mediaUrl,
            updatedAt = updatedAt,
            unreadCount = unreadCount,
            folder = runCatching { InboxFolder.valueOf(folder) }.getOrDefault(InboxFolder.PRIMARY),
            isPinned = isPinned
        )
    }

    private suspend fun PostEntity.toDomain(): Post {
        val author = db.userDao().getById(authorId)?.toDomain()
            ?: User(authorId, "unknown", "Unknown", 0, "", "", 0, 0)
        val mediaEntities = db.postDao().mediaFor(id)
        val tagsByMedia = if (mediaEntities.isEmpty()) {
            emptyMap()
        } else {
            db.mediaTagDao().forMediaIds(mediaEntities.map { it.id }).groupBy { it.mediaId }
        }
        val media = mediaEntities.map { m ->
            val tags = tagsByMedia[m.id].orEmpty().mapNotNull { tag ->
                val u = db.userDao().getById(tag.userId)?.toDomain() ?: return@mapNotNull null
                MediaTag(u.id, u.username, u.displayName, tag.x, tag.y)
            }
            MediaItem(m.id, m.url, m.type, m.position, tags)
        }
        val liked = db.likeDao().isLiked(id, AppConstants.ME_USER_ID)
        val bookmarked = db.bookmarkDao().isBookmarked(id, AppConstants.ME_USER_ID)
        val reposted = db.repostDao().isReposted(id, AppConstants.ME_USER_ID)
        val preview = db.likeDao().likersPreview(id, 3).map { it.toDomain() }
        val realCount = db.likeDao().countForPost(id)
        return Post(
            id = id,
            author = author,
            caption = caption,
            mediaType = mediaType,
            media = media,
            createdAt = createdAt,
            likeCount = if (realCount > 0) realCount else likeCount,
            commentCount = commentCount,
            likedByMe = liked,
            bookmarked = bookmarked,
            musicTitle = musicTitle,
            musicArtist = musicArtist,
            showMusicLabel = showMusicLabel,
            originalPostId = originalPostId,
            repostedByMe = reposted,
            likedPreview = preview
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
    isMe = isMe,
    types = if (types.isBlank()) emptyList() else types.split(",").map { it.trim() }.filter { it.isNotEmpty() }
)
