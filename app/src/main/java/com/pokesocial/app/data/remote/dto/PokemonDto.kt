package com.pokesocial.app.data.remote.dto

import com.squareup.moshi.Json

// DTOs espelham o JSON da API. No domínio usamos Pokemon (modelo limpo).
// RN: tipar a response do axios com uma interface.
// Usamos reflexão Moshi (KotlinJsonAdapterFactory) — sem kapt/ksp neste projeto.

data class PokemonListResponseDto(
    val count: Int,
    val results: List<PokemonListItemDto>
)

data class PokemonListItemDto(
    val name: String,
    val url: String
)

data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val sprites: SpritesDto,
    val types: List<TypeSlotDto> = emptyList()
)

data class SpritesDto(
    @Json(name = "front_default") val frontDefault: String?,
    val other: OtherSpritesDto?
)

data class OtherSpritesDto(
    @Json(name = "official-artwork") val officialArtwork: OfficialArtworkDto?
)

data class OfficialArtworkDto(
    @Json(name = "front_default") val frontDefault: String?
)

data class TypeSlotDto(
    val slot: Int,
    val type: NamedResourceDto
)

data class NamedResourceDto(
    val name: String,
    val url: String
)
