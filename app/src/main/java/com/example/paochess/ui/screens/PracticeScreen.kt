package com.example.paochess.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
            
            // Game recording
            val gameHistory = remember { mutableStateListOf<RecordedMove>() }
            var showAnalysis by remember { mutableStateOf(false) }
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
                        gameStatus = "Your turn (White)"
                        
                        if (bestMove.taunt.isNotEmpty() && isTtsReady) {
                            tts?.setPitch(0.5f)   // Deep arrogant male
                            tts?.setSpeechRate(0.85f)
                            tts?.speak(bestMove.taunt, TextToSpeech.QUEUE_FLUSH, null, null)
                            lastAiTaunt = bestMove.taunt
                        } else {
                            lastAiTaunt = ""
                        }
                    } else {
                        gameStatus = "Checkmate or Stalemate!"
                        // Auto-show analysis when game ends
                        delay(1500L)
                        showAnalysis = true
                    }
                    
                    isAiThinking = false
                }
            }
            
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
                                        .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent)
                                        .clickable {
                                            if (isAiThinking) return@clickable
                                            
                                            if (isLegal) {
                                                // Record the move BEFORE applying
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
                
                // Move list
                Text("━━━ Move List ━━━", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                
                val pairs = whiteMoves.mapIndexed { idx, white ->
                    Pair(white, blackMoves.getOrNull(idx))
                }
                
                pairs.forEach { (white, black) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text("${white.moveNumber}. ", color = LightGrey, fontSize = 13.sp, modifier = Modifier.width(28.dp))
                        Text(squareLabel(white.from) + squareLabel(white.to), color = White, fontSize = 13.sp, modifier = Modifier.width(70.dp))
                        if (black != null) {
                            Text(squareLabel(black.from) + squareLabel(black.to), color = Color(0xFFFF6B6B), fontSize = 13.sp, modifier = Modifier.weight(1f))
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
                        Text(
                            "❌ Move ${move.moveNumber}: Lost ${move.capturedPiece?.name?.lowercase() ?: "a piece"}",
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A1E))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("💡 Tips", color = Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (blunders.size >= 3) {
                                    "You lost ${blunders.size} pieces. Before each move, ask: is this piece safe here? Defend with another piece, not just position."
                                } else if (blunders.size >= 1) {
                                    "Good game! Just ${blunders.size} piece(s) lost. Always check if your destination square is attacked before moving."
                                } else {
                                    "Perfect game! You protected all your pieces."
                                },
                                color = White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun squareLabel(sq: Square): String {
    val file = ('a' + sq.file)
    val rank = sq.rank + 1
    return "$file$rank"
}
