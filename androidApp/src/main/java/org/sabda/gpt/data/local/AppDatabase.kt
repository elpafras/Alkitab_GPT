package org.sabda.gpt.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import org.sabda.gpt.model.ChatbotData

@Database(entities = [ChatbotData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatbotDao(): ChatbotDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}