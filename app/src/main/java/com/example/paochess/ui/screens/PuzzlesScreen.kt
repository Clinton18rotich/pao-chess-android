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
    val solution: List<MoveStep>,
    val hint: String
)

val PUZZLES = listOf(

    // 1. Scholar's Mate (Full Game)
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

    // 2. Smothered Mate
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
        listOf(MoveStep("If White moves the Knight to f7, Black's own pieces block the escape. This is the Smothered Mate.", Square(6, 4), Square(5, 6))),
        "Jump in to smother the King."
    ),

    // 3. Anastasia's Mate
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
        listOf(MoveStep("If White slides the Rook to a8, the Knight covers the escape. This is Anastasia's Mate.", Square(6, 7), Square(0, 7))),
        "Slide the Rook along the back rank."
    ),

    // 4. Legal's Mate
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
        listOf(MoveStep("If White moves the Queen to e7, the Bishop covers the escape. This is Legal's Mate.", Square(3, 5), Square(4, 6))),
        "Find the mate the Bishop assists."
    ),

    // 5. Bishop & Knight
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
        listOf(MoveStep("If White jumps the Knight to f7, the Bishop covers the escape. This is the Bishop and Knight Mate.", Square(4, 4), Square(5, 6))),
        "Knight and Bishop mate the corner."
    ),

    // 6. The Windmill
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
        listOf(MoveStep("If White jumps the Knight to g6, the Knight checks and the Rook sweeps. This is the Windmill.", Square(4, 4), Square(6, 5))),
        "Start the discovered checks."
    ),

    // 7. Arabian Mate
    Puzzle(
        "Arabian Mate",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 5) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(7, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(MoveStep("If White moves the Rook to h7, the Rook checks and the Knight covers the escape. This is the Arabian Mate.", Square(7, 0), Square(7, 6))),
        "Rook and Knight trap the King."
    ),

    // 8. Back Rank Mate
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
        listOf(MoveStep("If White pushes the Rook to a8, the Black King is trapped by their own pawns. This is the Back Rank Mate.", Square(0, 0), Square(0, 7))),
        "Slide the Rook to the 8th rank."
    ),

    // 9. The Royal Fork
    Puzzle(
        "The Royal Fork",
        "White to move. Win the Queen.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(7, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(3, 1) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(7, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Nc3. The Knight jumps out.", Square(3, 1), Square(2, 2)),
            MoveStep("Move 2 (Black): a6. Black plays a waiting move.", Square(0, 6), Square(0, 5)),
            MoveStep("Move 3 (White): Ne4. The Knight moves to the center.", Square(2, 2), Square(4, 3)),
            MoveStep("Move 4 (Black): a5. Black continues.", Square(0, 5), Square(0, 4)),
            MoveStep("Move 5 (White): Nd6+. The Knight forks the King and Queen!", Square(4, 3), Square(3, 5)),
            MoveStep("Move 6 (Black): Kd7. The King must move.", Square(4, 7), Square(3, 6))
        ),
        "Use the Knight to attack two pieces at once."
    ),

    // 10. The Skewer
    Puzzle(
        "The Skewer",
        "White to move. Win the Queen.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(4, 6) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(7, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 1) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Re3. The Rook slides up the e-file.", Square(4, 0), Square(4, 2)),
            MoveStep("Move 2 (Black): a6. Black plays a waiting move.", Square(0, 6), Square(0, 5)),
            MoveStep("Move 3 (White): Rae1. The second Rook joins.", Square(0, 0), Square(4, 0)),
            MoveStep("Move 4 (Black): a5. Black continues.", Square(0, 5), Square(0, 4)),
            MoveStep("Move 5 (White): Rxe7+. Captures Queen, checks the King.", Square(4, 2), Square(4, 6)),
            MoveStep("Move 6 (Black): Kxe7. King must recapture.", Square(4, 7), Square(4, 6))
        ),
        "Line up your Rooks to force the King out."
    ),

    // 11. The Discovered Attack
    Puzzle(
        "The Discovered Attack",
        "White to move. Win the Queen.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 3) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Nc3. The Knight steps aside.", Square(4, 3), Square(2, 2)),
            MoveStep("Move 2 (Black): a6. Black plays a waiting move.", Square(0, 6), Square(0, 5)),
            MoveStep("Move 3 (White): Nb5. The Knight attacks the Queen!", Square(2, 2), Square(1, 3)),
            MoveStep("Move 4 (Black): Nc6. Black defends.", Square(1, 7), Square(2, 5)),
            MoveStep("Move 5 (White): Nxd8. Captures the Queen.", Square(1, 3), Square(3, 7))
        ),
        "Use the Knight to distract the Queen."
    ),

    // 12. Final Rook
    Puzzle(
        "Final Rook",
        "White to move. Checkmate in 1.",
        mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(6, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(MoveStep("If White slides the Rook to a8, the Knight covers the escape. This is Anastasia's Mate.", Square(6, 7), Square(0, 7))),
        "Slide the Rook along the back rank."
    ),
    // 13. The Pin
    Puzzle(
        "The Pin",
        "White to move. Win the Knight.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Re1. The Rook lines up with the King.", Square(0, 0), Square(4, 0)),
            MoveStep("Move 2 (Black): a6. Black ignores the threat.", Square(0, 6), Square(0, 5)),
            MoveStep("Move 3 (White): Rxe5. The Knight is pinned and falls.", Square(4, 0), Square(4, 4))
        ),
        "Line up your Rook with the enemy King."
    ),
    // 14. The Back Rank Weakness
    Puzzle(
        "Back Rank Weakness",
        "White to move. Deliver mate.",
        mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Ra8+. The Rook invades the back rank.", Square(0, 0), Square(0, 7)),
            MoveStep("Move 2 (Black): The King is trapped by his own pawns. Checkmate!", Square(6, 7), Square(6, 7))
        ),
        "Exploit the weak back rank."
    ),
    // 15. The Queen Fork
    Puzzle(
        "The Queen Fork",
        "White to move. Win the Rook.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(7, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(3, 3) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Qd4. The Queen steps into the center.", Square(3, 3), Square(3, 4)),
            MoveStep("Move 2 (Black): a6. Black ignores the threat.", Square(0, 6), Square(0, 5)),
            MoveStep("Move 3 (White): Qh8+. The Queen forks the King and Rook!", Square(3, 4), Square(7, 7))
        ),
        "Attack the King and Rook at the same time."
    ),
    // 16. The Knight Check
    Puzzle(
        "The Knight Check",
        "White to move. Win the Bishop.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(2, 6) to Piece(PieceType.BISHOP, PieceColor.BLACK),
            Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Nf6+. The Knight checks the King.", Square(4, 4), Square(5, 5)),
            MoveStep("Move 2 (Black): Kf7. The King must move.", Square(4, 7), Square(5, 6)),
            MoveStep("Move 3 (White): Nxh7. The Knight takes the Bishop next.", Square(5, 5), Square(7, 6))
        ),
        "Use the Knight to check and take."
    ),
    // 17. The Rook Lift
    Puzzle(
        "The Rook Lift",
        "White to move. Attack the King.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 1) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Ra3. The Rook lifts to the 3rd rank.", Square(0, 0), Square(0, 2)),
            MoveStep("Move 2 (Black): a6. Black plays a waiting move.", Square(0, 6), Square(0, 5)),
            MoveStep("Move 3 (White): Re3+. The Rook checks the King.", Square(0, 2), Square(4, 2))
        ),
        "Lift the Rook to attack."
    ),
    // 18. The Double Attack
    Puzzle(
        "The Double Attack",
        "White to move. Win the Knight.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Square(3, 3) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Qd4. The Queen attacks the Knight.", Square(3, 3), Square(3, 4)),
            MoveStep("Move 2 (Black): a6. Black ignores the threat.", Square(0, 6), Square(0, 5)),
            MoveStep("Move 3 (White): Qxe5+. The Queen takes the Knight and checks the King.", Square(3, 4), Square(4, 4))
        ),
        "Attack two pieces with one move."
    ),
    // 19. The Pawn Push
    Puzzle(
        "The Pawn Push",
        "White to move. Win the Queen.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 4) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): e5. The pawn pushes forward.", Square(4, 4), Square(4, 5)),
            MoveStep("Move 2 (Black): a6. Black ignores the threat.", Square(0, 6), Square(0, 5)),
            MoveStep("Move 3 (White): exd6. The pawn captures the Queen!", Square(4, 5), Square(3, 6))
        ),
        "Push your pawns to attack."
    ),
    // 20. The Bishop Pin
    Puzzle(
        "The Bishop Pin",
        "White to move. Win the Knight.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Square(5, 5) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Nf6+. The Knight checks the King.", Square(4, 4), Square(5, 5)),
            MoveStep("Move 2 (Black): Kf7. The King is forced to move.", Square(4, 7), Square(5, 6)),
            MoveStep("Move 3 (White): Nxh7. The Knight takes the Bishop.", Square(5, 5), Square(7, 6))
        ),
        "Use your pieces to pin the enemy."
    ),
    // 21. The Queen Sacrifice
    Puzzle(
        "The Queen Sacrifice",
        "White to move. Sacrifice your Queen.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(3, 5) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(5, 5) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Qxf7+. The Queen sacrifices herself to force the King.", Square(3, 5), Square(5, 6)),
            MoveStep("Move 2 (Black): Kxf7. The King takes the Queen.", Square(4, 7), Square(5, 6)),
            MoveStep("Move 3 (White): Bd5+. The Bishop checks the King and wins the game.", Square(5, 5), Square(3, 3))
        ),
        "Sacrifice the Queen to set up a deadly trap."
    ),
    // 22. The Rook Fork
    Puzzle(
        "The Rook Fork",
        "White to move. Win the Queen.",
        mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        listOf(
            MoveStep("Move 1 (White): Ra8+. The Rook checks the King.", Square(0, 7), Square(0, 7)),
            MoveStep("Move 2 (Black): Kd7. The King moves away.", Square(4, 7), Square(3, 6)),
            MoveStep("Move 3 (White): Rxa8. The Rook takes the Queen.", Square(0, 7), Square(0, 7))
        ),
        "Use your Rook to control the file."
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
                tts?.setPitch(1.15f)
                tts?.setSpeechRate(0.95f)
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
    val moves = currentPuzzle.solution

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
                delay(2500L)
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
