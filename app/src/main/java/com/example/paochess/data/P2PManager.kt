package com.example.paochess.data

import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import java.net.*

object P2PManager {
    var isHost = false
    var connected = false
    
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private var listenJob: Job? = null

    // Start a server and listen for a connection
    fun startHost(onConnected: () -> Unit, onMessage: (String) -> Unit) {
        isHost = true
        listenJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(8888)
                // Wait for the other phone to connect
                clientSocket = serverSocket?.accept()
                connected = true
                setupStreams()
                withContext(Dispatchers.Main) { onConnected() }
                listenForMessages(onMessage)
            } catch (e: Exception) {
                Log.e("P2P", "Host error: ${e.message}")
            }
        }
    }

    // Connect to a host IP
    fun joinHost(ip: String, onConnected: () -> Unit, onMessage: (String) -> Unit) {
        isHost = false
        listenJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                clientSocket = Socket(ip, 8888)
                connected = true
                setupStreams()
                withContext(Dispatchers.Main) { onConnected() }
                listenForMessages(onMessage)
            } catch (e: Exception) {
                Log.e("P2P", "Join error: ${e.message}")
                withContext(Dispatchers.Main) { onConnected() } // Still proceed for fallback
            }
        }
    }

    private fun setupStreams() {
        reader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))
        writer = PrintWriter(BufferedWriter(OutputStreamWriter(clientSocket?.getOutputStream())), true)
    }

    private suspend fun listenForMessages(onMessage: (String) -> Unit) {
        try {
            while (connected) {
                val msg = reader?.readLine()
                if (msg != null) {
                    withContext(Dispatchers.Main) { onMessage(msg) }
                }
            }
        } catch (e: Exception) {
            connected = false
        }
    }

    fun sendBoardState(serializedBoard: String) {
        writer?.println("BOARD:$serializedBoard")
        writer?.flush()
    }

    fun disconnect() {
        connected = false
        listenJob?.cancel()
        try {
            serverSocket?.close()
            clientSocket?.close()
        } catch (e: Exception) {}
    }
}
