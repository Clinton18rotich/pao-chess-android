package com.example.paochess.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

data class Opening(
    val name: String,
    val description: String,
    val moves: List<MoveStep>
)

fun parseSquare(algebraic: String): Square {
    val file = algebraic[0] - 'a'
    val rank = algebraic[1] - '1'
    return Square(file, rank)
}

val OPENINGS = listOf(
    Opening(
        "Italian Game",
        "A classic opening for White that fights for the center and aims at the weak f7 square.",
        listOf(
            MoveStep("White plays e4 — the King's pawn advances two squares, opening the diagonal for the Queen and the f1 Bishop, and grabbing central space on d5 and f5.", parseSquare("e2"), parseSquare("e4")),
            MoveStep("Black plays e5 — mirroring White, fighting for the center and opening lines for Black's own Queen and Bishop.", parseSquare("e7"), parseSquare("e5")),
            MoveStep("White plays Nf3 — the Knight develops and immediately attacks the Black e5 pawn.", parseSquare("g1"), parseSquare("f3")),
            MoveStep("Black plays Nc6 — the Knight defends the e5 pawn and adds pressure to the d4 square.", parseSquare("b8"), parseSquare("c6")),
            MoveStep("White plays Bc4 — the Bishop eyes the f7 square, the weakest point in Black's camp. This is the Italian Game.", parseSquare("f1"), parseSquare("c4"))
        )
    ),
    Opening(
        "Sicilian Defense",
        "The most popular response to 1.e4 — Black fights for the center asymmetrically, avoiding symmetry.",
        listOf(
            MoveStep("White plays e4 — the King's pawn takes the center.", parseSquare("e2"), parseSquare("e4")),
            MoveStep("Black plays c5 — instead of mirroring, Black attacks d4 and creates an unbalanced, counter-attacking position.", parseSquare("c7"), parseSquare("c5"))
        )
    ),
    Opening(
        "Queen's Gambit",
        "A classical opening where White offers a pawn to lure Black's center pawn away.",
        listOf(
            MoveStep("White plays d4 — the Queen's pawn claims central space and opens the c1 Bishop's diagonal.", parseSquare("d2"), parseSquare("d4")),
            MoveStep("Black plays d5 — Black stakes an equal claim in the center and blocks White's pawn.", parseSquare("d7"), parseSquare("d5")),
            MoveStep("White plays c4 — the gambit: White offers the c-pawn to deflect Black's d5 pawn and dominate the center.", parseSquare("c2"), parseSquare("c4"))
        )
    )
)

@Composable
fun OpeningsScreen() {
    var selectedOpening by remember { mutableStateOf(OPENINGS[0]) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }

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
    LaunchedEffect(currentStep) {
        if (currentStep in 1..selectedOpening.moves.size && isTtsReady) {
            tts?.speak(selectedOpening.moves[currentStep - 1].description,
                TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    LaunchedEffect(isPlaying, currentStep) {
        if (isPlaying) {
            if (currentStep < selectedOpening.moves.size) {
                // Announce the upcoming move FIRST, then wait
                if (isTtsReady) {
                    tts?.speak(
                        selectedOpening.moves[currentStep].description,
                        TextToSpeech.QUEUE_FLUSH, null, null
                    )
                }
                delay(3000L)
                currentStep++
            } else {
                isPlaying = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)
        .verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("📖 Openings Teacher", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OPENINGS.forEach { opening ->
                Button(
                    onClick = { selectedOpening = opening; currentStep = 0; isPlaying = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedOpening == opening) Orange else DarkSurface,
                        contentColor = if (selectedOpening == opening) Color.Black else White
                    ),
                    contentPadding = PaddingValues(8.dp)
                ) { Text(opening.name, fontSize = 11.sp) }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(selectedOpening.description, color = LightGrey, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (currentStep in 1..selectedOpening.moves.size) {
            Text(selectedOpening.moves[currentStep - 1].description, color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("Tap 'Start Teacher' or 'Next Move' to step through the opening.", color = LightGrey, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))
        InteractiveBoard(
            initialBoard = getInitialBoard(),
            moves = selectedOpening.moves,
            currentStep = currentStep
        )
        Spacer(Modifier.height(16.dp))

        // Row 1
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(modifier = Modifier.weight(1f), onClick = { currentStep = 0; isPlaying = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))) { Text("Reset") }
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    currentStep = 0
                    isPlaying = true
                    if (isTtsReady) {
                        tts?.speak(
                            selectedOpening.moves[0].description,
                            TextToSpeech.QUEUE_FLUSH, null, null
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black)
            ) { Text("Replay") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { isPlaying = !isPlaying },
                enabled = isPlaying || currentStep < selectedOpening.moves.size,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF3A2A10),
                    disabledContentColor = LightGrey
                )
            ) {
                Text(if (isPlaying) "Pause" else "Start Teacher")
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = { if (currentStep < selectedOpening.moves.size) currentStep++ },
                enabled = currentStep < selectedOpening.moves.size,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF1E3A1E),
                    disabledContentColor = LightGrey
                )
            ) { Text("Next Move") }
        }
    }
}
