package com.example.paochess.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.paochess.data.*
import com.example.paochess.ui.theme.*
import java.util.Locale

data class RecordedMove(
    val moveNumber: Int,
    val color: PieceColor,
    val from: Square,
    val to: Square,
    val piece: PieceType,
    val capturedPiece: PieceType?
)

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

    // Helper to enforce consistent voice pitch/rate every time
    fun speak(text: String) {
        tts?.setPitch(1.15f)
        tts?.setSpeechRate(0.95f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun resetPuzzle() {
        currentPuzzle = PUZZLES.random()
        board = currentPuzzle.board
        selectedSquare = null
        legalMoves = emptyList()
        isSolved = false
        currentPraise = ""
        speak("Find the checkmate in one move.")
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
                onClick = { 
                    practiceMode = "FEN"
                    speak("Solve the FEN puzzle. Find the forced mate.")
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (practiceMode == "FEN") Orange else DarkSurface, contentColor = if (practiceMode == "FEN") Color.Black else White),
                modifier = Modifier.weight(1f)
            ) { Text("FEN") }
        }

        Spacer(Modifier.height(24.dp))

        if (practiceMode == "AI") {
            // AI Game State
            var aiBoard by remember { mutableStateOf(getInitialBoard()) }
            var aiSelectedSquare by remember { mutableStateOf<Square?>(null) }
            var aiLegalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }
            var isAiThinking by remember { mutableStateOf(false) }
            var gameStatus by remember { mutableStateOf("Your turn (White)") }
            var lastAiTaunt by remember { mutableStateOf("") }
            var capturedSquareFlash by remember { mutableStateOf<Square?>(null) }
            var aiThinkingText by remember { mutableStateOf("") }
            
            // Game recording
            val gameHistory = remember { mutableStateListOf<RecordedMove>() }
            var showAnalysis by remember { mutableStateOf(false) }
            var showReplay by remember { mutableStateOf(false) }
            var moveCounter by remember { mutableStateOf(1) }
            
            // Reset voice to coach pitch when entering AI mode initially
            LaunchedEffect(Unit) {
                if (isTtsReady) {
                    tts?.setPitch(1.15f)
                    tts?.setSpeechRate(0.95f)
                }
            }
            
            // AI turn
            LaunchedEffect(isAiThinking) {
                if (isAiThinking) {
                    delay(600L)  // Think time
                    
                    val bestMove = ChessAI.getBestMove(aiBoard, PieceColor.BLACK)
                    
                    if (bestMove != null) {
                        // Record the AI's move BEFORE applying
                        val aiMovingPiece = aiBoard[bestMove.from]!!
                        val aiCapturedPiece = aiBoard[bestMove.to]?.type
                        
                        // Show "thinking" text with reasoning
                        aiThinkingText = if (aiCapturedPiece != null) {
                            val targetSquare = "${'a' + bestMove.to.file}${bestMove.to.rank + 1}"
                            "🤖 AI sees your ${aiCapturedPiece.name.lowercase()} on $targetSquare is undefended..."
                        } else {
                            "🤖 AI is evaluating the position..."
                        }
                        
                        // Flash the target square red for visibility
                        capturedSquareFlash = bestMove.to
                        delay(600L)  // 600ms flash
                        
                        // Now make the move
                        gameHistory.add(
                            RecordedMove(
                                moveNumber = moveCounter,
                                color = PieceColor.BLACK,
                                from = bestMove.from,
                                to = bestMove.to,
                                piece = aiMovingPiece.type,
                                capturedPiece = aiCapturedPiece
                            )
                        )
                        moveCounter++
                        
                        aiBoard = applyMove(aiBoard, bestMove.from, bestMove.to)
                        capturedSquareFlash = null
                        gameStatus = "Your turn (White)"
                        // Keep aiThinkingText visible — cleared via delayed launch below
                        launch {
                            delay(3000L)
                            aiThinkingText = ""
                        }
                        
                        if (bestMove.taunt.isNotEmpty() && isTtsReady) {
                            tts?.setPitch(0.5f)
                            tts?.setSpeechRate(0.85f)
                            tts?.speak(bestMove.taunt, TextToSpeech.QUEUE_FLUSH, null, null)
                            lastAiTaunt = bestMove.taunt
                        } else {
                            lastAiTaunt = ""
                        }
                    } else {
                        gameStatus = "Checkmate or Stalemate!"
                        delay(1500L)
                        showAnalysis = true
                    }
                    
                    isAiThinking = false
                }
            }
            
            Text(gameStatus, color = if (isAiThinking) LightGrey else Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            // AI reasoning text (before capture)
            if (aiThinkingText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2A00))
                ) {
                    Text(
                        aiThinkingText,
                        color = Color(0xFFFFD700),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            if (lastAiTaunt.isNotEmpty()) {
                Text("🤖 \"$lastAiTaunt\"", color = Color(0xFFFF6B6B), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Board with coordinates on all four sides
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top file labels (a-h)
                Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)) {
                    for (file in 0..7) {
                        Text(
                            "${'a' + file}",
                            color = LightGrey,
                            fontSize = 10.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Left rank labels + Board + Right rank labels
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    // Left rank labels (8-1)
                    Column(
                        modifier = Modifier.width(16.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (rank in 7 downTo 0) {
                            Text(
                                "${rank + 1}",
                                color = LightGrey,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                        }
                    }
                    
                    // Board
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            for (rank in 7 downTo 0) {
                                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                    for (file in 0..7) {
                                        val sq = Square(file, rank)
                                        val piece = aiBoard[sq]
                                        val isDark = (file + rank) % 2 == 1
                                        val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                                        val isSelected = aiSelectedSquare == sq
                                        val isLegal = aiLegalMoves.contains(sq)
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(squareColor)
                                                .border(
                                                    width = if (isSelected || capturedSquareFlash == sq) 3.dp else 0.dp,
                                                    color = when {
                                                        isSelected -> Orange
                                                        capturedSquareFlash == sq -> Color.Red
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable {
                                                    if (isAiThinking) return@clickable
                                                    
                                                    if (isLegal) {
                                                        val movingPiece = aiBoard[aiSelectedSquare!!]!!
                                                        val capturedPiece = aiBoard[sq]?.type
                                                        gameHistory.add(
                                                            RecordedMove(
                                                                moveNumber = moveCounter,
                                                                color = PieceColor.WHITE,
                                                                from = aiSelectedSquare!!,
                                                                to = sq,
                                                                piece = movingPiece.type,
                                                                capturedPiece = capturedPiece
                                                            )
                                                        )
                                                        moveCounter++
                                                        
                                                        aiBoard = applyMove(aiBoard, aiSelectedSquare!!, sq)
                                                        aiSelectedSquare = null
                                                        aiLegalMoves = emptyList()
                                                        isAiThinking = true
                                                    } else if (piece != null && piece.color == PieceColor.WHITE) {
                                                        aiSelectedSquare = sq
                                                        aiLegalMoves = getLegalMoves(sq, piece, aiBoard)
                                                    } else {
                                                        aiSelectedSquare = null
                                                        aiLegalMoves = emptyList()
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isLegal) Box(modifier = Modifier.size(12.dp).background(Color(0x8000FF00), CircleShape))
                                            if (piece != null) {
                                                val symbol = when (piece.type) {
                                                    PieceType.PAWN -> "♟"
                                                    PieceType.KNIGHT -> "♞"
                                                    PieceType.BISHOP -> "♝"
                                                    PieceType.ROOK -> "♜"
                                                    PieceType.QUEEN -> "♛"
                                                    PieceType.KING -> "♚"
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
                    
                    // Right rank labels (8-1)
                    Column(
                        modifier = Modifier.width(16.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (rank in 7 downTo 0) {
                            Text(
                                "${rank + 1}",
                                color = LightGrey,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                        }
                    }
                }
                
                // Bottom file labels (a-h)
                Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)) {
                    for (file in 0..7) {
                        Text(
                            "${'a' + file}",
                            color = LightGrey,
                            fontSize = 10.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        aiBoard = getInitialBoard()
                        aiSelectedSquare = null
                        aiLegalMoves = emptyList()
                        isAiThinking = false
                        gameStatus = "Your turn (White)"
                        lastAiTaunt = ""
                        gameHistory.clear()
                        moveCounter = 1
                        showAnalysis = false
                        if (isTtsReady) {
                            tts?.setPitch(1.15f)
                            tts?.setSpeechRate(0.95f)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                    modifier = Modifier.weight(1f)
                ) { Text("🔄 Restart") }
                
                Button(
                    onClick = { showAnalysis = true },
                    enabled = gameHistory.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black),
                    modifier = Modifier.weight(1f)
                ) { Text("📊 Analyze") }
                
                Button(
                    onClick = { showReplay = true },
                    enabled = gameHistory.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.Black),
                    modifier = Modifier.weight(1f)
                ) { Text("▶ Replay") }
            }
            
            // Replay Modal
            if (showReplay) {
                GameReplayDialog(
                    gameHistory = gameHistory,
                    onDismiss = { showReplay = false }
                )
            }
            
            // Analysis Modal
            if (showAnalysis) {
                GameAnalysisDialog(
                    gameHistory = gameHistory,
                    onDismiss = { showAnalysis = false }
                )
            }
            
            return@Column
        }



        // --- FEN PRACTICE MODE UI ---
        if (practiceMode == "FEN") {
            var fenPuzzleIndex by remember { mutableStateOf(0) }
            
            // Speak intro when entering FEN mode
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(500L)
                speak("Solve the FEN puzzle. Find the forced mate.")
            }
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
                                                        speak(encouragingPhrases.random())
                                                    } else {
                                                        // Auto-play Black's response
                                                        val nextMove = fenPuzzle.solution[fenStep]
                                                        if (nextMove.description.contains("Black", ignoreCase = true)) {
                                                            fenBoard = applyMove(fenBoard, nextMove.from, nextMove.to)
                                                            fenStep++
                                                        }
                                                        speak(encouragingPhrases.random())
                                                    }
                                                } else {
                                                    fenSelectedSquare = null
                                                    fenLegalMoves = emptyList()
                                                    speak("Try again. Look for the forcing move.")
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
                                                    speak(praise)
                                                }
                                            } else {
                                                // Wrong move
                                                selectedSquare = null
                                                legalMoves = emptyList()
                                                if (isTtsReady) {
                                                    speak("Try again. Look for a forcing checkmate.")
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
                onClick = { speak("Hint: ${currentPuzzle.hint}") },
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
fun GameAnalysisDialog(
    gameHistory: List<RecordedMove>,
    onDismiss: () -> Unit
) {
    val whiteMoves = gameHistory.filter { it.color == PieceColor.WHITE }
    val blackMoves = gameHistory.filter { it.color == PieceColor.BLACK }
    val blunders = whiteMoves.filter { it.capturedPiece != null }
    val aiCaptures = blackMoves.filter { it.capturedPiece != null }
    val aiAttacks = whiteMoves.count { it.to != null }  // Simplification: count all moves as attacks
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📊 Game Analysis", color = Orange, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Text("✕", color = White, fontSize = 20.sp)
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Summary
                Text("Moves played: ${gameHistory.size}", color = White, fontSize = 14.sp)
                Text("Your blunders: ${blunders.size} pieces lost", color = if (blunders.isEmpty()) Green else Color(0xFFFF6B6B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("AI captures: ${aiCaptures.size}", color = LightGrey, fontSize = 14.sp)
                
                Spacer(Modifier.height(16.dp))
                Divider(color = LightGrey.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                
                // Move list with annotations
                Text("━━━ Move List ━━━", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                
                val pairs = whiteMoves.mapIndexed { idx, white ->
                    Pair(white, blackMoves.getOrNull(idx))
                }
                
                // Track captured squares for warning annotation
                val capturedSquares = mutableSetOf<Pair<Int, Int>>()
                gameHistory.filter { it.capturedPiece != null }.forEach { move ->
                    capturedSquares.add(Pair(move.to.file, move.to.rank))
                }
                
                pairs.forEach { (white, black) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text("${white.moveNumber}. ", color = LightGrey, fontSize = 13.sp, modifier = Modifier.width(28.dp))
                        
                        // White move with warning if it was captured
                        val whiteSquare = Pair(white.to.file, white.to.rank)
                        val whiteWarning = if (capturedSquares.contains(whiteSquare)) " ⚠" else ""
                        Text(
                            squareLabel(white.from) + squareLabel(white.to) + whiteWarning,
                            color = if (whiteWarning.isNotEmpty()) Color(0xFFFFB300) else White,
                            fontSize = 13.sp,
                            modifier = Modifier.width(95.dp)
                        )
                        
                        // Black move
                        if (black != null) {
                            Text(
                                squareLabel(black.from) + squareLabel(black.to),
                                color = Color(0xFFFF6B6B),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text("—", color = LightGrey, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                if (blunders.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Divider(color = LightGrey.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))
                    
                    Text("━━━ Your Blunders ━━━", color = Color(0xFFFF6B6B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    
                    blunders.forEach { move ->
                        val squareName = "${'a' + move.to.file}${move.to.rank + 1}"
                        val pieceName = move.capturedPiece?.name?.lowercase() ?: "piece"
                        Text(
                            "❌ Move ${move.moveNumber}: Lost $pieceName on $squareName",
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp
                        )
                    }
                }
                
                // Tactical Summary
                Spacer(Modifier.height(16.dp))
                Divider(color = LightGrey.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                
                Text("━━━ Tactical Summary ━━━", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                
                Text("Your moves: ${whiteMoves.size}", color = White, fontSize = 13.sp)
                Text("AI moves: ${blackMoves.size}", color = White, fontSize = 13.sp)
                Text("Your pieces lost: ${blunders.size}", color = Color(0xFFFF6B6B), fontSize = 13.sp)
                Text("AI pieces lost: ${aiCaptures.size}", color = Green, fontSize = 13.sp)
                
                Spacer(Modifier.height(8.dp))
                
                val lesson = when {
                    blunders.isEmpty() && gameHistory.size > 10 -> "⭐ Excellent! You protected all pieces in a full game."
                    blunders.isEmpty() -> "Good start. Play longer to test your defenses."
                    blunders.size <= 2 -> "You lost ${blunders.size} pieces. Focus on defending before attacking."
                    blunders.size <= 5 -> "You lost ${blunders.size} pieces. Before each move, ask: is this piece safe?"
                    else -> "You lost ${blunders.size} pieces. Practice defending. Every move should protect your pieces from attack."
                }
                
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A1E))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("💡 Lesson", color = Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(lesson, color = White, fontSize = 13.sp)
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                Text(
                    "⚠ = piece was captured on this square",
                    color = LightGrey,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

fun squareLabel(sq: Square): String {
    val file = ('a' + sq.file)
    val rank = sq.rank + 1
    return "$file$rank"
}


@Composable
fun GameReplayDialog(
    gameHistory: List<RecordedMove>,
    onDismiss: () -> Unit
) {
    var replayStep by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    
    // Board state at current replay step
    val replayBoard = remember(replayStep) {
        var b = getInitialBoard()
        for (i in 0 until replayStep) {
            val move = gameHistory[i]
            b = applyMove(b, move.from, move.to)
        }
        b
    }
    
    // Last move info
    val lastMove = if (replayStep > 0) gameHistory[replayStep - 1] else null
    
    // Auto-play effect
    LaunchedEffect(isPlaying, replayStep) {
        if (isPlaying) {
            if (replayStep < gameHistory.size) {
                kotlinx.coroutines.delay(1500L)
                replayStep++
            } else {
                isPlaying = false
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("▶ Game Replay", color = Orange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Text("✕", color = White, fontSize = 20.sp)
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Move counter
                Text(
                    "Move ${replayStep} / ${gameHistory.size}",
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Last move description
                if (lastMove != null) {
                    val color = if (lastMove.color == PieceColor.WHITE) White else Color(0xFFFF6B6B)
                    val colorName = if (lastMove.color == PieceColor.WHITE) "White" else "Black"
                    Text(
                        "$colorName ${lastMove.piece.name.lowercase()}: " +
                        "${'a' + lastMove.from.file}${lastMove.from.rank + 1} → " +
                        "${'a' + lastMove.to.file}${lastMove.to.rank + 1}",
                        color = color,
                        fontSize = 13.sp
                    )
                } else {
                    Text("Initial position", color = LightGrey, fontSize = 13.sp)
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Board with coordinates (reused style from AI mode)
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top file labels
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp)) {
                        for (file in 0..7) {
                            Text(
                                "${'a' + file}",
                                color = LightGrey,
                                fontSize = 9.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Left labels + Board + Right labels
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        // Left
                        Column(
                            modifier = Modifier.width(14.dp).fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (rank in 7 downTo 0) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${rank + 1}", color = LightGrey, fontSize = 9.sp)
                                }
                            }
                        }
                        
                        // Board
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).background(Color(0xFF2B1A10)).padding(3.dp)) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                for (rank in 7 downTo 0) {
                                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                        for (file in 0..7) {
                                            val sq = Square(file, rank)
                                            val piece = replayBoard[sq]
                                            val isDark = (file + rank) % 2 == 1
                                            val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                                            
                                            // Highlight last move source and destination
                                            val isFrom = lastMove?.from == sq
                                            val isTo = lastMove?.to == sq
                                            val highlight = when {
                                                isFrom -> Color(0x88FFD700)  // Gold for source
                                                isTo -> Color(0x88FF6B6B)    // Red for destination
                                                else -> Color.Transparent
                                            }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .background(squareColor)
                                                    .border(
                                                        width = if (isFrom || isTo) 2.dp else 0.dp,
                                                        color = when {
                                                            isFrom -> Color(0xFFFFD700)
                                                            isTo -> Color(0xFFFF6B6B)
                                                            else -> Color.Transparent
                                                        }
                                                    ),
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
                                                    val text = if (piece.color == PieceColor.WHITE) {
                                                        symbol.replace("♟","♙").replace("♞","♘").replace("♝","♗")
                                                              .replace("♜","♖").replace("♛","♕").replace("♚","♔")
                                                    } else symbol
                                                    Text(text = text, color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Right
                        Column(
                            modifier = Modifier.width(14.dp).fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (rank in 7 downTo 0) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${rank + 1}", color = LightGrey, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                    
                    // Bottom file labels
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp)) {
                        for (file in 0..7) {
                            Text(
                                "${'a' + file}",
                                color = LightGrey,
                                fontSize = 9.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Controls row 1: Playback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { replayStep = (replayStep - 1).coerceAtLeast(0) },
                        enabled = replayStep > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) { Text("⏮", fontSize = 14.sp) }
                    
                    Button(
                        onClick = { isPlaying = !isPlaying },
                        enabled = replayStep < gameHistory.size,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) Color(0xFFFCA311) else Color(0xFF4CAF50), contentColor = Color.Black),
                        modifier = Modifier.weight(2f),
                        contentPadding = PaddingValues(4.dp)
                    ) { Text(if (isPlaying) "⏸ Pause" else "▶ Play", fontSize = 13.sp) }
                    
                    Button(
                        onClick = { if (replayStep < gameHistory.size) replayStep++ },
                        enabled = replayStep < gameHistory.size,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) { Text("⏭", fontSize = 14.sp) }
                }
                
                // Controls row 2: Reset / Jump to end
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { replayStep = 0; isPlaying = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) { Text("⏮ Start", fontSize = 12.sp) }
                    
                    Button(
                        onClick = { replayStep = gameHistory.size; isPlaying = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) { Text("End ⏭", fontSize = 12.sp) }
                }
                
                // Legend
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0x88FFD700)).border(2.dp, Color(0xFFFFD700)))
                        Spacer(Modifier.width(4.dp))
                        Text("From", color = LightGrey, fontSize = 10.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0x88FF6B6B)).border(2.dp, Color(0xFFFF6B6B)))
                        Spacer(Modifier.width(4.dp))
                        Text("To", color = LightGrey, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
