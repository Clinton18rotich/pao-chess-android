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

        // 8x8 Board
        for (rank in 7 downTo 0) {
            Row(modifier = Modifier.fillMaxWidth().aspectRatio(8f)) {
                for (file in 0..7) {
                    val square = Square(file, rank)
                    val piece = board[square]
                    val isDark = (file + rank) % 2 == 1
                    val squareColor = if (isDark) Color(0xFF5D4037) else Color(0xFFD7CCC8)
                    val isSelected = selectedSquare == square
                    val isLegalMove = legalMoves.contains(square)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(squareColor)
                            .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent)
                            .clickable {
                                if (isLegalMove) {
                                    // Move piece
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
                        if (isLegalMove) {
                            Box(modifier = Modifier.size(16.dp).background(Color(0x8000FF00), CircleShape))
                        }
                        if (piece != null) {
                            PieceView(piece)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Selected: ${selectedSquare ?: "None"}", color = LightGrey, fontSize = 14.sp)
    }
}

@Composable
fun PieceView(piece: Piece) {
    val color = if (piece.color == PieceColor.WHITE) Color.White else Color.Black
    val textColor = if (piece.color == PieceColor.WHITE) Color.Black else Color.White
    val letter = when (piece.type) {
        PieceType.PAWN -> "P"
        PieceType.KNIGHT -> "N"
        PieceType.BISHOP -> "B"
        PieceType.ROOK -> "R"
        PieceType.QUEEN -> "Q"
        PieceType.KING -> "K"
    }
    Box(
        modifier = Modifier.size(36.dp).background(color, CircleShape).border(1.dp, Color.Gray, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(letter, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
