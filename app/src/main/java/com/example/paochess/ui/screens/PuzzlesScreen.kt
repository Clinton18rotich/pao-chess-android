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

// Square(file, rank): a=0..h=7, 1=0..8=7.  Square(4,0)=e1, Square(4,7)=e8.

val PUZZLES = listOf(

    // 1. Back Rank Mate
    Puzzle(
        "Back Rank Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(4, 0) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(1, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(2, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Re8# — Rook slides to the back rank, mate behind Black's own pawns.", Square(0, 7), Square(4, 0)),
        "Slide the Rook to the 8th rank."
    ),

    // 2. Queen Mate
    Puzzle(
        "Queen Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Qg7# — Queen steps beside the cornered King.", Square(3, 6), Square(6, 6)),
        "Place the Queen next to the King."
    ),

    // 3. Scholar's Mate
    Puzzle(
        "Scholar's Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(5, 4) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Qxf7# — Queen captures f7, backed by the Bishop on c4.", Square(3, 7), Square(5, 6)),
        "Attack the weak f7 square."
    ),

    // 4. Fool's Mate
    Puzzle(
        "Fool's Mate",
        "Black to move. Checkmate in 1.",
        mapOf(
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(5, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(6, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(7, 2) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK)
        ),
        MoveStep("Black plays Qh4# — Queen mates on the exposed diagonal.", Square(3, 7), Square(7, 3)),
        "Deliver mate on h4."
    ),

    // 5. Smothered Mate
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
        MoveStep("White plays Nf7# — Knight lands on f7; Black's own pieces smother the King.", Square(6, 4), Square(5, 6)),
        "Jump in to smother the King."
    ),

    // 6. Arabian Mate
    Puzzle(
        "Arabian Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(6, 6) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Rh6# — Rook mates on the h-file; the Knight covers g8.", Square(6, 6), Square(7, 6)),
        "Rook and Knight trap the King."
    ),

    // 7. Anastasia's Mate
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
        MoveStep("White plays Rh7# — Rook strikes on h7; the Knight blocks g8.", Square(6, 7), Square(7, 6)),
        "Mate on the h-file."
    ),

    // 8. Legal's Mate
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
        MoveStep("White plays Qxf7# — Queen mates on f7, supported by the Bishop on g5.", Square(3, 5), Square(5, 6)),
        "Sacrifice the Queen to mate."
    ),

    // 9. Royal Fork
    Puzzle(
        "Royal Fork",
        "White to move. Find the Knight fork.",
        mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(3, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Nf6+ — Knight forks King on g8 and Queen on d7.", Square(3, 4), Square(5, 5)),
        "Fork King and Queen."
    ),

    // 10. Absolute Pin — replaced with a real moving mate
    Puzzle(
        "Absolute Pin",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(3, 5) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Qxf7# — Queen captures on f7; the King on g8 is boxed in.", Square(3, 5), Square(5, 6)),
        "Strike the weak f7 square."
    ),

    // 11. The Skewer
    Puzzle(
        "The Skewer",
        "White to move. Skewer the King.",
        mapOf(
            Square(2, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 1) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Re7+ — Rook checks the King on c8; the Queen on d8 falls next.", Square(4, 0), Square(4, 6)),
        "Skewer the King to the Queen."
    ),

    // 12. Discovered Attack
    Puzzle(
        "Discovered Attack",
        "White to move. Reveal the attack.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Nc6+ — Knight steps aside, revealing the Rook's check up the e-file.", Square(4, 4), Square(2, 5)),
        "Move the Knight to reveal the Rook."
    ),

    // 13. Double Check (as a supported Knight mate)
    Puzzle(
        "Double Check",
        "White to move. Deliver a decisive check.",
        mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(4, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(5, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Nf7# — Knight mates the g8 King; the Rook on e8 seals the file.", Square(5, 4), Square(5, 6)),
        "Check with the Knight, supported by the Rook."
    ),

    // 14. The Trap
    Puzzle(
        "The Trap",
        "White to move. Trap the Queen.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 3) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(0, 2) to Piece(PieceType.ROOK, PieceColor.WHITE)
        ),
        MoveStep("White plays Ra1 — Rook hits the Queen on a4; no safe square along the a-file.", Square(0, 2), Square(0, 0)),
        "Trap the Queen on the a-file."
    ),

    // 15. Bishop & Knight — replaced with a real moving mate
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
        MoveStep("White plays Nf6# — Knight mates the cornered King; the Bishop on c1 covers g5.", Square(4, 4), Square(5, 5)),
        "Knight and Bishop mate the corner."
    ),

    // 16. Exchange Sacrifice
    Puzzle(
        "Exchange Sacrifice",
        "White to move. Sacrifice the Rook.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(3, 5) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Rxg7+ — Rook sacrifices itself to crack the King's shelter.", Square(6, 7), Square(6, 6)),
        "Sacrifice the Rook on g7."
    ),

    // 17. Queen Back Rank
    Puzzle(
        "Queen Back Rank",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(4, 0) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(1, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(2, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(3, 3) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Qe8# — Queen invades the back rank; Black's King is boxed in.", Square(3, 3), Square(4, 0)),
        "Mate on the back rank."
    ),

    // 18. The Windmill
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
        MoveStep("White plays Nf6+ — Knight checks the King, preparing the windmill battery with the h8 Rook.", Square(4, 4), Square(5, 5)),
        "Start the discovered checks."
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

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
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
            if (currentStep < moves.size) { delay(2500L); currentStep++ } else isPlaying = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🧩 Puzzles Teacher", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(currentPuzzle.name, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { currentStep = 0; isPlaying = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))) { Text("Reset") }
            Button(onClick = { currentStep = 0; isPlaying = true },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black)) { Text("Replay") }
            Button(onClick = { isPlaying = !isPlaying },
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black)) {
                Text(if (isPlaying) "Pause" else "Play Solution")
            }
            Button(onClick = { if (currentStep < moves.size) currentStep++ },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)) { Text("Next Move") }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                currentPuzzleIndex = (currentPuzzleIndex + 1) % PUZZLES.size
                currentStep = 0
                isPlaying = false
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
