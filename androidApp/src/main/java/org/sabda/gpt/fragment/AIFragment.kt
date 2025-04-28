package org.sabda.gpt.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import org.sabda.gpt.ChatActivity
import org.sabda.gpt.adapter.ChatPreviewAdapter
import org.sabda.gpt.base.BaseFragment
import org.sabda.gpt.data.local.AppDatabase
import org.sabda.gpt.data.local.ChatMetaData
import org.sabda.gpt.data.local.ChatbotDao
import org.sabda.gpt.databinding.FragmentChatBinding
import org.sabda.gpt.utility.NetworkUtil

class AIFragment : BaseFragment<FragmentChatBinding>() {

    interface ChatFragmentCallback {
        fun onLoadChatMessagesByChatId(chatId: Long)
    }

    private var callback: ChatFragmentCallback? = null

    private lateinit var messageDao: ChatbotDao
    private val chatPreview: MutableList<ChatMetaData> = mutableListOf()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentChatBinding.inflate(inflater, container, false)

    override fun onBackPressed() {
        // Misalnya tampilkan konfirmasi sebelum keluar dari aplikasi
        Toast.makeText(requireContext(), "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ChatFragmentCallback) {
            callback = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!NetworkUtil.isNetworkAvailable(requireContext())) {
            NetworkUtil.showNoInternetDialog(requireContext())
            return
        }

        messageDao = AppDatabase.getDatabase(requireContext()).chatbotDao()
        setupRecyclerView()
        loadChatPreviews()

    }

    private fun setupRecyclerView() {
        binding.previewRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
                reverseLayout = true
            }
            adapter = ChatPreviewAdapter(chatPreview, lifecycleScope, messageDao) { chatId ->
                loadChatMessagesByChatId(chatId)
            }
        }
    }

    private fun loadChatPreviews() {
        messageDao.getChatMetaData().observe(viewLifecycleOwner) { previews ->
            val sortedList = previews.sortedByDescending { it.chatId }
            (binding.previewRecyclerView.adapter as? ChatPreviewAdapter)?.updateData(sortedList)
        }
    }

    private fun loadChatMessagesByChatId(chatId: Long) {
        callback?.onLoadChatMessagesByChatId(chatId)

        val intent = Intent(activity, ChatActivity::class.java).apply {
            putExtra("CHAT_ID", chatId)
        }

        startActivity(intent)
    }
}