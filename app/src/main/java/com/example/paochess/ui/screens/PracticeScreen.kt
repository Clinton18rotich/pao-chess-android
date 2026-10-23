package com.example.paochess.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.*
import com.example.paochess.ui.theme.*
import java.util.Locale

@Composable
fun PracticeScreen() {
    var practiceMode by remember { mutableStateOf("TACTICS") } // TACTICS, AI, FEN
    
    // Pulls from the full PUZZLES list (now 18 items)
    var currentPuzzle by remember { mutableStateOf(PUZZLES.random()) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var legalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }
    var isSolved by remember { mutableStateOf(false) }
    var board by remember { mutableStateOf(currentPuzzle.board) }

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsReady = true
            }
        }
    }

    fun resetPuzzle() {
        currentPuzzle = PUZZLES.random()
        board = currentPuzzle.board
        selectedSquare = null
        legalMoves = emptyList()
        isSolved = false
        if (isTtsReady) tts?.speak("Find the checkmate in one move.", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎯 Practice", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Mode Toggle Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { practiceMode = "TACTICS"; resetPuzzle() },
                colors = ButtonDefaults.buttonColors(containerColor = if (practiceMode == "TACTICS") Orange else DarkSurface, contentColor = if (practiceMode == "TACTICS") Color.Black else White),
                modifier = Modifier.weight(1f)
            ) { Text("Tactics") }
            
            Button(
                onClick = { practiceMode = "AI"; resetAIGame() },
                colors = ButtonDefaults.buttonColors(containerColor = if (practiceMode == "AI") Orange else DarkSurface, contentColor = if (practiceMode == "AI") Color.Black else White),
                modifier = Modifier.weight(1f)
            ) { Text("Play vs AI") }
        }

        Spacer(Modifier.height(24.dp))

        if (practiceMode == "AI") {
            // AI Placeholder
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(DarkSurface).border(2.dp, Orange),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤖", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Play vs AI", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Coming soon in a future update!", color = LightGrey, fontSize = 14.sp)
                }
            }
            return@Column
        }

        // --- TACTICS MODE UI ---
        Text("Find the checkmate in 1! (${PUZZLES.size} puzzles loaded)", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (isSolved) {
            Text("✅ Correct! ${currentPuzzle.name}", color = Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("Tap a White piece, then tap where it should move.", color = LightGrey, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Interactive Board
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
                                        if (isSolved) return@clickable
                                        
                                        if (isLegal) {
                                            if (selectedSquare == currentPuzzle.solution.from && square == currentPuzzle.solution.to) {
                                                // Correct!
                                                board = applyMove(board, selectedSquare!!, square)
                                                isSolved = true
                                                if (isTtsReady) {
                                                    tts?.speak("Correct! ${currentPuzzle.solution.description}", TextToSpeech.QUEUE_FLUSH, null, null)
                                                }
                                            } else {
                                                // Wrong move
                                                selectedSquare = null
                                                legalMoves = emptyList()
                                                if (isTtsReady) {
                                                    tts?.speak("Try again. Look for a forcing checkmate.", TextToSpeech.QUEUE_FLUSH, null, null)
                                                }
                                            }
                                        } else if (piece != null && piece.color == PieceColor.WHITE) {
                                            // Select a new piece
                                            selectedSquare = square
                                            legalMoves = getLegalMoves(square, piece, board)
                                        } else {
                                            // Deselect if tapping empty space
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
                                    
                                    val shadowColor = if (piece.color == PieceColor.WHITE) Color.White else Color(0xFFAAAAAA)
                                    Text(
                                        text = text, color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                                        style = androidx.compose.ui.text.TextStyle(
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = shadowColor, offset = androidx.compose.ui.geometry.Offset(1.5f, 1.5f), blurRadius = 3f
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!isSolved) {
            Button(
                onClick = { if (isTtsReady) tts?.speak("Hint: ${currentPuzzle.hint}", TextToSpeech.QUEUE_FLUSH, null, null) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                modifier = Modifier.fillMaxWidth()
            ) { Text("💡 Get a Hint") }
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { resetPuzzle() },
            colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isSolved) "Next Puzzle ➡️" else "Skip to Next Puzzle") }
    }
}
