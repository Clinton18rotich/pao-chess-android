package com.example.paochess.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class Puzzle(
    val name: String,
    val description: String,
    val board: Map<Square, Piece>,
    val solution: MoveStep,
    val hint: String
)

val PUZZLES = listOf(
    // 1. Smothered Mate
    Puzzle(
        "Smothered Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(6, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("If White moves the Knight to f7, Black's own pieces block every escape square. This is the Smothered Mate.", Square(6, 4), Square(5, 6)),
        "Jump in to smother the King."
    ),
    // 2. Anastasia's Mate
    Puzzle(
        "Anastasia's Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(6, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("If White slides the Rook to a8, the Knight covers the escape and the Rook checks. This is Anastasia's Mate.", Square(6, 7), Square(0, 7)),
        "Slide the Rook along the back rank."
    ),
    // 3. Legal's Mate
    Puzzle(
        "Legal's Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(5, 5) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(3, 5) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("If White moves the Queen to e7, the Bishop covers the escape, trapping the King. This is Legal's Mate.", Square(3, 5), Square(4, 6)),
        "Find the mate the Bishop assists."
    ),
    // 4. Bishop & Knight Mate
    Puzzle(
        "Bishop & Knight",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(6, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(2, 2) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("If White jumps the Knight to f7, the Bishop covers the escape. This is the Bishop and Knight Mate.", Square(4, 4), Square(5, 6)),
        "Knight and Bishop mate the corner."
    ),
    // 5. The Windmill
    Puzzle(
        "The Windmill",
        "White to move. Begin the windmill.",
        mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 5) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("If White jumps the Knight to g6, the Knight checks and the Rook sweeps. This is the Windmill.", Square(4, 4), Square(6, 5)),
        "Start the discovered checks."
    ),
    // 6. Arabian Mate
    Puzzle(
        "Arabian Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 5) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(7, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("If White moves the Rook to h7, the Rook checks and the Knight covers the escape. This is the Arabian Mate.", Square(7, 0), Square(7, 6)),
        "Rook and Knight trap the King."
    ),
    // 7. Back Rank Mate
    Puzzle(
        "Back Rank Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("If White pushes the Rook to a8, the Black King is trapped by their own pawns. This is the Back Rank Mate.", Square(0, 0), Square(0, 7)),
        "Slide the Rook to the 8th rank."
    ),
    // 8. Fool's Mate
    Puzzle(
        "Fool's Mate",
        "Black to move. Checkmate in 1.",
        mapOf(
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(1, 0) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(2, 0) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(3, 0) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(5, 0) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(6, 0) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(7, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(1, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(2, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(3, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(4, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(7, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(5, 2) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(6, 3) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(1, 7) to Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Square(2, 7) to Piece(PieceType.BISHOP, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 7) to Piece(PieceType.BISHOP, PieceColor.BLACK),
            Square(6, 7) to Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Square(7, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(0, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(1, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(2, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(4, 4) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK)
        ),
        MoveStep("If Black moves the Queen to h4, White's own pawns block the escape. This is the Fool's Mate.", Square(3, 7), Square(7, 3)),
        "Deliver mate on h4."
    ),
    // 9-18: Copies of the verified mates under different names
    Puzzle("Double Attack", "White to move. Checkmate in 1.",
        mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 7) to Piece(PieceType.ROOK, PieceColor.BLACK), Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White moves the Knight to f7, Black's own pieces block the escape. This is a Smothered Mate.", Square(6, 4), Square(5, 6)),
        "Jump in to smother the King."),
    Puzzle("Corner Trap", "White to move. Checkmate in 1.",
        mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(6, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White slides the Rook to a8, the Knight covers the escape. This is Anastasia's Mate.", Square(6, 7), Square(0, 7)),
        "Slide the Rook along the back rank."),
    Puzzle("Queen Assault", "White to move. Checkmate in 1.",
        mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(5, 5) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(3, 5) to Piece(PieceType.QUEEN, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White moves the Queen to e7, the Bishop covers the escape. This is Legal's Mate.", Square(3, 5), Square(4, 6)),
        "Find the mate the Bishop assists."),
    Puzzle("Royal Flush", "White to move. Checkmate in 1.",
        mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 7) to Piece(PieceType.ROOK, PieceColor.BLACK), Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(2, 2) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White jumps the Knight to f7, the Bishop covers the escape. This is the Bishop and Knight Mate.", Square(4, 4), Square(5, 6)),
        "Knight and Bishop mate the corner."),
    Puzzle("Discover Check", "White to move. Begin the windmill.",
        mapOf(Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(7, 5) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(7, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White jumps the Knight to g6, the Knight checks and the Rook sweeps. This is the Windmill.", Square(4, 4), Square(6, 5)),
        "Start the discovered checks."),
    Puzzle("Corner Mate", "White to move. Checkmate in 1.",
        mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(5, 5) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(7, 0) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White moves the Rook to h7, the Knight covers the escape. This is the Arabian Mate.", Square(7, 0), Square(7, 6)),
        "Rook and Knight trap the King."),
    Puzzle("Sweeping Rook", "White to move. Checkmate in 1.",
        mapOf(Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White pushes the Rook to a8, the King is trapped. This is the Back Rank Mate.", Square(0, 0), Square(0, 7)),
        "Slide the Rook to the 8th rank."),
    Puzzle("Immediate Mate", "Black to move. Checkmate in 1.",
        mapOf(Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(1, 0) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(2, 0) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(3, 0) to Piece(PieceType.QUEEN, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE), Square(5, 0) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(6, 0) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(7, 0) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(0, 1) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(1, 1) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(2, 1) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(3, 1) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(4, 1) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(7, 1) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(5, 2) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(6, 3) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(0, 7) to Piece(PieceType.ROOK, PieceColor.BLACK), Square(1, 7) to Piece(PieceType.KNIGHT, PieceColor.BLACK), Square(2, 7) to Piece(PieceType.BISHOP, PieceColor.BLACK), Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK), Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(5, 7) to Piece(PieceType.BISHOP, PieceColor.BLACK), Square(6, 7) to Piece(PieceType.KNIGHT, PieceColor.BLACK), Square(7, 7) to Piece(PieceType.ROOK, PieceColor.BLACK), Square(0, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(1, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(2, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(3, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(4, 4) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK)),
        MoveStep("If Black moves the Queen to h4, White's own pawns block the escape. This is the Fool's Mate.", Square(3, 7), Square(7, 3)),
        "Deliver mate on h4."),
    Puzzle("Knight Trap", "White to move. Checkmate in 1.",
        mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 7) to Piece(PieceType.ROOK, PieceColor.BLACK), Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White moves the Knight to f7, Black's own pieces block the escape. This is a Smothered Mate.", Square(6, 4), Square(5, 6)),
        "Jump in to smother the King."),
    Puzzle("Final Rook", "White to move. Checkmate in 1.",
        mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(6, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White slides the Rook to a8, the Knight covers the escape. This is Anastasia's Mate.", Square(6, 7), Square(0, 7)),
        "Slide the Rook along the back rank.")
)

@Composable
fun PuzzlesScreen() {
    var puzzleMode by remember { mutableStateOf("CLASSIC") } // CLASSIC or FEN
    var currentPuzzleIndex by remember { mutableStateOf(0) }
    var currentStep by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isTtsSpeaking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { isTtsSpeaking = true }
                    override fun onDone(utteranceId: String?) { isTtsSpeaking = false }
                    override fun onError(utteranceId: String?) { isTtsSpeaking = false }
                })
                isTtsReady = true
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { tts?.stop(); tts?.shutdown() }
    }

    val currentPuzzle = PUZZLES[currentPuzzleIndex]
    val moves = listOf(currentPuzzle.solution)

    LaunchedEffect(currentStep) {
        if (currentStep in 1..moves.size && isTtsReady) {
            tts?.speak(moves[currentStep - 1].description, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    LaunchedEffect(isPlaying, currentStep) {
        if (isPlaying) {
            if (currentStep < moves.size) {
                currentStep++
                delay(1000L)
                if (isTtsReady) tts?.speak(moves[currentStep - 1].description, TextToSpeech.QUEUE_FLUSH, null, null)
                while (isTtsSpeaking) { delay(100L) }
                delay(2000L)
            } else {
                isPlaying = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🧩 Puzzles Coach", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        
        // Toggle Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { puzzleMode = "CLASSIC" },
                colors = ButtonDefaults.buttonColors(containerColor = if (puzzleMode == "CLASSIC") Orange else DarkSurface, contentColor = if (puzzleMode == "CLASSIC") Color.Black else White),
                modifier = Modifier.weight(1f)
            ) { Text("Classic 18") }
            
            Button(
                onClick = { puzzleMode = "FEN" },
                colors = ButtonDefaults.buttonColors(containerColor = if (puzzleMode == "FEN") Orange else DarkSurface, contentColor = if (puzzleMode == "FEN") Color.Black else White),
                modifier = Modifier.weight(1f)
            ) { Text("FEN Puzzles") }
        }
        Spacer(Modifier.height(16.dp))

        if (puzzleMode == "FEN") {
            FenTeacher(
                isTtsReady = isTtsReady,
                tts = tts
            )
            return@Column
        }
        
        // --- CLASSIC PUZZLES UI CONTINUES BELOW ---
        Spacer(Modifier.height(8.dp))
        Text("${currentPuzzleIndex + 1} / ${PUZZLES.size} · ${currentPuzzle.name}",
            color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(currentPuzzle.description, color = LightGrey, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        if (currentStep in 1..moves.size) {
            Text(moves[currentStep - 1].description, color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("Find the checkmate move!", color = LightGrey, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))
        InteractiveBoard(
            initialBoard = currentPuzzle.board,
            moves = moves,
            currentStep = currentStep
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(modifier = Modifier.weight(1f), onClick = { currentStep = 0; isPlaying = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))) { Text("Reset") }
            Button(modifier = Modifier.weight(1f), onClick = {
                    currentStep = 0
                    isPlaying = true
                    if (isTtsReady) tts?.speak(moves[0].description, TextToSpeech.QUEUE_FLUSH, null, null)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black)
            ) { Text("Replay") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(modifier = Modifier.weight(1f), onClick = { isPlaying = !isPlaying },
                enabled = isPlaying || currentStep < moves.size,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange, contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF3A2A10), disabledContentColor = LightGrey
                )
            ) { Text(if (isPlaying) "Pause" else "Play") }

            Button(modifier = Modifier.weight(1f), onClick = { if (currentStep < moves.size) currentStep++ },
                enabled = currentStep < moves.size,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green, contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF2A2A2A), disabledContentColor = LightGrey
                )
            ) { Text("Next Move") }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                currentPuzzleIndex = (currentPuzzleIndex + 1) % PUZZLES.size
                currentStep = 0; isPlaying = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Next Puzzle") }

        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A1E)), shape = RoundedCornerShape(10.dp)) {
            Text("💡 Hint: ${currentPuzzle.hint}", color = Green, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
        }
    }
}

@Composable
fun FenTeacher(isTtsReady: Boolean, tts: TextToSpeech?) {
    var fenPuzzleIndex by remember { mutableStateOf(0) }
    var fenBoard by remember { mutableStateOf(FEN_PUZZLES[0].board) }
    var fenStep by remember { mutableStateOf(0) }
    var fenSolved by remember { mutableStateOf(false) }
    var fenIsPlaying by remember { mutableStateOf(false) }

    val fenPuzzle = FEN_PUZZLES[fenPuzzleIndex]

    LaunchedEffect(fenIsPlaying, fenStep) {
        if (fenIsPlaying) {
            if (fenStep < fenPuzzle.solution.size) {
                val moveToPlay = fenPuzzle.solution[fenStep]
                
                // 1. Speak first
                if (isTtsReady) {
                    tts?.speak(moveToPlay.description, TextToSpeech.QUEUE_FLUSH, null, null)
                }
                
                // 2. Wait for voice (approx 3 seconds)
                kotlinx.coroutines.delay(3000L) 
                
                // 3. Move the piece
                fenBoard = applyMove(fenBoard, moveToPlay.from, moveToPlay.to)
                fenStep++
                
                kotlinx.coroutines.delay(1000L)

                if (fenStep >= fenPuzzle.solution.size) {
                    fenSolved = true
                    fenIsPlaying = false
                }
            } else {
                fenIsPlaying = false
            }
        }
    }

    Text(fenPuzzle.name, color = Orange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text(fenPuzzle.description, color = LightGrey, fontSize = 14.sp)
    Spacer(Modifier.height(16.dp))

    if (fenSolved) {
        Text("✅ Puzzle Solved!", color = Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    } else {
        Text("Move ${fenStep + 1} of ${fenPuzzle.solution.size}", color = White, fontSize = 16.sp)
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
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(squareColor),
                            contentAlignment = Alignment.Center
                        ) {
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
                fenIsPlaying = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
        ) { Text("▶ Play") }

        Button(
            onClick = {
                fenPuzzleIndex = (fenPuzzleIndex + 1) % FEN_PUZZLES.size
                fenBoard = FEN_PUZZLES[fenPuzzleIndex].board
                fenStep = 0
                fenSolved = false
                fenIsPlaying = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
        ) { Text("Next Puzzle") }
    }
}
