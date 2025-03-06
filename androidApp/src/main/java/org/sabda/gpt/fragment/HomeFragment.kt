package org.sabda.gpt.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.sabda.gpt.AlkitabGPT
import org.sabda.gpt.R
import org.sabda.gpt.adapter.ResourcesAdapter
import org.sabda.gpt.databinding.FragmentHomeBinding
import org.sabda.gpt.model.ResourceData
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.ToastUtil


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var maxCharCount: Int = 250
    private var isConnected: Boolean = false
    private var selectedOption: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        val resourceList = listOf(
            ResourceData("1", "Seminar: AI & Alkitab", R.drawable.aidanalkitab),
            ResourceData("2", "Seminar: Alkitab GPT", R.drawable.alkitabgptdetail),
            ResourceData("3", "Metode AI Squared", R.drawable.aisquare),
            ResourceData("4", "Metode F.O.K.U.S.", R.drawable.fokus),
            ResourceData("5", "Bible + GPT", R.drawable.biblegpt)
        )

        binding.horizontalRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = ResourcesAdapter(resourceList, ::showToast)
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
        val inputText = binding.input.text.toString()
        when {
            inputText.length < 5 -> showToast("Masukkan prompt minimal 5 karakter")
            inputText.length > maxCharCount -> showToast("Maksimal 250 karakter")
            isConnected -> {
                if (selectedOption.isEmpty()) {
                    // Jika OpenAI dipilih, buka AlkitabGPT
                    startActivity(Intent(requireContext(), AlkitabGPT::class.java).apply {
                        putExtra("inputtext", inputText)
                    })
                } else {
                    // Jika ChatGPT, Copilot, atau Meta, buka URL
                    val url = "https://gpt.sabda.org$selectedOption?t=$inputText"
                    openUrl(url)
                }

            }
            else -> ToastUtil.showToast(requireContext())
        }
    }

    private fun handleSendInput() {
        val inputText = binding.input.text.toString()
        openUrl("https://gpt.sabda.org/chat.php?t=$inputText")

    }

    private fun showPopUp2(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.send_option, popup.menu)

        popup.setOnMenuItemClickListener { item ->
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
            ToastUtil.showToast(requireContext())
        }
    }

    private fun checkInternetAndProceed(action: () -> Unit) {
        if (NetworkUtil.isNetworkAvailable(requireContext())){
            action()
        } else {
            ToastUtil.showToast(requireContext())
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Menghindari memory leak
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}