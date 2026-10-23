package com.example.paochess.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.flow.first
import java.util.Locale

data class FamousGame(
    val name: String,
    val description: String,
    val moves: List<MoveStep>
)

val FAMOUS_GAMES = listOf(
    FamousGame(
        "The Opera Game (Morphy, 1858)",
        "Paul Morphy vs. the Duke of Brunswick and Count Isouard. Played in a Paris opera box, this game is a masterclass in rapid development and punishing mistakes.",
        listOf(
            MoveStep("This is one of the most famous games in chess history. Paul Morphy, playing White, faces the Duke of Brunswick and Count Isouard. The game begins with Morphy playing e4, claiming the center and opening lines for his Queen and Bishop.", Square(4, 1), Square(4, 3)),
            MoveStep("The Duke replies with e5, copying Morphy's move to fight for the center.", Square(4, 6), Square(4, 4)),
            MoveStep("Morphy develops his Knight to f3, attacking the Duke's pawn on e5 and preparing to control the center.", Square(6, 0), Square(5, 2)),
            MoveStep("The Duke makes a passive move, developing his Bishop to c5. This is fine, but it leaves the f2 square slightly weak. Morphy will remember this.", Square(5, 7), Square(2, 4)),
            MoveStep("Morphy strikes with Bc4, aiming his Bishop directly at the weakest point in the Duke's camp — the f7 pawn.", Square(5, 0), Square(2, 3)),
            MoveStep("The Duke develops his Knight to c6, defending the center and adding pressure to d4. So far, the game looks balanced.", Square(1, 7), Square(2, 5)),
            MoveStep("Now Morphy makes a shocking move: he sacrifices his Knight on e5! This is a bold attacking idea. If the Duke takes the Knight, his position will collapse.", Square(5, 2), Square(4, 4)),
            MoveStep("The Duke makes a critical mistake. He captures the Knight with his own Knight on e5, falling straight into Morphy's trap. He should have been more cautious.", Square(2, 5), Square(4, 4)),
            MoveStep("Morphy unleashes his Queen to h5! The Queen now attacks the weak f7 square and threatens checkmate. The Duke's King is suddenly in serious danger.", Square(3, 0), Square(7, 4)),
            MoveStep("The Duke tries to defend with Queen to e7, blocking the checkmate threat. But it's too late — Morphy's attack is already in motion.", Square(3, 7), Square(4, 6)),
            MoveStep("Morphy sacrifices his Bishop on f7! This brilliant move drags the Duke's King out of safety and opens up the position for the final attack.", Square(2, 3), Square(5, 6)),
            MoveStep("The Duke is forced to capture the Bishop with his King. The King is now in the open, with no pawns to protect him.", Square(4, 7), Square(5, 6)),
            MoveStep("Morphy follows up with Queen takes f7! Another sacrifice — Morphy gives up his Queen to keep the attack alive and drag the King further into the open.", Square(7, 4), Square(5, 6)),
            MoveStep("The Duke must capture the Queen with his King. But now the King is completely exposed in the middle of the board.", Square(5, 6), Square(5, 6)),
            MoveStep("Morphy brings his Knight to e5 with check! The Knight attacks the King and Morphy's pieces are all swarming around.", Square(5, 2), Square(4, 4)),
            MoveStep("The Duke retreats his King to f6, running for safety. But there's nowhere to hide.", Square(5, 6), Square(5, 5)),
            MoveStep("Morphy plays Knight to g4 with check! Another check, and the King is forced to keep moving.", Square(5, 4), Square(6, 3)),
            MoveStep("The Duke's King moves to g5. The King is running deeper into Morphy's territory.", Square(5, 5), Square(6, 4)),
            MoveStep("Morphy checks again with Knight to f6! The King is being chased all over the board.", Square(6, 3), Square(5, 5)),
            MoveStep("The Duke moves his King to g6. He's still running, but his options are running out.", Square(6, 4), Square(6, 5)),
            MoveStep("Morphy brings his Bishop to c1 with check! A quiet but deadly discovered attack.", Square(2, 0), Square(2, 0)),
            MoveStep("The Duke moves his King to g7, hoping to find safety. But the storm isn't over.", Square(6, 5), Square(6, 6)),
            MoveStep("Morphy plays Rook to h3 with check! Every White piece is now attacking the Duke's King.", Square(7, 0), Square(7, 2)),
            MoveStep("The Duke makes his final mistake, capturing the Rook with his King. This allows Morphy to deliver the final blow.", Square(6, 6), Square(7, 2)),
            MoveStep("Morphy delivers the final blow with Queen to g4! Checkmate. This is one of the most brilliant attacking games in chess history. Morphy sacrificed his Queen, both Bishops, and a Rook to win.", Square(7, 4), Square(6, 3))
        )
    )
)

