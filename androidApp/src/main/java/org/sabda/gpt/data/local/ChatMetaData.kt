package org.sabda.gpt.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_metadata")
data class ChatMetaData(
    @PrimaryKey val chatId: Long,
    var title: String
)
