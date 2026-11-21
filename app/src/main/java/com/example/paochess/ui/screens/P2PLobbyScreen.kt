package com.example.paochess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.P2PManager
import com.example.paochess.ui.theme.*

@Composable
fun P2PLobbyScreen(onNavigateToGame: () -> Unit) {
    val context = LocalContext.current
    var isHosting by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    val onMessageReceived: (String) -> Unit = { /* Handled in Game Screen */ }
    val onConnected = { onNavigateToGame() }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👥 Play with a Friend", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📖 How to Play (No Internet Needed)", color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("1. Player 1 taps 'Host Game'.", color = White, fontSize = 13.sp)
                Text("2. Player 2 taps 'Scan for Game'.", color = White, fontSize = 13.sp)
                Text("3. The app will find nearby phones automatically.", color = White, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text("No QR code or Wi-Fi settings needed!", color = Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (!isHosting && !isScanning) {
            Button(
                onClick = {
                    isHosting = true
                    P2PManager.startHost(context, onConnected, onMessageReceived)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("Host Game", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    isScanning = true
                    P2PManager.joinHost(context, onConnected, onMessageReceived)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("Scan for Game", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        } else if (isHosting) {
            Text("Waiting for opponent to join...", color = White, fontSize = 18.sp)
            CircularProgressIndicator(color = Orange, modifier = Modifier.padding(16.dp))
        } else if (isScanning) {
            Text("Searching for nearby games...", color = White, fontSize = 18.sp)
            CircularProgressIndicator(color = Orange, modifier = Modifier.padding(16.dp))
        }
    }
}
