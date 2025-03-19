package org.sabda.gpt;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class materi extends AppCompatActivity implements NetworkUtil.NetworkChangeCallback {

    public static final String EXTRA_URL = "extra_url";
    Button back;
    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materi);

        isConnected = NetworkUtil.isNetworkAvailable(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        NetworkUtil.registerNetworkChangeReceiver(this, this::updateConnectionStatus);

        String url = getIntent().getStringExtra(EXTRA_URL);

        WebView webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        String html = "<html><body><iframe src='" + url + "' width='100%' height='100%' frameborder='0' marginwidth='0' marginheight='0' scrolling='no'></iframe></body></html>";

        webView.loadData(html, "text/html", "UTF-8");

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());

    }

    private void updateConnectionStatus(boolean isConnected){
        this.isConnected = isConnected;

        if (!isConnected){
            ToastUtil.showToast(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkUtil.unregisterNetworkChangeReceiver(this);
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        updateConnectionStatus(isConnected);
    }
}