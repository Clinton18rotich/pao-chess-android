package com.example.paochess.ui.screens

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.*
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.paochess.data.WifiDirectManager
import com.example.paochess.ui.theme.*

@Composable
fun P2PLobbyScreen(onNavigateToGame: () -> Unit) {
    val context = LocalContext.current
    var isHost by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf<List<WifiP2pDevice>>(emptyList()) }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            val channel = manager.initialize(context, context.mainLooper, null)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                manager.discoverPeers(channel, null)
            }
        }
    }

    DisposableEffect(Unit) {
        permissionLauncher.launch(permissions)
        
        val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val channel = manager.initialize(context, context.mainLooper, null)
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        manager.requestPeers(channel) { peerList ->
                            devices = peerList.deviceList.toList()
                        }
                    }
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION))

        onDispose { context.unregisterReceiver(receiver) }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👥 Nearby Devices", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        if (!isHost) {
            Button(
                onClick = {
                    isHost = true
                    WifiDirectManager.startHost(context, onReady = { onNavigateToGame() })
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("Host Game", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            
            Spacer(Modifier.height(24.dp))
            Text("Available Devices:", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(devices) { device ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            WifiDirectManager.joinHost(context, device)
                            onNavigateToGame()
                        },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Text(device.deviceName, color = White, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        } else {
            Text("Hosting... Waiting for opponent to connect.", color = Orange, fontSize = 18.sp)
            CircularProgressIndicator(color = Orange, modifier = Modifier.padding(16.dp))
        }
    }
}
