package org.sabda.gpt

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import org.sabda.gpt.databinding.ActivityMateriBinding
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import android.webkit.WebChromeClient
import org.sabda.gpt.adapter.MateriPagerAdapter
import androidx.viewpager2.widget.ViewPager2
import android.widget.ImageButton
import android.widget.TextView
import android.util.Log
import android.widget.ProgressBar
import android.view.View
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import org.sabda.gpt.utility.showToast

class Materi : AppCompatActivity(), NetworkChangeCallback {
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: MateriPagerAdapter
    private lateinit var viewPagerMateri: ViewPager2
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var binding: ActivityMateriBinding
    private var youtubeUrl: String? = null
    private var pdfUrl: String? = null
    private var currentPosition = 0
    private var totalItems = 0
    private var youtubeLoaded = false
    private var pdfLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMateriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi ProgressBar dan ViewPager
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        viewPagerMateri = findViewById(R.id.viewPagerMateri)
        viewPagerMateri.visibility = View.INVISIBLE

        // Cek koneksi internet sebelum memuat materi
        if (!NetworkUtil.isNetworkAvailable(this)) {
            applicationContext.showToast(getString(R.string.toast_offline))
            progressBar.visibility = View.GONE
            return
        }

        // Load Materi langsung tanpa timer
        loadMateri()

        val materiTitle = intent.getStringExtra("TITLE")
        val titleTextView: TextView = findViewById(R.id.materiTitle)
        titleTextView.text = materiTitle ?: "Judul Tidak Ditemukan"

        viewPagerMateri = binding.viewPagerMateri
        btnPrev = binding.btnPrev
        btnNext = binding.btnNext

        setupOrientation()
        setupUI()

        binding.viewPagerMateri.offscreenPageLimit = 2

