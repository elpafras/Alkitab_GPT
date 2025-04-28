package org.sabda.gpt.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    val chatId: Long,
    var text: String,
    val isSent: Boolean,
    val timestamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
