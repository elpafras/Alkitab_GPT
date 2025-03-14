package org.sabda.gpt

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.utility.ToastUtil

class Tentang : AppCompatActivity(), NetworkChangeCallback {
    private var back: ImageView? = null
    var version: TextView? = null
    private var sabdaLink: TextView? = null
    private var kotakSaranLink: TextView? = null
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tentang)

        isConnected = NetworkUtil.isNetworkAvailable(this)
        back = findViewById(R.id.back)
        version = findViewById(R.id.version)
        sabdaLink = findViewById(R.id.sabdaLink)
        kotakSaranLink = findViewById(R.id.kotakSaranLink)

        updateConnectionStatus(isConnected)

        NetworkUtil.registerNetworkChangeReceiver(
            this
        ) { isConnected: Boolean -> this.updateConnectionStatus(isConnected) }

        back!!.setOnClickListener { finish() }

        version!!.text = "Versi: 2.2"

        sabdaLink!!.setOnClickListener { handleClick("https://sabda.app") }

        kotakSaranLink!!.setOnClickListener { handleClick("mailto:apps@sabda.org") }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected
        sabdaLink!!.isEnabled = isConnected
        kotakSaranLink!!.isEnabled = isConnected

        if (!isConnected) {
            ToastUtil.showToast(this)
        }
    }

    private fun handleClick(url: String) {
        if (isConnected) {
            NetworkUtil.openUrl(this, url)
        } else {
            ToastUtil.showToast(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtil.unregisterNetworkChangeReceiver(this)
    }

    override fun onNetworkChange(isConnected: Boolean) {
        updateConnectionStatus(isConnected)
    }
}