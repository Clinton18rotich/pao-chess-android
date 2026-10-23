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
    var currentPraise by remember { mutableStateOf("") }

    val encouragingPhrases = listOf(
        "Excellent! That's exactly it.",
        "Beautifully spotted. Keep going.",
        "You're starting to see the patterns. Nice work.",
        "Exactly! The trap is set.",
        "Brilliant. You're thinking like a grandmaster.",
        "Perfect. That's the move.",
        "Well done! You saw right through their defense.",
        "Superb. Keep that up."
    )
    
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
        currentPraise = ""
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
                onClick = { practiceMode = "AI" },
                colors = ButtonDefaults.buttonColors(containerColor = if (practiceMode == "AI") Orange else DarkSurface, contentColor = if (practiceMode == "AI") Color.Black else White),
                modifier = Modifier.weight(1f)
            ) { Text("AI") }

            Button(
                onClick = { practiceMode = "FEN" },
                colors = ButtonDefaults.buttonColors(containerColor = if (practiceMode == "FEN") Orange else DarkSurface, contentColor = if (practiceMode == "FEN") Color.Black else White),
                modifier = Modifier.weight(1f)
            ) { Text("FEN") }
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



        // --- FEN PRACTICE MODE UI ---
        if (practiceMode == "FEN") {
            var fenPuzzleIndex by remember { mutableStateOf(0) }
            var fenBoard by remember { mutableStateOf(FEN_PUZZLES[0].board) }
            var fenSelectedSquare by remember { mutableStateOf<Square?>(null) }
            var fenLegalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }
            var fenStep by remember { mutableStateOf(0) }
            var fenSolved by remember { mutableStateOf(false) }

            val fenPuzzle = FEN_PUZZLES[fenPuzzleIndex]

            Text("Practice FEN: ${fenPuzzle.name}", color = Orange, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            if (fenSolved) {
                Text("✅ Puzzle solved! Great job.", color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("Move ${fenStep + 1} of ${fenPuzzle.solution.size}. Tap a White piece.", color = White, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (rank in 7 downTo 0) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (file in 0..7) {
                                val sq = Square(file, rank)
                                val piece = fenBoard[sq]
                                val isDark = (file + rank) % 2 == 1
                                val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                                val isSelected = fenSelectedSquare == sq
                                val isLegal = fenLegalMoves.contains(sq)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(squareColor)
                                        .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent)
                                        .clickable {
                                            if (fenSolved) return@clickable

                                            if (isLegal) {
                                                val expected = fenPuzzle.solution[fenStep]
                                                if (fenSelectedSquare == expected.from && sq == expected.to) {
                                                    fenBoard = applyMove(fenBoard, fenSelectedSquare!!, sq)
                                                    fenStep++
                                                    
                                                    if (fenStep >= fenPuzzle.solution.size) {
                                                        fenSolved = true
                                                        if (isTtsReady) tts?.speak(encouragingPhrases.random(), TextToSpeech.QUEUE_FLUSH, null, null)
                                                    } else {
                                                        // Auto-play Black's response
                                                        val nextMove = fenPuzzle.solution[fenStep]
                                                        if (nextMove.description.contains("Black", ignoreCase = true)) {
                                                            fenBoard = applyMove(fenBoard, nextMove.from, nextMove.to)
                                                            fenStep++
                                                        }
                                                        if (isTtsReady) tts?.speak(encouragingPhrases.random(), TextToSpeech.QUEUE_FLUSH, null, null)
                                                    }
                                                } else {
                                                    fenSelectedSquare = null
                                                    fenLegalMoves = emptyList()
                                                    if (isTtsReady) tts?.speak("Try again. Look for the forcing move.", TextToSpeech.QUEUE_FLUSH, null, null)
                                                }
                                            } else if (piece != null && piece.color == PieceColor.WHITE) {
                                                fenSelectedSquare = sq
                                                fenLegalMoves = getLegalMoves(sq, piece, fenBoard)
                                            } else {
                                                fenSelectedSquare = null
                                                fenLegalMoves = emptyList()
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
                                        Text(text = text, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        fenBoard = fenPuzzle.board
                        fenStep = 0
                        fenSolved = false
                        fenSelectedSquare = null
                        fenLegalMoves = emptyList()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                    modifier = Modifier.weight(1f)
                ) { Text("Reset") }
                
                Button(
                    onClick = {
                        fenPuzzleIndex = (fenPuzzleIndex + 1) % FEN_PUZZLES.size
                        fenBoard = FEN_PUZZLES[fenPuzzleIndex].board
                        fenStep = 0
                        fenSolved = false
                        fenSelectedSquare = null
                        fenLegalMoves = emptyList()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black),
                    modifier = Modifier.weight(1f)
                ) { Text("Next Puzzle") }
            }
            return@Column
        }

        // --- TACTICS MODE UI ---
        Text("Find the checkmate in 1! (${PUZZLES.size} puzzles loaded)", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (isSolved) {
            Text("✅ ${if (currentPraise.isNotEmpty()) currentPraise else "Correct! ${currentPuzzle.name}"}", color = Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                                                val praise = encouragingPhrases.random()
                                                currentPraise = praise
                                                if (isTtsReady) {
                                                    tts?.speak(praise, TextToSpeech.QUEUE_FLUSH, null, null)
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
