package com.example.paochess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.*
import com.example.paochess.ui.theme.*
import kotlinx.coroutines.withContext

fun serializeBoard(board: Map<Square, Piece>): String {
    return board.entries.joinToString("|") { "${it.key.file},${it.key.rank},${it.value.type.name},${it.value.color.name}" }
}

fun deserializeBoard(str: String): Map<Square, Piece> {
    if (str.isEmpty()) return getInitialBoard()
    val map = mutableMapOf<Square, Piece>()
    str.split("|").forEach { entry ->
        val parts = entry.split(",")
        if (parts.size == 4) {
            map[Square(parts[0].toInt(), parts[1].toInt())] = Piece(PieceType.valueOf(parts[2]), PieceColor.valueOf(parts[3]))
        }
    }
    return map
}

@Composable
fun P2PGameScreen() {
    var board by remember { mutableStateOf(getInitialBoard()) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var legalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }
    
    // Host is White, Join is Black
    val myColor = if (P2PManager.isHost) PieceColor.WHITE else PieceColor.BLACK
    var currentTurn by remember { mutableStateOf(PieceColor.WHITE) }

    // Listen for incoming board states
    LaunchedEffect(Unit) {
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            while (P2PManager.connected) {
                // Not used directly, P2PManager handles the threading
                kotlinx.coroutines.delay(500)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Room Active", color = Orange, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        Text(
            if (currentTurn == myColor) "Your Turn" else "Opponent's Turn", 
            color = if (currentTurn == myColor) Green else LightGrey, 
            fontSize = 18.sp
        )
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (rank in 7 downTo 0) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        for (file in 0..7) {
                            val sq = Square(file, rank)
                            val piece = board[sq]
                            val isDark = (file + rank) % 2 == 1
                            val sqColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                            val isSelected = selectedSquare == sq
                            val isLegal = legalMoves.contains(sq)

                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight().background(sqColor)
                                    .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent)
                                    .clickable {
                                        if (currentTurn != myColor) return@clickable

                                        if (isLegal) {
                                            board = applyMove(board, selectedSquare!!, sq)
                                            P2PManager.sendBoardState(serializeBoard(board))
                                            selectedSquare = null
                                            legalMoves = emptyList()
                                            currentTurn = if (myColor == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                                        } else if (piece != null && piece.color == myColor) {
                                            selectedSquare = sq
                                            legalMoves = getLegalMoves(sq, piece, board)
                                        } else {
                                            selectedSquare = null
                                            legalMoves = emptyList()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLegal) Box(modifier = Modifier.size(12.dp).background(Color(0x8000FF00), CircleShape))
                                if (piece != null) {
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
                            }
                        }
                    }
                }
            }
        }
    }
}
