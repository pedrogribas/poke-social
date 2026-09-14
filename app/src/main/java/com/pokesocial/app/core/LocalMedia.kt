package com.pokesocial.app.core

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri

object LocalMedia {
    /**
     * Converte URI salva no Room para algo que Coil/ExoPlayer entendem.
     * Room guarda: file:///android_asset/media/...
     * ExoPlayer prefere: asset:///media/...
     */
    fun forCoil(pathOrUri: String): String = pathOrUri

    fun forExoPlayer(pathOrUri: String): Uri {
        val assetPath = when {
            pathOrUri.startsWith("file:///android_asset/") ->
                pathOrUri.removePrefix("file:///android_asset/")
            pathOrUri.startsWith("asset:///") ->
                pathOrUri.removePrefix("asset:///")
            pathOrUri.startsWith("media/") -> pathOrUri
            else -> return pathOrUri.toUri()
        }
        return "asset:///$assetPath".toUri()
    }

    fun existsInAssets(context: Context, relativePath: String): Boolean =
        runCatching {
            context.assets.open(relativePath.removePrefix("media/").let {
                if (relativePath.startsWith("media/")) relativePath else relativePath
            }).close()
            true
        }.getOrDefault(false)
}
