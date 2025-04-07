package org.sabda.gpt.utility

import android.os.Handler
import android.os.Looper
import org.sabda.gpt.adapter.ChatAdapter
import org.sabda.gpt.model.ChatbotData

class LoadingUtil {

    private val handler = Handler(Looper.getMainLooper())
    private var loadingIndex: Int = -1
    private var countdownRunnable: Runnable? = null
    private var remainingSeconds = 60

    fun showLoadingMessage(chatAdapter: ChatAdapter, messageList: MutableList<ChatbotData>, chatId: Long) {
        stopCountdown() // make sure previous one is stopped
        remainingSeconds = 60

        val initialMessage = ChatbotData("⏳ Tunggu sebentar... (60)", false, chatId, System.currentTimeMillis(), 0)
        messageList.add(initialMessage)
        loadingIndex = messageList.size - 1
        chatAdapter.notifyItemInserted(loadingIndex)

        countdownRunnable = object : Runnable {
            override fun run() {
                if (remainingSeconds > 0) {
                    val msg = "⏳ Tunggu sebentar... ($remainingSeconds)"
                    messageList[loadingIndex].text = msg
                    chatAdapter.notifyItemChanged(loadingIndex)
                    remainingSeconds--
                    handler.postDelayed(this, 1000)
                }
            }
        }

        handler.post(countdownRunnable!!)
    }

    fun hideLoadingMessage(chatAdapter: ChatAdapter, messageList: MutableList<ChatbotData>) {
        if (loadingIndex != -1 && loadingIndex < messageList.size) {
            messageList.removeAt(loadingIndex)
            chatAdapter.notifyItemRemoved(loadingIndex)
            loadingIndex = -1
        }
        stopCountdown()
    }

    private fun stopCountdown() {
        handler.removeCallbacksAndMessages(null)
        countdownRunnable = null
    }
}