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
    Opening("Italian Game", "1. e4 e5 2. Nf3 Nc6 3. Bc4", listOf(
        MoveStep("White plays e4", parseSquare("e2"), parseSquare("e4")),
        MoveStep("Black plays e5", parseSquare("e7"), parseSquare("e5")),
        MoveStep("White plays Nf3", parseSquare("g1"), parseSquare("f3")),
        MoveStep("Black plays Nc6", parseSquare("b8"), parseSquare("c6")),
        MoveStep("White plays Bc4", parseSquare("f1"), parseSquare("c4"))
    )),
    Opening("Sicilian Defense", "1. e4 c5", listOf(
        MoveStep("White plays e4", parseSquare("e2"), parseSquare("e4")),
        MoveStep("Black plays c5", parseSquare("c7"), parseSquare("c5"))
    )),
    Opening("Queen's Gambit", "1. d4 d5 2. c4", listOf(
        MoveStep("White plays d4", parseSquare("d2"), parseSquare("d4")),
        MoveStep("Black plays d5", parseSquare("d7"), parseSquare("d5")),
        MoveStep("White plays c4", parseSquare("c2"), parseSquare("c4"))
    ))
)

@Composable
fun OpeningsScreen() {
    var selectedOpening by remember { mutableStateOf(OPENINGS[0]) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(0) }

    // TTS Setup
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
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Speak move description on step change
    LaunchedEffect(currentStep) {
        if (currentStep > 0 && currentStep <= selectedOpening.moves.size) {
            val moveDesc = selectedOpening.moves[currentStep - 1].description
            if (isTtsReady) {
                tts?.speak(moveDesc, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    // Auto-play loop
    LaunchedEffect(isPlaying, currentStep) {
        if (isPlaying) {
            if (currentStep < selectedOpening.moves.size) {
                delay(2500L)
                currentStep++
            } else {
                isPlaying = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("📖 Openings Teacher", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OPENINGS.forEach { opening ->
                Button(
                    onClick = { selectedOpening = opening; currentStep = 0; isPlaying = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedOpening == opening) Orange else DarkSurface, contentColor = if (selectedOpening == opening) Color.Black else White),
                    contentPadding = PaddingValues(8.dp)
                ) { Text(opening.name, fontSize = 11.sp) }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(selectedOpening.description, color = LightGrey, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (currentStep > 0 && currentStep <= selectedOpening.moves.size) {
            Text(selectedOpening.moves[currentStep - 1].description, color = Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("Tap 'Start Teacher' to see the moves.", color = LightGrey, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))

        InteractiveBoard(
            initialBoard = getInitialBoard(),
            moves = selectedOpening.moves,
            currentStep = currentStep
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { currentStep = 0; isPlaying = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555))) {
                Text("Reset")
            }
            Button(onClick = { isPlaying = !isPlaying }, colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black)) {
                Text(if (isPlaying) "Pause" else "Start Teacher")
            }
            Button(onClick = { if (currentStep < selectedOpening.moves.size) currentStep++ }, colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                Text("Next Move")
            }
        }
    }
}
