package org.sabda.gpt.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.sabda.gpt.ChatActivity
import org.sabda.gpt.Materi
import org.sabda.gpt.R
import org.sabda.gpt.adapter.ResourcesAdapter
import org.sabda.gpt.databinding.FragmentHomeBinding
import org.sabda.gpt.model.ResourceData
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.showToast


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var selectedItemId: Int = R.id.openai

    var maxCharCount: Int = 250
    private var isConnected: Boolean = false
    private var selectedOption: String = ""

    // Auto-scroll variables
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    private var scrollPosition = 0
    private var isAutoScrolling = true
    private var alreadySent = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        checkInternetAndProceed {
            isConnected = true
            setupRecyclerView()
            setupListeners()
        }

        setupAutoScroll()

        return binding.root
    }

    private fun setupRecyclerView() {
        val resourceList = listOf(
            ResourceData("1", "Seminar: AI & Alkitab", R.drawable.aidanalkitab, youtubeId = "gDLTyyabCvI", pdfUrl = "https://www.slideshare.net/slideshow/embed_code/key/aDfLeASoUQYFx0",""),
            ResourceData("2", "Seminar: Alkitab GPT", R.drawable.alkitabgptdetail, youtubeId = "Pf9DPEcX8JA", pdfUrl = "https://www.slideshare.net/slideshow/embed_code/key/F09kT2IosMpVwf",""),
            ResourceData("3", "Metode AI Squared", R.drawable.aisquare, youtubeId = "qHPwPjuAEs4", pdfUrl = "https://www.slideshare.net/slideshow/embed_code/key/MufsoIHKBIlR6V",""),
            ResourceData("4", "Metode F.O.K.U.S.", R.drawable.fokus, youtubeId = "GHcRwNXdnvQ", pdfUrl = "https://www.slideshare.net/slideshow/embed_code/key/7GNdvGTo9M4Fc9",""),
            ResourceData("5", "Bible + GPT", R.drawable.biblegpt, youtubeId = "vZkGKgtRPeU", pdfUrl = "https://www.slideshare.net/slideshow/embed_code/key/j402m0w1jgBb9m","")
        )

        binding.horizontalRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = ResourcesAdapter(resourceList) { resource ->
            val intent = Intent(requireContext(), Materi::class.java)
            intent.putExtra("TITLE", resource.title)
            intent.putExtra("YOUTUBE_ID", resource.youtubeId)
            intent.putExtra("PDF_URL", resource.pdfUrl)
            startActivity(intent)
        }

        // Tambahkan listener untuk mendeteksi sentuhan pengguna pada RecyclerView
        binding.horizontalRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        stopAutoScroll()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        startAutoScroll()
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    private fun setupAutoScroll() {
        autoScrollRunnable = Runnable {
            binding.horizontalRecyclerView.let { recyclerView ->
                if (isAutoScrolling) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val itemCount = recyclerView.adapter?.itemCount ?: 0

                    if (itemCount > 0) {
                        // Jika sudah di item terakhir, kembali ke awal
                        if (scrollPosition >= itemCount - 1) {
                            scrollPosition = 0
                            recyclerView.smoothScrollToPosition(scrollPosition)
                        } else {
                            // Scroll ke item berikutnya
                            scrollPosition++
                            recyclerView.smoothScrollToPosition(scrollPosition)
                        }
                    }
                }

                // Jadwalkan lagi untuk 5 detik berikutnya
                autoScrollHandler.postDelayed(autoScrollRunnable!!, 3000)
            }
        }
    }

    private fun startAutoScroll() {
        isAutoScrolling = true
        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
        autoScrollHandler.postDelayed(autoScrollRunnable!!, 5000)
    }

    private fun stopAutoScroll() {
        isAutoScrolling = false
        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
    }

    private fun setupListeners() {
        binding.apply {
            input.addTextChangedListener(InputTextWatcher())
            input.setOnEditorActionListener { _, actionId, _ -> handleEditorAction(actionId) }
            input.setOnKeyListener { _, keyCode, event -> handleEnterKey(keyCode, event) }
            send.setOnClickListener { handleSendButtonClick() }
            option.setOnClickListener { showPopUp2(it) }
            alkitabGPT.setOnClickListener { openUrl("https://chatgpt.com/g/g-QjHkF2IEk-alkitab-gpt-bible-man") }
        }
    }

    private fun handleEnterKey(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            handleSendInput()
            return true
        }
        return false
    }

    private fun handleEditorAction(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            handleSendInput()
            return true
        }
        return false
    }

    private fun handleSendButtonClick() {
        if (alreadySent) return

        val inputText = binding.input.text.toString()
        when {
            inputText.length < 5 -> requireContext().showToast(getString(R.string.prompt_minus))
            inputText.length > maxCharCount -> requireContext().showToast(getString(R.string.prompt_plus))
            isConnected -> {
                if (selectedOption.isEmpty()) {
                    alreadySent = true

                    // Jika OpenAI dipilih, buka AlkitabGPT
                    startActivity(Intent(requireContext(), ChatActivity::class.java).apply {
                        putExtra("inputtext", inputText)
                        putExtra("START_NEW_CHAT", true)
                    })
                    binding.input.text?.clear()
                } else {
                    // Jika ChatGPT, Copilot, atau Meta, buka URL
                    val url = "https://gpt.sabda.org$selectedOption?t=$inputText"
                    openUrl(url)
                }

            }
            else -> {
                requireContext().showToast(getString(R.string.toast_offline))
            }

        }
    }

    private fun handleSendInput() {
        val inputText = binding.input.text.toString()
        openUrl("https://gpt.sabda.org/chat.php?t=$inputText")
    }

    private fun showPopUp2(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.send_option, popup.menu)

        val highlightColor = ContextCompat.getColor(requireContext(), R.color.highlight_color)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.default_text_color)

        // Fungsi untuk mewarnai item yang dipilih
        fun highlightMenuItem(itemId: Int) {
            val menu = popup.menu
            for (i in 0 until menu.size) {
                val menuItem = menu[i]
                val spannable = SpannableString(menuItem.title)
                if (menuItem.itemId == itemId) {
                    spannable.setSpan(ForegroundColorSpan(highlightColor), 0, spannable.length, 0)
                } else {
                    spannable.setSpan(ForegroundColorSpan(defaultColor), 0, spannable.length, 0)
                }
                menuItem.title = spannable
            }
        }

        // Set warna awal untuk item yang sebelumnya dipilih
        highlightMenuItem(selectedItemId)

        popup.setOnMenuItemClickListener { item ->
            selectedItemId = item.itemId  // Simpan item yang dipilih
            highlightMenuItem(selectedItemId)  // Update warna teks yang dipilih

            when (item.itemId) {
                R.id.openai -> {
                    selectedOption = ""
                    binding.source.text = "ChatGPT"
                }
                R.id.chatgpt -> {
                    selectedOption = "/wa/openai.php"
                    binding.source.text = getString(R.string.wa_chatgpt)
                }
                R.id.copilot -> {
                    selectedOption = "/wa/copilot.php"
                    binding.source.text = getString(R.string.wa_copilot)
                }
                R.id.meta -> {
                    selectedOption = "/wa/meta.php"
                    binding.source.text = getString(R.string.wa_meta)
                }
            }
            true
        }

        popup.show()
    }

    private fun openUrl(url: String, title: String? = null) {
        if (isConnected) {
            title?.let { NetworkUtil.openWebView(requireContext(), url, it) } ?: NetworkUtil.openUrl(requireContext(), url)
        } else {
            requireContext().showToast(getString(R.string.toast_offline))
        }
    }

    private fun checkInternetAndProceed(action: () -> Unit) {
        if (NetworkUtil.isNetworkAvailable(requireContext())){
            action()
        } else {
            requireContext().showToast(getString(R.string.toast_offline))
        }
    }

    private inner class InputTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val length = s.length
            updateCharCount(length)

            if (length > maxCharCount) {
                binding.input.setText(s.subSequence(0, maxCharCount))
                binding.input.setSelection(maxCharCount)
            }
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private fun updateCharCount(length: Int) {
        val remaining = maxCharCount - length
        binding.characterCount.text = getString(R.string.cc, remaining)
    }

    override fun onResume() {
        super.onResume()
        // Mulai auto-scrolling saat fragment aktif
        startAutoScroll()
        alreadySent = false
    }

    override fun onPause() {
        super.onPause()
        // Hentikan auto-scrolling saat fragment tidak aktif
        stopAutoScroll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Membersihkan runnable untuk menghindari memory leak
        autoScrollHandler.removeCallbacks(autoScrollRunnable!!)
        _binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}