package org.sabda.gpt.utility

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.sabda.gpt.AlkitabGPT
import org.sabda.gpt.adapter.ChatAdapter
import org.sabda.gpt.data.local.ChatbotDao
import org.sabda.gpt.data.repository.ChatRepository
import org.sabda.gpt.model.ChatMessage
import org.sabda.gpt.R
import java.util.Calendar
import androidx.core.content.edit

object ChatUtil {

    fun startChatIfNeeded(
        inputText: String?,
        isNewChat: Boolean,
        isLoadingActive: Boolean,
        messageList: List<ChatMessage>,
        onSend: (String) -> Unit
    ) {
        if (isNewChat && !inputText.isNullOrBlank() && !isLoadingActive) {
            val alreadySent = messageList.any { it.isSent && it.text.trim() == inputText.trim() }
            if (!alreadySent) onSend(inputText)
        }
    }

    fun sendMessage(
        context: Context,
        messageText: String,
        currentChatId: Long,
        chatCounter: Int,
        messageList: MutableList<ChatMessage>,
        adapter: ChatAdapter,
        messageDao: ChatbotDao,
        loadingUtil: LoadingUtil,
        lifecycleScope: LifecycleCoroutineScope,
        chatRepository: ChatRepository,
        onReceive: (String) -> Unit,
        onError: (String) -> Unit
    ): Int {
        /*val prefs = context.getSharedPreferences("MessageLimitPrefs", Context.MODE_PRIVATE)
        val messageCount = resetMessageLimitIfNeeded(prefs)

        if (messageCount >= 20) {
            showMessageLimitDialog(context)
            return -1
        }*/

        val newMessage = createMessage(currentChatId, messageText, isSent = true)
        messageList.add(newMessage)
        adapter.notifyItemInserted(messageList.lastIndex)

        //prefs.edit { putInt("messageCount", messageCount + 1) }

        if (chatCounter == 1) {
            lifecycleScope.launch(Dispatchers.IO) {
                PreviewUtil.insertChatMetaDataIfNeeded(
                    messageDao = messageDao,
                    chatId = currentChatId,
                    firstMessageText = messageText
                )
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            messageDao.insertChatMessage(newMessage)
        }

        loadingUtil.showLoadingMessage(adapter, messageList, currentChatId)

        lifecycleScope.launch {
            try {
                val response = withTimeout(60_000) {
                    chatRepository.fetchChatResponse(messageText)
                }

                loadingUtil.hideLoadingMessage(adapter, messageList)

                if (response.isNullOrBlank()) {
                    onError(context.getString(R.string.no_response2))
                } else {
                    onReceive(response)
                }
            } catch (_: TimeoutCancellationException) {
                loadingUtil.hideLoadingMessage(adapter, messageList)
                onError(context.getString(R.string.timeout_error))
            } catch (_: Exception) {
                loadingUtil.hideLoadingMessage(adapter, messageList)
                onError(context.getString(R.string.no_response3))
            }
        }

        return chatCounter + 1
    }

    fun receiveMessage(
        response: String,
        currentChatId: Long,
        chatCounter: Int,
        messageList: MutableList<ChatMessage>,
        adapter: ChatAdapter,
        messageDao: ChatbotDao,
        lifecycleScope: LifecycleCoroutineScope
    ) {
        val botResponse = JSONObject(response).getString("response")
        val receivedMessage = createMessage(currentChatId, botResponse, isSent = false)
        messageList.add(receivedMessage)
        adapter.notifyItemInserted(messageList.lastIndex)

        lifecycleScope.launch(Dispatchers.IO) {
            messageDao.insertChatMessage(receivedMessage)
        }
    }

    fun showMessageLimitDialog(context: Context) {
        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sisa = midnight.timeInMillis - System.currentTimeMillis()
        val hours = sisa / (60 * 60 * 1000)
        val minutes = (sisa % (60 * 60 * 1000)) / (60 * 1000)

        AlertDialog.Builder(context)
            .setTitle("Pesan Terbatas")
            .setMessage("Anda telah mencapai batas maksimal 20 pesan hari ini. Coba lagi dalam $hours jam $minutes menit.")
            .setNegativeButton("Tutup") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Lanjutkan menggunakan AI lain") { dialog, _ ->
                dialog.dismiss()
                showAIOptionsDialog(context)
            }
            .show()
    }

    fun showAIOptionsDialog(context: Context) {
        val options = arrayOf("ChatGPT", "WhatsApp ChatGPT", "WhatsApp Copilot", "WhatsApp Meta")
        val urls = arrayOf("", "/wa/openai.php", "/wa/copilot.php", "/wa/meta.php")

        AlertDialog.Builder(context)
            .setTitle("Pilih AI")
            .setItems(options) { _, which ->
                val intentText = ""
                if (which == 0) {
                    context.startActivity(Intent(context, AlkitabGPT::class.java).apply {
                        putExtra("inputtext", intentText)
                    })
                } else {
                    val url = "https://gpt.sabda.org${urls[which]}?t=$intentText"
                    NetworkUtil.openUrl(context, url)
                }
            }
            .show()
    }

    fun addErrorBubble(
        message: String,
        messageList: MutableList<ChatMessage>,
        adapter: ChatAdapter,
        chatId: Long,
        messageDao: ChatbotDao,
        lifecycleScope: LifecycleCoroutineScope
    ) {
        val errorMessage = ChatMessage(chatId, message, false, System.currentTimeMillis())
        messageList.add(errorMessage)
        adapter.notifyItemInserted(messageList.lastIndex)
        lifecycleScope.launch(Dispatchers.IO) {
            messageDao.insertChatMessage(errorMessage)
        }
    }

    fun loadExistingChat(
        chatId: Long,
        messageList: MutableList<ChatMessage>,
        adapter: ChatAdapter,
        messageDao: ChatbotDao,
        lifecycleScope: LifecycleCoroutineScope
    ) {
        lifecycleScope.launch {
            val messages = messageDao.getChatMessages(chatId)
            messageList.clear()
            messageList.addAll(messages)
            adapter.notifyDataSetChanged()
        }
    }

    private fun createMessage(chatId: Long, text: String, isSent: Boolean): ChatMessage {
        return ChatMessage(chatId, text, isSent, System.currentTimeMillis())
    }

    private fun resetMessageLimitIfNeeded(prefs: android.content.SharedPreferences): Int {
        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val lastReset = prefs.getLong("lastResetDate", 0)
        return if (lastReset < todayMidnight) {
            prefs.edit().apply {
                putLong("lastResetDate", todayMidnight)
                putInt("messageCount", 0)
                apply()
            }
            0
        } else {
            prefs.getInt("messageCount", 0)
        }
    }
}