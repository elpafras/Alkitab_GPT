package org.sabda.gpt.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ChatRepository {
    suspend fun fetchChatResponse(message: String): String? {
        val urlString = "https://dev.sabda.org/bibleai/api/chatbot/getChatbot.php?chat="

        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString + message)

                Log.d("ChatRepository", "fetchChatResponse: $url")
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 15000
                }
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use {
                        val rawResponse = it.readText()

                        Log.d("ChatRepository", "fetchChatResponse - rawResponse: $rawResponse ")
                        return@withContext rawResponse
                    }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}