package org.sabda.gpt.utility

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sabda.gpt.adapter.ChatPreviewAdapter
import org.sabda.gpt.data.local.ChatMetaData
import org.sabda.gpt.data.local.ChatbotDao

object PreviewUtil {

    fun setupPreviewAdapter(
        context: Context,
        recyclerView: RecyclerView,
        chatPreview: MutableList<ChatMetaData>,
        lifecycleOwner: LifecycleOwner,
        messageDao: ChatbotDao,
        onChatClicked: (Long) -> Unit
    ) {
        val adapter = ChatPreviewAdapter(
            chatPreview,
            lifecycleOwner.lifecycleScope,
            messageDao,
            onChatClicked
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        messageDao.getChatMetaData().observe(lifecycleOwner) { metadata ->
            val sorted = metadata.sortedByDescending { it.chatId }
            adapter.updateData(sorted)
        }
    }

    suspend fun loadMessagesForChat(
        chatId: Long,
        messageDao: ChatbotDao
    ): List<org.sabda.gpt.model.ChatMessage> {
        return withContext(Dispatchers.IO) {
            messageDao.getChatMessages(chatId)
        }
    }

    suspend fun insertChatMetaDataIfNeeded(
        messageDao: ChatbotDao,
        chatId: Long,
        firstMessageText: String
    ) {
        val title = firstMessageText.take(50)
        val metaData = ChatMetaData( chatId = chatId, title = title )
        messageDao.insertChatMetaData(metaData)
    }
}