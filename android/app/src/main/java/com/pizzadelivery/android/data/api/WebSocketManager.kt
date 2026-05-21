package com.pizzadelivery.android.data.api

import com.google.gson.Gson
import com.pizzadelivery.android.BuildConfig
import com.pizzadelivery.android.data.local.TokenManager
import com.pizzadelivery.android.data.model.DeliveryInfo
import com.pizzadelivery.android.data.model.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenManager: TokenManager,
    private val gson: Gson
) {
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _deliveryUpdates = MutableSharedFlow<DeliveryInfo>()
    val deliveryUpdates: SharedFlow<DeliveryInfo> = _deliveryUpdates.asSharedFlow()

    private val _notifications = MutableSharedFlow<Notification>()
    val notifications: SharedFlow<Notification> = _notifications.asSharedFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    fun connect() {
        scope.launch {
            val token = tokenManager.accessToken.first() ?: return@launch
            val request = Request.Builder()
                .url("${BuildConfig.WS_URL}?token=$token")
                .build()

            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _connectionState.value = ConnectionState.CONNECTED
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _connectionState.value = ConnectionState.ERROR
                    // Attempt reconnection after delay
                    scope.launch {
                        kotlinx.coroutines.delay(5000)
                        connect()
                    }
                }
            })
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun subscribeToDelivery(orderId: String) {
        val message = """{"type":"subscribe","channel":"delivery:$orderId"}"""
        webSocket?.send(message)
    }

    fun unsubscribeFromDelivery(orderId: String) {
        val message = """{"type":"unsubscribe","channel":"delivery:$orderId"}"""
        webSocket?.send(message)
    }

    private fun handleMessage(text: String) {
        try {
            val event = gson.fromJson(text, WebSocketEvent::class.java)
            scope.launch {
                when (event.type) {
                    "delivery:location_update", "delivery:status_change" -> {
                        val delivery = gson.fromJson(event.data.toString(), DeliveryInfo::class.java)
                        _deliveryUpdates.emit(delivery)
                    }
                    "notification:new" -> {
                        val notification = gson.fromJson(event.data.toString(), Notification::class.java)
                        _notifications.emit(notification)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    enum class ConnectionState {
        CONNECTED, DISCONNECTED, ERROR
    }

    data class WebSocketEvent(
        val type: String,
        val data: Any?
    )
}
