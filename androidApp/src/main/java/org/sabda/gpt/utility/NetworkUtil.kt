package org.sabda.gpt.utility

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import org.sabda.gpt.AlkitabGPT

object NetworkUtil {
    private var broadcastReceiver: BroadcastReceiver? = null

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        if (network == null) {
            Log.d("network_debug_2", "no network")
            return false
        }
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected = capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))
        Log.d("network_debug_2", "isNetworkAvailable: $isConnected")
        return isConnected
    }

    fun showNoInternetDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Masalah Koneksi Internet")
            .setMessage("Silakan sambungkan perangkat dengan internet untuk memulai aplikasi ini")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                if (!isNetworkAvailable(context)) {
                    if (context is Activity) {
                        context.finishAffinity()
                    }
                } else {
                    dialog.dismiss()
                }
            }
            .show()
    }

    fun registerNetworkChangeReceiver(context: Context, callback: (Boolean) -> Unit) {
        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val isConnected = isNetworkAvailable(context)
                    callback(isConnected)
                    if (!isConnected) {
                        ToastUtil.showToast(context)
                    }
                }
            }
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(broadcastReceiver, filter)
    }

    fun unregisterNetworkChangeReceiver(context: Context) {
        if (broadcastReceiver != null) {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    fun openUrl(context: Context, url: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = url?.toUri()
        context.startActivity(intent)
    }

    fun openWebView(ctx: Context, url: String?, title: String?) {
        val intent = Intent(ctx, AlkitabGPT::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        ctx.startActivity(intent)
    }

    interface NetworkChangeCallback {
        fun onNetworkChange(isConnected: Boolean)
    }
}
