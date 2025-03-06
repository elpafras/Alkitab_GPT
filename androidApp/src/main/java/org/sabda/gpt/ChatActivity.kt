package org.sabda.gpt

import android.os.Bundle
import android.util.Log
import android.content.Intent
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.sabda.gpt.adapter.ChatAdapter
import org.sabda.gpt.adapter.ChatPreviewAdapter
import org.sabda.gpt.base.BaseActivity
import org.sabda.gpt.data.local.AppDatabase
import org.sabda.gpt.data.local.ChatbotDao
import org.sabda.gpt.data.repository.ChatRepository
import org.sabda.gpt.databinding.ActivityChatBinding
import org.sabda.gpt.fragment.AIFragment
import org.sabda.gpt.model.ChatbotData
import org.sabda.gpt.utility.LoadingUtil
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.StatusBarUtil
import java.util.Calendar
import android.content.Context
import androidx.appcompat.app.AlertDialog

class ChatActivity : BaseActivity<ActivityChatBinding>(), AIFragment.ChatFragmentCallback {

    override fun setupViewBinding(): ActivityChatBinding {
        return ActivityChatBinding.inflate(layoutInflater)
    }

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRepository: ChatRepository
    private lateinit var loadingUtil: LoadingUtil
    private lateinit var appDatabase: AppDatabase
    private lateinit var messageDao: ChatbotDao

