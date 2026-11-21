package com.example.paochess.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.P2PManager
import com.example.paochess.ui.theme.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.net.Inet4Address
import java.net.NetworkInterface

@Composable
fun P2PLobbyScreen(onNavigateToGame: () -> Unit) {
    var isHost by rememberSaveable { mutableStateOf(false) }
    var myIp by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Listener: When a connection is made and a move is received
    val onMessageReceived: (String) -> Unit = { msg ->
        Log.d("P2P", "Received: $msg")
        // We will handle this in the game screen later
    }

    // QR Scanner launcher
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            P2PManager.joinHost(result.contents, onConnected = { onNavigateToGame() }, onMessage = onMessageReceived)
            onNavigateToGame()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👥 Play with a Friend", color = Orange, fontSize = 26.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // ═══ SETUP GUIDE ═══
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📖 How to Play (No Internet Needed)", color = Green, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("1. Player 1 (Host) taps 'Host Game'.", color = White, fontSize = 13.sp)
                Text("2. Player 1 goes to phone Settings → turns ON Wi-Fi Hotspot.", color = White, fontSize = 13.sp)
                Text("3. Player 2 connects to Player 1's Wi-Fi in Settings.", color = White, fontSize = 13.sp)
                Text("4. Player 2 taps 'Scan to Join' and scans the QR Code.", color = White, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text("Once connected, the board will appear on both phones!", color = Orange, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))

        if (!isHost) {
            // Menu Options
            Button(
                onClick = {
                    isHost = true
                    myIp = getLocalIpAddress() ?: "192.168.43.1"
                    // Generate QR Code
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(myIp, BarcodeFormat.QR_CODE, 512, 512)
                    val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
                    for (x in 0 until 512) for (y in 0 until 512) {
                        bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                    }
                    qrBitmap = bmp
                    P2PManager.startHost(onConnected = { onNavigateToGame() }, onMessage = onMessageReceived)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("Host Game", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val options = ScanOptions()
                    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    options.setPrompt("Scan Host's QR Code")
                    options.setBeepEnabled(false)
                    scanLauncher.launch(options)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("Scan to Join", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }

        } else {
            // Host Screen
            Text("⚠️ ACTION REQUIRED: Turn on Wi-Fi Hotspot in Settings!", color = Orange, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Then have your friend scan this code:", color = LightGrey, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(300.dp).background(Color.White).padding(16.dp)
                )
            }
            
            Spacer(Modifier.height(24.dp))
            Text("IP: $myIp", color = Green, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            
            Spacer(Modifier.height(32.dp))
            Text("Waiting for opponent to connect...", color = LightGrey, fontSize = 14.sp)
        }
    }
}

fun getLocalIpAddress(): String? {
    try {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val intf = en.nextElement()
            val enumIpAddr = intf.inetAddresses
            while (enumIpAddr.hasMoreElements()) {
                val inetAddress = enumIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.hostAddress
                }
            }
        }
    } catch (ex: Exception) { ex.printStackTrace() }
    return null
}
