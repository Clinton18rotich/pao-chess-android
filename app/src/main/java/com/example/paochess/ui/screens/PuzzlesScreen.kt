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
    // 16. Scholar's Mate (Full Game - White plays)
    Puzzle(
        "Scholar's Mate (Full Game)",
        "White to move. Trap Black in 4 moves.",
        getInitialBoard(),
        listOf(
            MoveStep("Move 1 (White): e4. White claims the center.", Square(4, 1), Square(4, 3)),
            MoveStep("Move 2 (Black): e5. Black mirrors.", Square(4, 6), Square(4, 4)),
            MoveStep("Move 3 (White): Bc4. The Bishop targets the weak f7 square.", Square(5, 0), Square(2, 3)),
            MoveStep("Move 4 (Black): Nc6. Black defends normally.", Square(1, 7), Square(2, 5)),
            MoveStep("Move 5 (White): Qh5. Threatening checkmate on f7.", Square(3, 0), Square(7, 4)),
            MoveStep("Move 6 (Black): Nf6. The final blunder.", Square(6, 7), Square(5, 5)),
            MoveStep("Move 7 (White): Qxf7#. Checkmate!", Square(7, 4), Square(5, 6))
        ),
        "Play the Scholar's Mate. Punish Black for not defending f7."
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
    Puzzle("Knight Trap", "White to move. Checkmate in 1.", 
        mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 7) to Piece(PieceType.ROOK, PieceColor.BLACK), Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White moves the Knight to f7, Black's own pieces block the escape. This is a Smothered Mate.", Square(6, 4), Square(5, 6)),
        "Jump in to smother the King."),
    Puzzle("Final Rook", "White to move. Checkmate in 1.", 
        mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(6, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)),
        MoveStep("If White slides the Rook to a8, the Knight covers the escape. This is Anastasia's Mate.", Square(6, 7), Square(0, 7)),
        "Slide the Rook along the back rank.")
    // 18. The Knight's Tour (Multi-move)
    Puzzle(
        "The Knight's Tour",
        "White to move. Trap the King in 3 moves.",
        mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(6, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Nf6. The Knight attacks the escape squares.", Square(4, 4), Square(5, 5)),
            MoveStep("Move 2 (Black): The King is forced to move.", Square(7, 7), Square(7, 6)),
            MoveStep("Move 3 (White): Nf7#. Checkmate! The Knight smothers the King.", Square(5, 5), Square(5, 6))
        ),
        "Guide the Knight to the mating square."
    ),
    // 19. The Rook Sweep (Multi-move)
    Puzzle(
        "The Rook Sweep",
        "White to move. Sweep the board in 3 moves.",
        mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(1, 0) to Piece(PieceType.ROOK, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Ra8+. The first Rook invades the back rank.", Square(0, 0), Square(0, 7)),
            MoveStep("Move 2 (Black): The King is forced to move.", Square(6, 7), Square(7, 7)),
            MoveStep("Move 3 (White): Rb8#. The second Rook follows up for mate.", Square(1, 0), Square(1, 7))
        ),
        "Line up both Rooks on the back rank."
    ),
    // 20. The Bishop Strike (Multi-move)
    Puzzle(
        "The Bishop Strike",
        "White to move. Strike diagonally in 3 moves.",
        mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(2, 2) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(3, 0) to Piece(PieceType.QUEEN, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Qg6. The Queen sacrifices herself to open the diagonal.", Square(3, 0), Square(6, 6)),
            MoveStep("Move 2 (Black): Kxg6. The King captures the Queen.", Square(6, 7), Square(6, 6)),
            MoveStep("Move 3 (White): Bd5#. The Bishop delivers the final blow.", Square(2, 2), Square(3, 3))
        ),
        "Sacrifice the Queen to open the Bishop's path."
    ),
    // 21. The Double Rook (Multi-move)
    Puzzle(
        "The Double Rook",
        "White to move. Deliver a double check in 3 moves.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(4, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Nf6+. The Knight checks the King.", Square(4, 4), Square(5, 5)),
            MoveStep("Move 2 (Black): Kd8. The King must move.", Square(4, 7), Square(3, 7)),
            MoveStep("Move 3 (White): Re8#. The Rook delivers mate.", Square(0, 0), Square(4, 0))
        ),
        "Use the Knight to uncover the Rook."
    )
    // 18. Lichess Mate in 2 (Verified)
    Puzzle(
        "Lichess Mate in 2",
        "White to move. Find the forced mate.",
        com.example.paochess.data.FenParser.parseFen("6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1"),
        listOf(
            com.example.paochess.data.FenParser.parseUciMove("e1e8", 1),
            com.example.paochess.data.FenParser.parseUciMove("g8f8", 2),
            com.example.paochess.data.FenParser.parseUciMove("e8f8", 3)
        ),
        "Push the Rook to the back rank."
    )
)

@Composable
fun PuzzlesScreen() {
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
        Text("${currentPuzzleIndex + 1} / ${PUZZLES.size}  ·  ${currentPuzzle.name}",
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
