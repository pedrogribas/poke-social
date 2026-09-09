package com.pokesocial.app.data.local

import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.data.local.db.AppDatabase
import com.pokesocial.app.data.local.db.CommentEntity
import com.pokesocial.app.data.local.db.ConversationEntity
import com.pokesocial.app.data.local.db.LikeEntity
import com.pokesocial.app.data.local.db.MessageEntity
import com.pokesocial.app.data.local.db.MetaEntity
import com.pokesocial.app.data.local.db.PostEntity
import com.pokesocial.app.data.local.db.PostMediaEntity
import com.pokesocial.app.data.local.db.StoryEntity
import com.pokesocial.app.data.local.db.UserEntity
import com.pokesocial.app.data.remote.PokeApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SeedManager(
    private val db: AppDatabase,
    private val api: PokeApiService
) {
    data class Progress(val step: String, val fraction: Float)

    suspend fun needsSeed(): Boolean = withContext(Dispatchers.IO) {
        db.metaDao().get(AppConstants.META_SEEDED) != "1"
    }

    suspend fun seed(onProgress: (Progress) -> Unit) = withContext(Dispatchers.IO) {
        if (!needsSeed()) return@withContext

        onProgress(Progress("Buscando Gen 1 na PokeAPI…", 0.05f))
        val list = api.getPokemonList(limit = 151, offset = 0).results
        onProgress(Progress("Carregando Lucario…", 0.15f))
        val lucario = api.getPokemon("448")

        val users = mutableListOf<UserEntity>()
        list.forEachIndexed { index, item ->
            val id = index + 1
            val name = item.name.replaceFirstChar { it.uppercase() }
            users += UserEntity(
                id = "user-$id",
                username = "${item.name}_$id",
                displayName = name,
                pokemonId = id,
                avatarUrl = AppConstants.artworkUrl(id),
                bio = "Treinador(a) de $name · Kanto #$id",
                followers = Random.nextInt(120, 50_000),
                following = Random.nextInt(80, 900),
                isMe = false
            )
            if (id % 20 == 0) {
                onProgress(Progress("Montando Pokédex… $id/151", 0.15f + id / 151f * 0.25f))
            }
        }

        users += UserEntity(
            id = AppConstants.ME_USER_ID,
            username = AppConstants.ME_USERNAME,
            displayName = AppConstants.ME_DISPLAY_NAME,
            pokemonId = AppConstants.ME_POKEMON_ID,
            avatarUrl = AppConstants.artworkUrl(AppConstants.ME_POKEMON_ID),
            bio = "Aura trainer · #${lucario.id} · ${AppConstants.ME_USERNAME}",
            followers = 128_400,
            following = 312,
            isMe = true
        )

        onProgress(Progress("Salvando usuários…", 0.45f))
        db.userDao().insertAll(users)

        val captions = listOf(
            "Treino de aura no amanhecer 🌅",
            "Ginásio hoje? Eu toparia.",
            "Selfie pós-batalha. Quase faint.",
            "Rota 1 nunca decepciona.",
            "Alguém viu a Team Rocket por aqui?",
            "Berry bowl + vibes.",
            "Throwback da minha primeira captura.",
            "Lucario energy only.",
            "Centro Pokémon lotado de novo.",
            "Shiny? Quase… ângulo da foto.",
            "Dica: berries antes do duelo.",
            "Noite em Lavender Town 👀",
            "Surf na costa de Fuchsia.",
            "Missão: 10k passos, zero faint.",
            "Squad goals da Gen 1."
        )
        val commentPool = listOf(
            "Incrível!!", "🔥🔥🔥", "Muito aura", "Quero duelar", "Top demais",
            "haha clássico", "Salvou o feed", "Isso aí lucar_10", "Kanto vibes",
            "Manda o local", "Respect", "Lendário", "Repost isso"
        )

        onProgress(Progress("Gerando posts…", 0.55f))
        val now = System.currentTimeMillis()
        val posts = mutableListOf<PostEntity>()
        val media = mutableListOf<PostMediaEntity>()
        val comments = mutableListOf<CommentEntity>()
        val likes = mutableListOf<LikeEntity>()

        val authors = users.filter { !it.isMe }.shuffled()
        // ~88 posts from Gen1 + ~12 from Lucario
        repeat(88) { i ->
            val author = authors[i % authors.size]
            val postId = "post-$i"
            val kind = when {
                i % 7 == 0 -> "VIDEO"
                i % 4 == 0 -> "CAROUSEL"
                else -> "IMAGE"
            }
            val likeCount = Random.nextInt(40, 9200)
            val created = now - (i + 1) * 3_600_000L * Random.nextInt(1, 8)
            posts += PostEntity(
                id = postId,
                authorId = author.id,
                caption = captions[i % captions.size] + if (i % 5 == 0) " @${AppConstants.ME_USERNAME}" else "",
                mediaType = kind,
                createdAt = created,
                likeCount = likeCount,
                commentCount = 0
            )
            when (kind) {
                "VIDEO" -> media += PostMediaEntity(
                    id = "$postId-m0",
                    postId = postId,
                    url = AppConstants.SAMPLE_VIDEOS[i % AppConstants.SAMPLE_VIDEOS.size],
                    position = 0,
                    type = "VIDEO"
                )
                "CAROUSEL" -> {
                    val count = Random.nextInt(2, 4)
                    repeat(count) { p ->
                        media += PostMediaEntity(
                            id = "$postId-m$p",
                            postId = postId,
                            url = AppConstants.picsum("c-$i-$p"),
                            position = p,
                            type = "IMAGE"
                        )
                    }
                }
                else -> media += PostMediaEntity(
                    id = "$postId-m0",
                    postId = postId,
                    url = AppConstants.picsum("p-$i"),
                    position = 0,
                    type = "IMAGE"
                )
            }
            val cCount = Random.nextInt(2, 7)
            repeat(cCount) { c ->
                val cAuthor = authors[(i + c + 3) % authors.size]
                comments += CommentEntity(
                    id = "$postId-c$c",
                    postId = postId,
                    authorId = cAuthor.id,
                    text = commentPool[(i + c) % commentPool.size],
                    createdAt = created + (c + 1) * 60_000L
                )
            }
            posts[posts.lastIndex] = posts.last().copy(commentCount = cCount)
            if (i % 3 == 0) {
                likes += LikeEntity(postId, AppConstants.ME_USER_ID)
            }
        }

        repeat(12) { i ->
            val postId = "post-me-$i"
            val created = now - i * 86_400_000L
            val kind = if (i % 4 == 0) "CAROUSEL" else "IMAGE"
            posts += PostEntity(
                id = postId,
                authorId = AppConstants.ME_USER_ID,
                caption = listOf(
                    "Aura check matinal.",
                    "Treino com a Bone Rush.",
                    "Silph Co. vibes.",
                    "Só Lucario things.",
                    "Ready for the Elite Four.",
                    "Close-up do sensor."
                )[i % 6],
                mediaType = kind,
                createdAt = created,
                likeCount = Random.nextInt(800, 42_000),
                commentCount = 3
            )
            if (kind == "CAROUSEL") {
                repeat(3) { p ->
                    media += PostMediaEntity(
                        id = "$postId-m$p",
                        postId = postId,
                        url = AppConstants.picsum("me-$i-$p"),
                        position = p,
                        type = "IMAGE"
                    )
                }
            } else {
                media += PostMediaEntity(
                    id = "$postId-m0",
                    postId = postId,
                    url = AppConstants.picsum("me-$i"),
                    position = 0,
                    type = "IMAGE"
                )
            }
            repeat(3) { c ->
                val cAuthor = authors[(i + c) % authors.size]
                comments += CommentEntity(
                    id = "$postId-c$c",
                    postId = postId,
                    authorId = cAuthor.id,
                    text = commentPool[(i + c + 2) % commentPool.size],
                    createdAt = created + c * 120_000L
                )
            }
        }

        onProgress(Progress("Stories e curtidas…", 0.75f))
        db.postDao().insertAll(posts)
        db.postDao().insertMedia(media)
        db.commentDao().insertAll(comments)
        likes.forEach { db.likeDao().insert(it) }

        val storyAuthors = authors.take(14)
        val stories = storyAuthors.mapIndexed { i, u ->
            StoryEntity(
                id = "story-$i",
                authorId = u.id,
                mediaUrl = AppConstants.picsum("story-$i", 1080, 1920),
                createdAt = now - i * 1_800_000L,
                seenByMe = i > 8
            )
        } + StoryEntity(
            id = "story-me",
            authorId = AppConstants.ME_USER_ID,
            mediaUrl = AppConstants.artworkUrl(AppConstants.ME_POKEMON_ID),
            createdAt = now,
            seenByMe = true
        )
        db.storyDao().insertAll(stories)

        onProgress(Progress("Preparando DMs…", 0.88f))
        val chatPeers = listOf(25, 6, 150, 94, 130, 39).map { "user-$it" } // pikachu, charizard, mewtwo, gengar, gyarados, jigglypuff
            .mapNotNull { id -> users.find { it.id == id } }
        val conversations = mutableListOf<ConversationEntity>()
        val messages = mutableListOf<MessageEntity>()
        chatPeers.forEachIndexed { i, peer ->
            val convId = "conv-$i"
            val base = now - (i + 1) * 7_200_000L
            conversations += ConversationEntity(
                id = convId,
                peerUserId = peer.id,
                updatedAt = base + 300_000L,
                unreadCount = if (i < 2) Random.nextInt(1, 4) else 0
            )
            val lines = listOf(
                "E aí, Lucario!",
                "Bora treinar aura depois?",
                "Vi seu post agora.",
                "Elite Four tá difícil hein.",
                "Manda o local do ginásio."
            )
            lines.forEachIndexed { m, text ->
                val fromMe = m % 2 == 1
                messages += MessageEntity(
                    id = "$convId-m$m",
                    conversationId = convId,
                    senderId = if (fromMe) AppConstants.ME_USER_ID else peer.id,
                    text = if (fromMe) listOf("Fechado.", "Pode crer.", "Já tô a caminho.", "gg", "Partiu.")[m % 5] else text,
                    createdAt = base + m * 60_000L
                )
            }
        }
        db.chatDao().insertConversations(conversations)
        db.chatDao().insertMessages(messages)

        db.metaDao().put(MetaEntity(AppConstants.META_SEEDED, "1"))
        onProgress(Progress("Pronto!", 1f))
    }
}
