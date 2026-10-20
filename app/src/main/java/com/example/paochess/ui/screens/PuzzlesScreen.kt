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
    // ... (Puzzles list remains the same as before) ...
    Puzzle("Back Rank Mate", "White to move. Checkmate in 1.", mapOf(Square(4, 0) to Piece(PieceType.KING, PieceColor.BLACK), Square(0, 1) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(1, 1) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(2, 1) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(4, 7) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Re8#", Square(0, 7), Square(4, 7)), "Move the Rook to the back rank."),
    Puzzle("Queen Mate", "White to move. Checkmate in 1.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(3, 6) to Piece(PieceType.QUEEN, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Qg7#", Square(3, 6), Square(3, 7)), "Place the Queen next to the King."),
    Puzzle("Scholar's Mate", "White to move. Checkmate in 1.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.WHITE), Square(5, 4) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Qxf7#", Square(3, 7), Square(5, 6)), "Attack the weak f7 square."),
    Puzzle("Fool's Mate", "Black to move. Checkmate in 1.", mapOf(Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE), Square(5, 1) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(6, 1) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(7, 2) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK), Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK)), MoveStep("Black plays Qh4#", Square(3, 7), Square(7, 3)), "Deliver mate on h4."),
    Puzzle("Smothered Mate", "White to move. Checkmate in 1.", mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 7) to Piece(PieceType.ROOK, PieceColor.BLACK), Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(5, 5) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Nf7#", Square(5, 5), Square(5, 7)), "Jump in to smother the King."),
    Puzzle("Arabian Mate", "White to move. Checkmate in 1.", mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(5, 6) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(6, 6) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Rh6#", Square(6, 6), Square(7, 6)), "Rook and Knight trap the King."),
    Puzzle("Anastasia's Mate", "White to move. Checkmate in 1.", mapOf(Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(5, 5) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(6, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Rh7#", Square(6, 7), Square(7, 7)), "Mate on the h-file."),
    Puzzle("Legal's Mate", "White to move. Checkmate in 1.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(2, 5) to Piece(PieceType.BISHOP, PieceColor.BLACK), Square(5, 4) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(3, 5) to Piece(PieceType.QUEEN, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Qxe5#", Square(3, 5), Square(4, 4)), "Sacrifice the Queen to mate."),
    Puzzle("Royal Fork", "White to move. Find the Knight fork.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK), Square(5, 6) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Nf6+", Square(5, 6), Square(5, 6)), "Fork King and Queen."),
    Puzzle("Absolute Pin", "White to move. Pin the piece.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(3, 6) to Piece(PieceType.KNIGHT, PieceColor.BLACK), Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Re8+", Square(0, 7), Square(4, 7)), "Pin the Knight to the King."),
    Puzzle("The Skewer", "White to move. Skewer the King.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK), Square(4, 1) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Bf6+", Square(4, 1), Square(4, 1)), "Skewer the King to the Queen."),
    Puzzle("Discovered Attack", "White to move. Reveal the attack.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(4, 3) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Nf6+", Square(4, 3), Square(5, 5)), "Move the Knight to reveal the Rook."),
    Puzzle("Double Check", "White to move. Deliver double check.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(5, 4) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Nf6+", Square(5, 4), Square(5, 4)), "Check with Knight and Rook."),
    Puzzle("The Trap", "White to move. Trap the Queen.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(4, 3) to Piece(PieceType.QUEEN, PieceColor.BLACK), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE), Square(0, 2) to Piece(PieceType.BISHOP, PieceColor.WHITE)), MoveStep("White plays Re1", Square(0, 2), Square(4, 3)), "Trap the Queen."),
    Puzzle("Bishop & Knight", "White to move. Checkmate in 1.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(2, 5) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(5, 5) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Nd6#", Square(5, 5), Square(5, 6)), "Bishop and Knight cover escapes."),
    Puzzle("Exchange Sacrifice", "White to move. Sacrifice the Rook.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(3, 5) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Rxg7+", Square(0, 7), Square(6, 7)), "Sacrifice the Rook on g7."),
    Puzzle("Queen Back Rank", "White to move. Checkmate in 1.", mapOf(Square(4, 0) to Piece(PieceType.KING, PieceColor.BLACK), Square(0, 1) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(1, 1) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(2, 1) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(3, 3) to Piece(PieceType.QUEEN, PieceColor.WHITE), Square(4, 7) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Qe8#", Square(3, 3), Square(4, 0)), "Mate on the back rank."),
    Puzzle("The Windmill", "White to move. Begin the windmill.", mapOf(Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK), Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(6, 5) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(7, 4) to Piece(PieceType.PAWN, PieceColor.BLACK), Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(3, 6) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)), MoveStep("White plays Nf6+", Square(3, 6), Square(4, 7)), "Start the discovered checks.")
)

@Composable
fun PuzzlesScreen() {
    var currentPuzzleIndex by remember { mutableStateOf(0) }
    var currentStep by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    
    // TTS State
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // Initialize TTS
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsReady = true
            }
        }
    }

    // Clean up TTS
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val currentPuzzle = PUZZLES[currentPuzzleIndex]
    val moves = listOf(currentPuzzle.solution)

    // Play Sound + Speak when step changes
    LaunchedEffect(currentStep) {
        if (currentStep > 0 && currentStep <= moves.size) {
            val moveDesc = moves[currentStep - 1].description
            if (isTtsReady) {
                tts?.speak(moveDesc, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    LaunchedEffect(isPlaying, currentStep) {
        if (isPlaying) {
            if (currentStep < moves.size) {
                delay(2500L)
                currentStep++
            } else {
                isPlaying = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🧩 Puzzles Teacher", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(currentPuzzle.name, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(currentPuzzle.description, color = LightGrey, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        if (currentStep > 0 && currentStep <= moves.size) {
            Text(moves[currentStep - 1].description, color = Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            Button(onClick = { currentStep = 0; isPlaying = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))) {
                Text("Reset")
            }
            Button(onClick = { currentStep = 0; isPlaying = true }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black)) {
                Text("Replay")
            }
            Button(onClick = { isPlaying = !isPlaying }, colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black)) {
                Text(if (isPlaying) "Pause" else "Play Solution")
            }
            Button(onClick = { if (currentStep < moves.size) currentStep++ }, colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)) {
                Text("Next Move")
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            currentPuzzleIndex = (currentPuzzleIndex + 1) % PUZZLES.size
            currentStep = 0
            isPlaying = false
        }, colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black), modifier = Modifier.fillMaxWidth()) {
            Text("Next Puzzle")
        }

        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A1E)), shape = RoundedCornerShape(10.dp)) {
            Text("💡 Hint: ${currentPuzzle.hint}", color = Green, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
        }
    }
}
