package com.example.paochess.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

data class FamousGame(
    val name: String,
    val description: String,
    val moves: List<MoveStep>
)

val FAMOUS_GAMES = listOf(
    FamousGame(
        "The Opera Game (Morphy, 1858)",
        "Paul Morphy demolishes the Duke and Count with a brilliant queen sacrifice.",
        listOf(
            MoveStep("Move 1: e4. White claims the center.", Square(4, 1), Square(4, 3)),
            MoveStep("Move 2: Nf3. Developing the knight.", Square(6, 0), Square(5, 2)),
            MoveStep("Move 3: Bc4. The Bishop targets f7.", Square(5, 0), Square(2, 3)),
            MoveStep("Move 4: Qb3. Attacking the f7 pawn.", Square(3, 0), Square(1, 2)),
            // Add the full 17-move sequence here...
            MoveStep("Move 16: Qb8+. The final sacrifice!", Square(3, 3), Square(1, 7)),
            MoveStep("Move 17: Nd6#. The Knight delivers mate!", Square(2, 2), Square(3, 5))
        )
    )
)

@Composable
fun GameViewerScreen() {
    var currentGameIndex by remember { mutableStateOf(0) }
    var currentStep by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentBoard by remember { mutableStateOf(getInitialBoard()) }

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setPitch(1.15f)
                isTtsReady = true
            }
        }
    }

    val game = FAMOUS_GAMES[currentGameIndex]

    LaunchedEffect(isPlaying, currentStep) {
        if (isPlaying) {
            if (currentStep < game.moves.size) {
                val move = game.moves[currentStep]
                if (isTtsReady) tts?.speak(move.description, TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3000L)
                currentBoard = applyMove(currentBoard, move.from, move.to)
                currentStep++
                delay(1000L)
            } else {
                isPlaying = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("🎥 Famous Games", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(game.name, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(game.description, color = LightGrey, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        if (currentStep > 0 && currentStep <= game.moves.size) {
            Text(game.moves[currentStep - 1].description, color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("Tap Play to watch the game with coach explanations.", color = LightGrey, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
            InteractiveBoard(
                initialBoard = getInitialBoard(),
                moves = game.moves,
                currentStep = currentStep
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { currentStep = 0; isPlaying = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))) { Text("Reset") }
            Button(onClick = { isPlaying = !isPlaying }, colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black)) {
                Text(if (isPlaying) "Pause" else "Play Game")
            }
            Button(onClick = { if (currentStep < game.moves.size) currentStep++ }, colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black)) {
                Text("Next Move")
            }
        }
    }
}
