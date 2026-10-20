package com.example.paochess.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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

data class MoveStep(val description: String, val from: Square, val to: Square)

@Composable
fun InteractiveBoard(
    initialBoard: Map<Square, Piece>,
    moves: List<MoveStep>,
    currentStep: Int
) {
    var board by remember { mutableStateOf(initialBoard) }
    var highlightedSquare by remember { mutableStateOf<Square?>(null) }

    // Play beep sound on move
    LaunchedEffect(currentStep) {
        if (currentStep > 0) {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        }
    }

    // Update board when step changes
    LaunchedEffect(currentStep, initialBoard) {
        if (currentStep == 0) {
            board = initialBoard
            highlightedSquare = null
        } else if (currentStep <= moves.size) {
            val newBoard = initialBoard.toMutableMap()
            for (i in 0 until currentStep) {
                val step = moves[i]
                val piece = newBoard[step.from]
                if (piece != null) {
                    newBoard[step.to] = piece
                    newBoard.remove(step.from)
                }
            }
            board = newBoard
            highlightedSquare = moves[currentStep - 1].to
        }
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
