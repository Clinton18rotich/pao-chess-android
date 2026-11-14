package com.example.paochess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.*
import com.example.paochess.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ReplaysScreen() {
    val context = LocalContext.current
    var games by remember { mutableStateOf(ReplayStore.load(context)) }
    var viewing by remember { mutableStateOf<SavedGame?>(null) }
    var step by remember { mutableStateOf(0) }
    var playing by remember { mutableStateOf(false) }

    if (viewing != null) {
        val g = viewing!!
        val board = remember(step) {
            var b = getInitialBoard()
            for (i in 0 until step) {
                val m = g.moves[i]
                b = applyMove(b, Square(m.fromF, m.fromR), Square(m.toF, m.toR))
            }
            b
        }

        LaunchedEffect(playing, step) {
            if (playing && step < g.moves.size) {
                delay(800)
                step++
            } else if (step >= g.moves.size) {
                playing = false
            }
        }

        Column(
            Modifier.fillMaxSize().background(DarkBg)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(onClick = { viewing = null; step = 0; playing = false }) {
                Text("← Back", color = Orange, fontWeight = FontWeight.Bold)
            }

            Text(
                if (g.result == "win") "🏆 Victory" else if (g.result == "draw") "🤝 Draw" else "💀 Defeat",
                color = if (g.result == "win") Green else if (g.result == "draw") Orange else Red,
                fontSize = 22.sp, fontWeight = FontWeight.Bold
            )
            Text("Move $step / ${g.moves.size}", color = LightGrey, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))

            Box(Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
                Column(Modifier.fillMaxSize()) {
                    for (rank in 7 downTo 0) {
                        Row(Modifier.fillMaxWidth().weight(1f)) {
                            for (file in 0..7) {
                                val sq = Square(file, rank)
                                val piece = board[sq]
                                val isDark = (file + rank) % 2 == 1
                                val c = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                                val isHighlight = step in 1..g.moves.size &&
                                    (file == g.moves[step-1].toF && rank == g.moves[step-1].toR)
                                Box(
                                    Modifier.weight(1f).fillMaxHeight().background(c)
                                        .border(if (isHighlight) 3.dp else 0.dp,
                                            if (isHighlight) Green else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (piece != null) RenderPieceMini(piece)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { step = 0; playing = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)) { Text("⏮") }
                Button(onClick = { if (step > 0) step-- },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)) { Text("◀") }
                Button(onClick = { playing = !playing },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black)) {
                    Text(if (playing) "⏸" else "▶")
                }
                Button(onClick = { if (step < g.moves.size) step++ },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)) { Text("▶") }
                Button(onClick = { step = g.moves.size; playing = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)) { Text("⏭") }
            }
        }
        return
    }

    Column(
        Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("♟️ Replays", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("${games.size} saved games", color = LightGrey, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        if (games.isEmpty()) {
            Text("No games saved yet.\nPlay a game, then tap 💾 Save this game.",
                color = LightGrey, fontSize = 14.sp)
            return@Column
        }

        games.forEach { g ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                    viewing = g; step = 0; playing = false
                },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (g.result == "win") "🏆" else if (g.result == "draw") "🤝" else "💀",
                        fontSize = 24.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            g.result.uppercase(),
                            color = if (g.result == "win") Green else if (g.result == "draw") Orange else Red,
                            fontWeight = FontWeight.Bold
                        )
                        Text("${g.moves.size} moves",
                            color = LightGrey, fontSize = 11.sp)
                    }
                    TextButton(onClick = {
                        ReplayStore.delete(context, g.id)
                        games = ReplayStore.load(context)
                    }) { Text("×", color = LightGrey, fontSize = 20.sp) }
                }
            }
        }
    }
}

@Composable
private fun RenderPieceMini(piece: Piece) {
    val symbol = when (piece.type) {
        PieceType.PAWN -> "♟"; PieceType.KNIGHT -> "♞"; PieceType.BISHOP -> "♝"
        PieceType.ROOK -> "♜"; PieceType.QUEEN -> "♛"; PieceType.KING -> "♚"
    }
    val text = if (piece.color == PieceColor.WHITE) {
        symbol.replace("♟","♙").replace("♞","♘").replace("♝","♗")
            .replace("♜","♖").replace("♛","♕").replace("♚","♔")
    } else symbol
    Text(text = text, color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold)
}
