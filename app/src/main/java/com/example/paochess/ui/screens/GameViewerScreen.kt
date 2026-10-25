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
        "Paul Morphy vs the Duke of Brunswick. 17 moves. Morphy wins with a brilliant Queen sacrifice.",
        listOf(
            MoveStep("Morphy plays e4, claiming the center.", Square(4, 1), Square(4, 3)),
            MoveStep("The Duke replies with e5, mirroring.", Square(4, 6), Square(4, 4)),
            MoveStep("Morphy develops Nf3, attacking e5.", Square(6, 0), Square(5, 2)),
            MoveStep("The Duke plays d6, a passive move.", Square(3, 6), Square(3, 5)),
            MoveStep("Morphy plays d4, striking the center.", Square(3, 1), Square(3, 3)),
            MoveStep("The Duke plays Bg4, pinning the Knight.", Square(2, 7), Square(6, 3)),
            MoveStep("Morphy plays dxe5, capturing.", Square(3, 3), Square(4, 4)),
            MoveStep("The Duke plays Bxf3, capturing the Knight.", Square(6, 3), Square(5, 2)),
            MoveStep("Morphy recaptures with Qxf3.", Square(3, 0), Square(5, 2)),
            MoveStep("The Duke plays dxe5, taking back.", Square(3, 5), Square(4, 4)),
            MoveStep("Morphy plays Bc4, aiming at f7.", Square(5, 0), Square(2, 3)),
            MoveStep("The Duke plays Nf6, developing.", Square(6, 7), Square(5, 5)),
            MoveStep("Morphy plays Qb3, attacking f7.", Square(5, 2), Square(1, 2)),
            MoveStep("The Duke defends with Qe7.", Square(3, 7), Square(4, 6)),
            MoveStep("Morphy develops Nc3.", Square(1, 0), Square(2, 2)),
            MoveStep("The Duke plays c6.", Square(2, 6), Square(2, 5)),
            MoveStep("Morphy plays Bg5, pinning the Knight.", Square(2, 0), Square(6, 4)),
            MoveStep("The Duke plays b5, attacking the Bishop.", Square(1, 6), Square(1, 4)),
            MoveStep("Morphy sacrifices with Nxb5!", Square(2, 2), Square(1, 4)),
            MoveStep("The Duke plays cxb5, taking.", Square(2, 5), Square(1, 4)),
            MoveStep("Morphy plays Bxb5+, checking.", Square(2, 3), Square(1, 4)),
            MoveStep("The Duke blocks with Nbd7.", Square(1, 7), Square(3, 6)),
            MoveStep("Morphy castles queenside (O-O-O).", Square(4, 0), Square(2, 0)),
            MoveStep("The Duke plays Rd8.", Square(0, 7), Square(3, 7)),
            MoveStep("Morphy plays Rxd7!", Square(3, 0), Square(3, 6)),
            MoveStep("The Duke plays Rxd7, recapturing.", Square(3, 7), Square(3, 6)),
            MoveStep("Morphy plays Rd1, targeting the Queen.", Square(7, 0), Square(3, 0)),
            MoveStep("The Duke defends with Qe6.", Square(4, 6), Square(4, 5)),
            MoveStep("Morphy plays Bxd7+, checking.", Square(1, 4), Square(3, 6)),
            MoveStep("The Duke recaptures with Nxd7.", Square(5, 5), Square(3, 6)),
            MoveStep("Morphy sacrifices his Queen with Qb8+!", Square(1, 2), Square(1, 7)),
            MoveStep("The Duke takes with Nxb8.", Square(3, 6), Square(1, 7)),
            MoveStep("Morphy delivers mate with Rd8#!", Square(3, 0), Square(3, 7))
        )
    ),
    FamousGame(
        "The Immortal Game (Anderssen, 1851)",
        "Anderssen vs. Kieseritzky. Sacrifices both Rooks, a Bishop, and his Queen to deliver mate.",
        listOf(
            MoveStep("White plays e4", Square(4, 1), Square(4, 3)),
            MoveStep("Black plays e5", Square(4, 6), Square(4, 4)),
            MoveStep("White plays f4", Square(5, 1), Square(5, 3)),
            MoveStep("Black plays exf4, capturing on f4", Square(4, 4), Square(5, 3)),
            MoveStep("White plays Bc4", Square(5, 0), Square(2, 3)),
            MoveStep("Black plays Qh4+", Square(3, 7), Square(7, 3)),
            MoveStep("White plays Kf1", Square(4, 0), Square(5, 0)),
            MoveStep("Black plays b5", Square(1, 6), Square(1, 4)),
            MoveStep("White plays Bxb5, capturing on b5", Square(2, 3), Square(1, 4)),
            MoveStep("Black plays Nf6", Square(6, 7), Square(5, 5)),
            MoveStep("White plays Nf3", Square(6, 0), Square(5, 2)),
            MoveStep("Black plays Qh6", Square(7, 3), Square(7, 5)),
            MoveStep("White plays d3", Square(3, 1), Square(3, 2)),
            MoveStep("Black plays Nh5", Square(5, 5), Square(7, 4)),
            MoveStep("White plays Nh4", Square(5, 2), Square(7, 3)),
            MoveStep("Black plays Qg5", Square(7, 5), Square(6, 4)),
            MoveStep("White plays Nf5", Square(7, 3), Square(5, 4)),
            MoveStep("Black plays c6", Square(2, 6), Square(2, 5)),
            MoveStep("White plays g4", Square(6, 1), Square(6, 3)),
            MoveStep("Black plays Nf6", Square(7, 4), Square(5, 5)),
            MoveStep("White plays Rg1", Square(7, 0), Square(6, 0)),
            MoveStep("Black plays cxb5, capturing on b5", Square(2, 5), Square(1, 4)),
            MoveStep("White plays h4", Square(7, 1), Square(7, 3)),
            MoveStep("Black plays Qg6", Square(6, 4), Square(6, 5)),
            MoveStep("White plays h5", Square(7, 3), Square(7, 4)),
            MoveStep("Black plays Qg5", Square(6, 5), Square(6, 4)),
            MoveStep("White plays Qf3", Square(3, 0), Square(5, 2)),
            MoveStep("Black plays Ng8", Square(5, 5), Square(6, 7)),
            MoveStep("White plays Bxf4, capturing on f4", Square(2, 0), Square(5, 3)),
            MoveStep("Black plays Qf6", Square(6, 4), Square(5, 5)),
            MoveStep("White plays Nc3", Square(1, 0), Square(2, 2)),
            MoveStep("Black plays Bc5", Square(5, 7), Square(2, 4)),
            MoveStep("White plays Nd5", Square(2, 2), Square(3, 4)),
            MoveStep("Black plays Qxb2, capturing on b2", Square(5, 5), Square(1, 1)),
            MoveStep("White plays Bd6", Square(5, 3), Square(3, 5)),
            MoveStep("Black plays Bxg1, capturing on g1", Square(2, 4), Square(6, 0)),
            MoveStep("White plays e5", Square(4, 3), Square(4, 4)),
            MoveStep("Black plays Qxa1+, capturing on a1", Square(1, 1), Square(0, 0)),
            MoveStep("White plays Ke2", Square(5, 0), Square(4, 1)),
            MoveStep("Black plays Na6", Square(1, 7), Square(0, 5)),
            MoveStep("White plays Nxg7+, capturing on g7", Square(5, 4), Square(6, 6)),
            MoveStep("Black plays Kd8", Square(4, 7), Square(3, 7)),
            MoveStep("White plays Qf6+", Square(5, 2), Square(5, 5)),
            MoveStep("Black plays Nxf6, capturing on f6", Square(6, 7), Square(5, 5)),
            MoveStep("White plays Be7#", Square(3, 5), Square(4, 6))
        )
    ),
    FamousGame(
        "The Evergreen Game (Anderssen, 1852)",
        "Anderssen vs. Dufresne. Another brilliant attacking masterpiece ending in Bishop mate.",
        listOf(
            MoveStep("White plays e4", Square(4, 1), Square(4, 3)),
            MoveStep("Black plays e5", Square(4, 6), Square(4, 4)),
            MoveStep("White plays Nf3", Square(6, 0), Square(5, 2)),
            MoveStep("Black plays Nc6", Square(1, 7), Square(2, 5)),
            MoveStep("White plays Bc4", Square(5, 0), Square(2, 3)),
            MoveStep("Black plays Bc5", Square(5, 7), Square(2, 4)),
            MoveStep("White plays b4", Square(1, 1), Square(1, 3)),
            MoveStep("Black plays Bxb4, capturing on b4", Square(2, 4), Square(1, 3)),
            MoveStep("White plays c3", Square(2, 1), Square(2, 2)),
            MoveStep("Black plays Ba5", Square(1, 3), Square(0, 4)),
            MoveStep("White plays d4", Square(3, 1), Square(3, 3)),
            MoveStep("Black plays exd4, capturing on d4", Square(4, 4), Square(3, 3)),
            MoveStep("White castles kingside (O-O)", Square(4, 0), Square(6, 0)),
            MoveStep("Black plays d3", Square(3, 3), Square(3, 2)),
            MoveStep("White plays Qb3", Square(3, 0), Square(1, 2)),
            MoveStep("Black plays Qf6", Square(3, 7), Square(5, 5)),
            MoveStep("White plays e5", Square(4, 3), Square(4, 4)),
            MoveStep("Black plays Qg6", Square(5, 5), Square(6, 5)),
            MoveStep("White plays Re1", Square(5, 0), Square(4, 0)),
            MoveStep("Black plays Nge7", Square(6, 7), Square(4, 6)),
            MoveStep("White plays Ba3", Square(2, 0), Square(0, 2)),
            MoveStep("Black plays b5", Square(1, 6), Square(1, 4)),
            MoveStep("White plays Qxb5, capturing on b5", Square(1, 2), Square(1, 4)),
            MoveStep("Black plays Rb8", Square(0, 7), Square(1, 7)),
            MoveStep("White plays Qa4", Square(1, 4), Square(0, 3)),
            MoveStep("Black plays Bb6", Square(0, 4), Square(1, 5)),
            MoveStep("White plays Nbd2", Square(1, 0), Square(3, 1)),
            MoveStep("Black plays Bb7", Square(2, 7), Square(1, 6)),
            MoveStep("White plays Ne4", Square(3, 1), Square(4, 3)),
            MoveStep("Black plays Qf5", Square(6, 5), Square(5, 4)),
            MoveStep("White plays Bxd3, capturing on d3", Square(2, 3), Square(3, 2)),
            MoveStep("Black plays Qh5", Square(5, 4), Square(7, 4)),
            MoveStep("White plays Nf6+", Square(4, 3), Square(5, 5)),
            MoveStep("Black plays gxf6, capturing on f6", Square(6, 6), Square(5, 5)),
            MoveStep("White plays exf6, capturing on f6", Square(4, 4), Square(5, 5)),
            MoveStep("Black plays Rg8", Square(7, 7), Square(6, 7)),
            MoveStep("White plays Rad1", Square(0, 0), Square(3, 0)),
            MoveStep("Black plays Qxf3, capturing on f3", Square(7, 4), Square(5, 2)),
            MoveStep("White plays Rxe7+, capturing on e7", Square(4, 0), Square(4, 6)),
            MoveStep("Black plays Nxe7, capturing on e7", Square(2, 5), Square(4, 6)),
            MoveStep("White plays Qxd7+, capturing on d7", Square(0, 3), Square(3, 6)),
            MoveStep("Black plays Kxd7, capturing on d7", Square(4, 7), Square(3, 6)),
            MoveStep("White plays Bf5+", Square(3, 2), Square(5, 4)),
            MoveStep("Black plays Ke8", Square(3, 6), Square(4, 7)),
            MoveStep("White plays Bd7+", Square(5, 4), Square(3, 6)),
            MoveStep("Black plays Kf8", Square(4, 7), Square(5, 7)),
            MoveStep("White plays Bxe7#, capturing on e7", Square(0, 2), Square(4, 6))
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
    val speechSignalHolder = remember { SpeechSignalHolder() }

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
                
                tts?.setPitch(1.15f) // Same as Puzzles coach
                tts?.setSpeechRate(0.95f) // Same as Puzzles coach
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { isSpeaking = true }
                    override fun onDone(utteranceId: String?) { 
                        isSpeaking = false
                        speechSignalHolder.complete()
                    }
                    override fun onError(utteranceId: String?) { 
                        isSpeaking = false
                        speechSignalHolder.complete()
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
                val completionSignal = speechSignalHolder.newSignal()
                
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
                    kotlinx.coroutines.withTimeout(15000L) {
                        completionSignal.await()
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    // Voice took too long or failed — fall through to floor
                }
                
                // 4. FLOOR: minimum wait based on text length (~15 chars/second, 3s-15s)
                val textLen = if (isTtsReady) {
                    if (currentStep == 0) 
                        "This is one of the most famous games in chess history. ${game.description} ${moveToPlay.description}".length
                    else moveToPlay.description.length
                } else 0
                val estimatedMs = (textLen / 15.0 * 1000).toLong().coerceIn(3000L, 15000L)
                
                val elapsed = System.currentTimeMillis() - moveStartTime
                if (elapsed < estimatedMs) {
                    delay(estimatedMs - elapsed)
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
        
        // Game selector — one button per famous game
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FAMOUS_GAMES.forEachIndexed { idx, g ->
                Button(
                    onClick = {
                        if (currentGameIndex != idx) {
                            currentGameIndex = idx
                            currentStep = 0
                            isPlaying = false
                            tts?.stop()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentGameIndex == idx) Orange else DarkSurface,
                        contentColor = if (currentGameIndex == idx) Color.Black else White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        g.name,
                        fontSize = 12.sp,
                        fontWeight = if (currentGameIndex == idx) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        Text(game.name, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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


/**
 * Thread-safe holder for a speech-completion signal.
 * Uses @Volatile so the TTS thread sees new signals immediately
 * (Compose mutableStateOf doesn't publish cross-thread reliably).
 */
class SpeechSignalHolder {
    @Volatile
    private var current: kotlinx.coroutines.CompletableDeferred<Unit>? = null
    
    fun newSignal(): kotlinx.coroutines.CompletableDeferred<Unit> {
        val signal = kotlinx.coroutines.CompletableDeferred<Unit>()
        current = signal
        return signal
    }
    
    fun complete() {
        current?.complete(Unit)
        current = null
    }
}
