package com.pokesocial.app.data.local

import com.pokesocial.app.core.AppConstants
import com.pokesocial.app.core.LocalGifs
import com.pokesocial.app.core.PokemonChatBrain
import com.pokesocial.app.data.local.db.AppDatabase
import com.pokesocial.app.data.local.db.CommentEntity
import com.pokesocial.app.data.local.db.ConversationEntity
import com.pokesocial.app.data.local.db.FollowEntity
import com.pokesocial.app.data.local.db.HighlightEntity
import com.pokesocial.app.data.local.db.LikeEntity
import com.pokesocial.app.data.local.db.MediaTagEntity
import com.pokesocial.app.data.local.db.MessageEntity
import com.pokesocial.app.data.local.db.MetaEntity
import com.pokesocial.app.data.local.db.NoteEntity
import com.pokesocial.app.data.local.db.NotificationEntity
import com.pokesocial.app.data.local.db.PostEntity
import com.pokesocial.app.data.local.db.PostMediaEntity
import com.pokesocial.app.data.local.db.StoryEntity
import com.pokesocial.app.data.local.db.UserEntity
import com.pokesocial.app.data.remote.PokeApiService
import com.pokesocial.app.data.repository.toDomain
import com.pokesocial.app.domain.model.InboxFolder
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

        db.clearAllTables()

        onProgress(Progress("Buscando Gen 1 na PokeAPI…", 0.05f))
        val list = api.getPokemonList(limit = 151, offset = 0).results
        onProgress(Progress("Carregando Lucario…", 0.12f))
        val lucario = api.getPokemon("448")

        onProgress(Progress("Mapeando tipos Pokémon…", 0.18f))
        val typesByPokemonId = buildTypesMap()
        val lucarioTypes = lucario.types
            .sortedBy { it.slot }
            .map { it.type.name }
            .ifEmpty { typesByPokemonId[448] ?: listOf("fighting", "steel") }

        val users = mutableListOf<UserEntity>()
        list.forEachIndexed { index, item ->
            val id = index + 1
            val name = item.name.replaceFirstChar { it.uppercase() }
            val types = typesByPokemonId[id].orEmpty()
            users += UserEntity(
                id = "user-$id",
                username = "${item.name}_$id",
                displayName = name,
                pokemonId = id,
                // ÚNICO lugar com PokeAPI image: avatar de perfil
                avatarUrl = AppConstants.artworkUrl(id),
                bio = "Treinador(a) de $name · Kanto #$id",
                followers = Random.nextInt(120, 50_000),
                following = Random.nextInt(80, 900),
                isMe = false,
                types = types.joinToString(",")
            )
            if (id % 20 == 0) {
                onProgress(Progress("Montando Pokédex… $id/151", 0.18f + id / 151f * 0.22f))
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
            isMe = true,
            types = lucarioTypes.joinToString(",")
        )

        onProgress(Progress("Salvando usuários…", 0.45f))
        db.userDao().insertAll(users)

        onProgress(Progress("Montando seguidores…", 0.48f))
        val follows = mutableListOf<FollowEntity>()
        // Eu sigo ~70 pokémon; ~120 me seguem
        val others = users.filter { !it.isMe }
        others.shuffled().take(70).forEach { peer ->
            follows += FollowEntity(AppConstants.ME_USER_ID, peer.id)
        }
        others.shuffled().take(120).forEach { peer ->
            follows += FollowEntity(peer.id, AppConstants.ME_USER_ID)
        }
        // Grafo aleatório entre pokémon
        repeat(1_200) {
            val a = others.random()
            val b = others.random()
            if (a.id != b.id) follows += FollowEntity(a.id, b.id)
        }
        db.followDao().insertAll(follows.distinctBy { it.followerId to it.followeeId })
        users.forEach { u ->
            val fc = db.followDao().followersCount(u.id)
            val fg = db.followDao().followingCount(u.id)
            db.userDao().updateFollowCounts(u.id, fc, fg)
        }

        onProgress(Progress("Gerando posts com mídia local…", 0.55f))
        val now = System.currentTimeMillis()
        val posts = mutableListOf<PostEntity>()
        val media = mutableListOf<PostMediaEntity>()
        val mediaTags = mutableListOf<MediaTagEntity>()
        val comments = mutableListOf<CommentEntity>()
        val likes = mutableListOf<LikeEntity>()
        val authors = users.filter { !it.isMe }.shuffled()
        val songs = listOf(
            "Aura Symphony" to "Lucario Beats",
            "Viridian Mist" to "Kanto FM",
            "Mt. Moon Echo" to "Zubat Choir",
            "Surf Route" to "Squirtle Squad",
            "Thunder Groove" to "Pika Waves",
            "Night Pallet" to "Star Cluster",
            "Gym Anthem" to "Elite Four",
            "Berry Pop" to "Celadon Café"
        )

        fun assignLikes(postId: String, preferMe: Boolean): Int {
            val count = Random.nextInt(12, 48)
            val pool = authors.shuffled().take(count).toMutableList()
            if (preferMe) {
                likes += LikeEntity(postId, AppConstants.ME_USER_ID)
            }
            pool.take(if (preferMe) count - 1 else count).forEach { u ->
                likes += LikeEntity(postId, u.id)
            }
            return likes.count { it.postId == postId }
        }

        fun maybeMusic(i: Int): Triple<String?, String?, Boolean> {
            if (i % 3 != 0) return Triple(null, null, true)
            val song = songs[i % songs.size]
            return Triple(song.first, song.second, true)
        }

        repeat(280) { i ->
            val author = authors[i % authors.size]
            val theme = ContentThemes.forPokemonId(author.pokemonId)
            val postId = "post-$i"
            val kind = when {
                i % 6 == 0 -> "VIDEO"
                i % 4 == 0 -> "CAROUSEL"
                else -> "IMAGE"
            }
            val tagged = if (kind != "VIDEO" && i % 5 == 0) {
                authors.shuffled().take(Random.nextInt(1, 4))
            } else emptyList()
            val caption = buildString {
                append(ContentThemes.captionWithHashtags(theme, i))
                append(" — ")
                append(author.displayName)
                if (i % 5 == 0) append(" @${AppConstants.ME_USERNAME}")
                tagged.forEach { append(" @${it.username}") }
            }
            val created = now - (i + 1) * 3_600_000L * Random.nextInt(1, 8)
            val (musicTitle, musicArtist, showMusic) = maybeMusic(i)
            val likeCount = assignLikes(postId, preferMe = i % 3 == 0)

            posts += PostEntity(
                id = postId,
                authorId = author.id,
                caption = caption,
                mediaType = kind,
                createdAt = created,
                likeCount = likeCount,
                commentCount = 0,
                musicTitle = musicTitle,
                musicArtist = musicArtist,
                showMusicLabel = showMusic
            )

            when (kind) {
                "VIDEO" -> media += PostMediaEntity(
                    id = "$postId-m0",
                    postId = postId,
                    url = ContentThemes.videoUri(i),
                    position = 0,
                    type = "VIDEO"
                )
                "CAROUSEL" -> {
                    val count = minOf(5, theme.images.size)
                    repeat(count) { p ->
                        val mid = "$postId-m$p"
                        media += PostMediaEntity(
                            id = mid,
                            postId = postId,
                            url = ContentThemes.imageUri(theme, i + p),
                            position = p,
                            type = "IMAGE"
                        )
                        if (p == 0 && tagged.isNotEmpty()) {
                            tagged.forEachIndexed { ti, u ->
                                mediaTags += MediaTagEntity(
                                    mediaId = mid,
                                    userId = u.id,
                                    x = 0.2f + ti * 0.25f,
                                    y = 0.35f + (ti % 2) * 0.2f
                                )
                            }
                        }
                    }
                }
                else -> {
                    val url = if (i % 7 == 0 && ContentThemes.extraImages.isNotEmpty()) {
                        ContentThemes.assetUri(ContentThemes.extraImages[i % ContentThemes.extraImages.size])
                    } else {
                        ContentThemes.imageUri(theme, i)
                    }
                    val mid = "$postId-m0"
                    media += PostMediaEntity(
                        id = mid,
                        postId = postId,
                        url = url,
                        position = 0,
                        type = "IMAGE"
                    )
                    tagged.forEachIndexed { ti, u ->
                        mediaTags += MediaTagEntity(
                            mediaId = mid,
                            userId = u.id,
                            x = 0.25f + ti * 0.22f,
                            y = 0.4f + (ti % 2) * 0.18f
                        )
                    }
                }
            }

            val cCount = Random.nextInt(3, 8)
            repeat(cCount) { c ->
                val cAuthor = authors[(i + c + 3) % authors.size]
                val gif = c % 3 == 0
                comments += CommentEntity(
                    id = "$postId-c$c",
                    postId = postId,
                    authorId = cAuthor.id,
                    text = if (gif) "" else theme.comments[(i + c) % theme.comments.size],
                    createdAt = created + (c + 1) * 60_000L,
                    mediaUrl = if (gif) LocalGifs.pick(i * 10 + c) else null
                )
            }
            posts[posts.lastIndex] = posts.last().copy(commentCount = cCount)
        }

        val lucarioThemes = listOf("battle", "night", "mountain", "sunset", "beach", "forest")
            .map { id -> ContentThemes.all.first { it.id == id } }

        repeat(36) { i ->
            val theme = lucarioThemes[i % lucarioThemes.size]
            val postId = "post-me-$i"
            val created = now - i * 86_400_000L
            val kind = when {
                i % 5 == 0 -> "VIDEO"
                i % 4 == 0 -> "CAROUSEL"
                else -> "IMAGE"
            }
            val tagged = authors.shuffled().take(Random.nextInt(1, 3))
            val (musicTitle, musicArtist, showMusic) = maybeMusic(i + 1)
            val likeCount = assignLikes(postId, preferMe = false)
            posts += PostEntity(
                id = postId,
                authorId = AppConstants.ME_USER_ID,
                caption = buildString {
                    append(ContentThemes.captionWithHashtags(theme, i, "#lucar_10"))
                    tagged.forEach { append(" @${it.username}") }
                },
                mediaType = kind,
                createdAt = created,
                likeCount = likeCount,
                commentCount = 5,
                musicTitle = musicTitle,
                musicArtist = musicArtist,
                showMusicLabel = showMusic
            )
            when (kind) {
                "VIDEO" -> media += PostMediaEntity(
                    id = "$postId-m0",
                    postId = postId,
                    url = ContentThemes.videoUri(i + 100),
                    position = 0,
                    type = "VIDEO"
                )
                "CAROUSEL" -> {
                    repeat(minOf(5, theme.images.size)) { p ->
                        val mid = "$postId-m$p"
                        media += PostMediaEntity(
                            id = mid,
                            postId = postId,
                            url = ContentThemes.imageUri(theme, i + p),
                            position = p,
                            type = "IMAGE"
                        )
                        if (p == 0) {
                            tagged.forEachIndexed { ti, u ->
                                mediaTags += MediaTagEntity(mid, u.id, 0.3f + ti * 0.2f, 0.45f)
                            }
                        }
                    }
                }
                else -> {
                    val mid = "$postId-m0"
                    media += PostMediaEntity(
                        id = mid,
                        postId = postId,
                        url = ContentThemes.imageUri(theme, i),
                        position = 0,
                        type = "IMAGE"
                    )
                    tagged.forEachIndexed { ti, u ->
                        mediaTags += MediaTagEntity(mid, u.id, 0.3f + ti * 0.2f, 0.45f)
                    }
                }
            }
            repeat(5) { c ->
                val cAuthor = authors[(i + c) % authors.size]
                val gif = c == 1 || c == 4
                comments += CommentEntity(
                    id = "$postId-c$c",
                    postId = postId,
                    authorId = cAuthor.id,
                    text = if (gif) "" else theme.comments[(i + c) % theme.comments.size],
                    createdAt = created + c * 120_000L,
                    mediaUrl = if (gif) LocalGifs.pick(i + c) else null
                )
            }
        }

        onProgress(Progress("Stories e curtidas…", 0.75f))
        db.postDao().insertAll(posts)
        db.postDao().insertMedia(media)
        db.mediaTagDao().insertAll(mediaTags)
        db.commentDao().insertAll(comments)
        db.likeDao().insertAll(likes.distinctBy { it.postId to it.userId })

        // Vários frames por Pokémon → um carrossel por autor (estilo Instagram)
        val storyAuthors = authors.take(50)
        val stories = mutableListOf<StoryEntity>()
        storyAuthors.forEachIndexed { i, u ->
            val theme = ContentThemes.forPokemonId(u.pokemonId)
            val frames = Random.nextInt(3, 7)
            val song = if (i % 2 == 0) songs[i % songs.size] else null
            repeat(frames) { s ->
                stories += StoryEntity(
                    id = "story-$i-$s",
                    authorId = u.id,
                    mediaUrl = ContentThemes.imageUri(theme, i * 3 + s),
                    createdAt = now - i * 1_800_000L - s * 45_000L,
                    seenByMe = i > 30,
                    musicTitle = song?.first,
                    musicArtist = song?.second,
                    showMusicLabel = true
                )
            }
        }
        repeat(8) { s ->
            val song = songs[s % songs.size]
            stories += StoryEntity(
                id = "story-me-$s",
                authorId = AppConstants.ME_USER_ID,
                mediaUrl = ContentThemes.imageUri(lucarioThemes[s % lucarioThemes.size], s),
                createdAt = now - s * 40_000L,
                seenByMe = true,
                musicTitle = song.first,
                musicArtist = song.second,
                showMusicLabel = true
            )
        }
        db.storyDao().insertAll(stories)

        onProgress(Progress("Preparando DMs…", 0.88f))
        val chatPeers = listOf(
            25, 6, 150, 94, 130, 39, 4, 7, 133, 143,
            1, 9, 16, 26, 52, 65, 68, 74, 131, 144,
            149, 18, 38, 59, 123
        ).map { "user-$it" }
            .mapNotNull { id -> users.find { it.id == id } }
        val folders = listOf(
            InboxFolder.PRIMARY, InboxFolder.PRIMARY, InboxFolder.PRIMARY,
            InboxFolder.GENERAL, InboxFolder.GENERAL,
            InboxFolder.REQUESTS, InboxFolder.REQUESTS,
            InboxFolder.ARCHIVED, InboxFolder.PRIMARY, InboxFolder.GENERAL
        )
        val conversations = mutableListOf<ConversationEntity>()
        val messages = mutableListOf<MessageEntity>()
        chatPeers.forEachIndexed { i, peer ->
            val convId = "conv-$i"
            val base = now - (i + 1) * 7_200_000L
            conversations += ConversationEntity(
                id = convId,
                peerUserId = peer.id,
                updatedAt = base + 300_000L,
                unreadCount = if (i < 4) Random.nextInt(1, 5) else 0,
                folder = folders[i % folders.size].name,
                isPinned = i == 0
            )
            val peerLines = PokemonChatBrain.seedPeerLines(peer.toDomain())
            val myLines = listOf(
                "Fechado.", "Pode crer.", "Já tô a caminho.", "gg", "Partiu.",
                "Bora!", "Mandou bem.", "Top demais.", "Confia.", "Até já."
            )
            repeat(10) { m ->
                val fromMe = m % 2 == 1
                val gif = m == 2 || m == 7
                messages += MessageEntity(
                    id = "$convId-m$m",
                    conversationId = convId,
                    senderId = if (fromMe) AppConstants.ME_USER_ID else peer.id,
                    text = when {
                        gif -> ""
                        fromMe -> myLines[m % myLines.size]
                        else -> peerLines[m % peerLines.size]
                    },
                    createdAt = base + m * 60_000L,
                    mediaUrl = if (gif) LocalGifs.pick(i * 5 + m) else null,
                    reaction = if (m == 1 || m == 3 || m == 9) "❤️" else null
                )
            }
        }
        db.chatDao().insertConversations(conversations)
        db.chatDao().insertMessages(messages)

        onProgress(Progress("Criando notes…", 0.90f))
        val noteTexts = listOf(
            "Aura carregada ✨",
            "Partiu ginásio!",
            "Catching 'em all",
            "Elite Four next",
            "Surf no domingo 🌊",
            "Thunderbolt ready",
            "Vibe de Floresta",
            "Pokébola nova 🔴",
            "gg wp",
            "Farming berries",
            "Night owl mode",
            "Gym rematch soon",
            "Shiny hunt ON",
            "Coffee + XP",
            "Rota 1 vibes"
        )
        val noteUsers = listOf(AppConstants.ME_USER_ID) + chatPeers.take(14).map { it.id }
        val notes = noteUsers.mapIndexed { i, userId ->
            NoteEntity(
                userId = userId,
                text = noteTexts[i % noteTexts.size],
                updatedAt = now - i * 45_000L
            )
        }
        db.noteDao().insertAll(notes)

        onProgress(Progress("Notificações e highlights…", 0.95f))
        val notifActors = authors.take(50)
        val myPostIds = posts.filter { it.authorId == AppConstants.ME_USER_ID }.map { it.id }
        val notifications = mutableListOf<NotificationEntity>()
        notifActors.forEachIndexed { i, actor ->
            val type = when (i % 4) {
                0 -> "LIKE"
                1 -> "FOLLOW"
                2 -> "COMMENT"
                else -> "STORY_REPLY"
            }
            val postId = when (type) {
                "LIKE", "COMMENT" -> myPostIds.getOrElse(i % myPostIds.size.coerceAtLeast(1)) { "post-me-0" }
                else -> null
            }
            val text = when (type) {
                "LIKE" -> "curtiu sua publicação."
                "FOLLOW" -> "começou a seguir você."
                "COMMENT" -> {
                    val tc = ContentThemes.forIndex(i).comments
                    "comentou: \"${tc[i % tc.size]}\""
                }
                else -> "respondeu ao seu story."
            }
            notifications += NotificationEntity(
                id = "notif-$i",
                type = type,
                actorUserId = actor.id,
                postId = postId,
                text = text,
                createdAt = now - (i + 1) * 1_200_000L,
                seen = i > 30
            )
        }
        db.notificationDao().insertAll(notifications)

        fun highlightFor(
            id: String,
            userId: String,
            title: String,
            themeId: String,
            createdOffset: Long
        ): HighlightEntity {
            val theme = ContentThemes.all.first { it.id == themeId }
            val urls = theme.images.take(8).map { ContentThemes.assetUri(it) }
            return HighlightEntity(
                id = id,
                userId = userId,
                title = title,
                coverUrl = urls.first(),
                mediaUrls = urls.joinToString(","),
                createdAt = now - createdOffset
            )
        }

        val highlights = mutableListOf(
            highlightFor("hl-me-battle", AppConstants.ME_USER_ID, "Battle", "battle", 1_000_000L),
            highlightFor("hl-me-aura", AppConstants.ME_USER_ID, "Aura", "night", 2_000_000L),
            highlightFor("hl-me-gym", AppConstants.ME_USER_ID, "Gym", "mountain", 3_000_000L),
            highlightFor("hl-me-travel", AppConstants.ME_USER_ID, "Travel", "beach", 4_000_000L),
            highlightFor("hl-me-food", AppConstants.ME_USER_ID, "Berries", "food", 5_000_000L),
            highlightFor("hl-me-forest", AppConstants.ME_USER_ID, "Trail", "forest", 6_000_000L)
        )
        listOf(
            "user-25" to listOf("Battle" to "battle", "Travel" to "city", "Storm" to "rain"),
            "user-6" to listOf("Gym" to "mountain", "Aura" to "sunset", "Food" to "food"),
            "user-150" to listOf("Battle" to "battle", "Travel" to "lake", "Night" to "night"),
            "user-94" to listOf("Aura" to "night", "Flowers" to "flowers"),
            "user-130" to listOf("Ocean" to "beach", "Snow" to "snow"),
            "user-4" to listOf("Fire" to "sunset", "Gym" to "battle"),
            "user-133" to listOf("Evolve" to "forest", "City" to "city"),
            "user-1" to listOf("Garden" to "flowers", "Lake" to "lake")
        ).forEach { (userId, items) ->
            items.forEachIndexed { idx, (title, themeId) ->
                highlights += highlightFor(
                    id = "hl-$userId-$idx",
                    userId = userId,
                    title = title,
                    themeId = themeId,
                    createdOffset = (idx + 1) * 500_000L
                )
            }
        }
        db.highlightDao().insertAll(highlights)

        db.metaDao().put(MetaEntity(AppConstants.META_SEEDED, "1"))
        onProgress(Progress("Pronto!", 1f))
    }

    /**
     * Fetches all 18 types from PokeAPI and maps pokemon id → type names
     * for Gen 1 (1–151) + Lucario (448). Falls back to a small hardcoded map.
     */
    private suspend fun buildTypesMap(): Map<Int, List<String>> {
        val allowed = (1..151).toSet() + 448
        val result = mutableMapOf<Int, MutableList<String>>()
        val typeNames = listOf(
            "normal", "fire", "water", "electric", "grass", "ice",
            "fighting", "poison", "ground", "flying", "psychic", "bug",
            "rock", "ghost", "dragon", "dark", "steel", "fairy"
        )
        try {
            for (typeName in typeNames) {
                val detail = api.getType(typeName)
                for (entry in detail.pokemon) {
                    val id = pokemonIdFromUrl(entry.pokemon.url) ?: continue
                    if (id !in allowed) continue
                    val list = result.getOrPut(id) { mutableListOf() }
                    if (typeName !in list) list.add(typeName)
                }
            }
            if (result.isNotEmpty()) return result.mapValues { it.value.toList() }
        } catch (_: Exception) {
            // fall through to hardcoded
        }
        return FALLBACK_TYPES
    }

    private fun pokemonIdFromUrl(url: String): Int? {
        val trimmed = url.trimEnd('/')
        return trimmed.substringAfterLast('/').toIntOrNull()
    }

    companion object {
        /** Small fallback for common ids when PokeAPI type fetch fails. */
        private val FALLBACK_TYPES: Map<Int, List<String>> = buildFallbackGen1() + mapOf(
            1 to listOf("grass", "poison"),
            4 to listOf("fire"),
            6 to listOf("fire", "flying"),
            7 to listOf("water"),
            25 to listOf("electric"),
            39 to listOf("normal", "fairy"),
            94 to listOf("ghost", "poison"),
            130 to listOf("water", "flying"),
            133 to listOf("normal"),
            143 to listOf("normal"),
            150 to listOf("psychic"),
            448 to listOf("fighting", "steel")
        )

        private fun buildFallbackGen1(): Map<Int, List<String>> {
            val cycle = listOf(
                "normal", "fire", "water", "electric", "grass", "ice",
                "fighting", "poison", "ground", "flying", "psychic", "bug",
                "rock", "ghost", "dragon", "dark", "steel", "fairy"
            )
            return (1..151).associateWith { id ->
                listOf(cycle[(id - 1) % cycle.size])
            }
        }
    }
}
