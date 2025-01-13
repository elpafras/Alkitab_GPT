package org.sabda.gpt

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            Log.d(TAG, "Intent is null, proceeding to MainActivity")
            proceedToMainActivity()
            return
        }

        val inputPedia = intent.getStringExtra("inputPedia")
        val topic = intent.getStringExtra("topic")

        Log.d(TAG, "InputPedia: $inputPedia")
        Log.d(TAG, "Topic: $topic")

        if (inputPedia != null || topic != null) {
            Log.d(TAG, "Data exists, proceeding to AlkitabGPT")
            proceedToAlkitabGPT(intent)
        } else {
            Log.d(TAG, "No data, proceeding to MainActivity")
            proceedToMainActivity()
        }
    }

    private fun proceedToAlkitabGPT(intent: Intent) {
        val alkitabGPTIntent = Intent(this, AlkitabGPT::class.java)
        alkitabGPTIntent.putExtras(intent.extras!!)
        startActivity(alkitabGPTIntent)
        finish()
    }

    private fun proceedToMainActivity() {
        Handler().postDelayed({
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }, 2000L)
    }

    companion object {
        private const val TAG = "SplashScreenActivity"
    }
}