        // Listener untuk mendeteksi perubahan halaman pada ViewPager2
        binding.viewPagerMateri.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPosition = position
                Log.d("Materi", "Page selected: $position of $totalItems")
                updateNavigationButtonStates()
            }
        })

        binding.btnPrev.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                binding.viewPagerMateri.currentItem = currentPosition
                updateNavigationButtonStates()
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentPosition < totalItems - 1) {
                currentPosition++
                binding.viewPagerMateri.currentItem = currentPosition
                updateNavigationButtonStates()
            }
        }
    }

    private fun updateNavigationButtonStates() {
        // Atur tombol prev enabled/disabled
        val isPrevEnabled = currentPosition > 0
        binding.btnPrev.isEnabled = isPrevEnabled
        binding.btnPrev.alpha = if (isPrevEnabled) 1.0f else 0.5f

        // Atur tombol next enabled/disabled
        val isNextEnabled = currentPosition < totalItems - 1
        binding.btnNext.isEnabled = isNextEnabled
        binding.btnNext.alpha = if (isNextEnabled) 1.0f else 0.5f

        Log.d("Materi", "Navigation states updated: prev=$isPrevEnabled, next=$isNextEnabled, position=$currentPosition, total=$totalItems")
    }

    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setupViewPager() {
        val youtubeUrl = intent.getStringExtra("YOUTUBE_ID")?.let { "https://www.youtube.com/embed/$it" }
        val pdfUrl = intent.getStringExtra("PDF_URL")
        binding.viewPagerMateri.adapter = adapter

        // Update total items after setting adapter
        totalItems = adapter.itemCount
        Log.d("Materi", "ViewPager setup complete. Total items: $totalItems")

        // Initialize navigation button states
        updateNavigationButtonStates()
    }

    private fun setupUI() {
        val title = intent.getStringExtra("TITLE")
        youtubeUrl = intent.getStringExtra("YOUTUBE_ID")?.let { "https://www.youtube.com/embed/YOUTUBE_VIDEO_ID?controls=0&modestbranding=1&fs=0&rel=0$it" }


        pdfUrl = intent.getStringExtra("PDF_URL")
        val deskripsi = intent.getStringExtra("DESKRIPSI") ?: when (title) {
            "AI dan Alkitab" -> getString(R.string.teksartikelaidanalkitab)
            "Alkitab GPT" -> getString(R.string.teksartikelalkitab_gpt)
            "PA dengan AI: Cara AI2 (AI Squared)" -> getString(R.string.teksartikelmetode_ai_squared)
            "Metode F.O.K.U.S" -> getString(R.string.teksartikelmetode_f_o_k_u_s)
            "Bible GPT" -> getString(R.string.teksartikelbible_gpt)
            else -> "Deskripsi tidak tersedia untuk artikel ini."
        }

        binding.textViewDescription.text = deskripsi

        // Tombol kembali
        binding.back.setOnClickListener { finish() }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        if (!isConnected) {
            applicationContext.showToast(getString(R.string.toast_offline))
        }
    }

    private fun setupWebView(webView: WebView, url: String) {
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        }

        webView.loadUrl(url)
        webView.visibility = WebView.VISIBLE
    }

    private fun loadMateri() {
        val youtubeUrl = intent.getStringExtra("YOUTUBE_ID")?.let { "https://www.youtube.com/embed/$it" }
        val pdfUrl = intent.getStringExtra("PDF_URL")

        youtubeLoaded = false
        pdfLoaded = false

        // Inisialisasi elemen dengan findViewById
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val materiTitle = findViewById<TextView>(R.id.materiTitle)
        val viewPagerMateri = findViewById<ViewPager2>(R.id.viewPagerMateri)
        val navigationGroup = findViewById<LinearLayout>(R.id.navigationGroup)
        val textViewDescription = findViewById<TextView>(R.id.textViewDescription)

        // Sembunyikan semua elemen saat loading
        progressBar.visibility = View.VISIBLE
        materiTitle.visibility = View.INVISIBLE
        viewPagerMateri.visibility = View.INVISIBLE
        navigationGroup.visibility = View.INVISIBLE
        textViewDescription.visibility = View.INVISIBLE

        adapter = MateriPagerAdapter(this, youtubeUrl, pdfUrl, "Deskripsi untuk materi ini") { webViewType ->
            when (webViewType) {
                "YOUTUBE" -> {
                    youtubeLoaded = true
                    Log.d("Materi", "YouTube Loaded, youtubeLoaded = $youtubeLoaded, pdfLoaded = $pdfLoaded")
                }
                "PDF" -> {
                    pdfLoaded = true
                    Log.d("Materi", "PDF Loaded, youtubeLoaded = $youtubeLoaded, pdfLoaded = $pdfLoaded")
                }
            }

            // Tambahkan log sebelum kondisi if
            Log.d("Materi", "Cek loading status: youtubeLoaded = $youtubeLoaded, pdfLoaded = $pdfLoaded")

            // Jika kedua WebView sudah selesai, hilangkan loading bar dan tampilkan elemen satu per satu
            if (youtubeLoaded && pdfLoaded) {
                progressBar.visibility = View.GONE
                runOnUiThread {
                    materiTitle.visibility = View.VISIBLE
                    viewPagerMateri.visibility = View.VISIBLE
                    navigationGroup.visibility = View.VISIBLE
                    textViewDescription.visibility = View.VISIBLE
                }
                Log.d("Materi", "Semua konten berhasil dimuat")
            }
        }

        viewPagerMateri.adapter = adapter
        viewPagerMateri.offscreenPageLimit = 2

        Handler(Looper.getMainLooper()).postDelayed({
            totalItems = adapter.itemCount
            updateNavigationButtonStates()
            Log.d("Materi", "Total items diperbarui: $totalItems")
        }, 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            NetworkUtil.unregisterNetworkChangeReceiver(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNetworkChange(isConnected: Boolean) {
        updateConnectionStatus(isConnected)
    }
}