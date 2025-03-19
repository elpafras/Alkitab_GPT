package org.sabda.gpt.utility

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object PlayStoreUtils {
    fun openApps (context: Context, destination: String, packageName: String) {
        if (isAppInstalled(context, packageName)) {
            val intent = Intent(destination)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
              redirectToPlayStore(context, packageName)
            }
        } else {
            redirectToPlayStore(context, packageName)
        }
    }

    private fun redirectToPlayStore(context: Context, packageName: String) {
        val playStoreUri = "market://details?id=$packageName"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUri)))
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean{
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            Log.d("PlayStoreUtils", "$packageName not installed")
            false
        }
    }
}