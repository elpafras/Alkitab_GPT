package org.sabda.gpt.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import org.sabda.gpt.model.ChatMessage

@Dao
interface ChatbotDao {

    // **ChatMessage Queries**

    @Insert
    suspend fun insertChatMessage(chat: ChatMessage)

    // Mengambil semua pesan berdasarkan chatId, diurutkan berdasarkan timestamp
    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getChatMessages(chatId: Long): List<ChatMessage>

    // Menghapus semua pesan berdasarkan chatId
    @Query("DELETE FROM chat_messages WHERE chatId = :chatId")
    suspend fun deleteChatMessages(chatId: Long)

    // Mengambil semua pesan dari tabel chat_messages
    @Query("SELECT * FROM chat_messages")
    fun getAllMessages(): LiveData<List<ChatMessage>>

    // **ChatMetaData Queries**

    // Menyisipkan metadata baru ke tabel chat_metadata
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMetaData(chatMetaData: ChatMetaData)

    // Mengambil metadata berdasarkan chatId
    @Query("SELECT * FROM chat_metadata")
    fun getChatMetaData(): LiveData<List<ChatMetaData>>

    // Memperbarui judul metadata berdasarkan chatId
    @Query("UPDATE chat_metadata SET title = :newTitle WHERE chatId = :chatId")
    suspend fun updateChatMetaDataTitle(chatId: Long, newTitle: String)

    // Menghapus metadata berdasarkan chatId
    @Query("DELETE FROM chat_metadata WHERE chatId = :chatId")
    suspend fun deleteChatMetaData(chatId: Long)

    // **Relational Query**

    // Mengambil metadata beserta semua pesan yang terkait berdasarkan chatId
    @Transaction
    @Query("SELECT * FROM chat_metadata WHERE chatId = :chatId")
    suspend fun getChatWithMessages(chatId: Long): ChatWithMessages
}

// Data class untuk relasi antara ChatMetaData dan ChatMessage
data class ChatWithMessages(
    @Embedded val metaData: ChatMetaData,
    @Relation(
        parentColumn = "chatId",
        entityColumn = "chatId"
    )
    val messages: List<ChatMessage>
)