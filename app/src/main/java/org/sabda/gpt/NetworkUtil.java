package org.sabda.gpt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.util.Log;

public class NetworkUtil {

    private static BroadcastReceiver broadcastReceiver;

    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null){
            Network network = connectivityManager.getActiveNetwork();
            if (network == null){
                Log.d("network_debug_2", "no network");
                return false;
            }
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            boolean isConnected = capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
            Log.d("network_debug_2", "isNetworkAvailable: " + isConnected);
            return isConnected;
        }
        Log.d("network_debug_2", "isNetworkAvailable: null");
        return false;
    }

    public static void registerNetworkChangeReceiver (Context context, NetworkChangeCallback callback) {
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean isConnected = isNetworkAvailable(context);
                    callback.onNetworkChange(isConnected);
                    if (!isConnected) {
                        ToastUtil.showToast(context);
                    }
                }
            };
        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(broadcastReceiver, filter);
    }

    public  interface NetworkChangeCallback {
        void onNetworkChange(boolean isConnected);
    }

    public static void unregisterNetworkChangeReceiver (Context context) {
        if (broadcastReceiver != null){
            context.unregisterReceiver(broadcastReceiver);
        }
    }

    public static void openUrl(Context context, String url){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    public static void openWebView(Context ctx, String url, String title){
        Intent intent = new Intent(ctx, AlkitabGPT.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        ctx.startActivity(intent);
    }
}
