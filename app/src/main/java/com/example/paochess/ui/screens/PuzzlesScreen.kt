package com.example.paochess.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.*
import com.example.paochess.ui.theme.*
import kotlinx.coroutines.delay

data class Puzzle(
    val name: String,
    val description: String,
    val board: Map<Square, Piece>,
    val solution: MoveStep,
    val hint: String
)

val PUZZLES = listOf(
    // 1. Back Rank Mate
    Puzzle(
        name = "Back Rank Mate",
        description = "White to move. Deliver checkmate in 1.",
        board = mapOf(
            Square(4, 0) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(1, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(2, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Re8#", Square(0, 7), Square(4, 7)),
        hint = "Move the Rook to the back rank to trap the King."
    ),
    // 2. Queen Mate
    Puzzle(
        name = "Queen Mate",
        description = "White to move. Checkmate in 1.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Qg7#", Square(3, 6), Square(3, 7)),
        hint = "Place the Queen next to the King on the edge."
    ),
    // 3. Scholar's Mate
    Puzzle(
        name = "Scholar's Mate",
        description = "White to move. Deliver checkmate in 1 on f7.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(5, 4) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Qxf7#", Square(3, 7), Square(5, 6)),
        hint = "The Queen and Bishop team up to attack the weak f7 square."
    ),
    // 4. Fool's Mate
    Puzzle(
        name = "Fool's Mate",
        description = "Black to move. Checkmate in 1.",
        board = mapOf(
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(5, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(6, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(7, 2) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK)
        ),
        solution = MoveStep("Black plays Qh4#", Square(3, 7), Square(7, 3)),
        hint = "The Queen delivers mate on h4, exploiting the exposed King."
    ),
    // 5. Smothered Mate
    Puzzle(
        name = "Smothered Mate",
        description = "White to move. Checkmate in 1.",
        board = mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(6, 7) to Piece(PieceType.ROOK, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(5, 5) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Nf7#", Square(5, 5), Square(5, 7)),
        hint = "The Knight jumps in to deliver mate while the King is smothered by its own pieces."
    ),
    // 6. Arabian Mate
    Puzzle(
        name = "Arabian Mate",
        description = "White to move. Checkmate in 1.",
        board = mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(6, 6) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Rh6#", Square(6, 6), Square(7, 6)),
        hint = "The Rook and Knight work together to trap the King in the corner."
    ),
    // 7. Anastasia's Mate
    Puzzle(
        name = "Anastasia's Mate",
        description = "White to move. Checkmate in 1.",
        board = mapOf(
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(5, 5) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(6, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Rh7#", Square(6, 7), Square(7, 7)),
        hint = "The Rook delivers mate on the h-file while the Knight controls the escape squares."
    ),
    // 8. Legal's Mate
    Puzzle(
        name = "Legal's Mate",
        description = "White to move. Checkmate in 1.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(2, 5) to Piece(PieceType.BISHOP, PieceColor.BLACK),
            Square(5, 4) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(3, 5) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Qxe5#", Square(3, 5), Square(4, 4)),
        hint = "The Queen sacrifices herself to deliver mate, protected by the Bishop."
    ),
    // 9. The Fork
    Puzzle(
        name = "Royal Fork",
        description = "White to move. Find the Knight fork.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Nf6+", Square(5, 6), Square(5, 6)),
        hint = "Knight moves to f6, checking the King and attacking the Queen."
    ),
    // 10. The Pin
    Puzzle(
        name = "The Absolute Pin",
        description = "White to move. Pin the piece to the King.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Re8+", Square(0, 7), Square(4, 7)),
        hint = "The Rook pins the Knight to the King."
    ),
    // 11. The Skewer
    Puzzle(
        name = "The Skewer",
        description = "White to move. Skewer the King to the Queen.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 7) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 1) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Bf6+", Square(4, 1), Square(4, 1)),
        hint = "Bishop checks the King, forcing it to move and exposing the Queen."
    ),
    // 12. Discovered Attack
    Puzzle(
        name = "Discovered Attack",
        description = "White to move. Move the Knight to reveal the Rook's attack.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 3) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Nf6+", Square(4, 3), Square(5, 5)),
        hint = "Move the Knight to reveal the Rook's attack on the King."
    ),
    // 13. Double Check
    Puzzle(
        name = "Double Check",
        description = "White to move. Deliver double check.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(5, 4) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Nf6+", Square(5, 4), Square(5, 4)),
        hint = "Move the Knight to check the King with both the Knight and the Rook."
    ),
    // 14. The Trap
    Puzzle(
        name = "The Trap",
        description = "White to move. Trap the Queen.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(4, 3) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(0, 2) to Piece(PieceType.BISHOP, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Re1", Square(0, 2), Square(4, 3)),
        hint = "Move the Bishop to trap the Queen."
    ),
    // 15. Mate in 1 - Bishop and Knight
    Puzzle(
        name = "Bishop & Knight Mate",
        description = "White to move. Checkmate in 1.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(2, 5) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(5, 5) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Nd6#", Square(5, 5), Square(5, 6)),
        hint = "Use the Bishop and Knight to cover all escape squares."
    ),
    // 16. The Exchange Sacrifice
    Puzzle(
        name = "Exchange Sacrifice",
        description = "White to move. Sacrifice the Rook to mate.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(3, 5) to Piece(PieceType.BISHOP, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Rxg7+", Square(0, 7), Square(6, 7)),
        hint = "Sacrifice the Rook on g7 to draw the King out."
    ),
    // 17. Back Rank Mate with Queen
    Puzzle(
        name = "Queen Back Rank",
        description = "White to move. Checkmate in 1.",
        board = mapOf(
            Square(4, 0) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(1, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(2, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(3, 3) to Piece(PieceType.QUEEN, PieceColor.WHITE),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Qe8#", Square(3, 3), Square(4, 0)),
        hint = "Use the Queen to deliver mate on the back rank."
    ),
    // 18. The Windmill
    Puzzle(
        name = "The Windmill",
        description = "White to move. Begin the windmill.",
        board = mapOf(
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 5) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 4) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(3, 6) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        ),
        solution = MoveStep("White plays Nf6+", Square(3, 6), Square(4, 7)),
        hint = "The Knight checks the King, starting a series of discovered checks."
    )
)

