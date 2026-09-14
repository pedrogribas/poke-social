package com.pokesocial.app.core

object AppConstants {
    const val ME_USER_ID = "user-lucario"
    const val ME_USERNAME = "lucar_10"
    const val ME_DISPLAY_NAME = "Lucario"
    const val ME_POKEMON_ID = 448
    const val FEED_PAGE_SIZE = 8
    const val META_SEEDED = "seeded_v13"

    fun artworkUrl(pokemonId: Int): String =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokemonId.png"

    fun spriteUrl(pokemonId: Int): String =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"

    fun picsum(seed: String, w: Int = 1080, h: Int = 1350): String =
        "https://picsum.photos/seed/$seed/$w/$h"

    val SAMPLE_VIDEOS = listOf(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    )
}
