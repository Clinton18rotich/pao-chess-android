package com.example.paochess.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    var statusText by remember { mutableStateOf("") }
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) permissionsGranted = true
        else statusText = "Permissions denied. Cannot connect."
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions)
    }

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
                Text("📖 How to Play (Xender Style)", color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("1. Player 1 taps 'Host Game'.", color = White, fontSize = 13.sp)
                Text("2. Player 2 taps 'Scan for Game'.", color = White, fontSize = 13.sp)
                Text("3. Keep the phones near each other.", color = White, fontSize = 13.sp)
                Text("4. Both phones need Location ON.", color = White, fontSize = 13.sp)
            }
        }

        if (statusText.isNotEmpty()) {
            Text(statusText, color = Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
        }

        if (!isHosting && !isScanning) {
            Button(
                onClick = {
                    if (!permissionsGranted) { permissionLauncher.launch(permissions); return@Button }
                    isHosting = true
                    statusText = "Advertising game..."
                    P2PManager.startHost(context, onConnected = { onNavigateToGame() }, onMessage = {})
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("Host Game", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!permissionsGranted) { permissionLauncher.launch(permissions); return@Button }
                    isScanning = true
                    statusText = "Searching for nearby games..."
                    P2PManager.joinHost(context, onConnected = { onNavigateToGame() }, onMessage = {})
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("Scan for Game", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        } else {
            Text(statusText, color = White, fontSize = 18.sp)
            CircularProgressIndicator(color = Orange, modifier = Modifier.padding(16.dp))
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = {
                isHosting = false
                isScanning = false
                P2PManager.disconnect()
            }) { Text("Cancel", color = LightGrey) }
        }
    }
}
