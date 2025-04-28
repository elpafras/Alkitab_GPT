package org.sabda.gpt.utility

import android.os.Handler
import android.os.Looper
import org.sabda.gpt.adapter.ChatAdapter
import org.sabda.gpt.model.ChatMessage

class LoadingUtil {

    private val handler = Handler(Looper.getMainLooper())
    private var loadingIndex: Int = -1
    private var countdownRunnable: Runnable? = null
    private var currentSecond = 1

    companion object{
        private const val MAX_SECONDS = 60
    }


    fun showLoadingMessage(chatAdapter: ChatAdapter, messageList: MutableList<ChatMessage>, chatId: Long) {
        stopCountdown() // make sure previous one is stopped
        currentSecond = 1

        val initialMessage = ChatMessage(chatId,"⏳ Tunggu sebentar... ($currentSecond/$MAX_SECONDS)", false,  System.currentTimeMillis())
        messageList.add(initialMessage)
        loadingIndex = messageList.size - 1
        chatAdapter.notifyItemInserted(loadingIndex)

        countdownRunnable = object : Runnable {
            override fun run() {
                if (currentSecond <= MAX_SECONDS) {
                    val msg = "⏳ Tunggu sebentar... ($currentSecond/$MAX_SECONDS)"
                    messageList[loadingIndex].text = msg
                    chatAdapter.notifyItemChanged(loadingIndex)
                    currentSecond++
                    handler.postDelayed(this, 1000)
                }
            }
        }

        handler.post(countdownRunnable!!)
    }

    fun hideLoadingMessage(chatAdapter: ChatAdapter, messageList: MutableList<ChatMessage>) {
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