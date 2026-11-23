package com.example.paochess.data

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.*
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.*
import java.net.ServerSocket
import java.net.Socket

object WifiDirectManager {
    private const val TAG = "WifiDirect"
    private const val PORT = 8988
    var isHost = false
    var connected = false
    val incomingBoard = MutableStateFlow<String?>(null)

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private var receiver: BroadcastReceiver? = null
    private var contextRef: Context? = null

    fun init(context: Context) {
        contextRef = context
        manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager?.initialize(context, context.mainLooper, null)
        
        // Register the BroadcastReceiver to listen for Wi-Fi Direct state changes
        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        val networkInfo = intent.getParcelableExtra<android.net.NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                        if (networkInfo?.isConnected == true) {
                            Log.d(TAG, "Wi-Fi Direct Connected! Starting Socket...")
                            if (isHost) startServerSocket() else startClientSocket()
                        }
                    }
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION))
    }

    // ═══ HOST: Create Group ═══
    fun startHost(context: Context, onReady: () -> Unit) {
        isHost = true
        init(context)
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        
        manager?.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() { 
                Log.d(TAG, "Group created. Waiting for connection...")
                onReady()
            }
            override fun onFailure(reason: Int) { Log.e(TAG, "Group failed: $reason") }
        })
    }

    // ═══ JOIN: Connect to Host ═══
    fun joinHost(context: Context, hostDevice: WifiP2pDevice) {
        isHost = false
        init(context)
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        
        val config = WifiP2pConfig().apply { deviceAddress = hostDevice.deviceAddress }
        manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() { Log.d(TAG, "Connection initiated...") }
            override fun onFailure(reason: Int) { Log.e(TAG, "Connection failed: $reason") }
        })
    }

    private fun startServerSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(PORT)
                clientSocket = serverSocket?.accept()
                connected = true
                setupStreams()
                listenForMessages()
            } catch (e: Exception) { Log.e(TAG, "Server error: ${e.message}") }
        }
    }

    private fun startClientSocket() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // The Host is ALWAYS 192.168.49.1 in Wi-Fi Direct
                clientSocket = Socket("192.168.49.1", PORT)
                connected = true
                setupStreams()
                listenForMessages()
            } catch (e: Exception) { Log.e(TAG, "Client error: ${e.message}") }
        }
    }

    private fun setupStreams() {
        reader = BufferedReader(InputStreamReader(clientSocket?.inputStream))
        writer = PrintWriter(BufferedWriter(OutputStreamWriter(clientSocket?.outputStream)), true)
    }

    private suspend fun listenForMessages() {
        try {
            while (connected) {
                val msg = reader?.readLine()
                if (msg != null) incomingBoard.value = msg
            }
        } catch (e: Exception) { connected = false }
    }

    fun sendBoardState(serializedBoard: String) {
        writer?.println(serializedBoard)
        writer?.flush()
    }
}
