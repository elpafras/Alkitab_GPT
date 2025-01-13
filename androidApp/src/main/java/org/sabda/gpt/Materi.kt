package org.sabda.gpt

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.utility.ToastUtil

class Materi : AppCompatActivity(), NetworkChangeCallback {
    private lateinit var backButton: Button
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materi)

        setupOrientation()
        setupNetworkListener()
        setupWebView()
        setupBackButton()

    }

    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setupNetworkListener() {
        isConnected = NetworkUtil.isNetworkAvailable(this)
        NetworkUtil.registerNetworkChangeReceiver(this) {
            isConnected: Boolean -> this.updateConnectionStatus(isConnected)
        }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected

        if (!isConnected) {
            ToastUtil.showToast(this)
        }
    }

    private fun setupWebView() {
        val url = intent.getStringExtra(EXTRA_URL) ?: run {
            Toast.makeText(this@Materi, "URL Not Found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<WebView>(R.id.webview)?.apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadData(
                generateHtmlContent(url),
                "text/html",
                "UTF-8"
            )
        } ?: run {
            Toast.makeText(this@Materi, "Source Not Found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun generateHtmlContent(url: String): String {
        return """
            <html>
                <body>
                    <iframe src="$url" width="100%" height="100%" frameborder="0" marginwidth="0" marginheight="0" scrolling="no"></iframe>
                </body>
            </html>
        """.trimIndent()
    }

    private fun setupBackButton() {
        backButton = findViewById<Button>(R.id.back).apply {
            setOnClickListener { finish() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtil.unregisterNetworkChangeReceiver(this)
    }

    override fun onNetworkChange(isConnected: Boolean) {
        updateConnectionStatus(isConnected)
    }

    companion object {
        const val EXTRA_URL: String = "extra_url"
    }
}