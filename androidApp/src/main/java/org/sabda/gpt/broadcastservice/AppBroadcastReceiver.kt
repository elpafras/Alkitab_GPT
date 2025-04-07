package org.sabda.gpt.broadcastservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.sabda.gpt.SplashScreenActivity

class AppBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "AppBroadcastReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive - broadcast received")

        val action = intent.action

        Log.d(TAG, "onReceive - cek action: $action")

        if ("org.sabda.gpt.PEDIA_BROADCAST" == action) {
            val input = intent.getStringExtra("input")
            val topics = intent.getStringExtra("topic")
            val lastBookName = intent.getStringExtra("lastBookName")
            val lastChapter = intent.getIntExtra("lastChapter", 1)


            Log.d(TAG, "Input: $input")
            Log.d(TAG, "Topic: $topics")
            Log.d(TAG, "LastBookName: $lastBookName")
            Log.d(TAG, "LastChapter: $lastChapter")

            val pediaIntent = Intent(context, SplashScreenActivity::class.java).apply {
                putExtra("inputPedia", input)
                putExtra("topic", topics)
                putExtra("lastBookName", lastBookName)
                putExtra("lastChapter", lastChapter)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(pediaIntent)
        } else {
            Log.e(TAG, "Broadcast tidak dikenali")
        }
    }
}
