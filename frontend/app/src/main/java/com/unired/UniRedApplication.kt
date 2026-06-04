package com.unired

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.unired.data.websocket.WebSocketManager
import com.unired.util.SessionManager

class  UniRedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.initialize(this)

        // Observe the app's process lifecycle to connect/disconnect WebSocket
        // as the app moves between foreground and background.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // App came to foreground — reconnect if logged in
                val token = SessionManager.getToken()
                if (token != null) {
                    WebSocketManager.connect(token)
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                // App went to background — disconnect to save battery
                WebSocketManager.disconnect()
            }
        })
    }
}
