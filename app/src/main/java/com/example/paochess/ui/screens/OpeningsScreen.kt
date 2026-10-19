package com.example.paochess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.ui.theme.*

@Composable
fun OpeningsScreen() {
    val openings = listOf(
        "Italian Game" to "1. e4 e5 2. Nf3 Nc6 3. Bc4",
        "Sicilian Defense" to "1. e4 c5",
        "French Defense" to "1. e4 e6",
        "Caro-Kann Defense" to "1. e4 c6",
        "Ruy Lopez" to "1. e4 e5 2. Nf3 Nc6 3. Bb5",
        "Queen's Gambit" to "1. d4 d5 2. c4",
        "King's Indian Defense" to "1. d4 Nf6 2. c4 g6",
        "London System" to "1. d4 d5 2. Nf3 Nf6 3. Bf4"
    )

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("📖 Chess Openings", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("Memorize these famous opening sequences to start your games strong:", color = LightGrey, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        openings.forEach { (name, moves) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(name, color = Orange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(moves, color = White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