    private val messageList: MutableList<ChatbotData> = mutableListOf()
    private val chatPreview: MutableList<ChatbotData> = mutableListOf()
    private var currentChatId = System.currentTimeMillis()
    private var chatCounter = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!NetworkUtil.isNetworkAvailable(this)) {
            NetworkUtil.showNoInternetDialog(this)
            return
        }

        initDatabase()
        StatusBarUtil().setLightStatusBar(this, R.color.white)

        chatRepository = ChatRepository()
        loadingUtil = LoadingUtil()

        setupButtons()
        setupRecyclerView()
        setupNavigationView()

        currentChatId = getSharedPreferences("ChatPrefs", MODE_PRIVATE).getLong("currentChatId", currentChatId)
        intent.getLongExtra("CHAT_ID", -1L).takeIf { it != -1L }?.let { chatId ->
            currentChatId = chatId
            saveCurrentChatId(chatId)
        }

        if (intent.getBooleanExtra("START_NEW_CHAT", false)) {
            startNewChat()
        }

        loadMessagesFromDatabase()
        loadChatPreviews()
    }

    private fun initDatabase() {
        appDatabase = AppDatabase.getDatabase(this)
        messageDao = appDatabase.chatbotDao()
    }


    private fun setupButtons() {
        binding.btnSendMessage.setOnClickListener {
            Log.d("TAG", "setupButtons: clicked")
            val messageText = binding.editTextMessage.text.toString()
            val prefs = getSharedPreferences("MessageLimitPrefs", Context.MODE_PRIVATE)
            val currentDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Ambil jumlah pesan hari ini dari SharedPreferences
            val lastResetDate = prefs.getLong("lastResetDate", 0)
            var messageCount = prefs.getInt("messageCount", 0)

            // Reset hitung jika sudah lewat tengah malam
            if (lastResetDate < currentDate) {
                messageCount = 0
                prefs.edit {
                    putLong("lastResetDate", currentDate)
                    putInt("messageCount", 0)
                }
            }

            if (messageCount >= 20) {
                // Tampilkan dialog batasan pesan
                showMessageLimitDialog()
            } else {
                // Kirim pesan
                sendMessage(messageText)
                binding.editTextMessage.text.clear()
                loadingUtil.showLoadingMessage(chatAdapter, messageList, currentChatId)

                lifecycleScope.launch {
                    val response = chatRepository.fetchChatResponse(messageText)
                    response?.let {
                        loadingUtil.hideLoadingMessage(chatAdapter, messageList)
                        receiveMessage(it)
                    }
                }

                // Tambah hitungan pesan
                prefs.edit {
                    putInt("messageCount", messageCount + 1)
                }
            }
        }

        binding.btnHistory.setOnClickListener {
            with(binding.drawerLayout) {
                if (isDrawerOpen(binding.navView)) closeDrawer(binding.navView) else openDrawer(binding.navView)
            }
        }

        binding.newChatImageView.setOnClickListener {
            startNewChat()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        Log.d("MainActivity", "RecyclerView updated with ${messageList.size} messages")
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
    }

    private fun setupNavigationView() {
        val chatPreviewAdapter = ChatPreviewAdapter(chatPreview, lifecycleScope, messageDao) { chatId ->
            Log.d("ChatPreview", "Chat clicked: $chatId")
            currentChatId = chatId
            saveCurrentChatId(chatId)
            Log.d("MainActivity", "Updated currentChatId: $currentChatId")
            loadChatMessagesByChatId(chatId)
            binding.drawerLayout.closeDrawer(binding.navView)
        }

        val recyclerViewChatPreviews: RecyclerView = binding.navView.findViewById(R.id.recyclerViewPreview)
        recyclerViewChatPreviews.layoutManager = LinearLayoutManager(this)
        recyclerViewChatPreviews.adapter = chatPreviewAdapter

        loadChatPreviews()
    }

    private fun loadChatPreviews() {
        messageDao.getAllMessages().observe(this) { previews ->
            val existingPreviews = mutableSetOf<Pair<Long, Int>>()

            val previewList = previews.groupBy { it.chatId }
                .mapNotNull {
                    val previewMessage = it.value.first()
                    val chatId = it.key
                    val counter = previewMessage.counter

                    if (existingPreviews.contains(Pair(chatId, counter))) {
                        return@mapNotNull null
                    } else {
                        existingPreviews.add(Pair(chatId, counter))
                        ChatbotData(
                            text = previewMessage.text,
                            isSent = previewMessage.isSent,
                            chatId = chatId,
                            timestamp = previewMessage.timestamp,
                            counter = counter
                        )
                    }
                }
                .sortedByDescending { it.timestamp }

            (binding.navView.findViewById<RecyclerView>(R.id.recyclerViewPreview).adapter as ChatPreviewAdapter).updateData(previewList)
        }
    }

    private fun startNewChat() {
        currentChatId = System.currentTimeMillis()
        chatCounter = 1
        messageList.clear()
        saveCurrentChatId(currentChatId)
        binding.recyclerView.scrollToPosition(0)
    }

    private fun sendMessage(messageText: String) {
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Ambil jumlah pesan hari ini dari SharedPreferences
        val prefs = getSharedPreferences("MessageLimitPrefs", Context.MODE_PRIVATE)
        val lastResetDate = prefs.getLong("lastResetDate", 0)
        var messageCount = prefs.getInt("messageCount", 0)

        // Reset hitung jika sudah lewat tengah malam
        if (lastResetDate < currentDate) {
            messageCount = 0
            prefs.edit {
                putLong("lastResetDate", currentDate)
                putInt("messageCount", 0)
            }
        }

        // Cek jumlah pesan
        if (messageCount >= 20) {
            showMessageLimitDialog()
            return
        }

        if (!NetworkUtil.isNetworkAvailable(this)) {
            NetworkUtil.showNoInternetDialog(this)
            return
        }

        val newMessage = ChatbotData(messageText, true, currentChatId, timestamp = System.currentTimeMillis(), chatCounter)
        messageList.add(newMessage)
        chatAdapter.notifyItemInserted(messageList.lastIndex)
        binding.recyclerView.scrollToPosition(messageList.lastIndex)

        // Tambah hitungan pesan
        prefs.edit {
            putInt("messageCount", messageCount + 1)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            messageDao.insertChat(newMessage)
        }
        chatCounter++
    }

    private fun receiveMessage(response: String) {
        val jsonResponse = JSONObject(response)
        val botResponse = jsonResponse.getString("response")

        val receivedMessage = ChatbotData(botResponse, false, currentChatId, timestamp = System.currentTimeMillis(), chatCounter)
        messageList.add(receivedMessage)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        binding.recyclerView.scrollToPosition(messageList.size - 1)

        lifecycleScope.launch(Dispatchers.IO) {
            messageDao.insertChat(receivedMessage)
            Log.d(
                "MainActivity",
                "Received message for chatId: $currentChatId with counter: $chatCounter"
            )
        }
    }

    private fun showMessageLimitDialog() {
        val midnightTime = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val remainingTime = midnightTime.timeInMillis - System.currentTimeMillis()
        val hours = remainingTime / (60 * 60 * 1000)
        val minutes = (remainingTime % (60 * 60 * 1000)) / (60 * 1000)

        AlertDialog.Builder(this)
            .setTitle("Pesan Terbatas")
            .setMessage("Anda telah mencapai batas maksimal 20 pesan hari ini. Silakan coba lagi dalam ${hours} jam ${minutes} menit.")
            .setNegativeButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Lanjutkan menggunakan AI lain") { dialog, _ ->
                dialog.dismiss()
                showAIOptionsDialog()
            }
            .create()
            .show()
    }

    private fun showAIOptionsDialog() {
        val options = arrayOf(
            "ChatGPT",
            getString(R.string.wa_chatgpt),
            getString(R.string.wa_copilot),
            getString(R.string.wa_meta)
        )

        val urls = arrayOf(
            "", // Untuk ChatGPT yang membuka AlkitabGPT
            "/wa/openai.php",
            "/wa/copilot.php",
            "/wa/meta.php"
        )

        AlertDialog.Builder(this)
            .setTitle("Pilih AI")
            .setItems(options) { _, which ->
                val inputText = binding.editTextMessage.text.toString()

                if (which == 0) {
                    // ChatGPT - buka AlkitabGPT
                    val intent = Intent(this, AlkitabGPT::class.java).apply {
                        putExtra("inputtext", inputText)
                    }
                    startActivity(intent)
                } else {
                    // Opsi lainnya, buka URL
                    val selectedUrl = "https://gpt.sabda.org${urls[which]}?t=$inputText"
                    NetworkUtil.openUrl(this, selectedUrl)
                }

                binding.editTextMessage.text.clear()
            }
            .show()
    }

    private fun loadMessagesFromDatabase() {
        lifecycleScope.launch {
            val messages = withContext(Dispatchers.IO) { messageDao.getChatMessages(currentChatId) }
            messageList.addAll(messages)
            binding.recyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private fun loadChatMessagesByChatId(chatId: Long) {
        lifecycleScope.launch {
            messageList.clear()
            chatAdapter.notifyDataSetChanged()
            val messages = withContext(Dispatchers.IO) { messageDao.getChatMessages(chatId) }
            Log.d("MainActivity", "Loaded messages: ${messages.size}")
            messageList.addAll(messages)
            chatCounter = (messages.lastOrNull()?.counter ?: 0) + 1
            chatAdapter.notifyDataSetChanged()
            binding.recyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private fun saveCurrentChatId(chatId: Long) {
        getSharedPreferences("ChatPrefs", MODE_PRIVATE).edit { putLong("currentChatId", chatId) }
    }

    override fun onLoadChatMessagesByChatId(chatId: Long) {
        loadChatMessagesByChatId(chatId)
    }
}