package com.example.paochess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.*
import com.example.paochess.ui.theme.*

@Composable
fun LearnScreen() {
    var selectedPieceType by remember { mutableStateOf(PieceType.PAWN) }

    val explanation = when (selectedPieceType) {
        PieceType.PAWN   -> "The Pawn moves forward 1 square. On its very first move it may advance 2 squares. It captures diagonally (one square forward-left or forward-right). It can never move backward, and if it reaches the far rank it promotes (usually to a Queen)."
        PieceType.KNIGHT -> "The Knight moves in an 'L' shape: two squares in one direction, then one square perpendicular. It is the only piece that can jump over other pieces — so pawns and blockers never stop it. From the center of the board it controls 8 squares."
        PieceType.BISHOP -> "The Bishop moves diagonally any number of squares. Because it never leaves its color, each side begins with one light-squared Bishop and one dark-squared Bishop. Bishops love open diagonals and long-range pressure."
        PieceType.ROOK   -> "The Rook moves in straight lines — horizontally or vertically — any number of squares. It is worth about 5 points. Rooks become very powerful on open files (no pawns blocking the column) and on the 7th rank."
        PieceType.QUEEN  -> "The Queen combines the Rook and Bishop: she moves in straight lines OR diagonals, any number of squares. She is the strongest piece (about 9 points). In the opening, avoid bringing her out too early — she can be chased by weaker pieces."
        PieceType.KING   -> "The King moves 1 square in any direction. He cannot move into check, and if he is checkmated the game ends. In the endgame the King becomes a fighting piece and should march toward the center."
    }

    val icon = when (selectedPieceType) {
        PieceType.PAWN   -> "♙"
        PieceType.KNIGHT -> "♘"
        PieceType.BISHOP -> "♗"
        PieceType.ROOK   -> "♖"
        PieceType.QUEEN  -> "♕"
        PieceType.KING   -> "♔"
    }

    val buttonGlyph = { t: PieceType -> when (t) {
        PieceType.PAWN -> "♙"; PieceType.KNIGHT -> "♘"; PieceType.BISHOP -> "♗"
        PieceType.ROOK -> "♖"; PieceType.QUEEN -> "♕"; PieceType.KING -> "♔"
    }}

    val board = remember(selectedPieceType) {
        val b = mutableMapOf<Square, Piece>()
        b[Square(4, 3)] = Piece(selectedPieceType, PieceColor.WHITE)
        if (selectedPieceType == PieceType.PAWN) {
            b[Square(3, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK)
            b[Square(5, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK)
        }
        b
    }
    val legalMoves = remember(selectedPieceType) {
        getLegalMoves(Square(4, 3), Piece(selectedPieceType, PieceColor.WHITE), board)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("📖 Learn Chess", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(PieceType.PAWN, PieceType.KNIGHT, PieceType.BISHOP).forEach { t ->
                Button(
                    onClick = { selectedPieceType = t },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPieceType == t) Orange else DarkSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(buttonGlyph(t), fontSize = 24.sp,
                        color = if (selectedPieceType == t) Color.Black else White)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(PieceType.ROOK, PieceType.QUEEN, PieceType.KING).forEach { t ->
                Button(
                    onClick = { selectedPieceType = t },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPieceType == t) Orange else DarkSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(buttonGlyph(t), fontSize = 24.sp,
                        color = if (selectedPieceType == t) Color.Black else White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(10.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(icon, color = Orange, fontSize = 56.sp, modifier = Modifier.padding(end = 16.dp))
                Text(explanation, color = Color(0xFFDDDDDD), fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
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
                                modifier = Modifier.weight(1f).fillMaxHeight().background(squareColor)
                                    .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLegal) Box(modifier = Modifier.size(12.dp).background(Color(0x8000FF00), CircleShape))
                                if (piece != null) {
                                    val glyph = when (piece.type) {
                                        PieceType.PAWN -> "♟"; PieceType.KNIGHT -> "♞"; PieceType.BISHOP -> "♝"
                                        PieceType.ROOK -> "♜"; PieceType.QUEEN -> "♛"; PieceType.KING -> "♚"
                                    }
                                    val text = if (piece.color == PieceColor.WHITE) glyph
                                        .replace("♟","♙").replace("♞","♘").replace("♝","♗")
                                        .replace("♜","♖").replace("♛","♕").replace("♚","♔") else glyph
                                    Text(text = text, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
