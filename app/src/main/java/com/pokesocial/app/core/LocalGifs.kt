package com.pokesocial.app.core

/** Pack local de GIFs/memes em assets (comentários, DMs, stories). */
object LocalGifs {
    val all: List<String> = (1..32).map { i ->
        "file:///android_asset/media/gifs/${i.toString().padStart(2, '0')}.gif"
    }

    fun pick(seed: Int): String = all[seed.floorMod(all.size)]
}

private fun Int.floorMod(m: Int): Int {
    val r = this % m
    return if (r >= 0) r else r + m
}
