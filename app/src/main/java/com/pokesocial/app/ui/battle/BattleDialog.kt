package com.pokesocial.app.ui.battle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pokesocial.app.core.BattleMon
import com.pokesocial.app.core.BattleMove
import com.pokesocial.app.core.PokemonBattle
import com.pokesocial.app.domain.model.User
import com.pokesocial.app.ui.components.AppAsyncImage
import com.pokesocial.app.ui.components.typeDisplayName
import com.pokesocial.app.ui.theme.IgBlack
import com.pokesocial.app.ui.theme.IgGray
import com.pokesocial.app.ui.theme.IgWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class BattlePhase { MENU, MOVES, BUSY, RESULT }

@Composable
fun BattleDialog(
    peer: User,
    onDismiss: () -> Unit,
    onFinished: (won: Boolean, fled: Boolean) -> Unit
) {
    val player = remember { PokemonBattle.lucario() }
    val foe = remember(peer.id) { PokemonBattle.fromUser(peer) }
    var playerHp by remember { mutableIntStateOf(player.maxHp) }
    var foeHp by remember { mutableIntStateOf(foe.maxHp) }
    var phase by remember { mutableStateOf(BattlePhase.MENU) }
    var log by remember { mutableStateOf("${foe.name} quer batalhar!") }
    var won by remember { mutableStateOf(false) }
    var fled by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun close() {
        if (phase == BattlePhase.BUSY) return
        when {
            phase == BattlePhase.RESULT -> onFinished(won, fled)
            started -> onFinished(false, true)
        }
        onDismiss()
    }

    fun finish(playerWon: Boolean, didFlee: Boolean) {
        won = playerWon
        fled = didFlee
        phase = BattlePhase.RESULT
        log = when {
            didFlee -> "Você fugiu da batalha!"
            playerWon -> "${foe.name} foi derrotado!\nVocê venceu!"
            else -> "Lucario foi derrotado…"
        }
    }

    fun playTurn(move: BattleMove) {
        if (phase == BattlePhase.BUSY || phase == BattlePhase.RESULT) return
        started = true
        scope.launch {
            phase = BattlePhase.BUSY
            val ai = PokemonBattle.pickAiMove(foe, player)
            val playerFirst = PokemonBattle.playerGoesFirst(move, ai, player, foe)
            val firstMove = if (playerFirst) move else ai
            val firstAtk = if (playerFirst) player else foe
            val firstDef = if (playerFirst) foe else player
            val r1 = PokemonBattle.resolve(firstMove, firstAtk, firstDef)
            if (playerFirst) foeHp = foe.hp else playerHp = player.hp
            log = PokemonBattle.logLine(r1)
            delay(1100)
            if (firstDef.fainted) {
                finish(playerWon = playerFirst, didFlee = false)
                return@launch
            }
            val r2 = PokemonBattle.resolve(
                if (playerFirst) ai else move,
                if (playerFirst) foe else player,
                if (playerFirst) player else foe
            )
            if (playerFirst) playerHp = player.hp else foeHp = foe.hp
            log = PokemonBattle.logLine(r2)
            delay(1100)
            when {
                player.fainted -> finish(playerWon = false, didFlee = false)
                foe.fainted -> finish(playerWon = true, didFlee = false)
                else -> {
                    log = "O que Lucario fará?"
                    phase = BattlePhase.MENU
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { close() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(IgWhite)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Batalha",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = IgBlack,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { close() }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Fechar", tint = IgBlack)
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(268.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF87CEEB), Color(0xFFB4E391), Color(0xFF7CB342))
                        )
                    )
            ) {
                FoeHud(
                    mon = foe,
                    hp = foeHp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 12.dp)
                )
                AppAsyncImage(
                    data = PokemonBattle.spriteFront(foe.pokemonId),
                    contentDescription = foe.name,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 20.dp, top = 28.dp)
                        .size(128.dp)
                )
                AppAsyncImage(
                    data = PokemonBattle.spriteBack(player.pokemonId),
                    contentDescription = player.name,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 8.dp)
                        .size(140.dp)
                )
                PlayerHud(
                    mon = player,
                    hp = playerHp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 12.dp)
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F7F7))
                    .padding(12.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE6E6E6), RoundedCornerShape(12.dp))
                        .background(IgWhite)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(log, color = IgBlack, fontSize = 14.sp, lineHeight = 18.sp)
                }
                Spacer(Modifier.height(10.dp))
                when (phase) {
                    BattlePhase.MENU -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BattleChoice("Lutar", Color(0xFFE53935), Modifier.weight(1f)) {
                                phase = BattlePhase.MOVES
                                log = "Escolha um golpe."
                            }
                            BattleChoice("Fugir", Color(0xFF1E88E5), Modifier.weight(1f)) {
                                fled = true
                                finish(playerWon = false, didFlee = true)
                            }
                        }
                    }
                    BattlePhase.MOVES -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            player.moves.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    row.forEach { move ->
                                        MoveButton(move, Modifier.weight(1f)) { playTurn(move) }
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                            Text(
                                "Voltar",
                                color = IgGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable {
                                        phase = BattlePhase.MENU
                                        log = "O que Lucario fará?"
                                    }
                            )
                        }
                    }
                    BattlePhase.BUSY -> {
                        Text("…", color = IgGray, fontSize = 13.sp, modifier = Modifier.padding(8.dp))
                    }
                    BattlePhase.RESULT -> {
                        BattleChoice(
                            "Fechar",
                            if (won && !fled) Color(0xFF43A047) else IgBlack,
                            Modifier.fillMaxWidth()
                        ) {
                            close()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoeHud(mon: BattleMon, hp: Int, modifier: Modifier = Modifier) {
    HudCard(name = mon.name, types = mon.types, hp = hp, maxHp = mon.maxHp, modifier = modifier)
}

@Composable
private fun PlayerHud(mon: BattleMon, hp: Int, modifier: Modifier = Modifier) {
    HudCard(name = mon.name, types = mon.types, hp = hp, maxHp = mon.maxHp, modifier = modifier)
}

@Composable
private fun HudCard(
    name: String,
    types: List<String>,
    hp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier
) {
    val fraction = (hp.toFloat() / maxHp).coerceIn(0f, 1f)
    val animated by animateFloatAsState(fraction, animationSpec = tween(420), label = "hp")
    val barColor = when {
        animated > 0.5f -> Color(0xFF4CAF50)
        animated > 0.2f -> Color(0xFFFFC107)
        else -> Color(0xFFE53935)
    }
    Column(
        modifier
            .width(168.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(IgWhite.copy(alpha = 0.94f))
            .border(1.dp, Color(0x14000000), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            name,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = IgBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            types.joinToString(" · ") { typeDisplayName(it) },
            fontSize = 10.sp,
            color = IgGray
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("HP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = IgGray)
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(animated)
                        .height(8.dp)
                        .background(barColor)
                )
            }
        }
        Text(
            "$hp / $maxHp",
            fontSize = 10.sp,
            color = IgGray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun BattleChoice(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = IgWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun MoveButton(move: BattleMove, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val color = Color(PokemonBattle.typeColor(move.type))
    Row(
        modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(IgWhite)
            .border(1.dp, Color(0xFFE6E6E6), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                move.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = IgBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                typeDisplayName(move.type),
                fontSize = 10.sp,
                color = color,
                textAlign = TextAlign.Start
            )
        }
    }
}
