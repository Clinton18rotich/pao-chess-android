package com.example.paochess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun LearnScreen() {
    var selectedPieceType by remember { mutableStateOf(PieceType.PAWN) }
    
    val explanation = when(selectedPieceType) {
        PieceType.PAWN -> "Pawns move forward 1 square. On their first move, they can move forward 2 squares. They capture diagonally (1 square forward-left or forward-right)."
        PieceType.KNIGHT -> "Knights move in an 'L' shape: 2 squares in one direction, then 1 square perpendicular. They are the only piece that can jump over other pieces."
        PieceType.BISHOP -> "Bishops move diagonally any number of squares. They stay on the same color square for the entire game."
        PieceType.ROOK -> "Rooks move in straight lines (horizontally or vertically) any number of squares. They are powerful on open files."
        PieceType.QUEEN -> "The Queen is the most powerful piece. She can move in straight lines or diagonally, any number of squares."
        PieceType.KING -> "The King moves 1 square in any direction. The game ends if the King is checkmated. You cannot move the King into check."
    }

    // Generate the board for the selected piece
    val board = remember(selectedPieceType) {
        val b = mutableMapOf<Square, Piece>()
        // Place the piece on e4 (center) to show its moves clearly
        val startSquare = Square(4, 3) // e4
        b[startSquare] = Piece(selectedPieceType, PieceColor.WHITE)
        
        // Add a few enemy pieces for the pawn to capture
        if (selectedPieceType == PieceType.PAWN) {
            b[Square(3, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK) // d5
            b[Square(5, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK) // f5
        }
        b
    }

    val legalMoves = remember(selectedPieceType) {
        val startSquare = Square(4, 3)
        getLegalMoves(startSquare, Piece(selectedPieceType, PieceColor.WHITE), board)
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("📖 Learn Chess", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold,
                                        textStyle = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 2f)))
        Spacer(Modifier.height(16.dp))

        // Piece Selector
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(
                Pair(PieceType.PAWN, "♙"),
                Pair(PieceType.KNIGHT, "♘"),
                Pair(PieceType.BISHOP, "♗")
            ).forEach { (type, symbol) ->
                Button(onClick = { selectedPieceType = type }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPieceType == type) Orange else DarkSurface), contentPadding = PaddingValues(8.dp)) {
                    Text(symbol, fontSize = 20.sp, color = if (selectedPieceType == type) Color.Black else White)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(
                Pair(PieceType.ROOK, "♖"),
                Pair(PieceType.QUEEN, "♕"),
                Pair(PieceType.KING, "♔")
            ).forEach { (type, symbol) ->
                Button(onClick = { selectedPieceType = type }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedPieceType == type) Orange else DarkSurface), contentPadding = PaddingValues(8.dp)) {
                    Text(symbol, fontSize = 20.sp, color = if (selectedPieceType == type) Color.Black else White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Explanation Card
        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(10.dp)) {
            Text(explanation, color = Color(0xFFDDDDDD), fontSize = 14.sp, modifier = Modifier.padding(16.dp), lineHeight = 20.sp)
        }

        Spacer(Modifier.height(24.dp))
        Text("Tap the piece to see its legal moves:", color = LightGrey, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        // Mini Board
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (rank in 7 downTo 0) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        for (file in 0..7) {
                            val square = Square(file, rank)
                            val piece = board[square]
                            val isDark = (file + rank) % 2 == 1
                            val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                            val isLegal = legalMoves.contains(square)
                            val isSelected = square == Square(4, 3)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(squareColor)
                                    .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLegal) {
                                    Box(modifier = Modifier.size(12.dp).background(Color(0x8000FF00), CircleShape))
                                }
                                if (piece != null) {
                                    val symbol = when (piece.type) {
                                        PieceType.PAWN -> "♟"
                                        PieceType.KNIGHT -> "♞"
                                        PieceType.BISHOP -> "♝"
                                        PieceType.ROOK -> "♜"
                                        PieceType.QUEEN -> "♛"
                                        PieceType.KING -> "♚"
                                    }
                                    Text(
                                        text = if (piece.color == PieceColor.WHITE) symbol.replace("♟", "♙").replace("♞", "♘").replace("♝", "♗").replace("♜", "♖").replace("♛", "♕").replace("♚", "♔") else symbol,
                                        color = if (piece.color == PieceColor.WHITE) Color(0xFF000000) else Color(0xFFFFFFFF),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        textStyle = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 2f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
