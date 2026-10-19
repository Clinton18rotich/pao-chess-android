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

data class Puzzle(
    val name: String,
    val description: String,
    val board: Map<Square, Piece>,
    val solutionFrom: Square,
    val solutionTo: Square,
    val hint: String
)

val PUZZLES = listOf(
    Puzzle(
        name = "Back Rank Mate",
        description = "White to move. Deliver checkmate in 1.",
        board = mapOf(
            Square(4, 0) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(1, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(2, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solutionFrom = Square(0, 7),
        solutionTo = Square(4, 7),
        hint = "Move the Rook to the back rank to trap the King."
    ),
    Puzzle(
        name = "Queen Mate",
        description = "White to move. Checkmate in 1.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solutionFrom = Square(3, 6),
        solutionTo = Square(3, 7),
        hint = "Place the Queen next to the King on the edge."
    )
)

@Composable
fun PuzzlesScreen() {
    var currentPuzzleIndex by remember { mutableStateOf(0) }
    var board by remember { mutableStateOf(PUZZLES[0].board) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var showHint by remember { mutableStateOf(false) }

    val currentPuzzle = PUZZLES[currentPuzzleIndex]

    fun loadPuzzle(index: Int) {
        currentPuzzleIndex = index
        board = PUZZLES[index].board
        selectedSquare = null
        resultMessage = null
        showHint = false
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🧩 Puzzles", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(currentPuzzle.name, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(currentPuzzle.description, color = LightGrey, fontSize = 14.sp)
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

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(squareColor)
                                    .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent)
                                    .clickable {
                                        if (selectedSquare == null && piece != null && piece.color == PieceColor.WHITE) {
                                            selectedSquare = square
                                        } else if (selectedSquare != null) {
                                            if (selectedSquare == currentPuzzle.solutionFrom && square == currentPuzzle.solutionTo) {
                                                resultMessage = "✅ CHECKMATE! Brilliant!"
                                                val newBoard = board.toMutableMap()
                                                newBoard[square] = board[selectedSquare!!]!!
                                                newBoard.remove(selectedSquare!!)
                                                board = newBoard
                                            } else {
                                                resultMessage = "❌ Not quite. Try again."
                                            }
                                            selectedSquare = null
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (piece != null) {
                                    val symbol = when (piece.type) {
                                        PieceType.PAWN -> "♟"
                                        PieceType.KNIGHT -> "♞"
                                        PieceType.BISHOP -> "♝"
                                        PieceType.ROOK -> "♜"
                                        PieceType.QUEEN -> "♛"
                                        PieceType.KING -> "♚"
                                    }
                                    val text = if (piece.color == PieceColor.WHITE) symbol.replace("♟", "♙").replace("♞", "♘").replace("♝", "♗").replace("♜", "♖").replace("♛", "♕").replace("♚", "♔") else symbol
                                    Text(text = text, color = Color(0xFF000000), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (resultMessage != null) {
            Text(resultMessage!!, color = if (resultMessage!!.startsWith("✅")) Green else Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showHint = !showHint }, colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)) {
                Text(if (showHint) "Hide Hint" else "💡 Hint", color = White)
            }
            Button(onClick = { loadPuzzle((currentPuzzleIndex + 1) % PUZZLES.size) }, colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black)) {
                Text("Next Puzzle")
            }
        }

        if (showHint) {
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A1E))) {
                Text(currentPuzzle.hint, color = Green, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
            }
        }
    }
}
