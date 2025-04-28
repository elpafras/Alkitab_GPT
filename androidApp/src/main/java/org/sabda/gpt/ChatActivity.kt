package org.sabda.gpt

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.sabda.gpt.adapter.ChatAdapter
import org.sabda.gpt.base.BaseActivity
import org.sabda.gpt.data.local.AppDatabase
import org.sabda.gpt.data.local.ChatMetaData
import org.sabda.gpt.data.local.ChatbotDao
import org.sabda.gpt.data.repository.ChatRepository
import org.sabda.gpt.databinding.ActivityChatBinding
import org.sabda.gpt.fragment.AIFragment
import org.sabda.gpt.model.ChatMessage
import org.sabda.gpt.utility.ChatUtil
import org.sabda.gpt.utility.LoadingUtil
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.PreviewUtil
import org.sabda.gpt.utility.StatusBarUtil

class ChatActivity : BaseActivity<ActivityChatBinding>(), AIFragment.ChatFragmentCallback {

    override fun setupViewBinding(): ActivityChatBinding {
        return ActivityChatBinding.inflate(layoutInflater)
    }

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRepository: ChatRepository
    private lateinit var loadingUtil: LoadingUtil
    private lateinit var messageDao: ChatbotDao

    private val messageList: MutableList<ChatMessage> = mutableListOf()
    private var currentChatId = System.currentTimeMillis()
    private var chatCounter = 1
    private var isNewChat = true
    private var isLoading = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!NetworkUtil.isNetworkAvailable(this)) {
            NetworkUtil.showNoInternetDialog(this)
            return
        }

        StatusBarUtil().setLightStatusBar(this, R.color.white)

        messageDao = AppDatabase.getDatabase(this).chatbotDao()
        chatRepository = ChatRepository()
        loadingUtil = LoadingUtil()

        setupButtons()
        setupChatAdapter()
        setupNavigationView()

        val chatIdFromIntent = intent.getLongExtra("CHAT_ID", -1)
        if (chatIdFromIntent != -1L) {
            currentChatId = chatIdFromIntent
            ChatUtil.loadExistingChat(
                chatId = chatIdFromIntent,
                messageList = messageList,
                adapter = chatAdapter,
                messageDao = messageDao,
                lifecycleScope = lifecycleScope
            )
        }

        currentChatId = getSharedPreferences("ChatPrefs", MODE_PRIVATE).getLong("currentChatId", currentChatId)
        intent.getLongExtra("CHAT_ID", -1L).takeIf { it != -1L }?.let { chatId ->
            currentChatId = chatId
            saveCurrentChatId(chatId)
        }

        if (intent.getBooleanExtra("START_NEW_CHAT", false)) {
            startNewChat()
        }
    }

    private fun setupChatAdapter() {
        chatAdapter = ChatAdapter(messageList)
        Log.d("MainActivity", "RecyclerView updated with ${messageList.size} messages")
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
    }

    private fun setupNavigationView() {
        val chatPreviewList = mutableListOf<ChatMetaData>()
        val recyclerViewChatPreviews: RecyclerView = binding.navView.findViewById(R.id.recyclerViewPreview)

        PreviewUtil.setupPreviewAdapter(this, recyclerViewChatPreviews, chatPreviewList, this, messageDao) { selectedChatId ->
            currentChatId = selectedChatId
            isNewChat = false
            loadChatMessagesByChatId(currentChatId)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun setupButtons() {
        binding.btnSendMessage.setOnClickListener {
            Log.d("TAG", "setupButtons: clicked")
            val messageText = binding.editTextMessage.text.toString()

            ChatUtil.startChatIfNeeded(messageText, isNewChat, isLoading, messageList) {
                sendMessage(it)
            }
        }

        binding.btnHistory.setOnClickListener {
            with(binding.drawerLayout) {
                if (isDrawerOpen(binding.navView)) closeDrawer(binding.navView) else openDrawer(binding.navView)
            }
        }

        binding.home.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.newChatImageView.setOnClickListener {
            startNewChat()
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
        isLoading = true
        binding.editTextMessage.setText("")

        chatCounter = ChatUtil.sendMessage(
            context = this,
            messageText = messageText,
            currentChatId = currentChatId,
            chatCounter = chatCounter,
            messageList = messageList,
            adapter = chatAdapter,
            messageDao = messageDao,
            loadingUtil = loadingUtil,
            lifecycleScope = lifecycleScope,
            chatRepository = chatRepository,
            onReceive = { response ->
                ChatUtil.receiveMessage(
                    response,
                    currentChatId,
                    chatCounter,
                    messageList,
                    chatAdapter,
                    messageDao,
                    lifecycleScope
                )
                isLoading = false
            },
            onError = { errorMsg ->
                ChatUtil.addErrorBubble(
                    errorMsg,
                    messageList,
                    chatAdapter,
                    currentChatId,
                    messageDao,
                    lifecycleScope
                )
                isLoading = false
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadChatMessagesByChatId(chatId: Long) {
        lifecycleScope.launch {
            messageList.clear()
            chatAdapter.notifyDataSetChanged()
            messageList.addAll(PreviewUtil.loadMessagesForChat(chatId, messageDao))
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