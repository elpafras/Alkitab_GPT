package org.sabda.gpt.utility

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import org.sabda.gpt.adapter.ChatAdapter
import org.sabda.gpt.model.ChatbotData

class LoadingUtil {

    private val handler = Handler(Looper.getMainLooper())
    private var loadingIndex: Int = -1
    private val loadingMessages = arrayOf("Loading.", "Loading..", "Loading...")

    fun showLoadingMessage(chatAdapter: ChatAdapter, messageList: MutableList<ChatbotData>, chatId: Long) {
        startLoadingAnimation(
            onUpdate = { index ->
                if (loadingIndex == -1) {
                    messageList.add( ChatbotData(loadingMessages[index], false, chatId, System.currentTimeMillis(), 0 ) )
                    loadingIndex = messageList.size - 1
                    chatAdapter.notifyItemInserted(loadingIndex)
                } else {
                    messageList[loadingIndex].text = loadingMessages[index]
                    chatAdapter.notifyItemChanged(loadingIndex)
                }
            }
        )
    }

    fun hideLoadingMessage(chatAdapter: ChatAdapter, messageList: MutableList<ChatbotData>) {
        if (loadingIndex != -1) {
            messageList.removeAt(loadingIndex)
            chatAdapter.notifyItemRemoved(loadingIndex)
            loadingIndex = -1
        }
        stopLoadingAnimation()
    }

    fun showLoadingView(loadingTextView: View) {
        if (loadingTextView is TextView) {
            loadingTextView.visibility = View.VISIBLE
            startLoadingAnimation(
                onUpdate = { index -> loadingTextView.text = loadingMessages[index] }
            )
        }
    }

    fun hideLoadingView(loadingTextView: View) {
        loadingTextView.visibility = View.GONE
        stopLoadingAnimation()
    }

    private fun startLoadingAnimation(onUpdate: (index: Int) -> Unit) {
        handler.post(object : Runnable {
            var index = 0
            override fun run() {
                onUpdate(index)
                index = (index + 1) % loadingMessages.size
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun stopLoadingAnimation() {
        handler.removeCallbacksAndMessages(null)
    }
}