package com.pokesocial.app.core

import com.pokesocial.app.domain.model.User
import kotlin.math.max
import kotlin.random.Random

enum class MoveCategory { PHYSICAL, SPECIAL }

data class BattleMove(
    val name: String,
    val type: String,
    val category: MoveCategory,
    val power: Int,
    val priority: Int = 0
)

data class BattleMon(
    val pokemonId: Int,
    val name: String,
    val types: List<String>,
    val maxHp: Int,
    val atk: Int,
    val def: Int,
    val spa: Int,
    val spd: Int,
    val spe: Int,
    val moves: List<BattleMove>,
    var hp: Int = maxHp
) {
    val fainted: Boolean get() = hp <= 0
    val hpFraction: Float get() = (hp.toFloat() / maxHp).coerceIn(0f, 1f)
}

data class MoveResult(
    val attackerName: String,
    val move: BattleMove,
    val damage: Int,
    val effectiveness: Float,
    val critical: Boolean,
    val defenderHp: Int,
    val defenderMaxHp: Int
)

object PokemonBattle {

    fun lucario(): BattleMon = build(
        id = AppConstants.ME_POKEMON_ID,
        name = AppConstants.ME_DISPLAY_NAME,
        types = listOf("fighting", "steel"),
        moves = lucarioMoves
    )

    fun fromUser(user: User): BattleMon = build(
        id = user.pokemonId,
        name = user.displayName.ifBlank { user.username },
        types = user.types.map { it.lowercase() }.ifEmpty { listOf("normal") }
    )

    fun resolve(move: BattleMove, attacker: BattleMon, defender: BattleMon): MoveResult {
        val eff = typeEffectiveness(move.type, defender.types)
        val crit = Random.nextFloat() < 0.0625f
        val damage = if (eff == 0f) 0 else computeDamage(move, attacker, defender, eff, crit)
        defender.hp = (defender.hp - damage).coerceAtLeast(0)
        return MoveResult(
            attackerName = attacker.name,
            move = move,
            damage = damage,
            effectiveness = eff,
            critical = crit && damage > 0,
            defenderHp = defender.hp,
            defenderMaxHp = defender.maxHp
        )
    }

    fun playerGoesFirst(
        playerMove: BattleMove,
        foeMove: BattleMove,
        player: BattleMon,
        foe: BattleMon
    ): Boolean {
        if (playerMove.priority != foeMove.priority) return playerMove.priority > foeMove.priority
        if (player.spe != foe.spe) return player.spe > foe.spe
        return true
    }

    fun pickAiMove(attacker: BattleMon, defender: BattleMon): BattleMove {
        val scored = attacker.moves.maxBy { move ->
            val eff = typeEffectiveness(move.type, defender.types)
            val stab = if (move.type in attacker.types) 1.5f else 1f
            move.power * eff * stab
        }
        return if (Random.nextFloat() < 0.75f) scored else attacker.moves.random()
    }

    fun logLine(result: MoveResult): String = buildString {
        append("${result.attackerName} usou ${result.move.name}!")
        when {
            result.effectiveness == 0f -> append(" Não afetou ${if (result.attackerName == AppConstants.ME_DISPLAY_NAME) "o oponente" else AppConstants.ME_DISPLAY_NAME}…")
            result.effectiveness >= 4f -> append(" É extremamente eficaz!")
            result.effectiveness >= 2f -> append(" É super eficaz!")
            result.effectiveness <= 0.25f -> append(" Quase não fez efeito…")
            result.effectiveness < 1f -> append(" Não é muito eficaz…")
        }
        if (result.critical) append(" Acerto crítico!")
    }

    fun spriteFront(id: Int): String =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

    fun spriteBack(id: Int): String =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/$id.png"

    fun typeColor(type: String): Long = typeColors[type.lowercase()] ?: 0xFFA8A878

    private fun build(
        id: Int,
        name: String,
        types: List<String>,
        moves: List<BattleMove>? = null
    ): BattleMon {
        val s = statsFor(id, types)
        return BattleMon(
            pokemonId = id,
            name = name,
            types = types,
            maxHp = s.hp,
            atk = s.atk,
            def = s.def,
            spa = s.spa,
            spd = s.spd,
            spe = s.spe,
            moves = moves ?: movesFor(types)
        )
    }

