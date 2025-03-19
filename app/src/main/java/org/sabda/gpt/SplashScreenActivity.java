package org.sabda.gpt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = "SplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            Log.d(TAG, "Intent is null, proceeding to MainActivity");
            proceedToMainActivity();
            return;
        }

        String inputPedia = intent.getStringExtra("inputPedia");
        String topic = intent.getStringExtra("topic");

        Log.d(TAG, "InputPedia: " + inputPedia);
        Log.d(TAG, "Topic: " + topic);

        if (inputPedia != null || topic != null){
            Log.d(TAG, "Data exists, proceeding to AlkitabGPT");
            proceedToAlkitabGPT(intent);
        } else {
            Log.d(TAG, "No data, proceeding to MainActivity");
            proceedToMainActivity();
        }
    }

    private void proceedToAlkitabGPT(Intent intent) {
        Intent alkitabGPTIntent = new Intent(this, AlkitabGPT.class);
        alkitabGPTIntent.putExtras(intent.getExtras());
        startActivity(alkitabGPTIntent);
        finish();
    }

    private void proceedToMainActivity() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }, 2000L);
    }
}