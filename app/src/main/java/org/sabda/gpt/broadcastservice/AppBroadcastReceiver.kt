package org.sabda.gpt.broadcastservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.sabda.gpt.MainActivity

class AppBroadcastReceiver : BroadcastReceiver() {
    var TAG: String = "AppBroadcastReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if ("org.sabda.gpt.PEDIA_BROADCAST" == action) {
            val input = intent.getStringExtra("input")
            val topics = intent.getStringExtra("topic")
            val lastBookName = intent.getStringExtra("lastBookName")
            val lastChapter = intent.getIntExtra("lastChapter", 1)


            /*Log.d(TAG, "Input: " + input);
            Log.d(TAG, "Topic: " + topics);
            Log.d(TAG, "LastBookName: " + lastBookName);
            Log.d(TAG, "LastChapter: " + lastChapter);*/
            val pediaIntent = Intent(context, MainActivity::class.java)
            if (input != null || topics != null) {
                pediaIntent.putExtra("inputPedia", input)
                pediaIntent.putExtra("topic", topics)
                pediaIntent.putExtra("lastBookName", lastBookName)
                pediaIntent.putExtra("lastChapter", lastChapter)
            }

            pediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(pediaIntent)
        }
    }
}
