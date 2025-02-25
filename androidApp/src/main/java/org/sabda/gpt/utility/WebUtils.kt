package org.sabda.gpt.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

object WebUtils {
    fun openUrl(context: Context, url: String) {
        if (NetworkUtil.isNetworkAvailable(context)) {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } else {
            showToast(context, "No internet connection")
        }
    }

    fun openWebView(context: Context, url: String, title: String) {
        if (NetworkUtil.isNetworkAvailable(context)) {
            // Implement WebView opening logic here
            Toast.makeText(context, "Opening: $title", Toast.LENGTH_SHORT).show()
        } else {
            showToast(context, "No internet connection")
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}