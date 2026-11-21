package com.example.paochess.data

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

    // Start a server and listen for a connection
    fun startHost(onMessage: (String) -> Unit) {
        isHost = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(8888)
                clientSocket = serverSocket?.accept()
                connected = true
                setupStreams()
                listenForMessages(onMessage)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Connect to a host IP
    fun joinHost(ip: String, onMessage: (String) -> Unit) {
        isHost = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clientSocket = Socket(ip, 8888)
                connected = true
                setupStreams()
                listenForMessages(onMessage)
            } catch (e: Exception) { e.printStackTrace() }
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
        } catch (e: Exception) { connected = false }
    }

    fun sendMove(fromF: Int, fromR: Int, toF: Int, toR: Int) {
        writer?.println("MOVE:$fromF,$fromR:$toF,$toR")
        writer?.flush() // Force the data to send immediately
    }

    fun disconnect() {
        connected = false
        try {
            serverSocket?.close()
            clientSocket?.close()
        } catch (e: Exception) {}
    }

    // Read a message from the socket
    fun receiveMessage(): String? {
        return try {
            reader?.readLine()
        } catch (e: Exception) {
            null
        }
    }

    fun sendPromotion() {
        writer?.println("PROMOTE")
    }
}
