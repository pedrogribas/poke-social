package com.pokesocial.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokesocial.app.ui.theme.IgGray

private val TypeDisplayNames = mapOf(
    "normal" to "Normal",
    "fire" to "Fogo",
    "water" to "Água",
    "electric" to "Elétrico",
    "grass" to "Planta",
    "ice" to "Gelo",
    "fighting" to "Lutador",
    "poison" to "Veneno",
    "ground" to "Terra",
    "flying" to "Voador",
    "psychic" to "Psíquico",
    "bug" to "Inseto",
    "rock" to "Pedra",
    "ghost" to "Fantasma",
    "dragon" to "Dragão",
    "dark" to "Sombrio",
    "steel" to "Aço",
    "fairy" to "Fada"
)

fun typeDisplayName(type: String): String {
    val key = type.lowercase().trim()
    return TypeDisplayNames[key] ?: type.replaceFirstChar { it.uppercase() }
}

/** Linha de categoria estilo Instagram (no lugar de “Influencer”). */
@Composable
fun PokemonTypeChips(types: List<String>, modifier: Modifier = Modifier) {
    if (types.isEmpty()) return
    val label = types.joinToString(" | ") { typeDisplayName(it) }
    Text(
        label,
        modifier = modifier.padding(top = 2.dp),
        color = IgGray,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal
    )
}
