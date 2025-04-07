package org.sabda.gpt.broadcastservice

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat

class AppService : Service() {
    private val receiver = AppBroadcastReceiver()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val filter = IntentFilter("org.sabda.gpt.PEDIA_BROADCAST")
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_EXPORTED)

        Log.d("AppService", "📡 AppBroadcastReceiver telah didaftarkan!")
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        Log.d("AppService", "⛔ AppBroadcastReceiver di-unregister!")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
