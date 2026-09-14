package com.pokesocial.app

import android.app.Application
import android.os.Build.VERSION.SDK_INT
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.pokesocial.app.data.local.SeedManager
import com.pokesocial.app.data.local.db.AppDatabase
import com.pokesocial.app.data.remote.PokeApiClient
import com.pokesocial.app.data.repository.SocialRepository

class PokeSocialApp : Application(), ImageLoaderFactory {

    lateinit var database: AppDatabase
        private set
    lateinit var socialRepository: SocialRepository
        private set
    lateinit var seedManager: SeedManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.get(this)
        val api = PokeApiClient.create()
        socialRepository = SocialRepository(database)
        seedManager = SeedManager(database, api)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
}
