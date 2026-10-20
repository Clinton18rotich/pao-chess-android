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

// Coordinate system: Square(file, rank), file a=0..h=7, rank 1=0..8=7.
// Square(4,0)=e1, Square(4,7)=e8, Square(7,7)=h8.

val PUZZLES = listOf(

    // 1. Smothered Mate — verified by engine: g5 -> f7 (KNIGHT)
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

    // 2. Anastasia's Mate — verified: g8 -> a8 (ROOK), back-rank mate
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
        MoveStep("White plays Ra8# — the Rook slides to the 8th rank, mate along the back rank.", Square(6, 7), Square(0, 7)),
        "Slide the Rook along the back rank."
    ),

    // 3. Legal's Mate — verified: d6 -> e7 (QUEEN)
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
        MoveStep("White plays Qe7# — the Queen steps to e7, mate with the Bishop covering the escape squares.", Square(3, 5), Square(4, 6)),
        "Find the mate the Bishop assists."
    ),

    // 4. Bishop & Knight — verified: e5 -> f7 (KNIGHT)
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
        MoveStep("White plays Nf7# — the Knight mates the cornered King; the Bishop covers g5.", Square(4, 4), Square(5, 6)),
        "Knight and Bishop mate the corner."
    ),

    // 5. The Windmill — verified: e5 -> g6 (KNIGHT)
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
        MoveStep("White plays Ng6# — the Knight delivers mate, uncovering the Rook on the h-file.", Square(4, 4), Square(6, 5)),
        "Start the discovered checks."
    ),

    // 6. Back Rank Mate — new board, engine will verify or fail this puzzle
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
        MoveStep("White plays Ra8# — the Rook invades the back rank; Black's own pawns box the King.", Square(0, 0), Square(0, 7)),
        "Slide the Rook to the 8th rank."
    ),

    // 7. Fool's Mate — full position from 1.f3 e5 2.g4 Qh4#
    // White pawns on d2/e2 block the King; f-pawn on f3 and g-pawn on g4 clear the diagonal.
    Puzzle(
        "Fool's Mate",
        "Black to move. Checkmate in 1.",
        mapOf(
            // White back rank
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(1, 0) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(2, 0) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(3, 0) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(5, 0) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(6, 0) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(7, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            // White pawns that stayed home (block King's escape squares)
            Square(0, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(1, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(2, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(3, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(4, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(7, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            // White pawns that moved
            Square(5, 2) to Piece(PieceType.PAWN, PieceColor.WHITE),  // f3
            Square(6, 3) to Piece(PieceType.PAWN, PieceColor.WHITE),  // g4
            // Black back rank
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(1, 7) to Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Square(2, 7) to Piece(PieceType.BISHOP, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 7) to Piece(PieceType.BISHOP, PieceColor.BLACK),
            Square(6, 7) to Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Square(7, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            // Black pawns
            Square(0, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(1, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(2, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(4, 4) to Piece(PieceType.PAWN, PieceColor.BLACK),  // e5
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK)
        ),
        MoveStep("Black plays Qh4# — the Queen mates along the exposed diagonal; White's own pawns block every escape.", Square(3, 7), Square(7, 3)),
        "Deliver mate on h4."
    ),

    // 8. Scholar's Mate — new board, engine will verify or fail
    Puzzle(
        "Scholar's Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 4) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(2, 3) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        MoveStep("White plays Qxf7# — the Queen mates on f7, supported by the Bishop on c4.", Square(7, 4), Square(5, 6)),
        "Strike the weak f7 square."
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
            if (status == TextToSpeech.SUCCESS) { tts?.language = Locale.US; isTtsReady = true }
        }
    }
    DisposableEffect(Unit) { onDispose { tts?.stop(); tts?.shutdown() } }

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
                // Announce the upcoming move BEFORE waiting
                if (isTtsReady) {
                    tts?.speak(
                        moves[currentStep].description,
                        TextToSpeech.QUEUE_FLUSH, null, null
                    )
                }
                delay(2500L)
                currentStep++
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
        Text("🧩 Puzzles Teacher", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { currentStep = 0; isPlaying = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))) { Text("Reset") }
            Button(
                onClick = {
                    currentStep = 0
                    isPlaying = true
                    // Speak immediately instead of waiting for delay
                    if (isTtsReady) {
                        tts?.speak(moves[0].description, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black)
            ) { Text("Replay") }
            Button(
                onClick = { isPlaying = !isPlaying },
                enabled = isPlaying || currentStep < moves.size,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF3A2A10),
                    disabledContentColor = LightGrey
                )
            ) {
                Text(if (isPlaying) "Pause" else "Play")
            }
            Button(
                onClick = { if (currentStep < moves.size) currentStep++ },
                enabled = currentStep < moves.size,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurface,
                    disabledContainerColor = Color(0xFF2A2A2A),
                    disabledContentColor = LightGrey
                )
            ) { Text("Next") }
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
