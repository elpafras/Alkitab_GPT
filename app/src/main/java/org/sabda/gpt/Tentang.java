package org.sabda.gpt;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ceylonlabs.imageviewpopup.BuildConfig;

public class Tentang extends AppCompatActivity implements NetworkUtil.NetworkChangeCallback {

    ImageView back;
    TextView version, sabdaLink, kotakSaranLink;
    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tentang);

        isConnected = NetworkUtil.isNetworkAvailable(this);
        back = findViewById(R.id.back);
        version = findViewById(R.id.version);
        sabdaLink = findViewById(R.id.sabdaLink);
        kotakSaranLink = findViewById(R.id.kotakSaranLink);

        updateConnectionStatus(isConnected);

        NetworkUtil.registerNetworkChangeReceiver(this, this::updateConnectionStatus);

        back.setOnClickListener(v -> finish());

        version.setText("Versi: 2.2");

        sabdaLink.setOnClickListener(v -> handleClick("https://sabda.app"));

        kotakSaranLink.setOnClickListener(v -> handleClick("mailto:apps@sabda.org"));

    }

    private void updateConnectionStatus(boolean isConnected){
        this.isConnected = isConnected;
        sabdaLink.setEnabled(isConnected);
        kotakSaranLink.setEnabled(isConnected);

        if (!isConnected){
            ToastUtil.showToast(this);
        }
    }

    private void handleClick(String url){
        if (isConnected){
            NetworkUtil.openUrl(this, url);
        } else {
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