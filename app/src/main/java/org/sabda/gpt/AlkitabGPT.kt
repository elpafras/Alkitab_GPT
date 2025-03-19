package org.sabda.gpt

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.utility.ToastUtil

class AlkitabGPT : AppCompatActivity(), NetworkChangeCallback {
    private var back: ImageView? = null
    private var webView: WebView? = null

    /* From Alkitab GPT*/
    private var inputText: String? = null
    private var inputTitle: String? = null
    private var inputUrl: String? = null
    var title: TextView? = null
    var loadingText: TextView? = null

    /* From Alkitab GPT*/ /* From Alkipedia*/
    private var inputPedia: String? = null
    private var inputPediaTopics: String? = null
    private var lastBookName: String? = null
    private var lastChapter: Int = 0

    /* From Alkipedia */
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alkitab_gpt)

        initializeView()
        initializeWebViewSetting()

        isConnected = NetworkUtil.isNetworkAvailable(this)

        if (savedInstanceState != null) {
            webView!!.restoreState(savedInstanceState)
        } else {
            loadWebView()
        }
        NetworkUtil.registerNetworkChangeReceiver(
            this
        ) { isConnected: Boolean -> this.updateConnectionStatus(isConnected) }
    }

    private fun loadWebView() {
        if (inputText != null) {
            webView!!.loadUrl("https://gpt.sabda.org/chat.php?t=$inputText")
        } else if (inputPedia != null && lastBookName != null && lastChapter != -1) {
            val lCp = lastChapter.toString()
            webView!!.loadUrl("https://gpt.sabda.org/chat.php?p=$lastBookName $lCp&t=$inputPedia")
        } else if (inputPediaTopics != null && lastBookName != null && lastChapter != -1) {
            val lCp = lastChapter.toString()
            webView!!.loadUrl("https://gpt.sabda.org/chat.php?p=$lastBookName $lCp&d=$inputPediaTopics")
        } else {
            webView!!.loadUrl(inputUrl!!)
        }
    }

    private fun initializeView() {
        back = findViewById(R.id.out)
        webView = findViewById(R.id.alkitabGPTWebView)
        title = findViewById(R.id.title)
        loadingText = createLoadingText()

        inputText = intent.getStringExtra("inputtext")
        inputTitle = intent.getStringExtra("title")
        inputUrl = intent.getStringExtra("url")
        inputPedia = intent.getStringExtra("inputPedia")
        inputPediaTopics = intent.getStringExtra("topic")
        lastBookName = intent.getStringExtra("lastBookName")
        lastChapter = intent.getIntExtra("lastChapter", 1)

        setTitletext()
        setupButton()
    }

    private fun initializeWebViewSetting() {
        val webSettings = webView!!.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowUniversalAccessFromFileURLs = true

        webView!!.webViewClient = CustomizeWebViewClient()
        webView!!.webChromeClient = WebChromeClient()
    }


    private fun createLoadingText(): TextView {
        loadingText = TextView(this)
        loadingText!!.text = "Loading..."
        loadingText!!.textSize = 20f

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )

        loadingText!!.layoutParams = params

        val root = findViewById<ViewGroup>(android.R.id.content)
        root.addView(loadingText)

        return loadingText as TextView
    }

    private fun setTitletext() {
        if (inputTitle != null) {
            title!!.text = inputTitle
        } else {
            title!!.setText(R.string.app_name)
        }
    }

    private fun setupButton() {
        back!!.setOnClickListener { v: View? -> finish() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView!!.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView!!.restoreState(savedInstanceState)
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected

        if (!isConnected) {
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

    private inner class CustomizeWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
            super.onPageStarted(view, url, favicon)
            loadingText!!.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            loadingText!!.visibility = View.GONE
        }
    }
}