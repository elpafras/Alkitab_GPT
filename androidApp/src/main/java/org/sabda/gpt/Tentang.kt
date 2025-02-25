package org.sabda.gpt

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.utility.ToastUtil

class Tentang : AppCompatActivity(), NetworkChangeCallback {

    private lateinit var back: ImageView
    private lateinit var version: TextView
    private lateinit var sabdaLink: TextView
    private lateinit var kotakSaranLink: TextView
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tentang)

        initViews()
        setupListener()
        registerNetworkCallback()
    }

    private fun initViews() {
        back = findViewById(R.id.back)
        version = findViewById(R.id.version)
        sabdaLink = findViewById(R.id.sabdaLink)
        kotakSaranLink = findViewById(R.id.kotakSaranLink)

        isConnected = NetworkUtil.isNetworkAvailable(this)
        updateConnectionStatus(isConnected)
        version.text = "Versi: 2.2"
    }

    private fun setupListener() {
        back.setOnClickListener { finish() }

        sabdaLink.setOnClickListener { handleClick("https://sabda.app") }
        kotakSaranLink.setOnClickListener { handleClick("mailto:apps@sabda.org") }
    }

    private fun registerNetworkCallback() {
        NetworkUtil.registerNetworkChangeReceiver(this) { isConnected ->
            updateConnectionStatus(isConnected)
        }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected
        sabdaLink.isEnabled = isConnected
        kotakSaranLink.isEnabled = isConnected

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