package org.sabda.gpt.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import org.sabda.gpt.R

class YoutubeFragment(private val onContentLoaded: (String) -> Unit) : Fragment() {

    private var youtubeUrl: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            youtubeUrl = it.getString(ARG_YOUTUBE_URL)
            description = it.getString(ARG_DESCRIPTION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_youtube, container, false)
        val webView = view.findViewById<WebView>(R.id.webViewYoutube)

        // Konfigurasi WebView
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onContentLoaded("YOUTUBE") // Callback setelah YouTube selesai loading
            }
        }

        youtubeUrl?.let { webView.loadUrl(it) }

        return view
    }

    companion object {
        private const val ARG_YOUTUBE_URL = "youtube_url"
        private const val ARG_DESCRIPTION = "description"

        fun newInstance(youtubeUrl: String, description: String, onContentLoaded: (String) -> Unit) =
            YoutubeFragment(onContentLoaded).apply {
                arguments = Bundle().apply {
                    putString(ARG_YOUTUBE_URL, youtubeUrl)
                    putString(ARG_DESCRIPTION, description)
                }
            }
    }
}