    private data class Stats(val hp: Int, val atk: Int, val def: Int, val spa: Int, val spd: Int, val spe: Int)

    /** Nível fixo implícito (~50): HP de batalha equilibrado, sem grind. */
    private fun statsFor(id: Int, types: List<String>): Stats {
        val base = uniqueBase[id] ?: typeBase(types)
        return Stats(
            hp = 95 + (base[0] * 1.15).toInt(),
            atk = 40 + base[1],
            def = 40 + base[2],
            spa = 40 + base[3],
            spd = 40 + base[4],
            spe = 35 + base[5]
        )
    }

    private val uniqueBase: Map<Int, IntArray> = mapOf(
        448 to intArrayOf(70, 110, 70, 115, 70, 90),
        25 to intArrayOf(35, 55, 40, 50, 50, 90),
        6 to intArrayOf(78, 84, 78, 109, 85, 100),
        150 to intArrayOf(106, 110, 90, 154, 90, 130),
        94 to intArrayOf(60, 65, 60, 130, 75, 110),
        130 to intArrayOf(95, 125, 79, 60, 100, 81),
        39 to intArrayOf(115, 45, 20, 45, 25, 20),
        4 to intArrayOf(39, 52, 43, 60, 50, 65),
        7 to intArrayOf(44, 48, 65, 50, 64, 43),
        133 to intArrayOf(55, 55, 50, 45, 65, 55),
        143 to intArrayOf(160, 110, 65, 65, 110, 30),
        3 to intArrayOf(80, 82, 83, 100, 100, 80),
        9 to intArrayOf(79, 83, 100, 85, 105, 78),
        65 to intArrayOf(55, 50, 45, 135, 95, 120),
        68 to intArrayOf(90, 130, 80, 65, 85, 55),
        76 to intArrayOf(80, 120, 130, 55, 65, 45),
        131 to intArrayOf(130, 85, 80, 85, 95, 60),
        144 to intArrayOf(90, 85, 100, 95, 125, 85),
        145 to intArrayOf(90, 90, 85, 125, 90, 100),
        146 to intArrayOf(90, 100, 90, 125, 85, 90),
        149 to intArrayOf(91, 134, 95, 100, 100, 80),
        151 to intArrayOf(100, 100, 100, 100, 100, 100)
    )

    private fun typeBase(types: List<String>): IntArray {
        val acc = IntArray(6)
        val list = types.ifEmpty { listOf("normal") }
        list.forEach { t ->
            val b = typeBases[t] ?: intArrayOf(70, 70, 70, 70, 70, 70)
            for (i in 0..5) acc[i] += b[i]
        }
        for (i in 0..5) acc[i] = acc[i] / list.size
        return acc
    }

    private val typeBases = mapOf(
        "normal" to intArrayOf(80, 75, 70, 60, 70, 70),
        "fire" to intArrayOf(70, 80, 65, 95, 70, 80),
        "water" to intArrayOf(80, 70, 80, 85, 80, 65),
        "electric" to intArrayOf(60, 65, 60, 95, 70, 100),
        "grass" to intArrayOf(75, 75, 75, 80, 80, 60),
        "ice" to intArrayOf(70, 70, 70, 90, 80, 65),
        "fighting" to intArrayOf(75, 110, 75, 55, 70, 70),
        "poison" to intArrayOf(70, 75, 70, 75, 75, 70),
        "ground" to intArrayOf(85, 95, 90, 55, 70, 50),
        "flying" to intArrayOf(70, 80, 65, 75, 70, 95),
        "psychic" to intArrayOf(70, 55, 65, 110, 90, 85),
        "bug" to intArrayOf(65, 80, 70, 55, 70, 75),
        "rock" to intArrayOf(75, 90, 110, 50, 70, 40),
        "ghost" to intArrayOf(60, 60, 65, 100, 80, 85),
        "dragon" to intArrayOf(85, 100, 85, 95, 85, 80),
        "dark" to intArrayOf(70, 90, 70, 80, 70, 80),
        "steel" to intArrayOf(75, 80, 110, 70, 90, 55),
        "fairy" to intArrayOf(75, 60, 70, 90, 95, 70)
    )

