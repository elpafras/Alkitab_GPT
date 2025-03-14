package org.sabda.gpt;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class YoutubePlayer extends AppCompatActivity implements NetworkUtil.NetworkChangeCallback {

    static String EXTRA_VIDEO_ID = "EXTRA_VIDEO_ID";
    static String EXTRA_START_TIME = "EXTRA_START_TIME";
    boolean isConnected = false;
    boolean isPlayerReady = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        isConnected = NetworkUtil.isNetworkAvailable(this);
        updateConnectionStatus(isConnected);
        NetworkUtil.registerNetworkChangeReceiver(this, this::updateConnectionStatus);

        String videoId = getIntent().getStringExtra(EXTRA_VIDEO_ID);
        int startTime = getIntent().getIntExtra(EXTRA_START_TIME, 0);

        setupYoutubePlayer(videoId, startTime);
        ImageButton close = findViewById(R.id.close);
        close.setOnClickListener(v -> finish());
    }

    private void setupYoutubePlayer(String videoId, int startTime){
        YouTubePlayerView youTubePlayerView = findViewById(R.id.player_view);
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                super.onReady(youTubePlayer);
                youTubePlayer.loadVideo(videoId, startTime);
            }
        });
    }

    private void updateConnectionStatus(boolean isConnected){
        this.isConnected = isConnected;

        if (!isConnected){
            ToastUtil.showToast(this);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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