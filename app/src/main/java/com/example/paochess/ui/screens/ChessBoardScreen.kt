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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
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
        Text("♟️ Chess Trainer", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold,
                                        textStyle = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 2f)))
        Text("Tap a piece to see its legal moves", color = LightGrey, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        // Realistic Wooden Board Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(12.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(Color(0xFF2B1A10)) // Dark wood frame
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (rank in 7 downTo 0) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        for (file in 0..7) {
                            val square = Square(file, rank)
                            val piece = board[square]
                            val isDark = (file + rank) % 2 == 1
                            
                            // Realistic Wood Colors
                            val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
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
                                    RealisticPieceView(piece)
                                }
                            }
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
fun RealisticPieceView(piece: Piece) {
    val isWhite = piece.color == PieceColor.WHITE
    val woodColor = if (isWhite) Color(0xFFFFFFFF) else Color(0xFF000000)
    val shadowColor = if (isWhite) Color(0xFFA1887F) else Color(0xFF1A0D08)
    val highlightColor = if (isWhite) Color(0xFFFFFFFF) else Color(0xFF8D6E63)

    Box(
        modifier = Modifier
            .size(36.dp)
            .shadow(6.dp, CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(highlightColor, woodColor, shadowColor)
                ),
                shape = CircleShape
            ).border(1.dp, Color(0xFF000000).copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val symbol = when (piece.type) {
            PieceType.PAWN -> "♟"
            PieceType.KNIGHT -> "♞"
            PieceType.BISHOP -> "♝"
            PieceType.ROOK -> "♜"
            PieceType.QUEEN -> "♛"
            PieceType.KING -> "♚"
        }
        
        Text(
            text = if (isWhite) symbol.replace("♟", "♙").replace("♞", "♘").replace("♝", "♗").replace("♜", "♖").replace("♛", "♕").replace("♚", "♔") else symbol,
            color = if (isWhite) Color(0xFF000000) else Color(0xFFFFFFFF),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
                                        textStyle = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 2f))
        )
    }
}