    private val lucarioMoves = listOf(
        BattleMove("Esfera de Aura", "fighting", MoveCategory.SPECIAL, 80),
        BattleMove("Canhão de Flash", "steel", MoveCategory.SPECIAL, 80),
        BattleMove("Close Combat", "fighting", MoveCategory.PHYSICAL, 90),
        BattleMove("Velocidade Extrema", "normal", MoveCategory.PHYSICAL, 80, priority = 2)
    )

    private val poolByType: Map<String, List<BattleMove>> = mapOf(
        "normal" to listOf(
            BattleMove("Investida", "normal", MoveCategory.PHYSICAL, 40),
            BattleMove("Hiper Voz", "normal", MoveCategory.SPECIAL, 90),
            BattleMove("Destruição", "normal", MoveCategory.PHYSICAL, 80)
        ),
        "fire" to listOf(
            BattleMove("Lança-chamas", "fire", MoveCategory.SPECIAL, 90),
            BattleMove("Giro de Fogo", "fire", MoveCategory.PHYSICAL, 60),
            BattleMove("Onda de Calor", "fire", MoveCategory.SPECIAL, 95)
        ),
        "water" to listOf(
            BattleMove("Jato d'Água", "water", MoveCategory.SPECIAL, 40),
            BattleMove("Hidrobomba", "water", MoveCategory.SPECIAL, 110),
            BattleMove("Cachoeira", "water", MoveCategory.PHYSICAL, 80)
        ),
        "electric" to listOf(
            BattleMove("Choque do Trovão", "electric", MoveCategory.SPECIAL, 40),
            BattleMove("Raio", "electric", MoveCategory.SPECIAL, 90),
            BattleMove("Trovão", "electric", MoveCategory.SPECIAL, 110)
        ),
        "grass" to listOf(
            BattleMove("Folha Navalha", "grass", MoveCategory.PHYSICAL, 55),
            BattleMove("Raio Solar", "grass", MoveCategory.SPECIAL, 120),
            BattleMove("Chicote de Vinha", "grass", MoveCategory.PHYSICAL, 45)
        ),
        "ice" to listOf(
            BattleMove("Raio de Gelo", "ice", MoveCategory.SPECIAL, 90),
            BattleMove("Punho de Gelo", "ice", MoveCategory.PHYSICAL, 75),
            BattleMove("Nevasca", "ice", MoveCategory.SPECIAL, 110)
        ),
        "fighting" to listOf(
            BattleMove("Chute Duplo", "fighting", MoveCategory.PHYSICAL, 60),
            BattleMove("Close Combat", "fighting", MoveCategory.PHYSICAL, 90),
            BattleMove("Aú", "fighting", MoveCategory.PHYSICAL, 60)
        ),
        "poison" to listOf(
            BattleMove("Ácido", "poison", MoveCategory.SPECIAL, 40),
            BattleMove("Bomba de Lodo", "poison", MoveCategory.SPECIAL, 90),
            BattleMove("Picada Venenosa", "poison", MoveCategory.PHYSICAL, 50)
        ),
        "ground" to listOf(
            BattleMove("Terremoto", "ground", MoveCategory.PHYSICAL, 100),
            BattleMove("Cavada", "ground", MoveCategory.PHYSICAL, 80),
            BattleMove("Lama", "ground", MoveCategory.SPECIAL, 55)
        ),
        "flying" to listOf(
            BattleMove("Ataque de Asa", "flying", MoveCategory.PHYSICAL, 60),
            BattleMove("Golpe Aéreo", "flying", MoveCategory.SPECIAL, 75),
            BattleMove("Brave Bird", "flying", MoveCategory.PHYSICAL, 90)
        ),
        "psychic" to listOf(
            BattleMove("Confusão", "psychic", MoveCategory.SPECIAL, 50),
            BattleMove("Psíquico", "psychic", MoveCategory.SPECIAL, 90),
            BattleMove("Futuro Sombrio", "psychic", MoveCategory.SPECIAL, 80)
        ),
        "bug" to listOf(
            BattleMove("Picada", "bug", MoveCategory.PHYSICAL, 60),
            BattleMove("Tesoura X", "bug", MoveCategory.PHYSICAL, 80),
            BattleMove("Zumbido", "bug", MoveCategory.SPECIAL, 90)
        ),
        "rock" to listOf(
            BattleMove("Lançamento de Rocha", "rock", MoveCategory.PHYSICAL, 50),
            BattleMove("Tombos", "rock", MoveCategory.PHYSICAL, 75),
            BattleMove("Pedra Afiada", "rock", MoveCategory.PHYSICAL, 100)
        ),
        "ghost" to listOf(
            BattleMove("Soco Sombrio", "ghost", MoveCategory.PHYSICAL, 80),
            BattleMove("Bola Sombria", "ghost", MoveCategory.SPECIAL, 80),
            BattleMove("Maldição", "ghost", MoveCategory.SPECIAL, 70)
        ),
        "dragon" to listOf(
            BattleMove("Pulso do Dragão", "dragon", MoveCategory.SPECIAL, 85),
            BattleMove("Garra de Dragão", "dragon", MoveCategory.PHYSICAL, 80),
            BattleMove("Draco Meteoro", "dragon", MoveCategory.SPECIAL, 110)
        ),
        "dark" to listOf(
            BattleMove("Mordida", "dark", MoveCategory.PHYSICAL, 60),
            BattleMove("Pulso Sombrio", "dark", MoveCategory.SPECIAL, 80),
            BattleMove("Feitiço Sombrio", "dark", MoveCategory.SPECIAL, 80)
        ),
        "steel" to listOf(
            BattleMove("Garra de Metal", "steel", MoveCategory.PHYSICAL, 50),
            BattleMove("Canhão de Flash", "steel", MoveCategory.SPECIAL, 80),
            BattleMove("Cabeça de Ferro", "steel", MoveCategory.PHYSICAL, 80)
        ),
        "fairy" to listOf(
            BattleMove("Brilho Encantador", "fairy", MoveCategory.SPECIAL, 80),
            BattleMove("Vento Feérico", "fairy", MoveCategory.SPECIAL, 40),
            BattleMove("Força da Lua", "fairy", MoveCategory.SPECIAL, 95)
        )
    )

