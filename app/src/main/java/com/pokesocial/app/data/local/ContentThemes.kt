package com.pokesocial.app.data.local

/**
 * Catálogo local: tema → captions/comments + arquivos em assets/media/{tema}/
 * PokeAPI NÃO entra em posts — só avatar de perfil.
 */
data class ContentTheme(
    val id: String,
    val captions: List<String>,
    val comments: List<String>,
    /** Caminhos relativos a assets/, ex: media/beach/01.jpg */
    val images: List<String>
)

object ContentThemes {

    private fun imgs(theme: String, count: Int = 12) = (1..count).map {
        "media/$theme/${it.toString().padStart(2, '0')}.jpg"
    }

    val videos: List<String> = (1..17).map {
        "media/videos/${it.toString().padStart(2, '0')}.mp4"
    }

    val extraImages: List<String> = (1..48).map {
        "media/extra/${it.toString().padStart(2, '0')}.jpg"
    }

    val all: List<ContentTheme> = listOf(
        ContentTheme(
            id = "beach",
            captions = listOf(
                "Dia de praia em Fuchsia. Água perfeita 🌊",
                "Surf liberado. Quem topa a próxima onda?",
                "Pé na areia, Berry na mão.",
                "Pôr do sol na costa. Sem Team Rocket à vista.",
                "Rota costeira + brisa boa. Catch rate max.",
                "Morning swim. Squirtle squad approved."
            ),
            comments = listOf(
                "Que mar lindo!!",
                "Leva um Squirtle aí 💙",
                "Tô indo agora",
                "Cuidado com o Gyarados kkkk",
                "Preciso de férias assim",
                "Salvou o domingo",
                "Água gelada demais?"
            ),
            images = imgs("beach")
        ),
        ContentTheme(
            id = "forest",
            captions = listOf(
                "Viridian Forest nunca mentiu nas vibes 🌲",
                "Trilha + bug catcher energy.",
                "Achei um esconderijo de Caterpie.",
                "Sombra boa pra treinar stealth."
            ),
            comments = listOf(
                "Que floresta!",
                "Leva repel kkk",
                "Ambiente top",
                "Odores de Kanto",
                "Foto incrível"
            ),
            images = imgs("forest")
        ),
        ContentTheme(
            id = "sunset",
            captions = listOf(
                "Golden hour pós-ginásio 🌅",
                "O céu pagou o treino de hoje.",
                "Fim de rota com esse visual.",
                "Quase shiny… era o sol."
            ),
            comments = listOf(
                "Que luz!!",
                "Cinematic demais",
                "Salvou o feed",
                "Hora perfeita",
                "Repost isso"
            ),
            images = imgs("sunset")
        ),
        ContentTheme(
            id = "city",
            captions = listOf(
                "Celadon de noite. Neon + berries.",
                "Centro da cidade lotado de novo.",
                "Street vibes depois do shopping.",
                "Silph Co. no horizonte."
            ),
            comments = listOf(
                "Cidade viva!",
                "Mood noturno",
                "Onde é isso?",
                "Looks caros kkk",
                "Urban Kanto"
            ),
            images = imgs("city")
        ),
        ContentTheme(
            id = "mountain",
            captions = listOf(
                "Mt. Moon no peito. Subida pesada ⛰️",
                "Vista lá de cima vale o faint.",
                "Ar puro + echo de Zubat.",
                "Quase no cume. Quase."
            ),
            comments = listOf(
                "Que vista!",
                "Pernas de aço",
                "Leva poção",
                "Épico",
                "Quero ir junto"
            ),
            images = imgs("mountain")
        ),
        ContentTheme(
            id = "food",
            captions = listOf(
                "Berry bowl do chef. Energia max 🍓",
                "Café da manhã de treinador.",
                "Snack pré-batalha. Não julguem.",
                "Receita secreta do Centro Pokémon."
            ),
            comments = listOf(
                "Que fome!",
                "Manda a receita",
                "Looks delicious",
                "Berry supremacy",
                "Quero um"
            ),
            images = imgs("food")
        ),
        ContentTheme(
            id = "battle",
            captions = listOf(
                "Ginásio quente hoje. Quase faint 🔥",
                "Crit confirmado. Rematch quando quiser.",
                "Treino de aura / power-up session.",
                "VS no campo. GG."
            ),
            comments = listOf(
                "Que duelo!",
                "Manda replay",
                "Respect",
                "Quero batalhar",
                "Insano"
            ),
            images = imgs("battle")
        ),
        ContentTheme(
            id = "rain",
            captions = listOf(
                "Chuva em Lavender. Mood on 💧",
                "Puddle pics porque sim.",
                "Clima perfeito pra tipo Água.",
                "Guarda-chuva esquecido. De novo."
            ),
            comments = listOf(
                "Que clima!",
                "Molhado e estiloso",
                "Aesthetic",
                "Leva toalha",
                "Choveu vibe"
            ),
            images = imgs("rain")
        ),
        ContentTheme(
            id = "flowers",
            captions = listOf(
                "Jardim de Celadon florescendo 🌸",
                "Spring arc unlocked.",
                "Polinizando o feed.",
                "Foto mansa pro álbum."
            ),
            comments = listOf(
                "Que flor!",
                "Lindo demais",
                "Cheiro daqui",
                "Soft feed",
                "Amei"
            ),
            images = imgs("flowers")
        ),
        ContentTheme(
            id = "snow",
            captions = listOf(
                "Seafoam gelado. Tipo Gelo aprovou ❄️",
                "Neve no sneaker. Valeu a pena.",
                "Breath visible. Aura também.",
                "Inverno mode."
            ),
            comments = listOf(
                "Que frio!",
                "Leva casaco",
                "Ice vibes",
                "Foto limpa",
                "Congelou o feed"
            ),
            images = imgs("snow")
        ),
        ContentTheme(
            id = "lake",
            captions = listOf(
                "Lago de Cerulean espelhado 🪞",
                "Reflexo perfeito. Quase Narciso.",
                "Água calma, mente também.",
                "Pescaria? Só de likes."
            ),
            comments = listOf(
                "Que reflexo!",
                "Zen total",
                "Lugar mágico",
                "Quero acampar",
                "Peace"
            ),
            images = imgs("lake")
        ),
        ContentTheme(
            id = "night",
            captions = listOf(
                "Céu de Pallet Town. Sem light pollution ✨",
                "Constelação de treinador.",
                "Noite quieta depois da Rota 1.",
                "Contando estrela = contando XP."
            ),
            comments = listOf(
                "Que céu!!",
                "Astro vibes",
                "Incrível",
                "Leva telescópio",
                "Soninho depois"
            ),
            images = imgs("night")
        )
    )