@Composable
fun GameViewerScreen() {
    var currentGameIndex by remember { mutableStateOf(0) }
    var currentStep by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var speechDoneSignal by remember { mutableStateOf<kotlinx.coroutines.CompletableDeferred<Unit>?>(null) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                
                // Enumerate all English voices
                try {
                    val voices = tts?.voices
                    if (voices != null) {
                        val englishVoices = voices.filter { it.locale.language == "en" }
                        android.util.Log.d("TTS_VOICES", "Total: ${voices.size}, English: ${englishVoices.size}")
                        for (v in englishVoices) {
                            android.util.Log.d("TTS_VOICES", "name=${v.name}, locale=${v.locale}, quality=${v.quality}")
                        }
                        
                        // Try to find a male voice using multiple patterns
                        val maleCandidates = englishVoices.filter { v ->
                            val n = v.name.lowercase()
                            (n.contains("male") && !n.contains("female")) ||
                            n.contains("#male") ||
                            n.contains("_male") ||
                            n.contains("iom") ||
                            n.contains("iob") ||
                            n.contains("gb-male") ||
                            n.contains("-m-")
                        }
                        
                        android.util.Log.d("TTS_VOICES", "Male candidates: ${maleCandidates.size}")
                        
                        if (maleCandidates.isNotEmpty()) {
                            tts?.voice = maleCandidates.first()
                            android.util.Log.d("TTS_VOICES", "Selected: ${maleCandidates.first().name}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TTS_VOICES", "Voice selection failed: ${e.message}")
                }
                
                tts?.setPitch(0.55f) // Deeper male narrator
                tts?.setSpeechRate(0.85f) // Slower for calm delivery
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { isSpeaking = true }
                    override fun onDone(utteranceId: String?) { 
                        isSpeaking = false
                        speechDoneSignal?.complete(Unit)
                    }
                    override fun onError(utteranceId: String?) { 
                        isSpeaking = false
                        speechDoneSignal?.complete(Unit)
                    }
                })
                isTtsReady = true
            }
        }
    }

    val game = FAMOUS_GAMES[currentGameIndex]

    // Auto-play with proper voice sync
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (currentStep < game.moves.size) {
                val moveToPlay = game.moves[currentStep]
                val moveStartTime = System.currentTimeMillis()
                
                // 1. Create completion signal BEFORE speaking
                val completionSignal = kotlinx.coroutines.CompletableDeferred<Unit>()
                speechDoneSignal = completionSignal
                
                // 2. Speak
                if (isTtsReady) {
                    val textToSpeak = if (currentStep == 0) {
                        "This is one of the most famous games in chess history. ${game.description} ${moveToPlay.description}"
                    } else {
                        moveToPlay.description
                    }
                    tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
                }
                
                // 3. Wait for voice to finish (60s safety timeout)
                try {
                    kotlinx.coroutines.withTimeout(60000L) {
                        completionSignal.await()
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    // Voice took too long or failed — fall through to floor
                }
                
                // 4. MINIMUM 4 SECOND FLOOR (safety net if voice finished fast or failed)
                val elapsed = System.currentTimeMillis() - moveStartTime
                if (elapsed < 4000L) {
                    delay(4000L - elapsed)
                }
                
                // 5. Move the piece
                delay(500L)
                currentStep++
                
                if (currentStep >= game.moves.size) {
                    isPlaying = false
                    break
                }
                
                delay(500L)
            }
            isPlaying = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("🎥 Famous Games", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(game.name, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(game.description, color = LightGrey, fontSize = 14.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(16.dp))

        if (currentStep > 0 && currentStep <= game.moves.size) {
            Text(game.moves[currentStep - 1].description, color = Green, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { currentStep = 0; isPlaying = false; tts?.stop() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))) { Text("Reset") }
            Button(onClick = { isPlaying = !isPlaying },
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black)) {
                Text(if (isPlaying) "Pause" else "Play Game")
            }
            Button(onClick = { if (currentStep < game.moves.size) currentStep++ },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black)) {
                Text("Next Move")
            }
        }
    }
}