    private fun movesFor(types: List<String>): List<BattleMove> {
        val primary = types.firstOrNull() ?: "normal"
        val secondary = types.getOrNull(1)
        val picked = mutableListOf<BattleMove>()
        poolByType[primary]?.take(2)?.let { picked += it }
        if (secondary != null) {
            poolByType[secondary]?.firstOrNull()?.let { picked += it }
        }
        val coverage = when (primary) {
            "ghost" -> BattleMove("Bola Sombria", "ghost", MoveCategory.SPECIAL, 80)
            "steel", "rock" -> BattleMove("Terremoto", "ground", MoveCategory.PHYSICAL, 100)
            "water" -> BattleMove("Raio de Gelo", "ice", MoveCategory.SPECIAL, 90)
            "fire" -> BattleMove("Golpe Aéreo", "flying", MoveCategory.SPECIAL, 75)
            "electric" -> BattleMove("Soco Sombrio", "ghost", MoveCategory.PHYSICAL, 80)
            else -> BattleMove("Investida", "normal", MoveCategory.PHYSICAL, 40)
        }
        if (picked.none { it.name == coverage.name }) picked += coverage
        while (picked.size < 4) {
            val extra = poolByType[primary]?.getOrNull(picked.size) ?: coverage
            if (picked.none { it.name == extra.name }) picked += extra else break
        }
        return picked.take(4)
    }

    private fun computeDamage(
        move: BattleMove,
        attacker: BattleMon,
        defender: BattleMon,
        effectiveness: Float,
        critical: Boolean
    ): Int {
        val atk = if (move.category == MoveCategory.SPECIAL) attacker.spa else attacker.atk
        val def = if (move.category == MoveCategory.SPECIAL) defender.spd else defender.def
        val base = ((22 * move.power * atk) / max(def, 1)) / 50 + 2
        val stab = if (move.type in attacker.types) 1.5f else 1f
        val crit = if (critical) 1.5f else 1f
        val rand = Random.nextDouble(0.85, 1.0).toFloat()
        return max(1, (base * stab * effectiveness * crit * rand).toInt())
    }

