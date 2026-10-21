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
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PracticeScreen() {
    var isTacticsMode by remember { mutableStateOf(true) }

    // TACTICS STATE
    var currentPuzzle by remember { mutableStateOf(PUZZLES.random()) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var legalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }
    var isSolved by remember { mutableStateOf(false) }
    var practiceStep by remember { mutableStateOf(0) }
    var puzzleBoard by remember { mutableStateOf(currentPuzzle.board) }

    // AI STATE
    var aiBoard by remember { mutableStateOf(getInitialBoard()) }
    var aiSelectedSquare by remember { mutableStateOf<Square?>(null) }
    var aiLegalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }
    var isAiThinking by remember { mutableStateOf(false) }
    var gameStatus by remember { mutableStateOf("Your turn (White)") }
    var lastAiTaunt by remember { mutableStateOf("") }

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
        puzzleBoard = currentPuzzle.board
        selectedSquare = null
        legalMoves = emptyList()
        isSolved = false
        practiceStep = 0
        if (isTtsReady) tts?.speak("Find the checkmate move.", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun resetAIGame() {
        aiBoard = getInitialBoard()
        aiSelectedSquare = null
        aiLegalMoves = emptyList()
        isAiThinking = false
        gameStatus = "Your turn (White)"
        lastAiTaunt = ""
    }

    // AI TURN LOGIC
    LaunchedEffect(isAiThinking) {
        if (isAiThinking) {
            delay(600L)
            val bestMove = ChessAI.getBestMove(aiBoard, PieceColor.BLACK)
            if (bestMove != null) {
                aiBoard = applyMove(aiBoard, bestMove.from, bestMove.to)
                gameStatus = "Your turn (White)"
                if (bestMove.taunt.isNotEmpty()) {
                    lastAiTaunt = bestMove.taunt
                    if (isTtsReady) {
                        tts?.setPitch(0.6f) // Arrogant male voice for AI
                        tts?.speak(bestMove.taunt, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                } else {
                    lastAiTaunt = ""
                }
            } else {
                gameStatus = "Checkmate or Stalemate!"
            }
            isAiThinking = false
        }
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
                onClick = { isTacticsMode = true; resetPuzzle(); if (isTtsReady) tts?.setPitch(1.15f) },
                colors = ButtonDefaults.buttonColors(containerColor = if (isTacticsMode) Orange else DarkSurface, contentColor = if (isTacticsMode) Color.Black else White),
                modifier = Modifier.weight(1f)
            ) { Text("Tactics") }
            
            Button(
                onClick = { isTacticsMode = false; resetAIGame() },
                colors = ButtonDefaults.buttonColors(containerColor = if (!isTacticsMode) Orange else DarkSurface, contentColor = if (!isTacticsMode) Color.Black else White),
                modifier = Modifier.weight(1f)
            ) { Text("Play vs AI") }
        }

        Spacer(Modifier.height(24.dp))

        // ==========================================
        // AI MODE UI
        // ==========================================
        if (!isTacticsMode) {
            Text(gameStatus, color = if (isAiThinking) LightGrey else Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            if (lastAiTaunt.isNotEmpty()) {
                Text("🤖 \"$lastAiTaunt\"", color = Color(0xFFFF6B6B), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (rank in 7 downTo 0) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (file in 0..7) {
                                val square = Square(file, rank)
                                val piece = aiBoard[square]
                                val isDark = (file + rank) % 2 == 1
                                val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                                val isSelected = aiSelectedSquare == square
                                val isLegal = aiLegalMoves.contains(square)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(squareColor)
                                        .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent)
                                        .clickable {
                                            if (isAiThinking) return@clickable
                                            
                                            if (isLegal) {
                                                aiBoard = applyMove(aiBoard, aiSelectedSquare!!, square)
                                                aiSelectedSquare = null
                                                aiLegalMoves = emptyList()
                                                isAiThinking = true
                                            } else if (piece != null && piece.color == PieceColor.WHITE) {
                                                aiSelectedSquare = square
                                                aiLegalMoves = getLegalMoves(square, piece, aiBoard)
                                            } else {
                                                aiSelectedSquare = null
                                                aiLegalMoves = emptyList()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLegal) Box(modifier = Modifier.size(12.dp).background(Color(0x8000FF00), CircleShape))
                                    if (piece != null) {
                                        RenderPiece(piece, size = 28.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Button(onClick = { resetAIGame() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)), modifier = Modifier.fillMaxWidth()) {
                Text("Restart Game")
            }
            return@Column
        }

        // ==========================================
        // TACTICS MODE UI
        // ==========================================
        Text("Find the checkmate in 1! (${PUZZLES.size} puzzles loaded)", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (isSolved) {
            Text("✅ Correct! ${currentPuzzle.name}", color = Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("Tap a White piece, then tap where it should move.", color = LightGrey, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (rank in 7 downTo 0) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        for (file in 0..7) {
                            val square = Square(file, rank)
                            val piece = puzzleBoard[square]
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
                                            if (practiceStep >= currentPuzzle.solution.size) return@clickable
                                            val expectedMove = currentPuzzle.solution[practiceStep]
                                            val isWhiteMove = expectedMove.description.contains("White", ignoreCase = true)

                                            if (isWhiteMove && selectedSquare == expectedMove.from && square == expectedMove.to) {
                                                puzzleBoard = applyMove(puzzleBoard, selectedSquare!!, square)
                                                practiceStep++
                                                
                                                if (practiceStep >= currentPuzzle.solution.size) {
                                                    isSolved = true
                                                    if (isTtsReady) tts?.speak("Correct! You found the tactic.", TextToSpeech.QUEUE_FLUSH, null, null)
                                                } else {
                                                    val nextMove = currentPuzzle.solution[practiceStep]
                                                    if (!nextMove.description.contains("White", ignoreCase = true)) {
                                                        puzzleBoard = applyMove(puzzleBoard, nextMove.from, nextMove.to)
                                                        practiceStep++
                                                    }
                                                    if (isTtsReady) tts?.speak("Good. Keep going.", TextToSpeech.QUEUE_FLUSH, null, null)
                                                }
                                            } else {
                                                selectedSquare = null
                                                legalMoves = emptyList()
                                                if (isTtsReady) tts?.speak("Try again. Follow the sequence.", TextToSpeech.QUEUE_FLUSH, null, null)
                                            }
                                        } else if (piece != null && piece.color == PieceColor.WHITE) {
                                            selectedSquare = square
                                            legalMoves = getLegalMoves(square, piece, puzzleBoard)
                                        } else {
                                            selectedSquare = null
                                            legalMoves = emptyList()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLegal) Box(modifier = Modifier.size(12.dp).background(Color(0x8000FF00), CircleShape))
                                if (piece != null) {
                                    RenderPiece(piece, size = 28.sp)
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

@Composable
private fun RenderPiece(piece: Piece, size: androidx.compose.ui.unit.TextUnit) {
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
        text = text, color = Color.Black, fontSize = size, fontWeight = FontWeight.Bold,
        style = androidx.compose.ui.text.TextStyle(
            shadow = androidx.compose.ui.graphics.Shadow(
                color = shadowColor, offset = androidx.compose.ui.geometry.Offset(1.5f, 1.5f), blurRadius = 3f
            )
        )
    )
}
