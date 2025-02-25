package org.sabda.gpt.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.sabda.gpt.model.ChatbotData

@Dao
interface ChatbotDao {

    @Insert
    suspend fun insertChat(chat: ChatbotData)

    @Query("SELECT MAX(counter) FROM chatbot WHERE chatId = :chatId")
    suspend fun getMaxCounterForChat(chatId: Long): Int?

    @Query("SELECT * FROM chatbot WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getChatMessages(chatId: Long): List<ChatbotData>

    @Query("DELETE FROM chatbot WHERE chatId = :chatId")
    suspend fun deleteChat(chatId: Long)

    @Query("SELECT * FROM chatbot")
    fun getAllMessages(): LiveData<List<ChatbotData>>

}