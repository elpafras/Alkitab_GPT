package org.sabda.gpt

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.sabda.gpt.databinding.ActivityAlkitabGptBinding
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.utility.ToastUtil

class AlkitabGPT : AppCompatActivity(), NetworkChangeCallback {
    private lateinit var binding: ActivityAlkitabGptBinding
    private val loadingText by lazy {
        TextView(this).apply {
            text = getString(R.string.loading)
            textSize = 20f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            binding.root.addView(this)
        }
    }

    /* From Alkitab GPT*/
    private val inputText by lazy { intent.getStringExtra("inputtext").orEmpty() }
    private val inputUrl by lazy { intent.getStringExtra("url").orEmpty() }

    /* From Alkitab GPT*/ /* From Alkipedia*/
    private val inputPedia by lazy { intent.getStringExtra("inputPedia").orEmpty() }
    private val inputPediaTopics by lazy { intent.getStringExtra("topic").orEmpty() }
    private val lastBookName by lazy { intent.getStringExtra("lastBookName").orEmpty() }
    private val lastChapter by lazy { intent.getIntExtra("lastChapter", 1) }

    /* From Alkipedia */
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlkitabGptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeView()
        initializeWebViewSetting()

        isConnected = NetworkUtil.isNetworkAvailable(this)

        savedInstanceState?.let { binding.alkitabGPTWebView.restoreState(it) } ?: loadWebView()

        NetworkUtil.registerNetworkChangeReceiver(this, ::updateConnectionStatus)
    }

    private fun initializeView() {
        binding.title.text = intent.getStringExtra("title") ?: getString(R.string.app_name)
        binding.out.setOnClickListener { finish() }
    }

    private fun initializeWebViewSetting() {
        with(binding.alkitabGPTWebView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
        }

        binding.alkitabGPTWebView.webViewClient = CustomizeWebViewClient()
        binding.alkitabGPTWebView.webChromeClient = WebChromeClient()
    }

    private fun loadWebView() {
        val url = when {
            inputText.isNotEmpty() -> "https://gpt.sabda.org/chat.php?t=$inputText"
            inputPedia.isNotEmpty() && lastBookName.isNotEmpty() && lastChapter != -1 ->
                "https://gpt.sabda.org/chat.php?p=$lastBookName $lastChapter&t=$inputPedia"
            inputPediaTopics.isNotEmpty() && lastBookName.isNotEmpty() && lastChapter != -1 ->
                "https://gpt.sabda.org/chat.php?p=$lastBookName $lastChapter&d=$inputPediaTopics"
            inputUrl.isNotEmpty() -> inputUrl
            else -> return
        }
        binding.alkitabGPTWebView.loadUrl(url)
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected
        if (!isConnected) ToastUtil.showToast(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.alkitabGPTWebView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.alkitabGPTWebView.restoreState(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtil.unregisterNetworkChangeReceiver(this)
    }

    override fun onNetworkChange(isConnected: Boolean) {
        updateConnectionStatus(isConnected)
    }

    private inner class CustomizeWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loadingText.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            loadingText.visibility = View.GONE
        }
    }
}