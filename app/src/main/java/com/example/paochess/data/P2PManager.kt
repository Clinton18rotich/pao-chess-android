package com.example.paochess.data

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object P2PManager {
    val incomingBoard = MutableStateFlow<String?>(null)
    private const val SERVICE_ID = "com.example.paochess.P2P"
    private const val STRATEGY = Strategy.P2P_STAR // Good for 2 players
    private const val TAG = "P2P"

    var isHost = false
    var connected = false

    private var connectionsClient: ConnectionsClient? = null
    private var endpointId: String? = null

    fun init(context: Context) {
        connectionsClient = Nearby.getConnectionsClient(context)
    }

    // ═══ HOST: Advertise the game ═══
    fun startHost(context: Context, onConnected: () -> Unit, onMessage: (String) -> Unit) {
        isHost = true
        init(context)
        
        val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                // Auto-accept the connection
                connectionsClient?.acceptConnection(endpointId, payloadCallback(onMessage))
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                if (result.status.isSuccess) {
                    connected = true
                    P2PManager.endpointId = endpointId
                    onConnected()
                }
            }

            override fun onDisconnected(endpointId: String) {
                connected = false
            }
        }

        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient?.startAdvertising("Host", SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
            ?.addOnSuccessListener { Log.d(TAG, "Advertising started") }
            ?.addOnFailureListener { Log.e(TAG, "Advertising failed: ${it.message}") }
    }

    // ═══ JOIN: Discover the host ═══
    fun joinHost(context: Context, onConnected: () -> Unit, onMessage: (String) -> Unit) {
        isHost = false
        init(context)

        val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                connectionsClient?.acceptConnection(endpointId, payloadCallback(onMessage))
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                if (result.status.isSuccess) {
                    connected = true
                    P2PManager.endpointId = endpointId
                    onConnected()
                }
            }

            override fun onDisconnected(endpointId: String) {
                connected = false
            }
        }

        val discoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // Found the host, request connection
                connectionsClient?.requestConnection("Join", endpointId, connectionLifecycleCallback)
            }
            override fun onEndpointLost(endpointId: String) {}
        }

        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient?.startDiscovery(SERVICE_ID, discoveryCallback, discoveryOptions)
            ?.addOnSuccessListener { Log.d(TAG, "Discovery started") }
            ?.addOnFailureListener { Log.e(TAG, "Discovery failed: ${it.message}") }
    }

    // ═══ Message Receiver ═══
    private fun payloadCallback(onMessage: (String) -> Unit): PayloadCallback {
        return object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                if (payload.type == Payload.Type.BYTES) {
                    val msg = String(payload.asBytes() ?: return)
                    incomingBoard.value = msg
                    onMessage(msg)
                }
            }
            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
        }
    }

    // ═══ Send Move ═══
    fun sendBoardState(serializedBoard: String) {
        endpointId?.let { id ->
            connectionsClient?.sendPayload(id, Payload.fromBytes(serializedBoard.toByteArray()))
        }
    }

    fun disconnect() {
        connectionsClient?.stopAdvertising()
        connectionsClient?.stopDiscovery()
        endpointId?.let { connectionsClient?.disconnectFromEndpoint(it) }
        connected = false
    }
}
