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
fun NotationScreen() {
    val notationGuide = listOf(
        "K" to "King",
        "Q" to "Queen",
        "R" to "Rook",
        "B" to "Bishop",
        "N" to "Knight",
        "Pawn" to "No letter (e.g., e4)",
        "x" to "Capture (e.g., Nxe5)",
        "O-O" to "Kingside Castling",
        "O-O-O" to "Queenside Castling",
        "+" to "Check",
        "#" to "Checkmate",
        "=" to "Promotion (e.g., e8=Q)"
    )

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("📝 Chess Notation", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("Standard Algebraic Notation (SAN) is how chess moves are recorded. Here is the cheat sheet:", color = LightGrey, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        notationGuide.forEach { (symbol, meaning) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(symbol, color = Orange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(meaning, color = White, fontSize = 16.sp)
                }
            }
        }
    }
}
