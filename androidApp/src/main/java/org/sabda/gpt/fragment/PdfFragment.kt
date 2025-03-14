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

class PdfFragment(private val onContentLoaded: (String) -> Unit) : Fragment() {

    private var pdfUrl: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pdfUrl = it.getString(ARG_PDF_URL)
            description = it.getString(ARG_DESCRIPTION) // Ambil deskripsi dari arguments
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pdf, container, false)
        val webView = view.findViewById<WebView>(R.id.webViewPdf)

        // Konfigurasi WebView
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onContentLoaded("PDF") // Callback setelah PDF selesai loading
            }
        }

        pdfUrl?.let {
            val embedUrl = if (it.contains("slideshare.net")) {
                "https://www.slideshare.net/slideshow/embed_code/key/${extractSlideShareKey(it)}"
            } else {
                "https://drive.google.com/viewerng/viewer?embedded=true&url=$it"
            }
            webView.loadUrl(embedUrl)
        }

        return view
    }

    companion object {
        private const val ARG_PDF_URL = "pdf_url"
        private const val ARG_DESCRIPTION = "description"

        fun newInstance(pdfUrl: String, description: String?, onContentLoaded: (String) -> Unit) =
            PdfFragment(onContentLoaded).apply {
                arguments = Bundle().apply {
                    putString(ARG_PDF_URL, pdfUrl)
                    putString(ARG_DESCRIPTION, description)
                }
            }

        private fun extractSlideShareKey(url: String): String {
            return url.substringAfterLast("/")
        }
    }
}