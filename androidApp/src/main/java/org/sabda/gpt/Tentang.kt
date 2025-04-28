package org.sabda.gpt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.sabda.gpt.databinding.ActivityTentangBinding
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.utility.showToast

class Tentang : AppCompatActivity(), NetworkChangeCallback {

    private lateinit var binding: ActivityTentangBinding
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTentangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupListener()
        registerNetworkCallback()
    }

    private fun initViews() {
        isConnected = NetworkUtil.isNetworkAvailable(this)
        updateConnectionStatus(isConnected)
        val versi = packageManager.getPackageInfo(packageName, 0).versionName
        binding.version.text = getString(R.string.version, versi)
    }

    private fun setupListener() {
        binding.back.setOnClickListener { finish() }
        binding.sabdaLink.setOnClickListener { handleClick("https://sabda.app") }
        binding.kotakSaranLink.setOnClickListener { handleClick("mailto:apps@sabda.org") }
    }

    private fun registerNetworkCallback() {
        NetworkUtil.registerNetworkChangeReceiver(this) { isConnected ->
            updateConnectionStatus(isConnected)
        }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected
        binding.sabdaLink.isEnabled = isConnected
        binding.kotakSaranLink.isEnabled = isConnected

        if (!isConnected) applicationContext.showToast(getString(R.string.toast_offline))
    }

    private fun handleClick(url: String) {
        if (isConnected) {
            NetworkUtil.openUrl(this, url)
        } else {
            applicationContext.showToast(getString(R.string.toast_offline))
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