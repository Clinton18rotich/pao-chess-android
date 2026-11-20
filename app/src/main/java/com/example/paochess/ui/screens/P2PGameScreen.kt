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

@Composable
fun P2PGameScreen() {
    var board by remember { mutableStateOf(getInitialBoard()) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var legalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }
    var myTurn by remember { mutableStateOf(P2PManager.isHost) } // Host is White

    // Since socket listening happens in background, we'd ideally use a ViewModel or Flow here
    // For this simple implementation, we assume the move sync is handled by calling a function
    // (This part requires a bit more Compose state management for production)

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (myTurn) "Your Turn" else "Opponent's Turn", color = if (myTurn) Green else LightGrey, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                                        if (!myTurn) return@clickable

                                        if (isLegal) {
                                            board = applyMove(board, selectedSquare!!, sq)
                                            P2PManager.sendMove(selectedSquare!!.file, selectedSquare!!.rank, sq.file, sq.rank)
                                            selectedSquare = null
                                            legalMoves = emptyList()
                                            myTurn = false
                                        } else if (piece != null && piece.color == (if (P2PManager.isHost) PieceColor.WHITE else PieceColor.BLACK)) {
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
