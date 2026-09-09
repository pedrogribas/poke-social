package com.pokesocial.app

import android.app.Application
import com.pokesocial.app.data.local.SeedManager
import com.pokesocial.app.data.local.db.AppDatabase
import com.pokesocial.app.data.remote.PokeApiClient
import com.pokesocial.app.data.repository.SocialRepository

class PokeSocialApp : Application() {

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
}
