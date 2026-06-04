package com.unired.data.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.unired.BuildConfig
import com.unired.data.model.Notification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
import kotlin.math.min

private const val TAG = "WebSocketManager"
private const val NORMAL_CLOSURE = 1000
private const val MAX_RECONNECT_DELAY_MS = 30_000L
private const val BASE_RECONNECT_DELAY_MS = 1_000L

/**
 * Singleton WebSocket client.
 *
 * Usage:
 *  - Call [connect] with the user's JWT after a successful login.
 *  - Collect [incomingNotifications] in any Composable / ViewModel.
 *  - Call [disconnect] on logout or when the app goes to background.
 */
object WebSocketManager {

    // ── Public API ────────────────────────────────────────────────────────────

    private val _incomingNotifications = MutableSharedFlow<Notification>(extraBufferCapacity = 64)
    val incomingNotifications: SharedFlow<Notification> = _incomingNotifications.asSharedFlow()

    // ── Internal state ────────────────────────────────────────────────────────

    private val gson = Gson()

    /** Dedicated OkHttpClient for WebSockets (no auth interceptor — auth is in the URL). */
    private val wsClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)   // hardware-level keepalive
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)     // no timeout on read — WS streams are open indefinitely
        .build()

    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var currentToken: String? = null
    @Volatile private var isIntentionallyClosed = false
    @Volatile private var reconnectDelayMs = BASE_RECONNECT_DELAY_MS

    // ── Public functions ──────────────────────────────────────────────────────

    /**
     * Open a WebSocket connection using [token] for authentication.
     * Idempotent — calling while already connected is a no-op.
     */
    fun connect(token: String) {
        if (webSocket != null && currentToken == token) {
            Log.d(TAG, "Already connected, skipping.")
            return
        }
        currentToken = token
        isIntentionallyClosed = false
        reconnectDelayMs = BASE_RECONNECT_DELAY_MS
        openWebSocket(token)
    }

    /**
     * Close the WebSocket connection (e.g., on logout or app background).
     */
    fun disconnect() {
        isIntentionallyClosed = true
        webSocket?.close(NORMAL_CLOSURE, "Client disconnected")
        webSocket = null
        currentToken = null
        Log.d(TAG, "Disconnected.")
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun openWebSocket(token: String) {
        // Derive WS URL from the HTTP base URL:
        // "http://10.0.2.2:3000/api/" → "ws://10.0.2.2:3000/?token=..."
        val wsUrl = BuildConfig.BASE_URL
            .removeSuffix("/api/")
            .removeSuffix("/api")
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .plus("/?token=$token")

        Log.d(TAG, "Connecting to $wsUrl")

        val request = Request.Builder().url(wsUrl).build()
        webSocket = wsClient.newWebSocket(request, listener)
    }

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "Connection opened.")
            reconnectDelayMs = BASE_RECONNECT_DELAY_MS   // reset backoff on success
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            handleMessage(text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            handleMessage(bytes.utf8())
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "Connection failure: ${t.message}")
            this@WebSocketManager.webSocket = null
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "Connection closed: [$code] $reason")
            this@WebSocketManager.webSocket = null
            if (!isIntentionallyClosed) {
                scheduleReconnect()
            }
        }
    }

    private fun handleMessage(text: String) {
        try {
            val json: JsonObject = JsonParser.parseString(text).asJsonObject
            when (json.get("type")?.asString) {
                "new_notification" -> {
                    val data = json.getAsJsonObject("data")
                    val notification = gson.fromJson(data, Notification::class.java)
                    _incomingNotifications.tryEmit(notification)
                    Log.d(TAG, "New notification received: ${notification.type} from ${notification.senderName}")
                }
                "connected" -> Log.d(TAG, "Server acknowledged connection.")
                "auth_expired" -> {
                    Log.w(TAG, "Auth expired — disconnecting.")
                    disconnect()
                }
                else -> Log.d(TAG, "Unknown WS message type: $text")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse WS message: $text", e)
        }
    }

    private fun scheduleReconnect() {
        if (isIntentionallyClosed || currentToken == null) return

        val delay = reconnectDelayMs
        Log.d(TAG, "Scheduling reconnect in ${delay}ms")
        reconnectDelayMs = min(delay * 2, MAX_RECONNECT_DELAY_MS)  // exponential backoff

        Thread {
            Thread.sleep(delay)
            val token = currentToken ?: return@Thread
            if (!isIntentionallyClosed && webSocket == null) {
                openWebSocket(token)
            }
        }.start()
    }
}