@Composable
fun PuzzlesScreen() {
    var currentPuzzleIndex by remember { mutableStateOf(0) }
    var currentStep by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }

    val currentPuzzle = PUZZLES[currentPuzzleIndex]
    val moves = listOf(currentPuzzle.solution)

    LaunchedEffect(isPlaying, currentStep) {
        if (isPlaying) {
            if (currentStep < moves.size) {
                delay(1500L)
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

        if (currentStep > 0) {
            Text(moves[currentStep - 1].description, color = Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("Find the checkmate move!", color = LightGrey, fontSize = 14.sp)
        }
        Spacer(Modifier.height(16.dp))

        InteractiveBoard(
            initialBoard = currentPuzzle.board,
            moves = moves,
            isPlaying = isPlaying,
            currentStep = currentStep,
            onStepChanged = { currentStep = it }
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { currentStep = 0; isPlaying = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))) {
                Text("Reset")
            }
            Button(onClick = { isPlaying = !isPlaying }, colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black)) {
                Text(if (isPlaying) "Pause" else "Play Solution")
            }
            Button(onClick = { if (currentStep < moves.size) currentStep++ }, colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                Text("Next Move")
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showHint = !showHint }, colors = ButtonDefaults.buttonColors(containerColor = DarkSurface)) {
                Text(if (showHint) "Hide Hint" else "💡 Hint", color = White)
            }
            Button(onClick = {
                currentPuzzleIndex = (currentPuzzleIndex + 1) % PUZZLES.size
                currentStep = 0
                isPlaying = false
            }, colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black)) {
                Text("Next Puzzle")
            }
        }

        if (showHint) {
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A1E))) {
                Text(currentPuzzle.hint, color = Green, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
            }
        }
    }
}
