package com.example.paochess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
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
fun ChessBoardScreen() {
    var board by remember { mutableStateOf(getInitialBoard()) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var legalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("♟️ Chess Trainer", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Tap a piece to see its legal moves", color = LightGrey, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (rank in 7 downTo 0) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        for (file in 0..7) {
                            val square = Square(file, rank)
                            val piece = board[square]
                            val isDark = (file + rank) % 2 == 1
                            val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                            val isSelected = selectedSquare == square
                            val isLegal = legalMoves.contains(square)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(squareColor)
                                    .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent)
                                    .clickable {
                                        if (isLegal) {
                                            val newBoard = board.toMutableMap()
                                            newBoard[square] = board[selectedSquare!!]!!
                                            newBoard.remove(selectedSquare!!)
                                            board = newBoard
                                            selectedSquare = null
                                            legalMoves = emptyList()
                                        } else if (piece != null) {
                                            selectedSquare = square
                                            legalMoves = getLegalMoves(square, piece, board)
                                        } else {
                                            selectedSquare = null
                                            legalMoves = emptyList()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLegal) {
                                    Box(modifier = Modifier.size(12.dp).background(Color(0x8000FF00), CircleShape))
                                }
                                if (piece != null) {
                                    PieceView(piece)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieceView(piece: Piece) {
    val symbol = when (piece.type) {
        PieceType.PAWN -> "♟"
        PieceType.KNIGHT -> "♞"
        PieceType.BISHOP -> "♝"
        PieceType.ROOK -> "♜"
        PieceType.QUEEN -> "♛"
        PieceType.KING -> "♚"
    }
    val text = if (piece.color == PieceColor.WHITE) {
        symbol.replace("♟", "♙").replace("♞", "♘").replace("♝", "♗")
            .replace("♜", "♖").replace("♛", "♕").replace("♚", "♔")
    } else {
        symbol
    }
    val color = if (piece.color == PieceColor.WHITE) Color(0xFF000000) else Color(0xFF000000)
    Text(text = text, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
}
