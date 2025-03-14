package org.sabda.gpt;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AlkitabGPT extends AppCompatActivity implements NetworkUtil.NetworkChangeCallback {

    ImageView back;
    WebView webView;

    /* From Alkitab GPT*/
    String inputText, inputTitle, inputUrl;
    TextView title, loadingText;
    /* From Alkitab GPT*/

    /* From Alkipedia*/
    String inputPedia, inputPediaTopics, lastBookName;
    int lastChapter;
    /* From Alkipedia */

    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alkitab_gpt);

        initializeView();
        initializeWebViewSetting();

        isConnected = NetworkUtil.isNetworkAvailable(this);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            loadWebView();
        }
        NetworkUtil.registerNetworkChangeReceiver(this, this::updateConnectionStatus);
    }

    private void loadWebView() {
        if (inputText != null){
            webView.loadUrl("https://gpt.sabda.org/chat.php?t=" + inputText);
        } else if (inputPedia != null && lastBookName != null && lastChapter != -1) {
            String lCp = String.valueOf(lastChapter);
            webView.loadUrl("https://gpt.sabda.org/chat.php?p=" + lastBookName + " " + lCp + "&t=" + inputPedia);
        } else if (inputPediaTopics != null && lastBookName != null && lastChapter != -1) {
            String lCp = String.valueOf(lastChapter);
            webView.loadUrl("https://gpt.sabda.org/chat.php?p="+ lastBookName + " " + lCp + "&d=" + inputPediaTopics);
        } else {
            webView.loadUrl(inputUrl);
        }
    }

    private void initializeView() {
        back = findViewById(R.id.out);
        webView = findViewById(R.id.alkitabGPTWebView);
        title = findViewById(R.id.title);
        loadingText = createLoadingText();

        inputText = getIntent().getStringExtra("inputtext");
        inputTitle = getIntent().getStringExtra("title");
        inputUrl = getIntent().getStringExtra("url");
        inputPedia = getIntent().getStringExtra("inputPedia");
        inputPediaTopics = getIntent().getStringExtra("topic");
        lastBookName = getIntent().getStringExtra("lastBookName");
        lastChapter = getIntent().getIntExtra("lastChapter", 1);

        setTitletext();
        setupButton();
    }

    private void initializeWebViewSetting() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        webView.setWebViewClient(new CustomizeWebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
    }


    private TextView createLoadingText() {
        loadingText = new TextView(this);
        loadingText.setText("Loading...");
        loadingText.setTextSize(20);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );

        loadingText.setLayoutParams(params);

        ViewGroup root = findViewById(android.R.id.content);
        root.addView(loadingText);

        return loadingText;
    }

    private void setTitletext() {
        if (inputTitle != null) {
            title.setText(inputTitle);
        } else {
            title.setText(R.string.app_name);
        }
    }

    private void setupButton() {
        back.setOnClickListener(v -> finish());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    private void updateConnectionStatus(boolean isConnected) {
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

    private class CustomizeWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            loadingText.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            loadingText.setVisibility(View.GONE);
        }
    }
}