    fun typeEffectiveness(attackType: String, defenderTypes: List<String>): Float {
        var mult = 1f
        defenderTypes.forEach { def ->
            val atk = attackType.lowercase()
            val d = def.lowercase()
            when {
                immune[atk]?.contains(d) == true -> mult = 0f
                superEffective[atk]?.contains(d) == true -> if (mult != 0f) mult *= 2f
                notVery[atk]?.contains(d) == true -> if (mult != 0f) mult *= 0.5f
            }
        }
        return mult
    }

    private val superEffective = mapOf(
        "normal" to emptySet<String>(),
        "fire" to setOf("grass", "ice", "bug", "steel"),
        "water" to setOf("fire", "ground", "rock"),
        "electric" to setOf("water", "flying"),
        "grass" to setOf("water", "ground", "rock"),
        "ice" to setOf("grass", "ground", "flying", "dragon"),
        "fighting" to setOf("normal", "ice", "rock", "dark", "steel"),
        "poison" to setOf("grass", "fairy"),
        "ground" to setOf("fire", "electric", "poison", "rock", "steel"),
        "flying" to setOf("grass", "fighting", "bug"),
        "psychic" to setOf("fighting", "poison"),
        "bug" to setOf("grass", "psychic", "dark"),
        "rock" to setOf("fire", "ice", "flying", "bug"),
        "ghost" to setOf("psychic", "ghost"),
        "dragon" to setOf("dragon"),
        "dark" to setOf("psychic", "ghost"),
        "steel" to setOf("ice", "rock", "fairy"),
        "fairy" to setOf("fighting", "dragon", "dark")
    )

    private val notVery = mapOf(
        "normal" to setOf("rock", "steel"),
        "fire" to setOf("fire", "water", "rock", "dragon"),
        "water" to setOf("water", "grass", "dragon"),
        "electric" to setOf("electric", "grass", "dragon"),
        "grass" to setOf("fire", "grass", "poison", "flying", "bug", "dragon", "steel"),
        "ice" to setOf("fire", "water", "ice", "steel"),
        "fighting" to setOf("poison", "flying", "psychic", "bug", "fairy"),
        "poison" to setOf("poison", "ground", "rock", "ghost"),
        "ground" to setOf("grass", "bug"),
        "flying" to setOf("electric", "rock", "steel"),
        "psychic" to setOf("psychic", "steel"),
        "bug" to setOf("fire", "fighting", "poison", "flying", "ghost", "steel", "fairy"),
        "rock" to setOf("fighting", "ground", "steel"),
        "ghost" to setOf("dark"),
        "dragon" to setOf("steel"),
        "dark" to setOf("fighting", "dark", "fairy"),
        "steel" to setOf("fire", "water", "electric", "steel"),
        "fairy" to setOf("fire", "poison", "steel")
    )

    private val immune = mapOf(
        "normal" to setOf("ghost"),
        "electric" to setOf("ground"),
        "fighting" to setOf("ghost"),
        "poison" to setOf("steel"),
        "ground" to setOf("flying"),
        "psychic" to setOf("dark"),
        "ghost" to setOf("normal"),
        "dragon" to setOf("fairy")
    )

    private val typeColors = mapOf(
        "normal" to 0xFFA8A878,
        "fire" to 0xFFF08030,
        "water" to 0xFF6890F0,
        "electric" to 0xFFF8D030,
        "grass" to 0xFF78C850,
        "ice" to 0xFF98D8D8,
        "fighting" to 0xFFC03028,
        "poison" to 0xFFA040A0,
        "ground" to 0xFFE0C068,
        "flying" to 0xFFA890F0,
        "psychic" to 0xFFF85888,
        "bug" to 0xFFA8B820,
        "rock" to 0xFFB8A038,
        "ghost" to 0xFF705898,
        "dragon" to 0xFF7038F8,
        "dark" to 0xFF705848,
        "steel" to 0xFFB8B8D0,
        "fairy" to 0xFFEE99AC
    )
}
