package com.pokesocial.app.data.remote

import com.pokesocial.app.data.remote.dto.PokemonDetailDto
import com.pokesocial.app.data.remote.dto.PokemonListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit transforma esta interface em chamadas HTTP.
 * RN: export const pokeApi = { getList: () => fetch(...), getById: (id) => fetch(...) }
 *
 * `suspend` = função assíncrona (≈ async function). Só pode ser chamada de uma coroutine.
 */
interface PokeApiService {

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 151,
        @Query("offset") offset: Int = 0
    ): PokemonListResponseDto

    @GET("pokemon/{idOrName}")
    suspend fun getPokemon(
        @Path("idOrName") idOrName: String
    ): PokemonDetailDto
}
