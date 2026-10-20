package com.example.paochess.ui.screens

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.*
import com.example.paochess.ui.theme.*
import kotlinx.coroutines.delay

// A single step in an opening or puzzle
data class MoveStep(val description: String, val from: Square, val to: Square)

@Composable
fun InteractiveBoard(
    initialBoard: Map<Square, Piece>,
    moves: List<MoveStep>,
    isPlaying: Boolean,
    onStepChanged: (Int) -> Unit,
    currentStep: Int
) {
    var board by remember { mutableStateOf(initialBoard) }
    var highlightedSquare by remember { mutableStateOf<Square?>(null) }

    // Watch for step changes and animate the move
    LaunchedEffect(currentStep) {
        if (currentStep < moves.size) {
            val step = moves[currentStep]
            board = board.toMutableMap().apply {
                put(step.to, get(step.from)!!)
                remove(step.from)
            }
            highlightedSquare = step.to
            onStepChanged(currentStep)
        }
    }

    // Reset board when initialBoard changes (new puzzle/opening)
    LaunchedEffect(initialBoard) {
        board = initialBoard
        highlightedSquare = null
    }

    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (rank in 7 downTo 0) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    for (file in 0..7) {
                        val square = Square(file, rank)
                        val piece = board[square]
                        val isDark = (file + rank) % 2 == 1
                        val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                        val isHighlighted = highlightedSquare == square

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(squareColor)
                                .border(if (isHighlighted) 3.dp else 0.dp, if (isHighlighted) Orange else Color.Transparent),
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
                                Text(text = text, color = Color(0xFF000000), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
