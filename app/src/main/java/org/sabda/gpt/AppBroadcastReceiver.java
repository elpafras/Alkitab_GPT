package org.sabda.gpt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppBroadcastReceiver  extends BroadcastReceiver {
    String TAG = "AppBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("org.sabda.gpt.PEDIA_BROADCAST".equals(action)) {
            String input = intent.getStringExtra("input");
            String topics = intent.getStringExtra("topic");
            String lastBookName = intent.getStringExtra("lastBookName");
            int lastChapter = intent.getIntExtra("lastChapter", 1);

            Log.d(TAG, "Input: " + input);
            Log.d(TAG, "Topic: " + topics);
            Log.d(TAG, "LastBookName: " + lastBookName);
            Log.d(TAG, "LastChapter: " + lastChapter);


            Intent pediaIntent = new Intent(context, SplashScreenActivity.class);
            if (input != null || topics != null){
                pediaIntent.putExtra("inputPedia", input);
                pediaIntent.putExtra("topic", topics);
                pediaIntent.putExtra("lastBookName", lastBookName);
                pediaIntent.putExtra("lastChapter", lastChapter);
            }

            pediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pediaIntent);

        }
    }
}
