package org.sabda.gpt

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Pastikan aplikasi membaca intent terbaru
        Handler(Looper.getMainLooper()).postDelayed({
            handleIntent(intent)
        }, 1500L)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null || intent.extras == null) {
            Log.d(TAG, "Intent is null or has no extras, proceeding to MainActivity")
            proceedToMainActivity()
            return
        }

        val inputPedia = intent.getStringExtra("inputPedia")
        val topic = intent.getStringExtra("topic")

        Log.d(TAG, "Received intent data -> InputPedia: $inputPedia, Topic: $topic")

        // Pastikan hanya membuka AlkitabGPT jika ada data valid
        if (!inputPedia.isNullOrEmpty() || !topic.isNullOrEmpty()) {
            Log.d(TAG, "Valid data detected, proceeding to AlkitabGPT")
            proceedToAlkitabGPT(intent)
        } else {
            Log.d(TAG, "No valid data found, proceeding to MainActivity")
            proceedToMainActivity()
        }
    }

    private fun proceedToAlkitabGPT(intent: Intent) {
        val alkitabGPTIntent = Intent(this, AlkitabGPT::class.java).apply {
            putExtras(intent.extras ?: Bundle()) // Pastikan extras tidak null
        }
        startActivity(alkitabGPTIntent)
        finish()
    }

    private fun proceedToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "SplashScreenActivity"
    }
}