    fun forPokemonId(pokemonId: Int): ContentTheme = all[pokemonId.floorMod(all.size)]

    fun forIndex(i: Int): ContentTheme = all[i.floorMod(all.size)]

    /** Hashtags reais por tema (clicáveis no feed → Explore). */
    fun hashtagsFor(theme: ContentTheme): List<String> = when (theme.id) {
        "beach" -> listOf("#praia", "#kanto", "#fuchsia")
        "forest" -> listOf("#floresta", "#viridian", "#natureza")
        "sunset" -> listOf("#porDoSol", "#goldenHour", "#kanto")
        "city" -> listOf("#celadon", "#cidade", "#neon")
        "mountain" -> listOf("#montanha", "#treino", "#rota")
        "food" -> listOf("#berries", "#comida", "#lanche")
        "battle" -> listOf("#batalha", "#ginasio", "#versus")
        "rain" -> listOf("#chuva", "#clima", "#mood")
        "flowers" -> listOf("#flores", "#jardim", "#spring")
        "snow" -> listOf("#neve", "#inverno", "#ice")
        "lake" -> listOf("#lago", "#agua", "#pescador")
        "night" -> listOf("#noite", "#estrelas", "#pallet")
        else -> listOf("#pokemon", "#pokesocial")
    }

    fun captionWithHashtags(theme: ContentTheme, captionIndex: Int, extra: String? = null): String =
        buildString {
            append(theme.captions[captionIndex.floorMod(theme.captions.size)])
            append(' ')
            append(hashtagsFor(theme).joinToString(" "))
            if (!extra.isNullOrBlank()) {
                append(' ')
                append(extra)
            }
        }

    /** URI que Coil/ExoPlayer leem dos assets empacotados. */
    fun assetUri(relativePath: String): String = "file:///android_asset/$relativePath"

    fun imageUri(theme: ContentTheme, index: Int): String =
        assetUri(theme.images[index.floorMod(theme.images.size)])

    fun videoUri(index: Int): String =
        assetUri(videos[index.floorMod(videos.size)])
}

private fun Int.floorMod(m: Int): Int {
    val r = this % m
    return if (r >= 0) r else r + m
}
